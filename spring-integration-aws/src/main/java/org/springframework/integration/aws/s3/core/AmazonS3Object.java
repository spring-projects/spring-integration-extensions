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

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import org.springframework.util.Assert;



/**
 * The Amazon S3 Object representing the Object in S3
 *
 * @author Amol Nayak
 *
 * @since 0.5
 *
 */
public class AmazonS3Object implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -832622119907619624L;

	private Map<String, String> userMetaData;

	private Map<String, Object> metaData;

	private final InputStream inputStream;

	private final File fileSource;

	private final AmazonS3ObjectACL objectACL;

	/**
	 * The default constructor
	 *
	 * @param userMetaData
	 * @param metaData
	 * @param inputStream
	 */
	public AmazonS3Object(Map<String, String> userMetaData,
			Map<String, Object> metaData, InputStream inputStream,File fileSource,AmazonS3ObjectACL objectACL) {
		if(userMetaData != null)
			this.userMetaData = Collections.unmodifiableMap(userMetaData);

		if(metaData != null)
			this.metaData = Collections.unmodifiableMap(metaData);

		Assert.isTrue((inputStream == null) ^ (fileSource == null),
				"Exactly one of 'inputStream' or 'fileSource' must be provided");

		this.inputStream = inputStream;
		this.fileSource = fileSource;
		this.objectACL = objectACL;
	}

	/**
	 * The constructor that delegates to {@link #AmazonS3Object(Map, Map, InputStream, File, AmazonS3ObjectACL)}
	 * with null {@link AmazonS3ObjectACL}
	 *
	 * @param userMetaData
	 * @param metaData
	 * @param inputStream
	 * @param fileSource
	 */
	public AmazonS3Object(Map<String, String> userMetaData,
			Map<String, Object> metaData, InputStream inputStream,File fileSource) {
		this(userMetaData,metaData,inputStream,fileSource,null);
	}


	/**
	 * Gets the User Metadata associated with given Amazon S3 object
	 * @return
	 */
	public Map<String, String> getUserMetaData() {
		return userMetaData;
	}

	/**
	 * Gets the Metadata associated with the given Amazon S3 object
	 * @return
	 */
	public Map<String, Object> getMetaData() {
		return metaData;
	}

	/**
	 * Gets the {@link InputStream} to the resource
	 * @return
	 */
	public InputStream getInputStream() {
		return inputStream;
	}

	/**
	 * Gets the file source
	 * @return
	 */
	public File getFileSource() {
		return fileSource;
	}

	/**
	 * Gets the Access controls associated with the S3 Object
	 * @return
	 */
	public AmazonS3ObjectACL getObjectACL() {
		return objectACL;
	}
}
