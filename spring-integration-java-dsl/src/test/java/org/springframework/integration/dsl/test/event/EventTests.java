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

package org.springframework.integration.dsl.test.event;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.event.inbound.ApplicationEventListeningMessageProducer;
import org.springframework.messaging.Message;
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
public class EventTests {

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private PollableChannel resultsChannel;

	@Test
	public void testRawApplicationEventListeningMessageProducer() {
		this.applicationContext.publishEvent(new TestApplicationEvent1());
		Message<?> receive = this.resultsChannel.receive(10000);
		assertNotNull(receive);
		assertThat(receive.getPayload(), instanceOf(TestApplicationEvent1.class));

		this.applicationContext.publishEvent(new TestApplicationEvent2());
		receive = this.resultsChannel.receive(10000);
		assertNotNull(receive);
		assertThat(receive.getPayload(), instanceOf(TestApplicationEvent2.class));
	}

	@Configuration
	@EnableIntegration
	public static class ContextConfiguration {


		@Bean
		@SuppressWarnings("unchecked")
		public ApplicationListener<?> applicationListener() {
			ApplicationEventListeningMessageProducer producer = new ApplicationEventListeningMessageProducer();
			producer.setEventTypes(TestApplicationEvent1.class);
			producer.setOutputChannel(resultsChannel());
			return producer;
		}


		@Bean
		public PollableChannel resultsChannel() {
			return new QueueChannel();
		}

		@Bean
		@SuppressWarnings("unchecked")
		public IntegrationFlow eventProducerFlow() {
			ApplicationEventListeningMessageProducer producer = new ApplicationEventListeningMessageProducer();
			producer.setEventTypes(TestApplicationEvent2.class);

			return IntegrationFlows.from(producer)
					.channel(resultsChannel())
					.get();
		}

	}

	@SuppressWarnings("serial")
	private static class TestApplicationEvent1 extends ApplicationEvent {

		public TestApplicationEvent1() {
			super("TestApplicationEvent1");
		}

	}

	@SuppressWarnings("serial")
	private static class TestApplicationEvent2 extends ApplicationEvent {

		public TestApplicationEvent2() {
			super("TestApplicationEvent2");
		}

	}

}
