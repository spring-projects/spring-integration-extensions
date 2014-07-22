/*
 * Copyright 2002-2013 the original author or authors.
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
package org.springframework.integration.samples.kafka.outbound;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.samples.kafka.user.User;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;

public class OutboundRunner {
	private static final String CONFIG = "kafkaOutboundAdapterParserTests-context.xml";
	private static final Log LOG = LogFactory.getLog(OutboundRunner.class);

	public static void main(final String args[]) {
		final ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(CONFIG, OutboundRunner.class);
		ctx.start();

		final MessageChannel channel = ctx.getBean("inputToKafka", MessageChannel.class);
		LOG.info(channel.getClass());

		//sending 100,000 messages to Kafka server for topic test1
		for (int i = 0; i < 500; i++) {
			final User user = new User();
			user.setFirstName("fname" + i);
			user.setLastName("lname" + i);
			channel.send(
					MessageBuilder.withPayload(user)
							.setHeader("messageKey", String.valueOf(i))
							.setHeader("topic", "test1").build());

			LOG.info("message sent " + i);
		}

		//sending 5,000 messages to kafka server for topic test2
		for (int i = 0; i < 50; i++) {
			channel.send(
				MessageBuilder.withPayload("hello Fom ob adapter test2 -  " + i)
					.setHeader("messageKey", String.valueOf(i))
					.setHeader("topic", "test2").build());

			LOG.info("message sent " + i);
		}

		//Send some messages to multiple topics matching regex.
		for (int i = 0; i < 10; i++) {
			channel.send(
				MessageBuilder.withPayload("hello Fom ob adapter regextopic1 -  " + i)
					.setHeader("messageKey", String.valueOf(i))
					.setHeader("topic", "regextopic1").build());

			LOG.info("message sent " + i);
		}
		for (int i = 0; i < 10; i++) {
			channel.send(
				MessageBuilder.withPayload("hello Fom ob adapter regextopic2 -  " + i)
					.setHeader("messageKey", String.valueOf(i))
					.setHeader("topic", "regextopic2").build());

			LOG.info("message sent " + i);
		}
	}
}
