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
 * The basic implementation class holding the Access key and the secret
 * key for the AWS account .
 *
 * @author Amol Nayak
 *
 * @since 1.0
 *
 */
public class BasicAWSCredentials implements AWSCredentials {

	/**.
	 * Hold the Access key for the AWS account
	 */
	private String accessKey;

	/**.
	 * Hold the Secret key for the
	 */
	private String secretKey;


	/**.
	 * Default constructor
	 */
	public BasicAWSCredentials() {

	}

	/**.
	 * The constructor accepting the access and secret key
	 * @param accessKey
	 * @param secretKey
	 */
	public BasicAWSCredentials(String accessKey, String secretKey) {
		super();
		this.accessKey = accessKey;
		this.secretKey = secretKey;
	}

	/**
	 * Get the Access key to the Amazon WS account
	 * @return
	 */
	public String getAccessKey() {
		return accessKey;
	}

	/**
	 * Set the Access key to the Amazon WS account
	 * @param accessKey
	 */
	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}

	/**
	 * Get the Secret key to the Amazon WS account
	 * @return
	 */
	public String getSecretKey() {
		return secretKey;
	}

	/**.
	 * Set the Secret key to the Amazon WS account
	 * @param secretKey
	 */
	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}
}
