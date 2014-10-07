/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.integration.dsl.test.flows;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.isOneOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.net.ftp.FTPFile;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.AnonymousQueue;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.DirectFieldAccessor;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.ConfigFileApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.integration.IntegrationMessageHeaderAccessor;
import org.springframework.integration.MessageDispatchingException;
import org.springframework.integration.MessageRejectedException;
import org.springframework.integration.annotation.Header;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.ChannelInterceptorAware;
import org.springframework.integration.channel.FixedSubscriberChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.config.GlobalChannelInterceptor;
import org.springframework.integration.context.IntegrationContextUtils;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.Channels;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageProducers;
import org.springframework.integration.dsl.MessageSources;
import org.springframework.integration.dsl.MessagingGateways;
import org.springframework.integration.dsl.amqp.Amqp;
import org.springframework.integration.dsl.channel.DirectChannelSpec;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.integration.dsl.file.Files;
import org.springframework.integration.dsl.ftp.Ftp;
import org.springframework.integration.dsl.jms.Jms;
import org.springframework.integration.dsl.sftp.Sftp;
import org.springframework.integration.dsl.core.Pollers;
import org.springframework.integration.dsl.support.Transformers;
import org.springframework.integration.dsl.test.TestFtpServer;
import org.springframework.integration.dsl.test.TestSftpServer;
import org.springframework.integration.endpoint.MethodInvokingMessageSource;
import org.springframework.integration.event.core.MessagingEvent;
import org.springframework.integration.event.outbound.ApplicationEventPublishingMessageHandler;
import org.springframework.integration.file.DefaultFileNameGenerator;
import org.springframework.integration.file.FileHeaders;
import org.springframework.integration.file.FileWritingMessageHandler;
import org.springframework.integration.file.remote.RemoteFileTemplate;
import org.springframework.integration.file.remote.gateway.AbstractRemoteFileOutboundGateway;
import org.springframework.integration.file.tail.ApacheCommonsFileTailingMessageProducer;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;
import org.springframework.integration.handler.AbstractReplyProducingMessageHandler;
import org.springframework.integration.handler.advice.ExpressionEvaluatingRequestHandlerAdvice;
import org.springframework.integration.mongodb.store.MongoDbChannelMessageStore;
import org.springframework.integration.router.MethodInvokingRouter;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.integration.store.MessageStore;
import org.springframework.integration.store.PriorityCapableChannelMessageStore;
import org.springframework.integration.store.SimpleMessageStore;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.support.MutableMessageBuilder;
import org.springframework.integration.test.util.TestUtils;
import org.springframework.integration.transformer.PayloadDeserializingTransformer;
import org.springframework.integration.transformer.PayloadSerializingTransformer;
import org.springframework.integration.xml.transformer.support.XPathExpressionEvaluatingHeaderValueMessageProcessor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessageHandlingException;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.PollableChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.core.DestinationResolutionException;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptorAdapter;
import org.springframework.messaging.support.ErrorMessage;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.StreamUtils;

import com.jcraft.jsch.ChannelSftp;
import com.mongodb.MongoClient;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;

/**
 * @author Artem Bilan
 * @author Tim Ysewyn
 */
@ContextConfiguration(initializers = ConfigFileApplicationContextInitializer.class)
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
public class IntegrationFlowTests {

	private static final File tmpDir = new File(System.getProperty("java.io.tmpdir"));

	private static int mongoPort;

	private static MongodExecutable mongodExe;

	@Autowired
	private ListableBeanFactory beanFactory;

	@Autowired
	private ControlBusGateway controlBus;

	@Autowired
	@Qualifier("flow1QueueChannel")
	private PollableChannel outputChannel;

	@Autowired
	private TestChannelInterceptor testChannelInterceptor;

	@Autowired
	@Qualifier("inputChannel")
	private MessageChannel inputChannel;

	@Autowired
	@Qualifier("foo")
	private SubscribableChannel foo;

	@Autowired
	@Qualifier("successChannel")
	private PollableChannel successChannel;

	@Autowired
	@Qualifier("flow3Input")
	private MessageChannel flow3Input;

	@Autowired
	private AtomicReference<Object> eventHolder;

	@Autowired
	@Qualifier("bridgeFlowInput")
	private PollableChannel bridgeFlowInput;

	@Autowired
	@Qualifier("bridgeFlowOutput")
	private PollableChannel bridgeFlowOutput;

	@Autowired
	@Qualifier("bridgeFlow2Input")
	private MessageChannel bridgeFlow2Input;

	@Autowired
	@Qualifier("bridgeFlow2Output")
	private PollableChannel bridgeFlow2Output;

	@Autowired
	@Qualifier("fileFlow1Input")
	private MessageChannel fileFlow1Input;

	@Autowired
	@Qualifier("fileWriting.handler")
	private MessageHandler fileWritingMessageHandler;

	@Autowired
	@Qualifier("methodInvokingInput")
	private MessageChannel methodInvokingInput;

	@Autowired
	@Qualifier("delayedAdvice")
	private DelayedAdvice delayedAdvice;

	@Autowired
	@Qualifier("enricherInput")
	private FixedSubscriberChannel enricherInput;

	@Autowired
	@Qualifier("enricherInput2")
	private FixedSubscriberChannel enricherInput2;

	@Autowired
	@Qualifier("enricherInput3")
	private FixedSubscriberChannel enricherInput3;

	@Autowired
	@Qualifier("splitResequenceFlow.input")
	private MessageChannel splitInput;

	@Autowired
	@Qualifier("xpathHeaderEnricherInput")
	private MessageChannel xpathHeaderEnricherInput;

	@Autowired
	@Qualifier("splitAggregateInput")
	private MessageChannel splitAggregateInput;

	@Autowired
	@Qualifier("routerInput")
	private MessageChannel routerInput;

	@Autowired
	@Qualifier("oddChannel")
	private PollableChannel oddChannel;

	@Autowired
	@Qualifier("evenChannel")
	private PollableChannel evenChannel;

	@Autowired
	@Qualifier("routerMethodInput")
	private MessageChannel routerMethodInput;

	@Autowired
	@Qualifier("foo-channel")
	private PollableChannel fooChannel;

	@Autowired
	@Qualifier("bar-channel")
	private PollableChannel barChannel;

	@Autowired
	@Qualifier("routerMethod2Input")
	private MessageChannel routerMethod2Input;

	@Autowired
	@Qualifier("routeMethodInvocationFlow3.input")
	private MessageChannel routerMethod3Input;

	@Autowired
	@Qualifier("routerMultiInput")
	private MessageChannel routerMultiInput;

	@Autowired
	@Qualifier("recipientListInput")
	private MessageChannel recipientListInput;

	@Autowired
	@Qualifier("defaultOutputChannel")
	private PollableChannel defaultOutputChannel;

	@Autowired
	private MessageStore messageStore;

	@Autowired
	@Qualifier("claimCheckInput")
	private MessageChannel claimCheckInput;

	@Autowired
	@Qualifier("priorityChannel")
	private MessageChannel priorityChannel;

	@Autowired
	@Qualifier("priorityReplyChannel")
	private PollableChannel priorityReplyChannel;

	@Autowired
	@Qualifier("lamdasInput")
	private MessageChannel lamdasInput;

	@Autowired
	@Qualifier("tailChannel")
	private PollableChannel tailChannel;

	@Autowired
	private ApacheCommonsFileTailingMessageProducer tailer;

	@Autowired
	private AmqpTemplate amqpTemplate;

	@Autowired
	@Qualifier("queue")
	private Queue amqpQueue;

	@Autowired
	@Qualifier("gatewayInput")
	private MessageChannel gatewayInput;

	@Autowired
	@Qualifier("gatewayError")
	private PollableChannel gatewayError;

	@BeforeClass
	public static void setup() throws IOException {
		mongoPort = Network.getFreeServerPort();
		mongodExe = MongodStarter.getDefaultInstance()
				.prepare(new MongodConfigBuilder()
						.version(Version.Main.PRODUCTION)
						.net(new Net(mongoPort, Network.localhostIsIPv6()))
						.build());
		mongodExe.start();
	}

	@AfterClass
	public static void tearDown() {
		mongodExe.stop();
	}

