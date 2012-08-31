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
 * Identifies the type of the grantee. E.g. A grantee can be identified using the  canonical
 * identifier or the email id.
 *
 * @author Amol Nayak
 *
 * @since 0.5
 *
 */
public enum GranteeType {

	/**
	 * This represents the Canonical id of the users AWS account
	 */
	CANONICAL_GRANTEE_TYPE,

	/**
	 * This email is usually resolved to the canonical id of the user.
	 * This would fail and an error would be thrown if more than 2 accounts are
	 * related to the user's email account.
	 */
	EMAIL_GRANTEE_TYPE,

	/**
	 * These represent come constants representing some predefined groups by Amazon S3
	 */
	GROUP_GRANTEE_TYPE;
}
