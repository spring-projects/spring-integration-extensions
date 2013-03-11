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

import java.util.Map;

import org.springframework.integration.aws.core.AWSOperationException;

/**
 * This exception will be thrown upon failure in sending a mail from Amazon SES
 *
 * @author Amol Nayak
 *
 * @since 0.5
 *
 */
public class AmazonSESMailSendException extends AWSOperationException {


	/**
	 *
	 */
	private static final long serialVersionUID = -3035267174544370619L;

	private final Map<Object, Exception> failedMessages;

	/**
	 *
	 * @param accessKey
	 * @param message
	 * @param cause
	 * @param failedMessages
	 */
	public AmazonSESMailSendException(String accessKey, String message,
			Throwable cause,Map<Object, Exception> failedMessages) {
		super(accessKey, message, cause);
		this.failedMessages = failedMessages;
	}

	/**
	 *
	 * @param accessKey
	 * @param message
	 * @param failedMessages
	 */
	public AmazonSESMailSendException(String accessKey,
			String message,Map<Object, Exception> failedMessages) {
		super(accessKey, message);
		this.failedMessages = failedMessages;
	}

	/**
	 *
	 * @param accessKey
	 * @param cause
	 * @param failedMessages
	 */
	public AmazonSESMailSendException(String accessKey,
			Throwable cause,Map<Object, Exception> failedMessages) {
		super(accessKey, cause);
		this.failedMessages = failedMessages;
	}

	/**
	 *
	 * @param accessKey
	 * @param failedMessages
	 */
	public AmazonSESMailSendException(String accessKey,Map<Object, Exception> failedMessages) {
		super(accessKey);
		this.failedMessages = failedMessages;
	}

	/**
	 * Gets the map of failed messages where the failed message is the key and the exception
	 * while sending it is the value
	 */
	public Map<Object, Exception> getFailedMessages() {
		return failedMessages;
	}
}