	@Test
	public void testPollingFlow() {
		this.controlBus.send("@integerEndpoint.start()");
		assertThat(this.beanFactory.getBean("integerChannel"), instanceOf(FixedSubscriberChannel.class));
		for (int i = 0; i < 5; i++) {
			Message<?> message = this.outputChannel.receive(20000);
			assertNotNull(message);
			assertEquals("" + i, message.getPayload());
		}
		this.controlBus.send("@integerEndpoint.stop()");

		assertTrue(((ChannelInterceptorAware) this.outputChannel).getChannelInterceptors()
				.contains(this.testChannelInterceptor));
		assertThat(this.testChannelInterceptor.getInvoked(), Matchers.greaterThanOrEqualTo(5));

	}

	@Test
	public void testDirectFlow() {
		assertTrue(this.beanFactory.containsBean("filter"));
		assertTrue(this.beanFactory.containsBean("filter.handler"));
		QueueChannel replyChannel = new QueueChannel();
		Message<String> message = MessageBuilder.withPayload("100").setReplyChannel(replyChannel).build();
		try {
			this.inputChannel.send(message);
			fail("Expected MessageDispatchingException");
		}
		catch (Exception e) {
			assertThat(e, instanceOf(MessageDeliveryException.class));
			assertThat(e.getCause(), instanceOf(MessageDispatchingException.class));
			assertThat(e.getMessage(), containsString("Dispatcher has no subscribers"));
		}
		this.controlBus.send("@payloadSerializingTransformer.start()");

		final AtomicBoolean used = new AtomicBoolean();

		this.foo.subscribe(m -> used.set(true));

		this.inputChannel.send(message);
		Message<?> reply = replyChannel.receive(5000);
		assertNotNull(reply);
		assertEquals(200, reply.getPayload());

		Message<?> successMessage = this.successChannel.receive(5000);
		assertNotNull(successMessage);
		assertEquals(100, successMessage.getPayload());

		assertTrue(used.get());
	}

	@Test
	public void testHandle() {
		assertNull(this.eventHolder.get());
		this.flow3Input.send(new GenericMessage<>("2"));
		assertNotNull(this.eventHolder.get());
		assertEquals(4, this.eventHolder.get());
	}

	@Test
	public void testBridge() {
		GenericMessage<String> message = new GenericMessage<>("test");
		this.bridgeFlowInput.send(message);
		Message<?> reply = this.bridgeFlowOutput.receive(5000);
		assertNotNull(reply);
		assertEquals("test", reply.getPayload());

		assertTrue(this.beanFactory.containsBean("bridgeFlow2.channel#0"));
		assertThat(this.beanFactory.getBean("bridgeFlow2.channel#0"), instanceOf(FixedSubscriberChannel.class));

		try {
			this.bridgeFlow2Input.send(message);
			fail("Expected MessageDispatchingException");
		}
		catch (Exception e) {
			assertThat(e, instanceOf(MessageDeliveryException.class));
			assertThat(e.getCause(), instanceOf(MessageDispatchingException.class));
			assertThat(e.getMessage(), containsString("Dispatcher has no subscribers"));
		}
		this.controlBus.send("@bridge.start()");
		this.bridgeFlow2Input.send(message);
		reply = this.bridgeFlow2Output.receive(5000);
		assertNotNull(reply);
		assertEquals("test", reply.getPayload());
		assertTrue(this.delayedAdvice.getInvoked());
	}

	@Autowired
	@Qualifier("routerAsNonLastFlow.input")
	private MessageChannel routerAsNonLastFlowChannel;

	@Autowired
	@Qualifier("routerAsNonLastDefaultOutputChannel")
	private PollableChannel routerAsNonLastDefaultOutputChannel;

	@Test
	public void testRouterAsNonLastComponent() {
		this.routerAsNonLastFlowChannel.send(new GenericMessage<String>("Hello World"));
		Message<?> receive = this.routerAsNonLastDefaultOutputChannel.receive(1000);
		assertNotNull(receive);
		assertEquals("Hello World", receive.getPayload());
	}

	@Test
	public void testWrongLastMessageChannel() {
		ConfigurableApplicationContext context = null;
		try {
			context = new AnnotationConfigApplicationContext(InvalidLastMessageChannelFlowContext.class);
			fail("BeanCreationException expected");
		}
		catch (Exception e) {
			assertThat(e, instanceOf(BeanCreationException.class));
			assertThat(e.getMessage(), containsString("'.fixedSubscriberChannel()' " +
					"can't be the last EIP-method in the IntegrationFlow definition"));
		}
		finally {
			if (context != null) {
				context.close();
			}
		}
	}


	@Test
	public void testFileHandler() throws Exception {
		Message<?> message = MessageBuilder.withPayload("foo").setHeader(FileHeaders.FILENAME, "foo").build();
		try {
			this.fileFlow1Input.send(message);
			fail("NullPointerException expected");
		}
		catch (Exception e) {
			assertThat(e, instanceOf(MessageHandlingException.class));
			assertThat(e.getCause(), instanceOf(NullPointerException.class));
		}
		DefaultFileNameGenerator fileNameGenerator = new DefaultFileNameGenerator();
		fileNameGenerator.setBeanFactory(this.beanFactory);
		Object targetFileWritingMessageHandler = this.fileWritingMessageHandler;
		if (this.fileWritingMessageHandler instanceof Advised) {
			TargetSource targetSource = ((Advised) this.fileWritingMessageHandler).getTargetSource();
			if (targetSource != null) {
				targetFileWritingMessageHandler = targetSource.getTarget();
			}
		}
		DirectFieldAccessor dfa = new DirectFieldAccessor(targetFileWritingMessageHandler);
		dfa.setPropertyValue("fileNameGenerator", fileNameGenerator);
		this.fileFlow1Input.send(message);

		assertTrue(new File(tmpDir, "foo").exists());
	}

	@Test
	public void testMethodInvokingMessageHandler() {
		QueueChannel replyChannel = new QueueChannel();
		Message<?> message = MessageBuilder.withPayload("world")
				.setHeader(MessageHeaders.REPLY_CHANNEL, replyChannel)
				.build();
		this.methodInvokingInput.send(message);
		Message<?> receive = replyChannel.receive(5000);
		assertNotNull(receive);
		assertEquals("Hello World and world", receive.getPayload());
	}

	@Test
	public void testLambdas() {
		QueueChannel replyChannel = new QueueChannel();
		Message<?> message = MessageBuilder.withPayload("World")
				.setHeader(MessageHeaders.REPLY_CHANNEL, replyChannel)
				.build();
		this.lamdasInput.send(message);
		Message<?> receive = replyChannel.receive(5000);
		assertNotNull(receive);
		assertEquals("Hello World", receive.getPayload());

		message = MessageBuilder.withPayload("Spring")
				.setHeader(MessageHeaders.REPLY_CHANNEL, replyChannel)
				.build();

		this.lamdasInput.send(message);
		assertNull(replyChannel.receive(10));

	}

	@Test
	public void testWrongConfigurationWithSpecBean() {
		ConfigurableApplicationContext context = null;
		try {
			context = new AnnotationConfigApplicationContext(InvalidConfigurationWithSpec.class);
			fail("BeanCreationException expected");
		}
		catch (Exception e) {
			assertThat(e, instanceOf(IllegalArgumentException.class));
			assertThat(e.getCause(), instanceOf(BeanCreationException.class));
			assertThat(e.getCause().getMessage(),
					containsString("must be populated to target objects via 'get()' method call"));
		}
		finally {
			if (context != null) {
				context.close();
			}
		}
	}

	@Test
	public void testContentEnricher() {
		QueueChannel replyChannel = new QueueChannel();
		Message<?> message = MessageBuilder.withPayload(new TestPojo("Bar"))
				.setHeader(MessageHeaders.REPLY_CHANNEL, replyChannel)
				.build();
		this.enricherInput.send(message);
		Message<?> receive = replyChannel.receive(5000);
		assertNotNull(receive);
		assertEquals("Bar Bar", receive.getHeaders().get("foo"));
		Object payload = receive.getPayload();
		assertThat(payload, instanceOf(TestPojo.class));
		TestPojo result = (TestPojo) payload;
		assertEquals("Bar Bar", result.getName());
		assertNotNull(result.getDate());
		assertThat(new Date(), Matchers.greaterThanOrEqualTo(result.getDate()));
	}

