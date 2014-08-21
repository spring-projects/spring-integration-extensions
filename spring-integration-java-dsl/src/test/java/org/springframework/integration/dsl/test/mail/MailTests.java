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

import org.junit.Test;

import org.springframework.integration.dsl.mail.Mail;
import org.springframework.integration.dsl.test.PoorMansMailServer;
import org.springframework.integration.dsl.test.PoorMansMailServer.SmtpServer;
import org.springframework.integration.mail.MailHeaders;
import org.springframework.integration.mail.MailSendingMessageHandler;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.test.util.TestUtils;
import org.springframework.util.SocketUtils;

/**
 * @author Gary Russell
 *
 */
public class MailTests {

	@Test
	public void testOutbound() throws Exception {
		int port = SocketUtils.findAvailableTcpPort();
		SmtpServer server = new PoorMansMailServer().smtp(port);
		MailSendingMessageHandler handler =
				Mail.outboundAdapter(Mail.mailsender()
						.setHost("localhost")
						.setPort(port)
						.setUsername("user")
						.setPassword("pw")
						.setProtocol("smtp")
						.setJavaMailProperties(Mail.properties()
								.put("mail.debug", "true")
								.get())
							.get())
						.get();
		assertEquals("localhost", TestUtils.getPropertyValue(handler, "mailSender.host"));
		int n = 0;
		while (n++ < 100 && !server.isListening()) {
			Thread.sleep(100);
		}
		assertTrue(n < 100);
		handler.handleMessage(MessageBuilder.withPayload("foo")
				.setHeader(MailHeaders.SUBJECT, "foo")
				.setHeader(MailHeaders.FROM, "foo@bar")
				.setHeader(MailHeaders.TO, "bar@baz")
				.build());
		n = 0;
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
		server.stop();
	}

}
