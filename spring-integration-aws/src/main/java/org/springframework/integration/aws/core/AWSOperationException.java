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
 * The Base class for all other AWS operation exceptions
 *
 * @author Amol Nayak
 *
 * @since 1.0
 *
 */
public class AWSOperationException extends RuntimeException {

	/**
	 *
	 */
	private static final long serialVersionUID = 3391888045993691634L;

	private final String accessKey;

	public AWSOperationException(String accessKey) {
		super();
		this.accessKey = accessKey;
	}

	public AWSOperationException(String accessKey,String message) {
		super(message);
		this.accessKey = accessKey;
	}

	public AWSOperationException(String accessKey,String message, Throwable cause) {
		super(message, cause);
		this.accessKey = accessKey;
	}

	public AWSOperationException(String accessKey,Throwable cause) {
		super(cause);
		this.accessKey = accessKey;
	}

	/**
	 * Get the access key for the user who encountered the exception
	 * @return
	 */
	public String getAccessKey() {
		return accessKey;
	}



}