	@Test
	public void testContentEnricher2() {
		QueueChannel replyChannel = new QueueChannel();
		Message<?> message = MessageBuilder.withPayload(new TestPojo("Bar"))
				.setHeader(MessageHeaders.REPLY_CHANNEL, replyChannel)
				.build();
		this.enricherInput2.send(message);
		Message<?> receive = replyChannel.receive(5000);
		assertNotNull(receive);
		assertNull(receive.getHeaders().get("foo"));
		Object payload = receive.getPayload();
		assertThat(payload, instanceOf(TestPojo.class));
		TestPojo result = (TestPojo) payload;
		assertEquals("Bar Bar", result.getName());
		assertNotNull(result.getDate());
		assertThat(new Date(), Matchers.greaterThanOrEqualTo(result.getDate()));
	}

	@Test
	public void testContentEnricher3() {
		QueueChannel replyChannel = new QueueChannel();
		Message<?> message = MessageBuilder.withPayload(new TestPojo("Bar"))
				.setHeader(MessageHeaders.REPLY_CHANNEL, replyChannel)
				.build();
		this.enricherInput3.send(message);
		Message<?> receive = replyChannel.receive(5000);
		assertNotNull(receive);
		assertEquals("Bar Bar", receive.getHeaders().get("foo"));
		Object payload = receive.getPayload();
		assertThat(payload, instanceOf(TestPojo.class));
		TestPojo result = (TestPojo) payload;
		assertEquals("Bar", result.getName());
		assertNull(result.getDate());
	}

	@Test
	public void testSplitterResequencer() {
		QueueChannel replyChannel = new QueueChannel();

		this.splitInput.send(MessageBuilder.withPayload("")
				.setReplyChannel(replyChannel)
				.setHeader("foo", "bar")
				.build());

		for (int i = 0; i < 12; i++) {
			Message<?> receive = replyChannel.receive(2000);
			assertNotNull(receive);
			assertFalse(receive.getHeaders().containsKey("foo"));
			assertTrue(receive.getHeaders().containsKey("FOO"));
			assertEquals("BAR", receive.getHeaders().get("FOO"));
			assertEquals(i + 1, receive.getPayload());
		}
	}

	@Test
	public void testSplitterAggregator() {
		List<Character> payload = Arrays.asList('a', 'b', 'c', 'd', 'e');

		QueueChannel replyChannel = new QueueChannel();
		this.splitAggregateInput.send(MessageBuilder.withPayload(payload)
				.setReplyChannel(replyChannel)
				.build());

		Message<?> receive = replyChannel.receive(2000);
		assertNotNull(receive);
		assertThat(receive.getPayload(), instanceOf(List.class));
		@SuppressWarnings("unchecked")
		List<Object> result = (List<Object>) receive.getPayload();
		for (int i = 0; i < payload.size(); i++) {
			assertEquals(payload.get(i), result.get(i));
		}
	}

	@Test
	public void testHeaderEnricher() {
		QueueChannel replyChannel = new QueueChannel();

		Message<String> message =
				MessageBuilder.withPayload("<root><elementOne>1</elementOne><elementTwo>2</elementTwo></root>")
						.setReplyChannel(replyChannel)
						.build();

		try {
			this.xpathHeaderEnricherInput.send(message);
			fail("Expected MessageDispatchingException");
		}
		catch (Exception e) {
			assertThat(e, instanceOf(MessageDeliveryException.class));
			assertThat(e.getCause(), instanceOf(MessageDispatchingException.class));
			assertThat(e.getMessage(), containsString("Dispatcher has no subscribers"));
		}

		this.controlBus.send("@xpathHeaderEnricher.start()");
		this.xpathHeaderEnricherInput.send(message);

		Message<?> result = replyChannel.receive(2000);
		assertNotNull(result);
		MessageHeaders headers = result.getHeaders();
		assertEquals("1", headers.get("one"));
		assertEquals("2", headers.get("two"));
		assertThat(headers.getReplyChannel(), instanceOf(String.class));
	}

	@Test
	public void testRouter() {

		int[] payloads = new int[] {1, 2, 3, 4, 5, 6};

		for (int payload : payloads) {
			this.routerInput.send(new GenericMessage<>(payload));
		}

		for (int i = 0; i < 3; i++) {
			Message<?> receive = this.oddChannel.receive(2000);
			assertNotNull(receive);
			assertEquals(i * 2 + 1, receive.getPayload());

			receive = this.evenChannel.receive(2000);
			assertNotNull(receive);
			assertEquals(i * 2 + 2, receive.getPayload());
		}

	}

	@Test
	public void testMethodInvokingRouter() {
		Message<String> fooMessage = new GenericMessage<>("foo");
		Message<String> barMessage = new GenericMessage<>("bar");
		Message<String> badMessage = new GenericMessage<>("bad");

		this.routerMethodInput.send(fooMessage);

		Message<?> result1a = this.fooChannel.receive(2000);
		assertNotNull(result1a);
		assertEquals("foo", result1a.getPayload());
		assertNull(this.barChannel.receive(0));

		this.routerMethodInput.send(barMessage);
		assertNull(this.fooChannel.receive(0));
		Message<?> result2b = this.barChannel.receive(2000);
		assertNotNull(result2b);
		assertEquals("bar", result2b.getPayload());

		try {
			this.routerMethodInput.send(badMessage);
			fail("MessageDeliveryException expected.");
		}
		catch (MessageDeliveryException e) {
			assertThat(e.getMessage(),
					containsString("no channel resolved by router and no default output channel defined"));
		}

	}

	@Test
	public void testMethodInvokingRouter2() {
		Message<String> fooMessage = MessageBuilder.withPayload("foo").setHeader("targetChannel", "foo").build();
		Message<String> barMessage = MessageBuilder.withPayload("bar").setHeader("targetChannel", "bar").build();
		Message<String> badMessage = MessageBuilder.withPayload("bad").setHeader("targetChannel", "bad").build();

		this.routerMethod2Input.send(fooMessage);

		Message<?> result1a = this.fooChannel.receive(2000);
		assertNotNull(result1a);
		assertEquals("foo", result1a.getPayload());
		assertNull(this.barChannel.receive(0));

		this.routerMethod2Input.send(barMessage);
		assertNull(this.fooChannel.receive(0));
		Message<?> result2b = this.barChannel.receive(2000);
		assertNotNull(result2b);
		assertEquals("bar", result2b.getPayload());

		try {
			this.routerMethod2Input.send(badMessage);
			fail("DestinationResolutionException expected.");
		}
		catch (MessagingException e) {
			assertThat(e.getCause(), instanceOf(DestinationResolutionException.class));
			assertThat(e.getCause().getMessage(),
					containsString("failed to look up MessageChannel with name 'bad-channel'"));
		}

	}

	@Test
	public void testMethodInvokingRouter3() {
		Message<String> fooMessage = new GenericMessage<>("foo");
		Message<String> barMessage = new GenericMessage<>("bar");
		Message<String> badMessage = new GenericMessage<>("bad");

		this.routerMethod3Input.send(fooMessage);

		Message<?> result1a = this.fooChannel.receive(2000);
		assertNotNull(result1a);
		assertEquals("foo", result1a.getPayload());
		assertNull(this.barChannel.receive(0));

		this.routerMethod3Input.send(barMessage);
		assertNull(this.fooChannel.receive(0));
		Message<?> result2b = this.barChannel.receive(2000);
		assertNotNull(result2b);
		assertEquals("bar", result2b.getPayload());

		try {
			this.routerMethod3Input.send(badMessage);
			fail("DestinationResolutionException expected.");
		}
		catch (MessagingException e) {
			assertThat(e.getCause(), instanceOf(DestinationResolutionException.class));
			assertThat(e.getCause().getMessage(),
					containsString("failed to look up MessageChannel with name 'bad-channel'"));
		}
	}

	@Test
	public void testMultiRouter() {

		Message<String> fooMessage = new GenericMessage<>("foo");
		Message<String> barMessage = new GenericMessage<>("bar");
		Message<String> badMessage = new GenericMessage<>("bad");

		this.routerMultiInput.send(fooMessage);
		Message<?> result1a = this.fooChannel.receive(2000);
		assertNotNull(result1a);
		assertEquals("foo", result1a.getPayload());
		Message<?> result1b = this.barChannel.receive(2000);
		assertNotNull(result1b);
		assertEquals("foo", result1b.getPayload());

		this.routerMultiInput.send(barMessage);
		Message<?> result2a = this.fooChannel.receive(2000);
		assertNotNull(result2a);
		assertEquals("bar", result2a.getPayload());
		Message<?> result2b = this.barChannel.receive(2000);
		assertNotNull(result2b);
		assertEquals("bar", result2b.getPayload());

		try {
			this.routerMultiInput.send(badMessage);
			fail("MessageDeliveryException expected.");
		}
		catch (MessageDeliveryException e) {
			assertThat(e.getMessage(),
					containsString("no channel resolved by router and no default output channel defined"));
		}
	}

