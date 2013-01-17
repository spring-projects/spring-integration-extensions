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
package org.springframework.integration.samples.mailses;

import java.util.Scanner;

import org.apache.log4j.Logger;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Starts the Spring Context and will initialize the Spring Integration routes.
 *
 * @author Gunnar Hillert
 * @since 1.0
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
				+ "\n    http://www.springsource.org/spring-integration       "
				+ "\n" + HORIZONTAL_LINE);

		final GenericXmlApplicationContext context = new GenericXmlApplicationContext();
		final ConfigurableEnvironment environment = context.getEnvironment();

		final String fromEmailAddress;
		final String toEmailAddress;
		final String subject;
		final String body;

		System.out.print("\nFrom which email address would you like to send a message?: ");
		fromEmailAddress = scanner.nextLine();

		System.out.print("To which email address would you like to send a message?: ");
		toEmailAddress = scanner.nextLine();

		System.out.print("What is the subject line?: ");
		subject = scanner.nextLine();

		System.out.print("What is the body of the message?: ");
		body = scanner.nextLine();

		if (!environment.containsProperty("accessKey")) {
			System.out.print("Please enter your access key: ");
			final String accessKey = scanner.nextLine();
			environment.getSystemProperties().put("accessKey", accessKey);
		}

		if (!environment.containsProperty("secretKey")) {
			System.out.print("Please enter your secret key: ");
			final String secretKey = scanner.nextLine();
			environment.getSystemProperties().put("secretKey", secretKey);
		}

		context.load("classpath:META-INF/spring/integration/*-context.xml");
		context.registerShutdownHook();
		context.refresh();

		final EmailService emailService = context.getBean(EmailService.class);

		emailService.send(fromEmailAddress, toEmailAddress, subject, body);

		System.out.println(String.format("The email to '%s' was sent successfully.", toEmailAddress));

		System.exit(0);

	}
}
