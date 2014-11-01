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
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.integration.IntegrationMessageHeaderAccessor;
import org.springframework.integration.MessageDispatchingException;
import org.springframework.integration.MessageRejectedException;
import org.springframework.integration.annotation.Header;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.FixedSubscriberChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.context.IntegrationContextUtils;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.channel.DirectChannelSpec;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.integration.dsl.core.Pollers;
import org.springframework.integration.event.core.MessagingEvent;
import org.springframework.integration.event.outbound.ApplicationEventPublishingMessageHandler;
import org.springframework.integration.handler.AbstractReplyProducingMessageHandler;
import org.springframework.integration.handler.advice.ExpressionEvaluatingRequestHandlerAdvice;
import org.springframework.integration.router.MethodInvokingRouter;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.integration.store.MessageStore;
import org.springframework.integration.store.SimpleMessageStore;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.support.MutableMessageBuilder;
import org.springframework.integration.transformer.PayloadDeserializingTransformer;
import org.springframework.integration.transformer.PayloadSerializingTransformer;
import org.springframework.integration.xml.transformer.support.XPathExpressionEvaluatingHeaderValueMessageProcessor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.PollableChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.core.DestinationResolutionException;
import org.springframework.messaging.support.ErrorMessage;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Artem Bilan
 * @author Tim Ysewyn
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
public class IntegrationFlowTests {

	@Autowired
	private ListableBeanFactory beanFactory;

	@Autowired
	private ControlBusGateway controlBus;

	@Autowired
	@Qualifier("inputChannel")
	private MessageChannel inputChannel;

	@Autowired
	@Qualifier("discardChannel")
	private PollableChannel discardChannel;

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
	@Qualifier("recipientListSubFlow1Result")
	private PollableChannel recipientListSubFlow1Result;

	@Autowired
	@Qualifier("recipientListSubFlow2Result")
	private PollableChannel recipientListSubFlow2Result;

	@Autowired
	@Qualifier("defaultOutputChannel")
	private PollableChannel defaultOutputChannel;

	@Autowired
	private MessageStore messageStore;

	@Autowired
	@Qualifier("claimCheckInput")
	private MessageChannel claimCheckInput;

	@Autowired
	@Qualifier("lamdasInput")
	private MessageChannel lamdasInput;

	@Autowired
	@Qualifier("gatewayInput")
	private MessageChannel gatewayInput;

	@Autowired
	@Qualifier("gatewayError")
	private PollableChannel gatewayError;

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

		this.inputChannel.send(new GenericMessage<Object>(1000));
		Message<?> discarded = this.discardChannel.receive(5000);
		assertNotNull(discarded);
		assertEquals("Discarded: 1000", discarded.getPayload());
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
		this.beanFactory.containsBean("routeFlow.subFlow#0.channel#0");

		int[] payloads = new int[] {1, 2, 3, 4, 5, 6};

		for (int payload : payloads) {
			this.routerInput.send(new GenericMessage<>(payload));
		}

