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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import org.springframework.integration.dsl.mail.Mail;
import org.springframework.integration.mail.MailSendingMessageHandler;
import org.springframework.integration.test.util.TestUtils;

/**
 * @author Gary Russell
 *
 */
public class MailTests {

	@Test
	public void testOutbound() {
		MailSendingMessageHandler handler =
				Mail.outboundAdapter(Mail.mailsender()
						.setHost("test")
						.setPort(465)
						.setUsername("user")
						.setPassword("pw")
						.setProtocol("smtps")
						.setJavaMailProperties(Mail.properties()
								.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
								.put("mail.smtp.socketFactory.fallback", "false")
								.put("mail.starttls.enable", "true")
								.put("mail.debug", "true")
								.get())
							.get())
						.get();
		assertEquals("test", TestUtils.getPropertyValue(handler, "mailSender.host"));
	}

}
