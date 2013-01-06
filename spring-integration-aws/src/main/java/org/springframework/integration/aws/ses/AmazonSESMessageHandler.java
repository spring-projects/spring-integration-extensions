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
package org.springframework.integration.aws.ses;

import org.springframework.integration.aws.core.AWSCredentials;
import org.springframework.integration.aws.ses.core.DefaultAmazonSESMailSender;
import org.springframework.integration.mail.MailSendingMessageHandler;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * The Message handler for the SES Mail. This will be used to send email
 * using Amazon SES
 *
 * @author Amol Nayak
 *
 * @since 1.0
 *
 */
public class AmazonSESMessageHandler extends MailSendingMessageHandler {

	/**
	 * The Default constructor that extends from the {@link MailSendingMessageHandler} and passes
	 * it an instance of {@link DefaultAmazonSESMailSender}
	 * @param credentials
	 */
	public AmazonSESMessageHandler(AWSCredentials credentials) {
		super(new DefaultAmazonSESMailSender(credentials));
	}

	/**
	 * The constructor that accepts the {@link JavaMailSender} instance, used for
	 * unit tests only
	 * @param mailSender
	 */
	AmazonSESMessageHandler(JavaMailSender mailSender) {
		super(mailSender);
	}
}
