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

import java.util.Date;

/**
 * The summary of the Object stored on Amazon S3.
 *
 * @author Amol Nayak
 *
 * @since 0.5
 *
 */
public interface S3ObjectSummary {

	/**
	 * Gets the Bucket nane in which the object is kept on S3
	 * @return
	 */
	String getBucketName();

	/**
	 * Gets the keys under which the Object is stored on S3
	 * @return
	 */
	String getKey();

	/**
	 * Gets the Hex encoded 128 bit MD5 digest of the contents of the object uploaded on S3
	 * @return
	 */
	String getETag();

	/**
	 * Gets the size of the object in bytes
	 * @return
	 */
	long getSize();

	/**
	 * Gets the Date the object was last modified
	 * @return
	 */
	Date getLastModified();
}