		for (int i = 0; i < 3; i++) {
			Message<?> receive = this.oddChannel.receive(2000);
			assertNotNull(receive);
			assertEquals(payloads[i * 2] * 3, receive.getPayload());

			receive = this.evenChannel.receive(2000);
			assertNotNull(receive);
			assertEquals(payloads[i * 2 + 1], receive.getPayload());
		}
	}

	@Autowired
	@Qualifier("routerTwoSubFlows.input")
	private MessageChannel routerTwoSubFlowsInput;

	@Autowired
	@Qualifier("routerTwoSubFlowsOutput")
	private PollableChannel routerTwoSubFlowsOutput;

	@Test
	public void testRouterWithTwoSubflows() {
		this.routerTwoSubFlowsInput.send(new GenericMessage<Object>(Arrays.asList(1, 2, 3, 4, 5, 6)));
		Message<?> receive = this.routerTwoSubFlowsOutput.receive(5000);
		assertNotNull(receive);
		Object payload = receive.getPayload();
		assertThat(payload, instanceOf(List.class));
		@SuppressWarnings("unchecked")
		List<Integer> results = (List<Integer>) payload;

		assertArrayEquals(new Integer[] {3, 4, 9, 8, 15, 12}, results.toArray(new Integer[results.size()]));
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
		Message<String> bazMessage = new GenericMessage<>("baz");
		Message<String> badMessage = new GenericMessage<>("badPayload");

		this.recipientListInput.send(fooMessage);
		Message<?> result1a = this.fooChannel.receive(2000);
		assertNotNull(result1a);
		assertEquals("foo", result1a.getPayload());
		Message<?> result1b = this.barChannel.receive(2000);
		assertNotNull(result1b);
		assertEquals("foo", result1b.getPayload());
		Message<?> result1c = this.recipientListSubFlow1Result.receive(2000);
		assertNotNull(result1c);
		assertEquals("FOO", result1c.getPayload());
		assertNull(this.recipientListSubFlow2Result.receive(0));

		this.recipientListInput.send(barMessage);
		assertNull(this.fooChannel.receive(0));
		assertNull(this.recipientListSubFlow2Result.receive(0));
		Message<?> result2b = this.barChannel.receive(2000);
		assertNotNull(result2b);
		assertEquals("bar", result2b.getPayload());
		Message<?> result2c = this.recipientListSubFlow1Result.receive(2000);
		assertNotNull(result1c);
		assertEquals("BAR", result2c.getPayload());

		this.recipientListInput.send(bazMessage);
		assertNull(this.fooChannel.receive(0));
		assertNull(this.barChannel.receive(0));
		Message<?> result3c = this.recipientListSubFlow1Result.receive(2000);
		assertNotNull(result3c);
		assertEquals("BAZ", result3c.getPayload());
		Message<?> result4c = this.recipientListSubFlow2Result.receive(2000);
		assertNotNull(result4c);
		assertEquals("Hello baz", result4c.getPayload());

		this.recipientListInput.send(badMessage);
		assertNull(this.fooChannel.receive(0));
		assertNull(this.barChannel.receive(0));
		assertNull(this.recipientListSubFlow1Result.receive(0));
		assertNull(this.recipientListSubFlow2Result.receive(0));
		Message<?> resultD = this.defaultOutputChannel.receive(2000);
		assertNotNull(resultD);
		assertEquals("bad", resultD.getPayload());
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
	public void testGatewayFlow() throws Exception {
		PollableChannel replyChannel = new QueueChannel();
		Message<String> message = MessageBuilder.withPayload("foo").setReplyChannel(replyChannel).build();

		this.gatewayInput.send(message);

		Message<?> receive = replyChannel.receive(2000);
		assertNotNull(receive);
		assertEquals("From Gateway SubFlow: FOO", receive.getPayload());
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
	@Qualifier("subscribersFlow.input")
	private MessageChannel subscribersFlowInput;

	@Autowired
	@Qualifier("subscriber1Results")
	private PollableChannel subscriber1Results;

	@Autowired
	@Qualifier("subscriber2Results")
	private PollableChannel subscriber2Results;

	@Autowired
	@Qualifier("subscriber3Results")
	private PollableChannel subscriber3Results;

	@Test
	public void testSubscribersSubFlows() {
		this.subscribersFlowInput.send(new GenericMessage<Integer>(2));

		Message<?> receive1 = this.subscriber1Results.receive(5000);
		assertNotNull(receive1);
		assertEquals(1, receive1.getPayload());

		Message<?> receive2 = this.subscriber2Results.receive(5000);
		assertNotNull(receive2);
		assertEquals(4, receive2.getPayload());
		Message<?> receive3 = this.subscriber3Results.receive(5000);
		assertNotNull(receive3);
		assertEquals(6, receive3.getPayload());
	}

	@MessagingGateway(defaultRequestChannel = "controlBus")
	private static interface ControlBusGateway {

		void send(String command);
	}

	@Configuration
	@EnableIntegration
	@IntegrationComponentScan
	public static class ContextConfiguration {

		@Bean
		public IntegrationFlow controlBusFlow() {
			return IntegrationFlows.from("controlBus").controlBus().get();
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
					.filter(p -> p instanceof String, e -> e
							.id("filter")
							.discardFlow(df -> df
									.transform(String.class, "Discarded: "::concat)
									.channel(c -> c.queue("discardChannel"))))
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
		public IntegrationFlow subscribersFlow() {
			return flow -> flow
					.publishSubscribeChannel(Executors.newCachedThreadPool(), s -> s
							.subscribe(f -> f
									.<Integer>handle((p, h) -> p / 2)
									.channel(c -> c.queue("subscriber1Results")))
							.subscribe(f -> f
									.<Integer>handle((p, h) -> p * 2)
									.channel(c -> c.queue("subscriber2Results"))))
					.<Integer>handle((p, h) -> p * 3)
					.channel(c -> c.queue("subscriber3Results"));
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
					.routeToRecipients(r -> r
							.recipient("foo-channel", "'foo' == payload")
							.recipient("bar-channel", m ->
									m.getHeaders().containsKey("recipient")
											&& (boolean) m.getHeaders().get("recipient"))
							.recipientFlow("'foo' == payload or 'bar' == payload or 'baz' == payload",
									f -> f.transform(String.class, p -> p.toUpperCase())
											.channel(c -> c.queue("recipientListSubFlow1Result")))
							.recipientFlow(m -> "baz".equals(m.getPayload()),
									f -> f.transform("Hello "::concat)
											.channel(c -> c.queue("recipientListSubFlow2Result"))))
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
									.propertyFunction("date", m -> new Date())
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
							.requestPayload(Message::getPayload)
							.shouldClonePayload(false)
							.<Map<String, String>>headerFunction("foo", m -> m.getPayload().get("name")))
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
					.enrichHeaders(h ->
							h.headerFunction(IntegrationMessageHeaderAccessor.SEQUENCE_NUMBER, Message::getPayload))
					.resequence(r -> r.releasePartialSequences(true).correlationExpression("'foo'"), null)
					.headerFilter("foo", false);
		}

		@Bean
		public IntegrationFlow splitAggregateFlow() {
			return IntegrationFlows.from("splitAggregateInput", true)
					.split()
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
		public QueueChannel evenChannel() {
			return new QueueChannel();
		}

		@Bean
		public IntegrationFlow routeFlow() {
			return IntegrationFlows.from("routerInput")
					.<Integer, Boolean>route(p -> p % 2 == 0,
							m -> m.channelMapping("true", "evenChannel")
									.subFlowMapping("false", f ->
											f.<Integer>handle((p, h) -> p * 3)))
					.channel(c -> c.queue("oddChannel"))
					.get();
		}


		@Bean
		public IntegrationFlow routerTwoSubFlows() {
			return f -> f
					.split()
					.<Integer, Boolean>route(p -> p % 2 == 0, m -> m
							.subFlowMapping("true", sf -> sf.<Integer>handle((p, h) -> p * 2))
							.subFlowMapping("false", sf -> sf.<Integer>handle((p, h) -> p * 3)))
					.aggregate()
					.channel(c -> c.queue("routerTwoSubFlowsOutput"));
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
		@DependsOn("gatewayRequestFlow")
		public IntegrationFlow gatewayFlow() {
			return IntegrationFlows.from("gatewayInput")
					.gateway("gatewayRequest", g -> g.errorChannel("gatewayError").replyTimeout(10L))
					.gateway(f -> f.transform("From Gateway SubFlow: "::concat))
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

