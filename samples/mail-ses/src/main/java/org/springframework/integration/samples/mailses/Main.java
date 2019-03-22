/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.integration.samples.mailses;

import java.util.List;
import java.util.Scanner;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;

/**
 * Starts the Spring Context and will initialize the Spring Integration routes.
 *
 * @author Gunnar Hillert
 * @since 2.2
 *
 */
public final class Main {

	private static final Logger LOGGER = Logger.getLogger(Main.class);

	private static final String HORIZONTAL_LINE = "\n=========================================================";

	private Main() {
	}

	/**
	 * Load the Spring Integration Application Context
	 *
	 * @param args - command line arguments
	 */
	public static void main(final String... args) {

		final Scanner scanner = new Scanner(System.in);

		LOGGER.info(HORIZONTAL_LINE + "\n"
				+ "\n          Welcome to Spring Integration!                 "
				+ "\n"
				+ "\n    For more information please visit:                   "
				+ "\n    https://www.springsource.org/spring-integration       "
				+ "\n" + HORIZONTAL_LINE);

		System.out.println("Please enter a choice and press <enter>: ");
		System.out.println("\t1. Use Embedded SMTP Server (Wiser)");
		System.out.println("\t2. Use Amazon SES");

		System.out.println("\tq. Quit the application");
		System.out.print("Enter your choice: ");

		final GenericXmlApplicationContext context = new GenericXmlApplicationContext();
		final ConfigurableEnvironment environment = context.getEnvironment();

		boolean usingWiser = false;

		String toEmailAddress;

		while (true) {
			final String input = scanner.nextLine();

			if("1".equals(input.trim())) {
				environment.setActiveProfiles("default");
				usingWiser = true;

				System.out.print("\nTo which email address would you like to send a message?: ");
				toEmailAddress = scanner.nextLine();

				break;
			} else if("2".equals(input.trim())) {
				environment.setActiveProfiles("aws");

				if (!environment.containsProperty("accessKey")) {
					System.out.print("\nPlease enter your access key: ");
					final String accessKey = scanner.nextLine();
					environment.getSystemProperties().put("accessKey", accessKey);
				}

				if (!environment.containsProperty("secretKey")) {
					System.out.print("\nPlease enter your secret key: ");
					final String secretKey = scanner.nextLine();
					environment.getSystemProperties().put("secretKey", secretKey);
				}
				System.out.print("\nTo which email address would you like to send a message?: ");
				toEmailAddress = scanner.nextLine();

				break;
			} else if("q".equals(input.trim())) {
				System.out.println("Exiting application...bye.");
				System.exit(0);
			} else {
				System.out.println("Invalid choice\n\n");
				System.out.print("Enter you choice: ");
			}
		}

		context.load("classpath:META-INF/spring/integration/*-context.xml");
		context.registerShutdownHook();
		context.refresh();

		final JavaMailSender ms = context.getBean(JavaMailSender.class);
		final String toEmailAddressToUse = toEmailAddress;
		final MimeMessagePreparator preparator = new MimeMessagePreparator() {

			public void prepare(MimeMessage mimeMessage) throws Exception {

				mimeMessage.setRecipient(Message.RecipientType.TO,
						new InternetAddress(toEmailAddressToUse));
				mimeMessage.setFrom(new InternetAddress(toEmailAddressToUse));
				mimeMessage.setSubject("Testing Email - Subject");
				mimeMessage.setText("Hello World");
			}

		};

		try {
			ms.send(preparator);
		} catch (MailException e) {
			throw new IllegalStateException(e);
		}

		System.out.println(String.format("The email to '%s' was sent successfully.", toEmailAddress));

		if (usingWiser) {
			Wiser wiser = context.getBean(Wiser.class);
			List<WiserMessage> messages = wiser.getMessages();

			final String from;
			final String subject;
			try {
				from = messages.get(0).getMimeMessage().getFrom()[0].toString();
				subject = messages.get(0).getMimeMessage().getSubject();
			} catch (MessagingException e) {
				throw new IllegalStateException(e);
			}

			System.out.println(String.format("Wiser received an email from '%s' with subject '%s'", from, subject));
		}

		System.exit(0);

	}
}
