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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

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
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.integration.dsl.mail.Mail;
import org.springframework.integration.dsl.support.Pollers;
import org.springframework.integration.dsl.test.mail.PoorMansMailServer.ImapServer;
import org.springframework.integration.dsl.test.mail.PoorMansMailServer.Pop3Server;
import org.springframework.integration.dsl.test.mail.PoorMansMailServer.SmtpServer;
import org.springframework.integration.mail.MailHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.test.util.TestUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.PollableChannel;
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

	private final static int smtpPort = SocketUtils.findAvailableTcpPort();

	private static SmtpServer smtpServer = PoorMansMailServer.smtp(smtpPort);

	private final static int pop3Port = SocketUtils.findAvailableTcpPort(smtpPort + 1);

	private static Pop3Server pop3Server = PoorMansMailServer.pop3(pop3Port);

	private final static int imapPort = SocketUtils.findAvailableTcpPort(pop3Port + 1);

	private static ImapServer imapServer = PoorMansMailServer.imap(imapPort);


	@BeforeClass
	public static void setup() throws InterruptedException {
		int n = 0;
		while (n++ < 100 && (!smtpServer.isListening() || !pop3Server.isListening() || !imapServer.isListening())) {
			Thread.sleep(100);
		}
		assertTrue(n < 100);
	}

	@AfterClass
	public static void tearDown() {
		smtpServer.stop();
		pop3Server.stop();
		imapServer.stop();
	}

	@Autowired
	@Qualifier("sendMailChannel")
	private MessageChannel sendMailChannel;

	@Autowired
	@Qualifier("sendMailEndpoint.handler")
	private MessageHandler sendMailHandler;

	@Autowired
	private PollableChannel pop3Channel;

	@Autowired
	private PollableChannel imapChannel;

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
		while (n++ < 100 && smtpServer.getMessages().size() == 0) {
			Thread.sleep(100);
		}

		assertTrue(smtpServer.getMessages().size() > 0);
		String message = smtpServer.getMessages().get(0);
		assertThat(message, endsWith("foo\n"));
		assertThat(message, containsString("foo@bar"));
		assertThat(message, containsString("bar@baz"));
		assertThat(message, containsString("user:user"));
		assertThat(message, containsString("password:pw"));

	}

	@Test
	public void testPop3() throws Exception {
		Message<?> message = pop3Channel.receive(10000);
		assertNotNull(message);
		MimeMessage mm = (MimeMessage) message.getPayload();
		assertEquals("foo@bar", mm.getRecipients(RecipientType.TO)[0].toString());
		assertEquals("bar@baz", mm.getFrom()[0].toString());
		assertEquals("Test Email", mm.getSubject());
		assertEquals("foo\r\n", mm.getContent());
	}

	@Test
	public void testImap() throws Exception {
		Message<?> message = imapChannel.receive(10000);
		assertNotNull(message);
		MimeMessage mm = (MimeMessage) message.getPayload();
		assertEquals("foo@bar", mm.getRecipients(RecipientType.TO)[0].toString());
		assertEquals("bar@baz", mm.getFrom()[0].toString());
		assertEquals("Test Email", mm.getSubject());
		assertEquals("foo\r\n", mm.getContent());
	}

	@Configuration
	@EnableIntegration
	public static class ContextConfiguration {

		@Bean
		public IntegrationFlow sendMailFlow() {
			return IntegrationFlows.from("sendMailChannel")
					.handle(Mail.outboundAdapter("localhost")
									.port(smtpPort)
									.credentials("user", "pw")
									.protocol("smtp")
									.javaMailProperties(p -> p.put("mail.debug", "true")),
							e -> e.id("sendMailEndpoint"))
					.get();
		}

		@Bean
		public MessageChannel pop3Channel() {
			return MessageChannels.queue().get();
		}

		@Bean
		public IntegrationFlow pop3MailFlow() {
			return IntegrationFlows.from(Mail.pop3InboundAdapter("localhost", pop3Port, "user", "pw")
							.javaMailProperties(p -> p.put("mail.debug", "true")),
						e -> e.autoStartup(true)
								.poller(Pollers.fixedDelay(1000)))
					.channel(pop3Channel())
					.get();
		}

		@Bean
		public MessageChannel imapChannel() {
			return MessageChannels.queue().get();
		}

		@Bean
		public IntegrationFlow imapMailFlow() {
			return IntegrationFlows.from(Mail.imapInboundAdapter("imap://user:pw@localhost:" + imapPort + "/INBOX")
							.javaMailProperties(p -> p.put("mail.debug", "true")),
						e -> e.autoStartup(true)
								.poller(Pollers.fixedDelay(1000)))
					.channel(imapChannel())
					.get();
		}

	}

}