	@Test
	public void testRecipientListRouter() {

		Message<String> fooMessage = MessageBuilder.withPayload("fooPayload").setHeader("recipient", true).build();
		Message<String> barMessage = MessageBuilder.withPayload("barPayload").setHeader("recipient", true).build();
		Message<String> badMessage = new GenericMessage<>("badPayload");

		this.recipientListInput.send(fooMessage);
		Message<?> result1a = this.fooChannel.receive(2000);
		assertNotNull(result1a);
		assertEquals("foo", result1a.getPayload());
		Message<?> result1b = this.barChannel.receive(2000);
		assertNotNull(result1b);
		assertEquals("foo", result1b.getPayload());

		this.recipientListInput.send(barMessage);
		assertNull(this.fooChannel.receive(0));
		Message<?> result2b = this.barChannel.receive(2000);
		assertNotNull(result2b);
		assertEquals("bar", result2b.getPayload());


		this.recipientListInput.send(badMessage);
		assertNull(this.fooChannel.receive(0));
		assertNull(this.barChannel.receive(0));
		Message<?> result3c = this.defaultOutputChannel.receive(2000);
		assertNotNull(result3c);
		assertEquals("bad", result3c.getPayload());

	}

	@Test
	public void testClaimCheck() {
		QueueChannel replyChannel = new QueueChannel();

		Message<String> message = MutableMessageBuilder.withPayload("foo").setReplyChannel(replyChannel).build();

		this.claimCheckInput.send(message);

		Message<?> receive = replyChannel.receive(2000);
		assertNotNull(receive);
		assertSame(message, receive);

		assertEquals(1, this.messageStore.getMessageCount());
		assertSame(message, this.messageStore.getMessage(message.getHeaders().getId()));
	}

	@Test
	public void testPriority() throws InterruptedException {
		Message<String> message = MessageBuilder.withPayload("1").setPriority(1).build();
		this.priorityChannel.send(message);

		message = MessageBuilder.withPayload("-1").setPriority(-1).build();
		this.priorityChannel.send(message);

		message = MessageBuilder.withPayload("3").setPriority(3).build();
		this.priorityChannel.send(message);

		message = MessageBuilder.withPayload("0").setPriority(0).build();
		this.priorityChannel.send(message);

		message = MessageBuilder.withPayload("2").setPriority(2).build();
		this.priorityChannel.send(message);

		message = MessageBuilder.withPayload("none").build();
		this.priorityChannel.send(message);

		message = MessageBuilder.withPayload("31").setPriority(3).build();
		this.priorityChannel.send(message);

		this.controlBus.send("@priorityChannelBridge.start()");

		Message<?> receive = this.priorityReplyChannel.receive(2000);
		assertNotNull(receive);
		assertEquals("3", receive.getPayload());

		receive = this.priorityReplyChannel.receive(2000);
		assertNotNull(receive);
		assertEquals("31", receive.getPayload());

		receive = this.priorityReplyChannel.receive(2000);
		assertNotNull(receive);
		assertEquals("2", receive.getPayload());

		receive = this.priorityReplyChannel.receive(2000);
		assertNotNull(receive);
		assertEquals("1", receive.getPayload());

		receive = this.priorityReplyChannel.receive(2000);
		assertNotNull(receive);
		assertEquals("0", receive.getPayload());

		receive = this.priorityReplyChannel.receive(2000);
		assertNotNull(receive);
		assertEquals("-1", receive.getPayload());

		receive = this.priorityReplyChannel.receive(2000);
		assertNotNull(receive);
		assertEquals("none", receive.getPayload());

		this.controlBus.send("@priorityChannelBridge.stop()");
	}

	@Test
	public void testMessageProducerFlow() throws Exception {
		FileOutputStream file = new FileOutputStream(new File(tmpDir, "TailTest"));
		for (int i = 0; i < 50; i++) {
			file.write((i + "\n").getBytes());
		}
		this.tailer.start();
		for (int i = 0; i < 50; i++) {
			Message<?> message = this.tailChannel.receive(5000);
			assertNotNull(message);
			assertEquals("hello " + i, message.getPayload());
		}
		assertNull(this.tailChannel.receive(1));

		this.controlBus.send("@tailer.stop()");
		file.close();
	}


	@Test
	public void testAmqpInboundGatewayFlow() throws Exception {
		Object result = this.amqpTemplate.convertSendAndReceive(this.amqpQueue.getName(), "world");
		assertEquals("HELLO WORLD", result);
	}

	@Test
	public void testGatewayFlow() throws Exception {
		PollableChannel replyChannel = new QueueChannel();
		Message<String> message = MessageBuilder.withPayload("foo").setReplyChannel(replyChannel).build();

		this.gatewayInput.send(message);

		Message<?> receive = replyChannel.receive(2000);
		assertNotNull(receive);
		assertEquals("FOO", receive.getPayload());
		assertNull(this.gatewayError.receive(1));

		message = MessageBuilder.withPayload("bar").setReplyChannel(replyChannel).build();

		this.gatewayInput.send(message);

		receive = replyChannel.receive(1);
		assertNull(receive);

		receive = this.gatewayError.receive(2000);
		assertNotNull(receive);
		assertThat(receive, instanceOf(ErrorMessage.class));
		assertThat(receive.getPayload(), instanceOf(MessageRejectedException.class));
		assertThat(((Exception) receive.getPayload()).getMessage(), containsString("' rejected Message"));
	}

	@Autowired
	@Qualifier("amqpOutboundInput")
	private MessageChannel amqpOutboundInput;

	@Autowired
	@Qualifier("amqpReplyChannel.channel")
	private PollableChannel amqpReplyChannel;

	@Test
	public void testAmqpOutboundFlow() throws Exception {
		this.amqpOutboundInput.send(MessageBuilder.withPayload("hello through the amqp")
				.setHeader("routingKey", "foo")
				.build());
		Message<?> receive = null;
		int i = 0;
		do {
			receive = this.amqpReplyChannel.receive();
			if (receive != null) {
				break;
			}
			Thread.sleep(100);
			i++;
		} while (i < 10);

		assertNotNull(receive);
		assertEquals("HELLO THROUGH THE AMQP", receive.getPayload());
	}

	@Autowired
	@Qualifier("jmsOutboundFlow.input")
	private MessageChannel jmsOutboundInboundChannel;

	@Autowired
	@Qualifier("jmsOutboundInboundReplyChannel")
	private PollableChannel jmsOutboundInboundReplyChannel;

	@Test
	public void testJmsOutboundInboundFlow() {
		this.jmsOutboundInboundChannel.send(MessageBuilder.withPayload("hello THROUGH the JMS")
				.setHeader(SimpMessageHeaderAccessor.DESTINATION_HEADER, "jmsInbound")
				.build());

		Message<?> receive = this.jmsOutboundInboundReplyChannel.receive(5000);

		assertNotNull(receive);
		assertEquals("HELLO THROUGH THE JMS", receive.getPayload());

		this.jmsOutboundInboundChannel.send(MessageBuilder.withPayload("hello THROUGH the JMS")
				.setHeader(SimpMessageHeaderAccessor.DESTINATION_HEADER, "jmsMessageDriver")
				.build());

		receive = this.jmsOutboundInboundReplyChannel.receive(5000);

		assertNotNull(receive);
		assertEquals("hello through the jms", receive.getPayload());
	}

	@Autowired
	@Qualifier("jmsOutboundGatewayFlow.input")
	private MessageChannel jmsOutboundGatewayChannel;

	@Test
	public void testJmsPipelineFlow() {
		PollableChannel replyChannel = new QueueChannel();
		Message<String> message = MessageBuilder.withPayload("hello through the jms pipeline")
				.setReplyChannel(replyChannel)
				.setHeader("destination", "jmsPipelineTest")
				.build();
		this.jmsOutboundGatewayChannel.send(message);

		Message<?> receive = replyChannel.receive(5000);

		assertNotNull(receive);
		assertEquals("HELLO THROUGH THE JMS PIPELINE", receive.getPayload());
	}

	@Autowired
	@Qualifier("fileReadingResultChannel")
	private PollableChannel fileReadingResultChannel;

