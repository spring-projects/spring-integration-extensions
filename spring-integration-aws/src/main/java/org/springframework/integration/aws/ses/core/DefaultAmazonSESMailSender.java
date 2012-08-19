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

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import javax.mail.internet.MimeMessage;

import org.springframework.integration.aws.core.AWSCredentials;
import org.springframework.integration.aws.core.AWSOperationException;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleemail.model.RawMessage;
import com.amazonaws.services.simpleemail.model.SendRawEmailRequest;

/**
 * The implementation class for sending mail using the Amazon SES service
 *
 * @author Amol Nayak
 *
 * @since 1.0
 *
 */
public class DefaultAmazonSESMailSender extends JavaMailSenderImpl {

	/**.
	 * The API class used for sending email using Amazon SES
	 */
	private final AmazonSimpleEmailServiceClient emailService;

	private final AWSCredentials credentials;

	public DefaultAmazonSESMailSender(AWSCredentials credentials) {
		if(credentials == null)
			throw new AWSOperationException(null, "Credentials cannot be null, provide a non null valid set of credentials");
		this.credentials = credentials;

		emailService = new AmazonSimpleEmailServiceClient(
				new BasicAWSCredentials(credentials.getAccessKey(),
								credentials.getSecretKey()));
	}


	/**
	 * The Actual implementation of the sending of the mail message using the AWS SES
	 * SDK
	 */
	@Override
	protected void doSend(MimeMessage[] mimeMessages, Object[] originalMessages)
			throws MailException {
		Map<Object, Exception> failedMessageMap = new HashMap<Object, Exception>();
		if(mimeMessages != null && mimeMessages.length > 0) {
			int index = 0;
			for(MimeMessage mailMessage:mimeMessages) {
				try {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					mailMessage.writeTo(baos);
					RawMessage rawMessage = new RawMessage(ByteBuffer.wrap(baos.toByteArray()));
					SendRawEmailRequest rawMail = new SendRawEmailRequest(rawMessage);
					emailService.sendRawEmail(rawMail);
					index++;
				} catch (Exception e) {
					failedMessageMap.put(originalMessages != null? originalMessages[index]:mailMessage, e);
				}
			}
		}

		if(!failedMessageMap.isEmpty()) {
			throw new AmazonSESMailSendException(credentials.getAccessKey(),
			"Exception while sending one or more messages, see failedMessages for more details",
			failedMessageMap);
		}
	}

	@Override
	public void setPort(int port) {
		throw new UnsupportedOperationException("AWS SES Implementation of mail " +
				"sender does not allow setting the port number");
	}


	@Override
	public void setHost(String host) {
		throw new UnsupportedOperationException("AWS SES Implementation of mail " +
		"sender does not allow setting the host name. It is already configured with an endpoint");
	}

	//Should we disallow setSession?
}