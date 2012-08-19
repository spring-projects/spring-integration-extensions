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
package org.springframework.integration.aws.ses;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.springframework.integration.aws.ses.AmazonSESMailHeaders.BCC_EMAIL_ID;
import static org.springframework.integration.aws.ses.AmazonSESMailHeaders.CC_EMAIL_ID;
import static org.springframework.integration.aws.ses.AmazonSESMailHeaders.FROM_EMAIL_ID;
import static org.springframework.integration.aws.ses.AmazonSESMailHeaders.SUBJECT;
import static org.springframework.integration.aws.ses.AmazonSESMailHeaders.TO_EMAIL_ID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.mail.internet.MimeMessage;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.integration.Message;
import org.springframework.integration.MessageHandlingException;
import org.springframework.integration.aws.core.BasicAWSCredentials;
import org.springframework.integration.aws.ses.AmazonSESMessageHandler;
import org.springframework.integration.aws.ses.core.AmazonSESMailSendException;
import org.springframework.integration.aws.ses.core.AmazonSESMailSender;
import org.springframework.integration.aws.ses.core.AmazonSESSimpleMailMessage;
import org.springframework.integration.support.MessageBuilder;


/**
 * The test class for {@link AmazonSESMessageHandler}
 *
 * @author Amol Nayak
 *
 * @since 1.0
 *
 */
public class AmazonSESMessageHandlerTests {

	private static final List<AmazonSESSimpleMailMessage> messages = new ArrayList<AmazonSESSimpleMailMessage>();
	private static final List<MimeMessage> mimeMessages = new ArrayList<MimeMessage>();
	private static AmazonSESMessageHandler handler = new AmazonSESMessageHandler(new BasicAWSCredentials("dummy","dummy"));

	@BeforeClass
	public static void setupAmazonSESMailSender() {
		AmazonSESMailSender sender = mock(AmazonSESMailSender.class);
		doAnswer(new Answer<Void>() {
			public Void answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				if(args != null) {
					messages.add((AmazonSESSimpleMailMessage)args[0]);
				}
				return null;
			}
		}).when(sender).send(any(AmazonSESSimpleMailMessage.class));

		doAnswer(new Answer<Void>() {
			public Void answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				if(args != null) {
					messages.addAll(Arrays.asList((AmazonSESSimpleMailMessage[])args));
				}
				return null;
			}
		}).when(sender).send(any(AmazonSESSimpleMailMessage[].class));

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
		handler.setMailSender(sender);
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
	@Test
	public void withIncorrectSubjectClass() {
		Message<?> testMessage = MessageBuilder
									.withPayload("Test")
									.setHeader(SUBJECT, Arrays.asList("Some Header"))
									.build();
		String expectedMessage = "\"subject\" header is expected to be String, found java.util.Arrays.ArrayList";
		performNegativeTest(testMessage, expectedMessage);
	}


	/**
	 * Test case where the message text used of incorrect type
	 *
	 */
	@Test
	public void withIncorrectMessageTextClass() {
		Message<?> testMessage = MessageBuilder
					.withPayload(Arrays.asList("Content"))
					.build();
		String expectedMessage = "Body is expected to be String, found java.util.Arrays.ArrayList";
		performNegativeTest(testMessage, expectedMessage);
	}

	/**
	 * Test case where from id of the email is not specified
	 */
	@Test
	public void withoutFromId() {
		Message<?> testMessage = MessageBuilder
									.withPayload("Test Content")
									.setHeader(TO_EMAIL_ID, "to@to.com")
									.build();
		String expectedMessage  = "\"From Email Id\" is mandatory and cannot be null";
		performNegativeTest(testMessage, expectedMessage);
	}

	/**
	 * With all the details
	 */
	@Test
	public void withAllTheDetails() {
		clear();
		Message<?> testMessage = MessageBuilder
									.withPayload("Test Content")
									.setHeader(TO_EMAIL_ID, "to@to.com")
									.setHeader(BCC_EMAIL_ID, "bcc@bcc.com")
									.setHeader(CC_EMAIL_ID, "cc@cc.com")
									.setHeader(SUBJECT, "Test Subject")
									.setHeader(FROM_EMAIL_ID, "from@from.com")
									.build();
		handler.handleMessage(testMessage);
		AmazonSESSimpleMailMessage message = messages.get(0);
		List<String> toList = message.getToList();
		Assert.assertEquals(1,toList.size());
		Assert.assertEquals("to@to.com", toList.get(0));
		List<String> ccList = message.getCcList();
		Assert.assertEquals(1,ccList.size());
		Assert.assertEquals("cc@cc.com", ccList.get(0));
		List<String> bccList = message.getBccList();
		Assert.assertEquals(1,bccList.size());
		Assert.assertEquals("bcc@bcc.com", bccList.get(0));
		Assert.assertEquals("from@from.com",message.getFrom());
		Assert.assertEquals("Test Subject",message.getSubject());
		Assert.assertEquals("Test Content", message.getMessage());
	}

	/**
	 *
	 */
	@Test
	public void withAllMultipleToBccAndCC() {
		clear();
		Message<?> testMessage = MessageBuilder
									.withPayload("Test Content")
									.setHeader(TO_EMAIL_ID,
											Arrays.asList("to1@to.com","to2@to.com"))
									.setHeader(BCC_EMAIL_ID,
											Arrays.asList("bcc1@bcc.com","bcc2@bcc.com"))
									.setHeader(CC_EMAIL_ID,
											Arrays.asList("cc1@cc.com","cc2@cc.com"))
									.setHeader(SUBJECT, "Test Subject")
									.setHeader(FROM_EMAIL_ID, "from@from.com")
									.build();
		handler.handleMessage(testMessage);
		AmazonSESSimpleMailMessage message = messages.get(0);
		List<String> toList = message.getToList();
		Assert.assertEquals(2,toList.size());
		Assert.assertEquals("to1@to.com", toList.get(0));
		Assert.assertEquals("to2@to.com", toList.get(1));
		List<String> ccList = message.getCcList();
		Assert.assertEquals(2,ccList.size());
		Assert.assertEquals("cc1@cc.com", ccList.get(0));
		Assert.assertEquals("cc2@cc.com", ccList.get(1));
		List<String> bccList = message.getBccList();
		Assert.assertEquals(2,bccList.size());
		Assert.assertEquals("bcc1@bcc.com", bccList.get(0));
		Assert.assertEquals("bcc2@bcc.com", bccList.get(1));
		Assert.assertEquals("from@from.com",message.getFrom());
		Assert.assertEquals("Test Subject",message.getSubject());
		Assert.assertEquals("Test Content", message.getMessage());
	}

	private void performNegativeTest(Message<?> testMessage, String expectedMessage) {
		try {
			handler.handleMessage(testMessage);
		} catch (MessageHandlingException e) {
			Assert.assertEquals(AmazonSESMailSendException.class, e.getCause().getClass());
			Assert.assertEquals(expectedMessage, e.getCause().getMessage());
			return;
		}
		Assert.assertTrue("Expected an exception, didn't catch one",false);
	}
}
