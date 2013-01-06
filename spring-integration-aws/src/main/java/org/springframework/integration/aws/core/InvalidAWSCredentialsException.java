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
package org.springframework.integration.aws.core;

/**
 * Thrown when AWS Credentials provided by the user are incomplete or invalid
 *
 * @author Amol Nayak
 *
 * @since 1.0
 *
 */
public class InvalidAWSCredentialsException extends RuntimeException {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public InvalidAWSCredentialsException() {
		super();
	}

	public InvalidAWSCredentialsException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidAWSCredentialsException(String message) {
		super(message);
	}

	public InvalidAWSCredentialsException(Throwable cause) {
		super(cause);
	}
}
