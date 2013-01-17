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

import java.io.InputStream;
import java.util.Properties;

import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

import org.springframework.integration.aws.core.AWSCredentials;
import org.springframework.integration.aws.core.AWSOperationException;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessagePreparator;

import com.amazonaws.services.simpleemail.AWSJavaMailTransport;

/**
 * The implementation class for sending mail using the Amazon SES service
 *
 * @author Amol Nayak
 * @author Gunnar Hillert
 *
 * @since 0.5
 *
 */
public class DefaultAmazonSESMailSender implements JavaMailSender {

	public DefaultAmazonSESMailSender(AWSCredentials credentials) {

		if(credentials == null) {
			throw new AWSOperationException(null, "Credentials cannot be null, provide a non null valid set of credentials");
		}

		/*
		 * Setup JavaMail to use the Amazon Simple Email Service by specifying
		 * the "aws" protocol.
		 */
		Properties properties = new Properties();
		properties.setProperty("mail.transport.protocol", "aws");

		properties.setProperty(AWSJavaMailTransport.AWS_ACCESS_KEY_PROPERTY, credentials.getAccessKey());
		properties.setProperty(AWSJavaMailTransport.AWS_SECRET_KEY_PROPERTY, credentials.getSecretKey());

		javaMailSender.setJavaMailProperties(properties);

	}

	private JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl() {

		@Override
		protected Transport getTransport(Session session)
				throws NoSuchProviderException {
			return new AWSJavaMailTransport(session, null);
		}

	};

	@Override
	public void send(SimpleMailMessage simpleMessage) throws MailException {
		javaMailSender.send(simpleMessage);
	}

	@Override
	public void send(SimpleMailMessage[] simpleMessages) throws MailException {
		javaMailSender.send(simpleMessages);
	}

	@Override
	public MimeMessage createMimeMessage() {
		return javaMailSender.createMimeMessage();
	}

	@Override
	public MimeMessage createMimeMessage(InputStream contentStream)
			throws MailException {
		return javaMailSender.createMimeMessage(contentStream);
	}

	@Override
	public void send(MimeMessage mimeMessage) throws MailException {
		javaMailSender.send(mimeMessage);
	}

	@Override
	public void send(MimeMessage[] mimeMessages) throws MailException {
		javaMailSender.send(mimeMessages);
	}

	@Override
	public void send(MimeMessagePreparator mimeMessagePreparator)
			throws MailException {
		javaMailSender.send(mimeMessagePreparator);

	}

	@Override
	public void send(MimeMessagePreparator[] mimeMessagePreparators)
			throws MailException {
		javaMailSender.send(mimeMessagePreparators);
	}

}