	@Test
	public void testFileReadingFlow() throws Exception {
		List<Integer> evens = new ArrayList<>(25);
		for (int i = 0; i < 50; i++) {
			boolean even = i % 2 == 0;
			String extension = even ? ".sitest" : ".foofile";
			if (even) {
				evens.add(i);
			}
			FileOutputStream file = new FileOutputStream(new File(tmpDir, i + extension));
			file.write(("" + i).getBytes());
			file.flush();
			file.close();
		}

		Message<?> message = fileReadingResultChannel.receive(10000);
		assertNotNull(message);
		Object payload = message.getPayload();
		assertThat(payload, instanceOf(List.class));
		@SuppressWarnings("unchecked")
		List<String> result = (List<String>) payload;
		assertEquals(25, result.size());
		result.forEach(s -> assertTrue(evens.contains(Integer.parseInt(s))));
	}


	@Autowired
	@Qualifier("fileWritingInput")
	private MessageChannel fileWritingInput;

	@Autowired
	@Qualifier("fileWritingResultChannel")
	private PollableChannel fileWritingResultChannel;

	@Test
	public void testFileWritingFlow() throws Exception {
		String payload = "Spring Integration";
		this.fileWritingInput.send(new GenericMessage<>(payload));
		Message<?> receive = this.fileWritingResultChannel.receive(1000);
		assertNotNull(receive);
		assertThat(receive.getPayload(), instanceOf(File.class));
		File resultFile = (File) receive.getPayload();
		assertThat(resultFile.getAbsolutePath(),
				endsWith(TestUtils.applySystemFileSeparator("fileWritingFlow/foo.sitest")));
		String fileContent = StreamUtils.copyToString(new FileInputStream(resultFile), Charset.defaultCharset());
		assertEquals(payload, fileContent);
	}


	@Autowired
	private TestFtpServer ftpServer;

	@Autowired
	private DefaultFtpSessionFactory ftpSessionFactory;

	@Autowired
	private TestSftpServer sftpServer;

	@Autowired
	private DefaultSftpSessionFactory sftpSessionFactory;

	@Before
	@After
	public void setupRemoteFileServers() {
		this.ftpServer.recursiveDelete(this.ftpServer.getTargetLocalDirectory());
		this.ftpServer.recursiveDelete(this.ftpServer.getTargetFtpDirectory());
		this.sftpServer.recursiveDelete(this.sftpServer.getTargetLocalDirectory());
		this.sftpServer.recursiveDelete(this.sftpServer.getTargetSftpDirectory());
	}

	@Autowired
	@Qualifier("ftpInboundResultChannel")
	private PollableChannel ftpInboundResultChannel;

	@Test
	public void testFtpInboundFlow() {
		Message<?> message = this.ftpInboundResultChannel.receive(1000);
		assertNotNull(message);
		Object payload = message.getPayload();
		assertThat(payload, instanceOf(File.class));
		File file = (File) payload;
		assertThat(file.getName(), isOneOf("FTPSOURCE1.TXT.a", "FTPSOURCE2.TXT.a"));
		assertThat(file.getAbsolutePath(), containsString("ftpTest"));

		message = this.ftpInboundResultChannel.receive(1000);
		assertNotNull(message);
		file = (File) message.getPayload();
		assertThat(file.getName(), isOneOf("FTPSOURCE1.TXT.a", "FTPSOURCE2.TXT.a"));
		assertThat(file.getAbsolutePath(), containsString("ftpTest"));

		this.controlBus.send("@ftpInboundAdapter.stop()");
	}

	@Autowired
	@Qualifier("sftpInboundResultChannel")
	private PollableChannel sftpInboundResultChannel;

	@Test
	public void testSftpInboundFlow() {
		Message<?> message = this.sftpInboundResultChannel.receive(1000);
		assertNotNull(message);
		Object payload = message.getPayload();
		assertThat(payload, instanceOf(File.class));
		File file = (File) payload;
		assertThat(file.getName(), isOneOf("SFTPSOURCE1.TXT.a", "SFTPSOURCE2.TXT.a"));
		assertThat(file.getAbsolutePath(), containsString("sftpTest"));

		message = this.sftpInboundResultChannel.receive(1000);
		assertNotNull(message);
		file = (File) message.getPayload();
		assertThat(file.getName(), isOneOf("SFTPSOURCE1.TXT.a", "SFTPSOURCE2.TXT.a"));
		assertThat(file.getAbsolutePath(), containsString("sftpTest"));

		this.controlBus.send("@sftpInboundAdapter.stop()");
	}

	@Autowired
	@Qualifier("toFtpChannel")
	private MessageChannel toFtpChannel;

	@Test
	public void testFtpOutboundFlow() {
		String fileName = "foo.file";
		this.toFtpChannel.send(MessageBuilder.withPayload("foo")
				.setHeader(FileHeaders.FILENAME, fileName)
				.build());

		RemoteFileTemplate<FTPFile> template = new RemoteFileTemplate<>(this.ftpSessionFactory);
		FTPFile[] files = template.execute(session ->
				session.list(this.ftpServer.getTargetFtpDirectory().getName() + "/" + fileName));
		assertEquals(1, files.length);
		assertEquals(3, files[0].getSize());
	}

	@Autowired
	@Qualifier("toSftpChannel")
	private MessageChannel toSftpChannel;

	@Test
	public void testSftpOutboundFlow() {
		String fileName = "foo.file";
		this.toSftpChannel.send(MessageBuilder.withPayload("foo")
				.setHeader(FileHeaders.FILENAME, fileName)
				.build());

		RemoteFileTemplate<ChannelSftp.LsEntry> template = new RemoteFileTemplate<>(this.sftpSessionFactory);
		ChannelSftp.LsEntry[] files = template.execute(session ->
				session.list(this.sftpServer.getTargetSftpDirectory().getName() + "/" + fileName));
		assertEquals(1, files.length);
		assertEquals(3, files[0].getAttrs().getSize());
	}

	@Autowired
	@Qualifier("ftpMgetInputChannel")
	private MessageChannel ftpMgetInputChannel;

	@Autowired
	@Qualifier("remoteFileOutputChannel")
	private PollableChannel remoteFileOutputChannel;

	@Test
	@SuppressWarnings("unchecked")
	public void testFtpMgetFlow() {
		String dir = "ftpSource/";
		this.ftpMgetInputChannel.send(new GenericMessage<Object>(dir + "*"));
		Message<?> result = this.remoteFileOutputChannel.receive(1000);
		assertNotNull(result);
		List<File> localFiles = (List<File>) result.getPayload();
		// should have filtered ftpSource2.txt
		assertEquals(2, localFiles.size());

		for (File file : localFiles) {
			assertThat(file.getPath().replaceAll(Matcher.quoteReplacement(File.separator), "/"),
					Matchers.containsString(dir));
		}
		assertThat(localFiles.get(1).getPath().replaceAll(Matcher.quoteReplacement(File.separator), "/"),
				Matchers.containsString(dir + "subFtpSource"));
	}


	@Autowired
	@Qualifier("sftpMgetInputChannel")
	private MessageChannel sftpMgetInputChannel;

	@Test
	@SuppressWarnings("unchecked")
	public void testSftpMgetFlow() {
		String dir = "sftpSource/";
		this.sftpMgetInputChannel.send(new GenericMessage<Object>(dir + "*"));
		Message<?> result = this.remoteFileOutputChannel.receive(1000);
		assertNotNull(result);
		List<File> localFiles = (List<File>) result.getPayload();
		// should have filtered sftpSource2.txt
		assertEquals(2, localFiles.size());

		for (File file : localFiles) {
			assertThat(file.getPath().replaceAll(Matcher.quoteReplacement(File.separator), "/"),
					Matchers.containsString(dir));
		}
		assertThat(localFiles.get(1).getPath().replaceAll(Matcher.quoteReplacement(File.separator), "/"),
				Matchers.containsString(dir + "subSftpSource"));
	}

	@Autowired
	private MBeanServer mBeanServer;

	@Test
	public void testMBeansForDSL() throws MalformedObjectNameException {
		assertFalse(this.mBeanServer.queryMBeans(ObjectName.getInstance("org.springframework.integration:" +
				"bean=anonymous,name=sftpMgetInputChannel,type=MessageHandler"), null).isEmpty());
		assertFalse(this.mBeanServer.queryMBeans(ObjectName.getInstance("org.springframework.integration:" +
				"type=MessageHandler,name=ftpMgetInputChannel,bean=anonymous"), null).isEmpty());
	}

