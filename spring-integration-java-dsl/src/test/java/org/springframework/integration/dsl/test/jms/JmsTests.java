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

package org.springframework.integration.dsl.test.jms;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;

import javax.jms.ConnectionFactory;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.channel.ChannelInterceptorAware;
import org.springframework.integration.channel.FixedSubscriberChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.config.GlobalChannelInterceptor;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageSources;
import org.springframework.integration.dsl.MessagingGateways;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.integration.dsl.core.Pollers;
import org.springframework.integration.dsl.jms.Jms;
import org.springframework.integration.endpoint.MethodInvokingMessageSource;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.PollableChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptorAdapter;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Artem Bilan
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
public class JmsTests {

	@Autowired
	private ListableBeanFactory beanFactory;

	@Autowired
	private ControlBusGateway controlBus;

	@Autowired
	@Qualifier("flow1QueueChannel")
	private PollableChannel outputChannel;

	@Autowired
	@Qualifier("jmsOutboundFlow.input")
	private MessageChannel jmsOutboundInboundChannel;

	@Autowired
	@Qualifier("jmsOutboundInboundReplyChannel")
	private PollableChannel jmsOutboundInboundReplyChannel;

	@Autowired
	@Qualifier("jmsOutboundGatewayFlow.input")
	private MessageChannel jmsOutboundGatewayChannel;

	@Autowired
	private TestChannelInterceptor testChannelInterceptor;

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
		assertThat(this.testChannelInterceptor.invoked.get(), Matchers.greaterThanOrEqualTo(5));

	}

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

	@MessagingGateway(defaultRequestChannel = "controlBus.input")
	private static interface ControlBusGateway {

		void send(String command);

	}

	@Configuration
	@EnableAutoConfiguration
	@IntegrationComponentScan
	@ComponentScan
	public static class ContextConfiguration {

		@Autowired
		private ConnectionFactory jmsConnectionFactory;

		@Bean(name = PollerMetadata.DEFAULT_POLLER)
		public PollerMetadata poller() {
			return Pollers.fixedRate(500).maxMessagesPerPoll(1).get();
		}

		@Bean
		public IntegrationFlow controlBus() {
			return f -> f.controlBus();
		}

		@Bean
		public MessageSource<?> integerMessageSource() {
			MethodInvokingMessageSource source = new MethodInvokingMessageSource();
			source.setObject(new AtomicInteger());
			source.setMethodName("getAndIncrement");
			return source;
		}

		@Bean
		public IntegrationFlow flow1() {
			return IntegrationFlows.from(integerMessageSource(),
					c -> c.poller(p -> p.fixedRate(100))
							.id("integerEndpoint")
							.autoStartup(false))
					.fixedSubscriberChannel("integerChannel")
					.transform("payload.toString()")
					.channel(Jms.pollableChannel("flow1QueueChannel", this.jmsConnectionFactory)
							.destination("flow1QueueChannel"))
					.get();
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
			return f -> f.handleWithAdapter(a ->
					a.jmsGateway(this.jmsConnectionFactory)
							.replyContainer()
							.requestDestination("jmsPipelineTest"));
		}

		@Bean
		public IntegrationFlow jmsInboundGatewayFlow() {
			return IntegrationFlows.from((MessagingGateways g) ->
					g.jms(this.jmsConnectionFactory)
							.destination("jmsPipelineTest"))
					.<String, String>transform(String::toUpperCase)
					.get();
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

	}

}
