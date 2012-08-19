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

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import javax.mail.internet.MimeMessage;

import org.springframework.integration.aws.core.AmazonWSCredentials;
import org.springframework.integration.aws.core.AmazonWSOperationException;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.RawMessage;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.amazonaws.services.simpleemail.model.SendRawEmailRequest;

/**
 * The implementation class for sending mail using the Amazon SES service
 *
 * @author Amol Nayak
 *
 * @since 1.0
 *
 */
public class AmazonSESMailSenderImpl implements AmazonSESMailSender {

	/**.
	 * The API class used for sending email using Amazon SES
	 */
	private final AmazonSimpleEmailServiceClient emailService;

	private final AmazonWSCredentials credentials;

	public AmazonSESMailSenderImpl(AmazonWSCredentials credentials) {
		if(credentials == null)
			throw new AmazonWSOperationException(null, "Credentials cannot be null, provide a non null valid set of credentials");
		this.credentials = credentials;

		emailService = new AmazonSimpleEmailServiceClient(
				new BasicAWSCredentials(credentials.getAccessKey(),
								credentials.getSecretKey()));
	}

	/* (non-Javadoc)
	 * @see org.springframework.integration.aws.ses.AmazonSESMailSender#send(org.springframework.integration.aws.ses.AmazonSESSimpleMailMessage)
	 */
	public void send(AmazonSESSimpleMailMessage message) {
		send(new AmazonSESSimpleMailMessage[] {message});
	}

	/* (non-Javadoc)
	 * @see org.springframework.integration.aws.ses.AmazonSESMailSender#send(org.springframework.integration.aws.ses.AmazonSESSimpleMailMessage[])
	 */
	public void send(AmazonSESSimpleMailMessage[] messages) {
		AmazonSESSimpleMailMessage currentMessage = null;
		if (messages != null && messages.length > 0) {
			try {
				for (AmazonSESSimpleMailMessage message:messages) {
					if (message != null) {
						//Construct a mail message from the given parameters
						currentMessage = message;
						//First the subject
						Content subject = new Content(message.getSubject());
						//Then the body content
						Content body = new Content(message.getMessage());
						//Construct the Mail Message from the above two
						Body messageBody;
						if(message.isHtml())
							messageBody = new Body().withHtml(body);
						else
							messageBody = new Body().withText(body);

						//The final mail message from the body and subject
						Message finalMessage = new Message(subject, messageBody);

						//Destination of the email
						Destination destination = new Destination()
													.withToAddresses(message.getToList())
													.withCcAddresses(message.getCcList())
													.withBccAddresses(message.getBccList());

						//Now the SES mail request
						SendEmailRequest request =
							new SendEmailRequest(message.getFrom(),destination,finalMessage)
							.withReplyToAddresses(message.getReplyTo());

						emailService.sendEmail(request);
					}
				}
			} catch (Exception e) {
				//caught Exception
				throw new AmazonSESMailSendException(credentials.getAccessKey(),
						"Exception Caught with message \"" + e.getMessage() + "\" while sending mail",
						e,currentMessage);
			}
		}
	}


	public void send(MimeMessage mailMessage) {
		send(new MimeMessage[] {mailMessage});

	}


	public void send(MimeMessage[] mailMessages) {
		if(mailMessages != null && mailMessages.length > 0) {
			for(MimeMessage mailMessage:mailMessages) {
				try {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					mailMessage.writeTo(baos);
					RawMessage rawMessage = new RawMessage(ByteBuffer.wrap(baos.toByteArray()));
					SendRawEmailRequest rawMail = new SendRawEmailRequest(rawMessage);
					emailService.sendRawEmail(rawMail);
				} catch (Exception e) {
					throw new AmazonSESMailSendException(credentials.getAccessKey(),
							"Exception Caught with message \"" + e.getMessage() + "\" while sending raw mail",
							e,null);
				}
			}
		}

	}


}