	@MessagingGateway(defaultRequestChannel = "controlBus")
	private static interface ControlBusGateway {

		void send(String command);
	}

	@Configuration
	@Import({TestFtpServer.class, TestSftpServer.class})
	@EnableAutoConfiguration
	@IntegrationComponentScan
	public static class ContextConfiguration {

		@Bean
		public MessageSource<?> integerMessageSource() {
			MethodInvokingMessageSource source = new MethodInvokingMessageSource();
			source.setObject(new AtomicInteger());
			source.setMethodName("getAndIncrement");
			return source;
		}

		@Bean
		public IntegrationFlow controlBusFlow() {
			return IntegrationFlows.from("controlBus").controlBus().get();
		}

		@Autowired
		private javax.jms.ConnectionFactory jmsConnectionFactory;

		@Bean
		public IntegrationFlow flow1() {
			return IntegrationFlows.from(this.integerMessageSource(),
					c -> c.poller(Pollers.fixedRate(100))
							.id("integerEndpoint")
							.autoStartup(false))
					.fixedSubscriberChannel("integerChannel")
					.transform("payload.toString()")
					.channel(Jms.pollableChannel("flow1QueueChannel", this.jmsConnectionFactory)
							.destination("flow1QueueChannel"))
					.get();
		}

		@Bean(name = PollerMetadata.DEFAULT_POLLER)
		public PollerMetadata poller() {
			return Pollers.fixedRate(500).maxMessagesPerPoll(1).get();
		}

		@Bean(name = IntegrationContextUtils.TASK_SCHEDULER_BEAN_NAME)
		public TaskScheduler taskScheduler() {
			ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
			threadPoolTaskScheduler.setPoolSize(100);
			return threadPoolTaskScheduler;
		}

		@Bean
		public MessageChannel inputChannel() {
			return MessageChannels.direct().get();
		}

		@Bean
		public MessageChannel foo() {
			return MessageChannels.publishSubscribe().get();
		}


		@Bean
		public IntegrationFlow jmsOutboundFlow() {
			return f -> f.handleWithAdapter(h -> h.jms(this.jmsConnectionFactory)
					.destinationExpression("headers." + SimpMessageHeaderAccessor.DESTINATION_HEADER));
		}

		@Bean
		public MessageChannel jmsOutboundInboundReplyChannel() {
			return MessageChannels.queue().get();
		}

		@Bean
		public IntegrationFlow jmsInboundFlow() {
			return IntegrationFlows
					.from((MessageSources s) -> s.jms(this.jmsConnectionFactory).destination("jmsInbound"))
					.<String, String>transform(String::toUpperCase)
					.channel(this.jmsOutboundInboundReplyChannel())
					.get();
		}

		@Bean
		public IntegrationFlow jmsMessageDriverFlow() {
			return IntegrationFlows
					.from(Jms.messageDriverChannelAdapter(this.jmsConnectionFactory)
							.destination("jmsMessageDriver"))
					.<String, String>transform(String::toLowerCase)
					.channel(this.jmsOutboundInboundReplyChannel())
					.get();
		}

		@Bean
		public IntegrationFlow jmsOutboundGatewayFlow() {
			return f -> f.handleWithAdapter(a -> a.jmsGateway(this.jmsConnectionFactory)
					.replyContainer()
					.requestDestination("jmsPipelineTest"));
		}

		@Bean
		public IntegrationFlow jmsInboundGatewayFlow() {
			return IntegrationFlows.from((MessagingGateways g) -> g.jms(this.jmsConnectionFactory)
					.destination("jmsPipelineTest"))
					.<String, String>transform(String::toUpperCase)
					.get();
		}

		@Autowired
		private TestFtpServer ftpServer;

		@Autowired
		private DefaultFtpSessionFactory ftpSessionFactory;

		@Autowired
		private TestSftpServer sftpServer;

		@Autowired
		private DefaultSftpSessionFactory sftpSessionFactory;

		@Bean
		public IntegrationFlow ftpInboundFlow() {
			return IntegrationFlows
					.from(s -> s.ftp(this.ftpSessionFactory)
									.preserveTimestamp(true)
									.remoteDirectory("ftpSource")
									.regexFilter(".*\\.txt$")
									.localFilenameGeneratorExpression("#this.toUpperCase() + '.a'")
									.localDirectory(this.ftpServer.getTargetLocalDirectory()),
							e -> e.id("ftpInboundAdapter"))
					.channel(MessageChannels.queue("ftpInboundResultChannel"))
					.get();
		}

		@Bean
		public IntegrationFlow sftpInboundFlow() {
			return IntegrationFlows
					.from(s -> s.sftp(this.sftpSessionFactory)
									.preserveTimestamp(true)
									.remoteDirectory("sftpSource")
									.regexFilter(".*\\.txt$")
									.localFilenameGeneratorExpression("#this.toUpperCase() + '.a'")
									.localDirectory(this.sftpServer.getTargetLocalDirectory()),
							e -> e.id("sftpInboundAdapter"))
					.channel(MessageChannels.queue("sftpInboundResultChannel"))
					.get();
		}

		@Bean
		public IntegrationFlow ftpOutboundFlow() {
			return IntegrationFlows.from("toFtpChannel")
					.handle(Ftp.outboundAdapter(this.ftpSessionFactory)
									.useTemporaryFileName(false)
									.remoteDirectory(this.ftpServer.getTargetFtpDirectory().getName())
					).get();
		}

		@Bean
		public IntegrationFlow sftpOutboundFlow() {
			return IntegrationFlows.from("toSftpChannel")
					.handle(Sftp.outboundAdapter(this.sftpSessionFactory)
									.useTemporaryFileName(false)
									.remoteDirectory(this.sftpServer.getTargetSftpDirectory().getName())
					).get();
		}

		@Bean
		public PollableChannel remoteFileOutputChannel() {
			return new QueueChannel();
		}

		@Bean
		public MessageHandler ftpOutboundGateway() {
			return Ftp.outboundGateway(this.ftpSessionFactory, AbstractRemoteFileOutboundGateway.Command.MGET,
					"payload")
					.options(AbstractRemoteFileOutboundGateway.Option.RECURSIVE)
					.regexFileNameFilter("(subFtpSource|.*1.txt)")
					.localDirectoryExpression("@ftpServer.targetLocalDirectoryName + #remoteDirectory")
					.localFilenameGeneratorExpression("#remoteFileName.replaceFirst('ftpSource', " +
							"'localTarget')").get();
		}

		@Bean
		public IntegrationFlow ftpMGetFlow() {
			return IntegrationFlows.from("ftpMgetInputChannel")
					.handle(ftpOutboundGateway())
					.channel(remoteFileOutputChannel())
					.get();
		}

		@Bean
		public IntegrationFlow sftpMGetFlow() {
			return IntegrationFlows.from("sftpMgetInputChannel")
					.handleWithAdapter(h ->
							h.sftpGateway(this.sftpSessionFactory, AbstractRemoteFileOutboundGateway.Command.MGET,
									"payload")
									.options(AbstractRemoteFileOutboundGateway.Option.RECURSIVE)
									.regexFileNameFilter("(subSftpSource|.*1.txt)")
									.localDirectoryExpression("@sftpServer.targetLocalDirectoryName + #remoteDirectory")
									.localFilenameGeneratorExpression(
											"#remoteFileName.replaceFirst('sftpSource', 'localTarget')"))
					.channel(remoteFileOutputChannel())
					.get();
		}

		@Bean
		public IntegrationFlow routerAsNonLastFlow() {
			return f -> f.<String, String>route(p -> p, r -> r.resolutionRequired(false))
					.channel(MessageChannels.queue("routerAsNonLastDefaultOutputChannel"));
		}

	}

	@Configuration
	@ComponentScan
	public static class ContextConfiguration2 {

		@Autowired
		@Qualifier("inputChannel")
		private MessageChannel inputChannel;

		@Autowired
		@Qualifier("successChannel")
		private PollableChannel successChannel;


		@Bean
		public Advice expressionAdvice() {
			ExpressionEvaluatingRequestHandlerAdvice advice = new ExpressionEvaluatingRequestHandlerAdvice();
			advice.setOnSuccessExpression("payload");
			advice.setSuccessChannel(this.successChannel);
			return advice;
		}

