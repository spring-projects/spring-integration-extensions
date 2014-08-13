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
package org.springframework.integration.aws.s3;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.integration.Message;
import org.springframework.integration.MessageHeaders;
import org.springframework.integration.MessagingException;
import org.springframework.integration.aws.s3.core.AmazonS3Object;
import org.springframework.integration.aws.s3.core.AmazonS3OperationException;
import org.springframework.integration.aws.s3.core.AmazonS3Operations;
import org.springframework.integration.aws.s3.core.PaginatedObjectsView;
import org.springframework.integration.handler.MessageProcessor;
import org.springframework.util.StringUtils;

/**
 * The utility class that will be used to perform operations like put object, remove object,
 * list objects. The utility class uses the provided s3Operation instance and the request message
 * to perform the operation
 *
 * @author Amol Nayak
 * @since 0.5
 *
 */
public class AmazonS3OperationUtils {

	private static final Log logger = LogFactory.getLog(AmazonS3OperationUtils.class);

	public static final String FILE_NAME 					= "file_name";
	public static final String USER_METADATA				= "user_meta_data";
	public static final String METADATA						= "meta_data";
	public static final String OBJECT_ACLS					= "object_acls";
	public static final String OBJECT_NAME 					= "object_name";

	/**
	 * The header relevant for the remove command giving name of the object to
	 * be removed from the bucket
	 */
	public static final String HEADER_REMOVE_OBJECT_NAME 	= OBJECT_NAME;

	/**
	 * The header relevant for the get command giving name of the object to
	 * be retrieved from the bucket
	 */
	public static final String HEADER_GET_OBJECT_NAME 		= OBJECT_NAME;

	/**
	 * The header relevant for the list command which gives the next marker
	 * to be used in case the previous list operation didn't list all the
	 * objects in the bucket.
	 */
	public static final String HEADER_LIST_NEXT_MARKER 		= "list_next_marker";

	/**
	 * The header relevant for the list command giving the maximum number of
	 * objects per list operation
	 */
	public static final String HEADER_LIST_PAGE_SIZE 		= "list_page_size";

	/**
	 * The default size of the max objects per page if no value provided in the request message
	 */
	public static final int MAX_OBJECTS_PER_PAGE			= 100;


	/**
	 * The helper method used to get the object from the S3 bucket
	 *
	 * @param operations
	 * @param bucket
	 * @param accessKey
	 * @param remoteDirectoryProcessor
	 * @param message
	 * @return
	 */
	public static AmazonS3Object getObject(
			AmazonS3Operations operations,
			String bucket,
			String accessKey,
			MessageProcessor<String> remoteDirectoryProcessor,
			Message<?> message) {
		String objectName = getObjectName(message);
		if(!StringUtils.hasText(objectName)) {
			throw new MessagingException(message,
					"get operation needs to specify the mandatory " +
					"header " + HEADER_GET_OBJECT_NAME + " in the request message");
		}
		String remoteDirectory = getRemoteDirectory(remoteDirectoryProcessor, message);
		return operations.getObject(bucket, remoteDirectory, objectName);
	}

	/**
	 * Helper method to put an object in a bucket in S3
	 * @param operations
	 * @param bucket
	 * @param accessKey
	 * @param charset
	 * @param remoteDirectoryProcessor
	 * @param fileNameGenerator
	 * @param message
	 */
	@SuppressWarnings("unchecked")
	public static final void putObject(AmazonS3Operations operations,
								String bucket,
								String accessKey,
								String charset,
								MessageProcessor<String> remoteDirectoryProcessor,
								FileNameGenerationStrategy fileNameGenerator,
								Message<?> message) throws Exception {
		Object payload = message.getPayload();

		//The payload can be only of type java.io.File, java.io.InputStream, byte[] or String
		File file = null;
		InputStream in = null;

		//potentially unsafe operation if the types are not as those expected
		Map<String, String> userMetaData =  getHeaderValue(message,USER_METADATA,Map.class);
		Map<String, Object> metaData = getHeaderValue(message,METADATA,Map.class);
		Map<String, Collection<String>> objectAcls = getHeaderValue(message,OBJECT_ACLS,Map.class);

		AmazonS3ObjectBuilder builder = AmazonS3ObjectBuilder
		.getInstance()
		.withMetaData(metaData)
		.withUserMetaData(userMetaData)
		.withObjectACL(objectAcls);


		String folder = remoteDirectoryProcessor.processMessage(message);

		String objectName = fileNameGenerator.generateFileName(message);

		if(payload instanceof File) {
			file = (File)payload;
		}
		else if (payload instanceof InputStream) {
			in = (InputStream)payload;
		}
		else if(payload instanceof byte[]) {
			in = new ByteArrayInputStream((byte[])payload);
		}
		else if(payload instanceof String) {
			in = new ByteArrayInputStream(((String)payload).getBytes(charset));
		}
		else {
			throw new AmazonS3OperationException
			(accessKey,
					bucket, objectName, "The Message payload is of unexpected type "
					+ payload.getClass().getCanonicalName() + ", only supported types are"
					+" java.io.File, java.io.InputStream, byte[] and java.lang.String");
		}
		if(file != null) {
			builder.fromFile(file);
		}
		else  {
			builder.fromInputStream(in);
		}

		AmazonS3Object object = builder.build();

		if(logger.isDebugEnabled()) {
			logger.debug("Uploading Object to bucket " + bucket + ", to folder " + folder + ", with object name " + objectName);
		}

		operations.putObject(bucket, folder, objectName, object);
	}

