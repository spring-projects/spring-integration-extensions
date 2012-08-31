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
 * Represents the various types of permissions on the object in S3
 *
 * @author Amol Nayak
 *
 * @since 0.5
 *
 */
public enum ObjectPermissions {

	/**
	 * Indicates the grantee has permissions to read the object from the containing bucket
	 */
	READ,

	/**
	 * Indicates the grantee has permissions to read the Access control permissions of the
	 * Object in S3
	 */
	READ_ACP,

	/**
	 * Indicates the grantee has permissions to write the Access control permissions of the
	 * Object in S3
	 */
	WRITE_ACP;
}
