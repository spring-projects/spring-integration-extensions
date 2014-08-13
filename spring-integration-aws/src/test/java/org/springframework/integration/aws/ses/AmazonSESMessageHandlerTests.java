/*
 * Copyright 2002-2014 the original author or authors.
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
package org.springframework.integration.aws.ses;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.springframework.integration.mail.MailHeaders.BCC;
import static org.springframework.integration.mail.MailHeaders.CC;
import static org.springframework.integration.mail.MailHeaders.FROM;
import static org.springframework.integration.mail.MailHeaders.SUBJECT;
import static org.springframework.integration.mail.MailHeaders.TO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.mail.internet.MimeMessage;

import org.junit.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandlingException;


/**
 * The test class for {@link AmazonSESMessageHandler}
 *
 * @author Amol Nayak
 * @author Rob Harrop
 *
 * @since 0.5
 *
 */
public class AmazonSESMessageHandlerTests {

	private static final List<SimpleMailMessage> messages = new ArrayList<SimpleMailMessage>();
	private static final List<MimeMessage> mimeMessages = new ArrayList<MimeMessage>();
	private static AmazonSESMessageHandler handler;

	@BeforeClass
	public static void setupAmazonSESMailSender() {
		JavaMailSender sender = mock(JavaMailSender.class);
		doAnswer(new Answer<Void>() {
			public Void answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				if(args != null) {
					messages.add((SimpleMailMessage)args[0]);
				}
				return null;
			}
		}).when(sender).send(any(SimpleMailMessage.class));

		doAnswer(new Answer<Void>() {
			public Void answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				if(args != null) {
					messages.addAll(Arrays.asList((SimpleMailMessage[])args));
				}
				return null;
			}
		}).when(sender).send(any(SimpleMailMessage[].class));

		doAnswer(new Answer<Void>() {
			public Void answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				if(args != null) {
					mimeMessages.addAll(Arrays.asList((MimeMessage[])args));
				}
				return null;
			}
		}).when(sender).send(any(MimeMessage[].class));

		doAnswer(new Answer<Void>() {
			public Void answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				if(args != null) {
					mimeMessages.add((MimeMessage)args[0]);
				}
				return null;
			}
		}).when(sender).send(any(MimeMessage.class));
		handler =  new AmazonSESMessageHandler(sender);
	}

	public void clear() {
		messages.clear();
		mimeMessages.clear();
	}

	/**
	 *Test case for sending a message with subject line of unexpected type
	 *{@link AmazonSESSimpleMailMessage}
	 *
	 */
	@Test(expected=MessageHandlingException.class)
	public void withIncorrectSubjectClass() {
		Message<?> testMessage = MessageBuilder
									.withPayload("Test")
									.setHeader(SUBJECT, Arrays.asList("Some Header"))
									.build();
		handler.handleMessage(testMessage);
	}


	/**
	 * Test case where the message text used of incorrect type
	 *
	 */
	@Test(expected=MessageHandlingException.class)
	public void withIncorrectMessageTextClass() {
		Message<?> testMessage = MessageBuilder
					.withPayload(Arrays.asList("Content"))
					.build();
		handler.handleMessage(testMessage);
	}

	//Test case to check if from id exists or not cant work now as MailSendingMessageHandler
	//doesn't check if from is present or not

	/**
	 * With all the details
	 */
	@Test
	public void withAllTheDetails() {
		clear();
		Message<?> testMessage = MessageBuilder
									.withPayload("Test Content")
									.setHeader(TO, "to@to.com")
									.setHeader(BCC, "bcc@bcc.com")
									.setHeader(CC, "cc@cc.com")
									.setHeader(SUBJECT, "Test Subject")
									.setHeader(FROM, "from@from.com")
									.build();
		handler.handleMessage(testMessage);
		SimpleMailMessage message = messages.get(0);
		String[] to = message.getTo();
		Assert.assertNotNull(to);
		Assert.assertEquals(1,to.length);
		Assert.assertEquals("to@to.com", to[0]);

		String[] cc = message.getCc();
		Assert.assertNotNull(cc);
		Assert.assertEquals(1,cc.length);
		Assert.assertEquals("cc@cc.com", cc[0]);

		String[] bcc = message.getBcc();
		Assert.assertNotNull(bcc);
		Assert.assertEquals(1,bcc.length);
		Assert.assertEquals("bcc@bcc.com", bcc[0]);

		Assert.assertEquals("from@from.com",message.getFrom());
		Assert.assertEquals("Test Subject",message.getSubject());
		Assert.assertEquals("Test Content", message.getText());
	}

//Might need to uncomment this test later when we start allowing null in TO email id of
//spring-int-mail
//	/**
//	 *
//	 */
//	@Test
//	public void withAllMultipleToBccAndCC() {
//		clear();
//		Message<?> testMessage = MessageBuilder
//									.withPayload("Test Content")
//									.setHeader(TO,
//											Arrays.asList("to1@to.com","to2@to.com"))
//									.setHeader(BCC,
//											Arrays.asList("bcc1@bcc.com","bcc2@bcc.com"))
//									.setHeader(CC,
//											Arrays.asList("cc1@cc.com","cc2@cc.com"))
//									.setHeader(SUBJECT, "Test Subject")
//									.setHeader(FROM, "from@from.com")
//									.build();
//		handler.handleMessage(testMessage);
//		SimpleMailMessage message = messages.get(0);
//		String[] to = message.getTo();
//		Assert.assertNotNull(to);
//		Assert.assertEquals(2,to.length);
//		Assert.assertEquals("to1@to.com", to[0]);
//		Assert.assertEquals("to2@to.com", to[1]);
//
//		String[] cc = message.getCc();
//		Assert.assertNotNull(cc);
//		Assert.assertEquals(2,cc.length);
//		Assert.assertEquals("cc1@cc.com", cc[0]);
//		Assert.assertEquals("cc2@cc.com", cc[1]);
//
//		String[] bcc = message.getBcc();
//		Assert.assertEquals(2,bcc.length);
//		Assert.assertEquals("bcc1@bcc.com", bcc[0]);
//		Assert.assertEquals("bcc2@bcc.com", bcc[1]);
//
//		Assert.assertEquals("from@from.com",message.getFrom());
//
//		Assert.assertEquals("Test Subject",message.getSubject());
//		Assert.assertEquals("Test Content", message.getText());
//	}
}
