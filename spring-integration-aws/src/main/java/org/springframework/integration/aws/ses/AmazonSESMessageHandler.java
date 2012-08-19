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

import javax.mail.Address;
import javax.mail.internet.MimeMessage;

import org.springframework.integration.Message;
import org.springframework.integration.MessageHeaders;
import org.springframework.integration.aws.core.AmazonWSCredentials;
import org.springframework.integration.aws.ses.core.AmazonSESMailSendException;
import org.springframework.integration.aws.ses.core.AmazonSESMailSender;
import org.springframework.integration.aws.ses.core.AmazonSESMailSenderImpl;
import org.springframework.integration.aws.ses.core.AmazonSESSimpleMailMessage;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.util.Assert;

/**
 * The Message handler for the SES Mail. This will be used to send email
 * using Amazon SES
 *
 * @author Amol Nayak
 *
 * @since 1.0
 *
 */
public class AmazonSESMessageHandler extends AbstractMessageHandler {


	/**.
	 * The mail sender implementation of Amazon SES
	 */
	private AmazonSESMailSender mailSender;

	/**
	 * The AWS Access key of the user account
	 */
	private final String accessKey;

	public AmazonSESMessageHandler(AmazonWSCredentials credentials) {
		Assert.notNull(credentials, "'credentials' cannot be null");
		accessKey = credentials.getAccessKey();
		mailSender = new AmazonSESMailSenderImpl(credentials);
	}

	/* (non-Javadoc)
	 * @see org.springframework.integration.handler.AbstractMessageHandler#handleMessageInternal(org.springframework.integration.Message)
	 */

	public AmazonSESMessageHandler() {
		//the default constructor
		accessKey = null;
	}


	@Override
	protected void handleMessageInternal(Message<?> message) throws Exception {
		Object payload = message.getPayload();
		if(payload instanceof MimeMessage) {
			MimeMessage mimeMessage = (MimeMessage)payload;
			Address[] addresses = mimeMessage.getAllRecipients();
			if(addresses.length == 0)
				throw new AmazonSESMailSendException(accessKey,"Mime mesage should contain to/from/bcc email id",null);
			Address[] fromAddresses = mimeMessage.getFrom();
			if(fromAddresses.length == 0)
				throw new AmazonSESMailSendException(accessKey,"Mime mesage should contain a from email id",null);

			mailSender.send(mimeMessage);
		} else {
			AmazonSESSimpleMailMessage mailMsg = convertMessageToSESMailMessage(message);
			if(mailMsg != null)
				mailSender.send(mailMsg);
		}

	}

	/**.
	 * Convert the incoming {@link Message} into a {@link AmazonSESSimpleMailMessage} to be sent
	 * @param message
	 * @return
	 */
	private AmazonSESSimpleMailMessage convertMessageToSESMailMessage(Message<?> message) {
		Object payload = message.getPayload();
		if(payload instanceof AmazonSESSimpleMailMessage)
			return (AmazonSESSimpleMailMessage)payload;
		MessageHeaders headers = message.getHeaders();

		//Get the subject
		Object value = headers.get(AmazonSESMailHeaders.SUBJECT);
		String subject = null;

		if(value != null) {
			if(value instanceof String)
				subject = (String)value;
			else
				throw new AmazonSESMailSendException(accessKey,
						"\"" + AmazonSESMailHeaders.SUBJECT + "\" header is expected to be String, found "
						+ value.getClass().getCanonicalName(),
						null, null);
		}

		//Currently only String body is supported
		value = message.getPayload();
		String body = null;
		if(value != null) {
			if(value instanceof String)
				body = (String)value;
			else
				throw new AmazonSESMailSendException(accessKey,
						"Body is expected to be String, found "
						+ value.getClass().getCanonicalName(),
						null, null);
		}



		// construct the mail message
		AmazonSESSimpleMailMessage mailMessage =
			AmazonSESMailMessageBuilder
			.newBuilder(accessKey)
			.withBccAddress(headers.get(AmazonSESMailHeaders.BCC_EMAIL_ID))
			.withToAddress(headers.get(AmazonSESMailHeaders.TO_EMAIL_ID))
			.withFromAddress(headers.get(AmazonSESMailHeaders.FROM_EMAIL_ID))
			.withReplyToAddress(headers.get(AmazonSESMailHeaders.REPLYTO_EMAIL_ID))
			.withCCAddress(headers.get(AmazonSESMailHeaders.CC_EMAIL_ID))
			.withIsHtml(headers.get(AmazonSESMailHeaders.HTML_FORMAT))
			.withSubject(subject)
			.withMessage(body)
			.build();

		return mailMessage;


	}

	/**
	 * Sets the user provided {@link AmazonSESMailSender} instance
	 * @param mailSender
	 */
	public void setMailSender(AmazonSESMailSender mailSender) {
		this.mailSender = mailSender;
	}
}
