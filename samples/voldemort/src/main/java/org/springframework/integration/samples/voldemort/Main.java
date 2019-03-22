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
package org.springframework.integration.samples.voldemort;

import java.util.Scanner;

import org.apache.log4j.Logger;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import org.springframework.integration.samples.voldemort.service.BusinessService;


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

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("\n========================================================="
					  + "\n                                                         "
					  + "\n          Welcome to Spring Integration!                 "
					  + "\n                                                         "
					  + "\n    For more information please visit:                   "
					  + "\n    https://www.springsource.org/spring-integration       "
					  + "\n                                                         "
					  + "\n=========================================================" );
		}

		final AbstractApplicationContext context =
				new ClassPathXmlApplicationContext("classpath:META-INF/spring/integration/*-context.xml");

		context.registerShutdownHook();

		final Scanner scanner = new Scanner(System.in);

		final BusinessService service = context.getBean(BusinessService.class);

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("\n========================================================="
					  + "\n                                                         "
					  + "\n    Please press 'q + Enter' to quit the application.    "
					  + "\n                                                         "
					  + "\n=========================================================" );
		}

		System.out.print("Please enter a string and press <enter>: ");

		final String key = "hello";

		while (true) {

			final String data = scanner.nextLine();

			if("q".equals(data.trim())) {
				break;
			}

			try {

				System.out.println(String.format("Persisting String: '%s' with key '%s'.", data, key));
				service.saveData(key, data);

			} catch (Exception e) {
				LOGGER.error("An exception was caught: " + e);
			}

			System.out.print("Please enter a string and press <enter>:");

		}

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Exiting application...bye.");
		}

		System.exit(0);

	}
}
