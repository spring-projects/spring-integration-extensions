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
package org.springframework.integration.samples.smb;

import java.util.Scanner;

import org.apache.log4j.Logger;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.integration.smb.session.SmbSessionFactory;


/**
 * Starts the Spring Context and will initialize the Spring Integration routes.
 *
 * @author Gunnar Hillert
 * @since 1.0
 *
 */
public final class Main {

	private static final Logger LOGGER = Logger.getLogger(Main.class);

	private Main() { }

	/**
	 * Load the Spring Integration Application Context
	 *
	 * @param args - command line arguments
	 */
	public static void main(final String... args) {

		final Scanner scanner = new Scanner(System.in);

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("\n========================================================="
					  + "\n                                                         "
					  + "\n     Welcome to the Spring Integration Smb Sample        "
					  + "\n                                                         "
					  + "\n    For more information please visit:                   "
					  + "\nhttps://github.com/SpringSource/spring-integration-extensions"
					  + "\n                                                         "
					  + "\n=========================================================" );
		}

		final GenericXmlApplicationContext context = new GenericXmlApplicationContext();

		System.out.println("Please enter the: ");
		System.out.println("\t- SMB Host");
		System.out.println("\t- SMB Share and Directory");
		System.out.println("\t- SMB Username");
		System.out.println("\t- SMB Password");

		System.out.print("Host: ");
		final String host = scanner.nextLine();

		System.out.print("Share and Directory (e.g. myFile/path/to/): ");
		final String shareAndDir = scanner.nextLine();

		System.out.print("Username (e.g. guest): ");
		final String username = scanner.nextLine();

		System.out.print("Password (can be empty): ");
		final String password = scanner.nextLine();

		context.getEnvironment().getSystemProperties().put("host", host);
		context.getEnvironment().getSystemProperties().put("shareAndDir", shareAndDir);
		context.getEnvironment().getSystemProperties().put("username", username);
		context.getEnvironment().getSystemProperties().put("password", password);

		context.load("classpath:META-INF/spring/integration/*-context.xml");
		context.registerShutdownHook();
		context.refresh();

		context.registerShutdownHook();

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("\n========================================================="
					  + "\n                                                         "
					  + "\n    Please press 'q + Enter' to quit the application.    "
					  + "\n                                                         "
					  + "\n=========================================================" );
		}

		SmbSessionFactory smbSessionFactory = context.getBean("smbSession", SmbSessionFactory.class);

		System.out.println("Polling from Share: " + smbSessionFactory.getUrl());

		while (true) {

			final String input = scanner.nextLine();

			if("q".equals(input.trim())) {
				break;
			}

		}

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Exiting application...bye.");
		}

		System.exit(0);

	}
}
