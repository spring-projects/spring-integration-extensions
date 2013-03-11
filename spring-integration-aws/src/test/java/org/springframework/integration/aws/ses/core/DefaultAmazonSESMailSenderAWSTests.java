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
package org.springframework.integration.aws.ses.core;

import javax.mail.internet.MimeMessage;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.integration.aws.core.PropertiesAWSCredentials;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;

/**
*
* The test class for the {@link DefaultAmazonSESMailSender} class.
*
* NOTE: You will have to modify the to and from email's yourselves
* as you can send to verified emails only as part of the free tier
* in which you may be running the test.
* To run this test you need to have your AWSAccess key and Secret key in the
* file awscredentials.properties in the classpath. This file is not present in the
* repository and you need to add one yourselves to src/test/resources folder and have
* two properties accessKey and secretKey in it containing the access and the secret key
*
* @author Amol Nayak
*
* @since 0.5
*
*/
@Ignore
public class DefaultAmazonSESMailSenderAWSTests {

	private static final String TO_EMAIL_ID = "amolnayak311@gmail.com";
	private static DefaultAmazonSESMailSender sender;


	@BeforeClass
	public static final void setupSender() throws Exception {
		PropertiesAWSCredentials credentials =
			new PropertiesAWSCredentials("classpath:awscredentials.properties");
		credentials.afterPropertiesSet();
		sender = new DefaultAmazonSESMailSender(credentials);
	}

	/**
	 * Send a mail using {@link AmazonSESSimpleMailMessage}
	 */
	@Test
	public void sendSimpleMailMessage() {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(TO_EMAIL_ID);
		message.setText("Some Test body content");
		message.setSubject("Test subject message");
		message.setTo(new String[]{TO_EMAIL_ID});
		sender.send(message);
	}


	/**
	 * Send a mail using {@link AmazonSESSimpleMailMessage}
	 */
	@Test
	public void sendSimpleMailMessageArray() {
		SimpleMailMessage[] messages = new SimpleMailMessage[2];
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(TO_EMAIL_ID);
		message.setText("Some Test body content one");
		message.setSubject("Test subject message one");
		message.setTo(new String[]{TO_EMAIL_ID});
		messages[0] = message;
		message = new SimpleMailMessage();
		message.setFrom(TO_EMAIL_ID);
		message.setText("Some Test body content two");
		message.setSubject("Test subject message two");
		message.setTo(new String[]{TO_EMAIL_ID});
		messages[1] = message;
		sender.send(messages);
	}


	/**
	 * The test case for sending a {@link MimeMessage}
	 */
	@Test
	public void sendMimeMessage() throws Exception {
		MimeMessageHelper helper = new MimeMessageHelper(sender.createMimeMessage());
		helper.setText("Some HTML Text", true);
		helper.setTo(TO_EMAIL_ID);
		helper.setFrom(TO_EMAIL_ID);
		helper.setSubject("Some HTML Message's Subject Line");
		MimeMessage message = helper.getMimeMessage();
		sender.send(message);
	}

	/**
	 * The test case for sending a {@link MimeMessage}
	 */
	@Test
	public void sendMimeMessageArray() throws Exception {
		MimeMessage[] messages = new MimeMessage[2];
		MimeMessageHelper helper = new MimeMessageHelper(sender.createMimeMessage());
		helper.setText("Some HTML Text One", true);
		helper.setTo(TO_EMAIL_ID);
		helper.setFrom(TO_EMAIL_ID);
		helper.setSubject("Some HTML Message's Subject Line One");
		MimeMessage message = helper.getMimeMessage();
		messages[0] = message;
		helper = new MimeMessageHelper(sender.createMimeMessage());
		helper.setText("Some HTML Text Two", true);
		helper.setTo(TO_EMAIL_ID);
		helper.setFrom(TO_EMAIL_ID);
		helper.setSubject("Some HTML Message's Subject Line Two");
		message = helper.getMimeMessage();
		messages[1] = message;
		sender.send(messages);
	}
}