	/**
	 * The utility method that will be used to list the objects in a bucket and a folder
	 * @param operations
	 * @param bucket
	 * @param accessKey
	 * @param remoteDirectoryProcessor
	 * @param reuqestMessage
	 * @return
	 */
	public static PaginatedObjectsView listObjects(AmazonS3Operations operations,
										String bucket,
										String accessKey,
										MessageProcessor<String> remoteDirectoryProcessor,
										Message<?> requestMessage) {
		Object messagePayload = requestMessage.getPayload();
		String remoteDirectory;
		if(messagePayload != null && messagePayload instanceof String) {
			remoteDirectory = getRemoteDirectory(remoteDirectoryProcessor, requestMessage, (String)messagePayload);
		}
		else {
			remoteDirectory = getRemoteDirectory(remoteDirectoryProcessor, requestMessage);
		}
		MessageHeaders headers = requestMessage.getHeaders();
		Object providedMarker = headers.get(HEADER_LIST_NEXT_MARKER);
		String nextMarker;
		if(providedMarker != null && providedMarker instanceof String) {
			nextMarker = (String)providedMarker;
		}
		else {
			nextMarker = null;
		}
		int pageSize;
		Object pageSizeHeader = headers.get(HEADER_LIST_PAGE_SIZE);
		if(pageSizeHeader != null && pageSizeHeader instanceof Number) {
			pageSize = ((Number)pageSizeHeader).intValue();
		}
		else {
			pageSize = MAX_OBJECTS_PER_PAGE;
		}
		return operations.listObjects(bucket, remoteDirectory, nextMarker, pageSize);

	}


	/**
	 * Helper method to remove an object from a bucket in S3
	 * @param operations
	 * @param bucket
	 * @param accessKey
	 * @param remoteDirectoryProcessor
	 * @param message
	 */
	public static void removeObject(AmazonS3Operations operations,
									String bucket,
									String accessKey,
									MessageProcessor<String> remoteDirectoryProcessor,
									Message<?> message) {
		String objectName = getObjectName(message);
		if(!StringUtils.hasText(objectName)) {
			throw new MessagingException(message,
					"remove operation needs to specify the mandatory " +
					"header " + HEADER_REMOVE_OBJECT_NAME + " in the request message");
		}
		String remoteDirectory = getRemoteDirectory(remoteDirectoryProcessor, message);
		operations.removeObject(bucket, remoteDirectory, objectName);
	}

	/**
	 *
	 * @param message
	 * @return
	 */
	private static String getObjectName(Message<?> message) {
		MessageHeaders headers = message.getHeaders();
		String objectName = (String)headers.get(HEADER_REMOVE_OBJECT_NAME);
		if(objectName == null) {
			Object payload = message.getPayload();
			if(payload != null && payload instanceof String) {
				objectName = (String)payload;
			}
		}
		return objectName;
	}

	/**
	 *
	 * @param remoteDirectoryProcessor
	 * @param message
	 * @return
	 */
	private static String getRemoteDirectory(
			MessageProcessor<String> remoteDirectoryProcessor,
			Message<?> message) {
		return getRemoteDirectory(remoteDirectoryProcessor,	message, "/");
	}

	/**
	 *
	 * @param remoteDirectoryProcessor
	 * @param message
	 * @return
	 */
	private static String getRemoteDirectory(
			MessageProcessor<String> remoteDirectoryProcessor,
			Message<?> message,
			String defaultValue) {
		String remoteDirectory = remoteDirectoryProcessor.processMessage(message);
		if(!StringUtils.hasText(remoteDirectory)) {
			remoteDirectory = defaultValue != null? defaultValue : "/";
		}
		return remoteDirectory;
	}

	/**
	 * The common helper method that would read the message header and checks if it is of a particular type or not
	 * @param <T>
	 * @param message
	 * @param headerName
	 * @param expectedType
	 */
	@SuppressWarnings("unchecked")
	public static final <T> T getHeaderValue(Message<?> message, String headerName, Class<T> expectedType) {
		T header = null;
		Object genericHeader = message.getHeaders().get(headerName);
		if(genericHeader == null) {
			return null;
		}
		if(expectedType.isAssignableFrom(genericHeader.getClass())) {
			header = (T)genericHeader;
		}
		else {
			logger.warn("Found header " + USER_METADATA + " in the message but was not of required type");
		}
		return header;
	}
}
