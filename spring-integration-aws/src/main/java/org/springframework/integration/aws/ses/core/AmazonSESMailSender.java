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

import javax.mail.internet.MimeMessage;

/**
 * The common interface for sending the mail using Amazon Simple Email Service (SES)
 * for more information on Amazon SES visit
 * http://aws.amazon.com/ses/
 *
 * @author Amol Nayak
 *
 * @since 1.0
 *
 */
public interface AmazonSESMailSender {

	/**
	 * Send an email from the given mail message
	 * @param message: The Email message to be sent over using SES
	 */
	public void send(AmazonSESSimpleMailMessage message);

	/**
	 * Send an email from the given mail messages
	 * @param message: The Email messages to be sent over using SES
	 */
	public void send(AmazonSESSimpleMailMessage[] messages);

	/**
	 * Sends a {@link MimeMessage} as a raw mail message using Amazon SES
	 * @param mailMessage
	 */
	public void send(MimeMessage mailMessage);

	/**
	 * Sends raw mail messages for the given array of messages
	 * @param mailMessages
	 */
	public void send(MimeMessage[] mailMessages);


}
