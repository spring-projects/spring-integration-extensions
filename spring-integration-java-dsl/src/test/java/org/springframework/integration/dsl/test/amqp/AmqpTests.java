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

package org.springframework.integration.dsl.test.amqp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.AnonymousQueue;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageProducers;
import org.springframework.integration.dsl.amqp.Amqp;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.PollableChannel;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Artem Bilan
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
public class AmqpTests {

	@Autowired
	private AmqpTemplate amqpTemplate;

	@Autowired
	@Qualifier("queue")
	private Queue amqpQueue;

	@Test
	public void testAmqpInboundGatewayFlow() throws Exception {
		Object result = this.amqpTemplate.convertSendAndReceive(this.amqpQueue.getName(), "world");
		assertEquals("HELLO WORLD", result);
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

	@Configuration
	@EnableAutoConfiguration
	public static class ContextConfiguration {

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

	}

}
