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
package org.springframework.integration.aws.s3.core;

/**
 * Various types of Groups who can be granted permissions on amazon S3 objects
 *
 * @author Amol Nayak
 *
 * @since 0.5
 *
 */
public enum GroupGranteeType {

	/**
	 * To grants anonymous access to all objects in the bucket
	 */
	AllUsers("http://acs.amazonaws.com/groups/global/AllUsers"),
	/**
	 * To grant access to all authenticated users of AWS who is logged in using
	 * their AWS credentials
	 */
	AuthenticatedUsers("http://acs.amazonaws.com/groups/global/AuthenticatedUsers");

	private String identifier;

	private GroupGranteeType(String identifier) {
		this.identifier = identifier;
	}

	public String getIdentifier() {
		return identifier;
	}
}