		@Bean
		public IntegrationFlow flow2() {
			return IntegrationFlows.from(this.inputChannel)
					.filter(p -> p instanceof String, c -> c.id("filter"))
					.channel("foo")
					.fixedSubscriberChannel()
					.<String, Integer>transform(Integer::parseInt)
					.transform(new PayloadSerializingTransformer(),
							c -> c.autoStartup(false).id("payloadSerializingTransformer"))
					.channel(MessageChannels.queue(new SimpleMessageStore(), "fooQueue"))
					.transform(new PayloadDeserializingTransformer())
					.channel(publishSubscribeChannel())
					.transform((Integer p) -> p * 2, c -> c.advice(this.expressionAdvice()))
					.get();
		}

		@Bean
		public MessageChannel publishSubscribeChannel() {
			return MessageChannels.publishSubscribe().get();
		}

		@Bean
		public MongoDbFactory mongoDbFactory() throws Exception {
			return new SimpleMongoDbFactory(new MongoClient("localhost", mongoPort), "local");
		}

		@Bean
		public MongoDbChannelMessageStore mongoDbChannelMessageStore(MongoDbFactory mongoDbFactory) {
			MongoDbChannelMessageStore mongoDbChannelMessageStore = new MongoDbChannelMessageStore(mongoDbFactory);
			mongoDbChannelMessageStore.setPriorityEnabled(true);
			return mongoDbChannelMessageStore;
		}

		@Bean
		public IntegrationFlow priorityFlow(PriorityCapableChannelMessageStore mongoDbChannelMessageStore) {
			return IntegrationFlows.from((Channels c) ->
					c.priority("priorityChannel", mongoDbChannelMessageStore, "priorityGroup"))
					.bridge(s -> s.poller(Pollers.fixedDelay(100))
							.autoStartup(false)
							.id("priorityChannelBridge"))
					.channel(MessageChannels.queue("priorityReplyChannel"))
					.get();
		}

	}

	@MessageEndpoint
	public static class AnnotationTestService {

		@ServiceActivator(inputChannel = "publishSubscribeChannel")
		public void handle(Object payload) {
			assertEquals(100, payload);
		}
	}

	@Configuration
	public static class ContextConfiguration3 {

		@Autowired
		@Qualifier("delayedAdvice")
		private MethodInterceptor delayedAdvice;

		@Bean
		public QueueChannel successChannel() {
			return MessageChannels.queue().get();
		}

		@Bean
		public AtomicReference<Object> eventHolder() {
			return new AtomicReference<>();
		}

		@Bean
		public ApplicationListener<MessagingEvent> eventListener() {
			return new ApplicationListener<MessagingEvent>() {

				@Override
				public void onApplicationEvent(MessagingEvent event) {
					eventHolder().set(event.getMessage().getPayload());
				}
			};
		}

		@Bean
		public IntegrationFlow flow3() {
			return IntegrationFlows.from("flow3Input")
					.handle(Integer.class, (p, h) -> p * 2)
					.handle(new ApplicationEventPublishingMessageHandler())
					.get();
		}

		@Bean
		public IntegrationFlow bridgeFlow() {
			return IntegrationFlows.from(MessageChannels.queue("bridgeFlowInput"))
					.channel(MessageChannels.queue("bridgeFlowOutput"))
					.get();
		}

		@Bean
		public IntegrationFlow bridgeFlow2() {
			return IntegrationFlows.from("bridgeFlow2Input")
					.bridge(c -> c.autoStartup(false).id("bridge"))
					.fixedSubscriberChannel()
					.delay("delayer", "200", c -> c.advice(this.delayedAdvice).messageStore(this.messageStore()))
					.channel(c -> c.queue("bridgeFlow2Output"))
					.get();
		}

		@Bean
		public SimpleMessageStore messageStore() {
			return new SimpleMessageStore();
		}

		@Bean
		public IntegrationFlow claimCheckFlow() {
			return IntegrationFlows.from("claimCheckInput")
					.claimCheckIn(this.messageStore())
					.claimCheckOut(this.messageStore())
					.get();
		}

		@Bean(name = "foo-channel")
		public MessageChannel fooChannel() {
			return new QueueChannel();
		}

		@Bean(name = "bar-channel")
		public MessageChannel barChannel() {
			return new QueueChannel();
		}

		@Bean
		public MessageChannel defaultOutputChannel() {
			return new QueueChannel();
		}

		@Bean
		public IntegrationFlow recipientListFlow() {
			return IntegrationFlows.from("recipientListInput")
					.<String, String>transform(p -> p.replaceFirst("Payload", ""))
					.routeToRecipients(r -> r.recipient("foo-channel", "'foo' == payload")
							.recipient("bar-channel", m ->
									m.getHeaders().containsKey("recipient")
											&& (boolean) m.getHeaders().get("recipient")))
					.channel("defaultOutputChannel")
					.get();
		}
	}

	@Component("delayedAdvice")
	public static class DelayedAdvice implements MethodInterceptor {

		private final AtomicBoolean invoked = new AtomicBoolean();

		@Override
		public Object invoke(MethodInvocation invocation) throws Throwable {
			this.invoked.set(true);
			return invocation.proceed();
		}

		public Boolean getInvoked() {
			return invoked.get();
		}

	}

	@Configuration
	public static class ContextConfiguration4 {

		@Bean
		public IntegrationFlow fileFlow1() {
			return IntegrationFlows.from("fileFlow1Input")
					.<FileWritingMessageHandler>handleWithAdapter(h -> h.file(tmpDir).fileNameGenerator(message -> null)
							, c -> c.id("fileWriting"))
					.get();
		}

		@Autowired
		@Qualifier("integrationFlowTests.GreetingService")
		private MessageHandler greetingService;

		@Bean
		public IntegrationFlow methodInvokingFlow() {
			return IntegrationFlows.from("methodInvokingInput")
					.handle(this.greetingService)
					.get();
		}

		@Bean
		public IntegrationFlow lamdasFlow() {
			return IntegrationFlows.from("lamdasInput")
					.filter("World"::equals)
					.transform("Hello "::concat)
					.get();
		}

		@Bean
		@DependsOn("enrichFlow")
		public IntegrationFlow enricherFlow() {
			return IntegrationFlows.from("enricherInput", true)
					.enrich(e -> e.requestChannel("enrichChannel")
									.requestPayloadExpression("payload")
									.shouldClonePayload(false)
									.propertyExpression("name", "payload['name']")
									.propertyExpression("date", "new java.util.Date()")
									.headerExpression("foo", "payload['name']")
					)
					.get();
		}

		@Bean
		@DependsOn("enrichFlow")
		public IntegrationFlow enricherFlow2() {
			return IntegrationFlows.from("enricherInput2", true)
					.enrich(e -> e.requestChannel("enrichChannel")
									.requestPayloadExpression("payload")
									.shouldClonePayload(false)
									.propertyExpression("name", "payload['name']")
									.propertyExpression("date", "new java.util.Date()")
					)
					.get();
		}

		@Bean
		@DependsOn("enrichFlow")
		public IntegrationFlow enricherFlow3() {
			return IntegrationFlows.from("enricherInput3", true)
					.enrich(e -> e.requestChannel("enrichChannel")
									.requestPayloadExpression("payload")
									.shouldClonePayload(false)
									.headerExpression("foo", "payload['name']")
					)
					.get();
		}

		@Bean
		public IntegrationFlow enrichFlow() {
			return IntegrationFlows.from("enrichChannel")
					.<TestPojo, Map<?, ?>>transform(p -> Collections.singletonMap("name", p.getName() + " Bar"))
					.get();
		}

		@Bean
		public Executor taskExecutor() {
			return Executors.newCachedThreadPool();
		}

		@Bean
		public TestSplitterPojo testSplitterData() {
			List<String> first = new ArrayList<>();
			first.add("1,2,3");
			first.add("4,5,6");

			List<String> second = new ArrayList<>();
			second.add("7,8,9");
			second.add("10,11,12");

			return new TestSplitterPojo(first, second);
		}

		@Bean
		public IntegrationFlow splitResequenceFlow() {
			return f -> f.enrichHeaders(s -> s.header("FOO", "BAR"))
					.split("testSplitterData", "buildList", c -> c.applySequence(false))
					.channel(c -> c.executor(this.taskExecutor()))
					.split(Message.class, target -> (List<?>) target.getPayload(), c -> c.applySequence(false))
					.channel(MessageChannels.executor(this.taskExecutor()))
					.split(s -> s.applySequence(false).get().getT2().setDelimiters(","))
					.channel(c -> c.executor(this.taskExecutor()))
					.<String, Integer>transform(Integer::parseInt)
					.enrichHeaders(s -> s.headerExpression(IntegrationMessageHeaderAccessor.SEQUENCE_NUMBER, "payload"))
					.resequence(r -> r.releasePartialSequences(true).correlationExpression("'foo'"), null)
					.headerFilter("foo", false);
		}

