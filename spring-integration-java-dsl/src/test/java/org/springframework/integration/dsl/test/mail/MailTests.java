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
package org.springframework.integration.dsl.test.mail;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.mail.Mail;
import org.springframework.integration.dsl.test.mail.PoorMansMailServer.SmtpServer;
import org.springframework.integration.mail.MailHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.test.util.TestUtils;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.SocketUtils;

/**
 * @author Gary Russell
 * @author Artem Bilan
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
public class MailTests {

	private final static int port = SocketUtils.findAvailableTcpPort();

	private static SmtpServer server = PoorMansMailServer.smtp(port);


	@BeforeClass
	public static void setup() throws InterruptedException {
		int n = 0;
		while (n++ < 100 && !server.isListening()) {
			Thread.sleep(100);
		}
		assertTrue(n < 100);
	}

	@AfterClass
	public static void tearDown() {
		server.stop();
	}

	@Autowired
	@Qualifier("sendMailChannel")
	private MessageChannel sendMailChannel;

	@Autowired
	@Qualifier("sendMailEndpoint.handler")
	private MessageHandler sendMailHandler;

	@Test
	public void testOutbound() throws Exception {
		assertEquals("localhost", TestUtils.getPropertyValue(this.sendMailHandler, "mailSender.host"));

		Properties javaMailProperties = TestUtils.getPropertyValue(this.sendMailHandler,
				"mailSender.javaMailProperties", Properties.class);
		assertEquals("true", javaMailProperties.getProperty("mail.debug"));

		this.sendMailChannel.send(MessageBuilder.withPayload("foo")
				.setHeader(MailHeaders.SUBJECT, "foo")
				.setHeader(MailHeaders.FROM, "foo@bar")
				.setHeader(MailHeaders.TO, "bar@baz")
				.build());

		int n = 0;
		while (n++ < 100 && server.getMessages().size() == 0) {
			Thread.sleep(100);
		}

		assertTrue(server.getMessages().size() > 0);
		String message = server.getMessages().get(0);
		assertThat(message, endsWith("foo\n"));
		assertThat(message, containsString("foo@bar"));
		assertThat(message, containsString("bar@baz"));
		assertThat(message, containsString("user:user"));
		assertThat(message, containsString("password:pw"));

	}

	@Configuration
	@EnableIntegration
	public static class ContextConfiguration {

		@Bean
		public IntegrationFlow sendMailFlow() {
			return IntegrationFlows.from("sendMailChannel")
					.handle(Mail.outboundAdapter("localhost")
									.port(port)
									.credentials("user", "pw")
									.protocol("smtp")
									.javaMailProperties(p -> p.put("mail.debug", "true")),
							e -> e.id("sendMailEndpoint"))
					.get();
		}

	}

}
