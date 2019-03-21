/*
 * Copyright 2002-2013 the original author or authors.
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
package org.springframework.integration.samples.aws.s3;

import java.io.File;
import java.util.Scanner;

import org.apache.log4j.Logger;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.support.MessageBuilder;

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

	private Main() { }

	/**
	 * Load the Spring Integration Application Context
	 *
	 * @param args - command line arguments
	 */
	public static void main(final String... args) {

		final Scanner scanner = new Scanner(System.in);

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info(HORIZONTAL_LINE
					  + "\n                                                         "
					  + "\n     Welcome to the Spring Integration Amazon S3 Sample  "
					  + "\n                                                         "
					  + "\n    For more information please visit:                   "
					  + "\nhttps://github.com/SpringSource/spring-integration-extensions"
					  + "\n                                                         "
					  + HORIZONTAL_LINE );
		}

		final GenericXmlApplicationContext context = new GenericXmlApplicationContext();
		final ConfigurableEnvironment environment = context.getEnvironment();

		System.out.println("What would you like to do?");
		System.out.println("\t1. Upload a file to Amazon S3");
		System.out.println("\t2. Poll files from Amazon S3");
		System.out.println("\tq. Quit the application");
		System.out.print(" > ");

		String filePath;

		while (true) {
			final String input = scanner.nextLine();

			if("1".equals(input.trim())) {

				System.out.println("Uploading to Amazon S3...");

				environment.setActiveProfiles("upload-to-s3");
				setupCredentials(environment, scanner);
				setupS3info(environment, scanner);

				context.load("classpath:META-INF/spring/integration/*-context.xml");
				context.registerShutdownHook();
				context.refresh();

				System.out.print("\nPlease enter the path to the file you want to upload: ");
				filePath = scanner.nextLine();

				final MessageChannel messageChannel = context.getBean("s3channel", MessageChannel.class);
				messageChannel.send(MessageBuilder.withPayload(new File(filePath)).build());

				break;

			}
			else if("2".equals(input.trim())) {

				System.out.println("Polling files from Amazon S3...");
				environment.setActiveProfiles("poll-s3");
				setupCredentials(environment, scanner);
				setupS3info(environment, scanner);

				context.load("classpath:META-INF/spring/integration/*-context.xml");
				context.registerShutdownHook();
				context.refresh();

			}
			else if("q".equals(input.trim())) {
				System.out.println("Exiting application...bye.");
				System.exit(0);
			}
			else {
				System.out.println("Invalid choice\n\n");
				System.out.print("Enter you choice: ");
			}
		}


		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Exiting application...bye.");
		}

		System.exit(0);

	}

	private static void setupCredentials(ConfigurableEnvironment environment, Scanner scanner) {
		if (!environment.containsProperty("accessKey")) {
			System.out.print("\nPlease enter your Access Key ID: ");
			final String accessKey = scanner.nextLine();
			environment.getSystemProperties().put("accessKey", accessKey);
		}

		if (!environment.containsProperty("secretKey")) {
			System.out.print("\nPlease enter your Secret Access Key: ");
			final String secretKey = scanner.nextLine();
			environment.getSystemProperties().put("secretKey", secretKey);
		}
	}

	private static void setupS3info(ConfigurableEnvironment environment, Scanner scanner) {
		if (!environment.containsProperty("bucket")) {
			System.out.print("\nWhich bucket do you want to use? ");
			final String bucket = scanner.nextLine();
			environment.getSystemProperties().put("bucket", bucket);
		}

		if (!environment.containsProperty("remoteDirectory")) {
			System.out.print("\nPlease enter the S3 remote directory to use: ");
			final String remoteDirectory = scanner.nextLine();
			environment.getSystemProperties().put("remoteDirectory", remoteDirectory);
		}
	}
}