		@Bean
		public IntegrationFlow splitAggregateFlow() {
			return IntegrationFlows.from("splitAggregateInput", true)
					.split(null)
					.channel(MessageChannels.executor(this.taskExecutor()))
					.resequence()
					.aggregate()
					.get();
		}

		@Bean
		public IntegrationFlow xpathHeaderEnricherFlow() {
			return IntegrationFlows.from("xpathHeaderEnricherInput")
					.enrichHeaders(
							s -> s.header("one",
									new XPathExpressionEvaluatingHeaderValueMessageProcessor("/root/elementOne"))
									.header("two",
											new XPathExpressionEvaluatingHeaderValueMessageProcessor("/root/elementTwo"))
									.headerChannelsToString(),
							c -> c.autoStartup(false).id("xpathHeaderEnricher")
					)
					.get();
		}

		@Bean
		public QueueChannel oddChannel() {
			return new QueueChannel();
		}

		@Bean
		public QueueChannel evenChannel() {
			return new QueueChannel();
		}

		@Bean
		public IntegrationFlow routeFlow() {
			return IntegrationFlows.from("routerInput")
					.<Integer, Boolean>route(p -> p % 2 == 0,
							m -> m.suffix("Channel")
									.channelMapping("true", "even")
									.channelMapping("false", "odd"))
					.get();
		}

		@Bean
		public RoutingTestBean routingTestBean() {
			return new RoutingTestBean();
		}

		@Bean
		public IntegrationFlow routeMethodInvocationFlow() {
			return IntegrationFlows.from("routerMethodInput")
					.route("routingTestBean", "routeMessage")
					.get();
		}

		@Bean
		public IntegrationFlow routeMethodInvocationFlow2() {
			return IntegrationFlows.from("routerMethod2Input")
					.route(new MethodInvokingRouter(new RoutingTestBean(), "routeByHeader"))
					.get();
		}

		@Bean
		public IntegrationFlow routeMethodInvocationFlow3() {
			return f -> f.route((String p) -> ContextConfiguration4.this.routingTestBean().routePayload(p));
		}

		@Bean
		public IntegrationFlow routeMultiMethodInvocationFlow() {
			return IntegrationFlows.from("routerMultiInput")
					.route(String.class, p -> p.equals("foo") || p.equals("bar") ? new String[] {"foo", "bar"} : null,
							s -> s.suffix("-channel"))
					.get();
		}

		@Bean
		public IntegrationFlow tailFlow() {
			return IntegrationFlows.from((MessageProducers p) -> p.tail(new File(tmpDir, "TailTest"))
					.delay(500)
					.end(false)
					.id("tailer")
					.autoStartup(false))
					.transform("hello "::concat)
					.channel(MessageChannels.queue("tailChannel"))
					.get();
		}

		@Autowired
		private ConnectionFactory rabbitConnectionFactory;

		@Autowired
		private AmqpTemplate amqpTemplate;

		@Bean
		public Queue queue() {
			return new AnonymousQueue();
		}

		@Bean
		public IntegrationFlow amqpFlow() {
			return IntegrationFlows.from(Amqp.inboundGateway(this.rabbitConnectionFactory, queue()))
					.transform("hello "::concat)
					.transform(String.class, String::toUpperCase)
					.get();
		}

		@Bean
		public IntegrationFlow amqpOutboundFlow() {
			return IntegrationFlows.from(Amqp.channel("amqpOutboundInput", this.rabbitConnectionFactory))
					.handle(Amqp.outboundAdapter(this.amqpTemplate).routingKeyExpression("headers.routingKey"))
					.get();
		}

		@Bean
		public Queue fooQueue() {
			return new Queue("foo");
		}

		@Bean
		public Queue amqpReplyChannel() {
			return new Queue("amqpReplyChannel");
		}

		@Bean
		public IntegrationFlow amqpInboundFlow() {
			return IntegrationFlows.from((MessageProducers p) -> p.amqp(this.rabbitConnectionFactory, fooQueue()))
					.transform(String.class, String::toUpperCase)
					.channel(Amqp.pollableChannel(this.rabbitConnectionFactory)
							.queueName("amqpReplyChannel")
							.channelTransacted(true))
					.get();
		}

		@Bean
		@DependsOn("gatewayRequestFlow")
		public IntegrationFlow gatewayFlow() {
			return IntegrationFlows.from("gatewayInput")
					.gateway("gatewayRequest", g -> g.errorChannel("gatewayError").replyTimeout(10L))
					.get();
		}

		@Bean
		public IntegrationFlow gatewayRequestFlow() {
			return IntegrationFlows.from("gatewayRequest")
					.filter("foo"::equals, f -> f.throwExceptionOnRejection(true))
					.<String, String>transform(String::toUpperCase)
					.get();
		}

		@Bean
		public MessageChannel gatewayError() {
			return MessageChannels.queue().get();
		}


		@Bean
		public IntegrationFlow fileReadingFlow() {
			return IntegrationFlows
					.from(s -> s.file(tmpDir).patternFilter("*.sitest"),
							e -> e.poller(Pollers.fixedDelay(100)))
					.transform(Transformers.fileToString())
					.aggregate(a -> a.correlationExpression("1")
							.releaseExpression("size() == 25"), null)
					.channel(MessageChannels.queue("fileReadingResultChannel"))
					.get();
		}

		@Bean
		public IntegrationFlow fileWritingFlow() {
			return IntegrationFlows.from("fileWritingInput")
					.enrichHeaders(h -> h.header(FileHeaders.FILENAME, "foo.sitest")
							.header("directory", new File(tmpDir, "fileWritingFlow")))
					.handle(Files.outboundGateway("headers[directory]"))
					.channel(MessageChannels.queue("fileWritingResultChannel"))
					.get();
		}

	}

	private static class RoutingTestBean {

		public String routePayload(String name) {
			return name + "-channel";
		}

		public String routeByHeader(@Header("targetChannel") String name) {
			return name + "-channel";
		}

		public String routeMessage(Message<?> message) {
			if (message.getPayload().equals("foo")) {
				return "foo-channel";
			}
			else if (message.getPayload().equals("bar")) {
				return "bar-channel";
			}
			return null;
		}
	}

	@Service
	public static class GreetingService extends AbstractReplyProducingMessageHandler {

		@Autowired
		private WorldService worldService;

		@Override
		protected Object handleRequestMessage(Message<?> requestMessage) {
			return "Hello " + this.worldService.world() + " and " + requestMessage.getPayload();
		}
	}

	@Service
	public static class WorldService {

		public String world() {
			return "World";
		}
	}


	private static class InvalidLastMessageChannelFlowContext {

		@Bean
		public IntegrationFlow wrongLastComponent() {
			return IntegrationFlows.from(MessageChannels.direct())
					.fixedSubscriberChannel()
					.get();
		}

	}

	@EnableIntegration
	public static class InvalidConfigurationWithSpec {

		@Bean
		public DirectChannelSpec invalidBean() {
			return MessageChannels.direct();
		}

	}

	@Component
	@GlobalChannelInterceptor(patterns = "flow1QueueChannel")
	public static class TestChannelInterceptor extends ChannelInterceptorAdapter {

		private final AtomicInteger invoked = new AtomicInteger();

		@Override
		public Message<?> preSend(Message<?> message, MessageChannel channel) {
			this.invoked.incrementAndGet();
			return message;
		}

		public Integer getInvoked() {
			return invoked.get();
		}

	}

	private static class TestPojo {

		private String name;

		private Date date;

		private TestPojo(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Date getDate() {
			return date;
		}

		public void setDate(Date date) {
			this.date = date;
		}

	}

	private static class TestSplitterPojo {

		final List<String> first;

		final List<String> second;

		private TestSplitterPojo(List<String> first, List<String> second) {
			this.first = first;
			this.second = second;
		}

		public List<String> getFirst() {
			return first;
		}

		public List<String> getSecond() {
			return second;
		}

		public List<List<String>> buildList() {
			return Arrays.asList(this.first, this.second);
		}

	}

}

