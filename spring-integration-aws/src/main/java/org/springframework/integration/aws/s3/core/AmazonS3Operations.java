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
 * The Core interface for performing various operations on Amazon S3 like listing objects
 * in the bucket, get an object, put an object and remove an object
 *
 * @author Amol Nayak
 *
 * @since 0.5
 *
 */
public interface AmazonS3Operations {

	public static final String CONTENT_MD5_HEADER = "Content-MD5";

	/**
	 * Lists Objects in the given bucket and given folder. Provide / if you
	 * wish to list objects at the root of the bucket
	 *
	 * @param bucketName
	 * @param folder
	 * @param nextMarker
	 * @param pageSize
	 * @return the {@link PaginatedObjectsView} of the matching result
	 */
	PaginatedObjectsView listObjects(String bucketName,String folder,String nextMarker,int pageSize);

	/**
	 * Put the given {@link AmazonS3Object} in the provided bucket in the folder specified with the name given
	 * The object if exists, will be overwritten and the folder path hierarchy
	 * if absent will be created
	 *
	 * @param bucketName
	 * @param folder
	 * @param objectName
	 * @param s3Object
	 */
	void putObject(String bucketName,String folder,String objectName,AmazonS3Object s3Object);

	/**
	 * Gets the Object from Amazon S3 from the specified bucket,folder and with
	 * the given objectName
	 *
	 * @param bucketName
	 * @param folder
	 * @param objectName
	 * @return The S3 object corresponding to the given details. Null if no object found
	 */
	AmazonS3Object getObject(String bucketName,String folder,String objectName);

	/**
	 * Removes the specified object from the bucket given, folder specified
	 * and the given object name from S3
	 * @param bucketName
	 * @param folder
	 * @param objectName
	 * @return true if the object was successfully removed else false
	 */
	boolean removeObject(String bucketName,String folder,String objectName);
}
