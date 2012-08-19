/*
 * Copyright 2002-2012 the original author or authors.
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

import java.util.Arrays;
import java.util.Properties;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.integration.aws.core.PropertiesAWSCredentials;
import org.springframework.integration.aws.ses.core.AmazonSESMailSender;
import org.springframework.integration.aws.ses.core.AmazonSESMailSenderImpl;
import org.springframework.integration.aws.ses.core.AmazonSESSimpleMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;

/**
 *
 * The test class for the AmazonSESMailSenderImpl class.
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
 * @since 1.0
 *
 */
public class AmazonSESMailSenderImplAWSTests {

	private static AmazonSESMailSender sender;


	@BeforeClass
	public static final void setupSender() throws Exception {
		PropertiesAWSCredentials credentials =
			new PropertiesAWSCredentials("classpath:awscredentials.properties");
		credentials.afterPropertiesSet();
		sender = new AmazonSESMailSenderImpl(credentials);
	}

	/**
	 * Send a mail using {@link AmazonSESSimpleMailMessage}
	 */
	@Test
	public void sendAmazonSESSimpleMailMessage() {
		AmazonSESSimpleMailMessage message = new AmazonSESSimpleMailMessage();
		message.setFrom("amolnayak311@gmail.com");
		message.setMessage("Some Test body content");
		message.setSubject("Test subject message");
		message.setToList(Arrays.asList("amolnayak311@gmail.com"));
		sender.send(message);
	}


	/**
	 * Send a mail using {@link AmazonSESSimpleMailMessage}
	 */
	@Test
	public void sendAmazonSESSimpleMailMessageArray() {
		AmazonSESSimpleMailMessage[] messages = new AmazonSESSimpleMailMessage[2];
		AmazonSESSimpleMailMessage message = new AmazonSESSimpleMailMessage();
		message.setFrom("amolnayak311@gmail.com");
		message.setMessage("Some Test body content one");
		message.setSubject("Test subject message one");
		message.setToList(Arrays.asList("amolnayak311@gmail.com"));
		messages[0] = message;
		message = new AmazonSESSimpleMailMessage();
		message.setFrom("amolnayak311@gmail.com");
		message.setMessage("Some Test body content two");
		message.setSubject("Test subject message two");
		message.setToList(Arrays.asList("amolnayak311@gmail.com"));
		messages[1] = message;
		sender.send(messages);
	}


	/**
	 * The test case for sending a {@link MimeMessage}
	 */
	@Test
	public void sendMimeMessage() throws Exception {
		Session session = Session.getDefaultInstance(new Properties());
		MimeMessageHelper helper = new MimeMessageHelper(new MimeMessage(session));
		helper.setText("Some HTML Text", true);
		helper.setTo("amolnayak311@gmail.com");
		helper.setFrom("amolnayak311@gmail.com");
		helper.setSubject("Some HTML Message's Subject Line");
		MimeMessage message = helper.getMimeMessage();
		sender.send(message);
	}

	/**
	 * The test case for sending a {@link MimeMessage}
	 */
	@Test
	public void sendMimeMessageArray() throws Exception {
		Session session = Session.getDefaultInstance(new Properties());
		MimeMessage[] messages = new MimeMessage[2];
		MimeMessageHelper helper = new MimeMessageHelper(new MimeMessage(session));
		helper.setText("Some HTML Text One", true);
		helper.setTo("amolnayak311@gmail.com");
		helper.setFrom("amolnayak311@gmail.com");
		helper.setSubject("Some HTML Message's Subject Line One");
		MimeMessage message = helper.getMimeMessage();
		messages[0] = message;
		helper = new MimeMessageHelper(new MimeMessage(session));
		helper.setText("Some HTML Text Two", true);
		helper.setTo("amolnayak311@gmail.com");
		helper.setFrom("amolnayak311@gmail.com");
		helper.setSubject("Some HTML Message's Subject Line Two");
		message = helper.getMimeMessage();
		messages[1] = message;
		sender.send(messages);
	}
}
