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

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.integration.aws.s3.InboundFileSynchronizationImpl.CONTENT_MD5;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.integration.aws.s3.core.AmazonS3Object;
import org.springframework.integration.aws.s3.core.AmazonS3Operations;
import org.springframework.integration.aws.s3.core.PaginatedObjectsView;
import org.springframework.integration.aws.s3.core.S3ObjectSummary;
import org.springframework.util.StringUtils;

/**
 * The utility class for mocking the {@link AmazonS3Operations}
 *
 * @author Amol Nayak
 *
 * @since 0.5
 */
public final class AmazonS3OperationsMockingUtil {

	public static final String BUCKET = "com.si.aws.test.bucket";
	private static final List<S3ObjectSummary> summary = new ArrayList<S3ObjectSummary>();
	private static final Map<String, String[]> objectDetails = new HashMap<String, String[]>();

	private AmazonS3OperationsMockingUtil() {
		throw new AssertionError("Cannot instantiate utility class");
	}

	public static final AmazonS3Operations mockS3Operations() {

		AmazonS3Operations operations;

		PaginatedObjectsView view = new PaginatedObjectsView() {

			@Override
			public boolean hasMoreResults() {
				return false;
			}

			@Override
			public List<S3ObjectSummary> getObjectSummary() {
				return summary;
			}

			@Override
			public String getNextMarker() {
				return null;
			}
		};
		operations = mock(AmazonS3Operations.class);
		when(operations.listObjects(anyString(), anyString(), anyString(), anyInt()))
		.thenReturn(view);

		when(operations.getObject(anyString(), anyString(), anyString()))
		.then(new Answer<AmazonS3Object>() {
			public AmazonS3Object answer(InvocationOnMock invocation)
					throws Throwable {
				String folderName = (String)invocation.getArguments()[1];
				String fileName = (String)invocation.getArguments()[2];

				if(StringUtils.hasText(folderName)) {
					if(folderName.startsWith("/")) {
						folderName = folderName.substring(1);
					}
					if(folderName.endsWith("/")) {
						folderName = folderName  + "/";
					}
				}
				else {
					folderName = "";
				}


				String[] object = objectDetails.get(folderName + fileName);
				AmazonS3Object s3Object;
				if(object != null) {
					s3Object = new AmazonS3ObjectBuilder()
							.fromInputStream(new ByteArrayInputStream(object[1].getBytes()))
							.withUserMetaData(Collections.singletonMap(CONTENT_MD5, object[2]))
							.build();
				}
				else {
					s3Object = null;
				}

				return s3Object;
			}
		});

		return operations;
	}


	/**
	 * The private helper method that is used to mock the {@link AmazonS3Operations#listObjects(String, String, String, int)
	 * method
	 *
	 * The method accepts a List of object[] where each element of object array has the
	 * following significance
	 *
	 * object[0] is the key of the file
	 * object[1] is the content of the file
	 * object[2] is the MD5 content to be used (optional)
	 * object[3] is the etag of the object
	 *
	 *  @param listObjects
	 */
	public static void mockAmazonS3Operations(final List<String[]> listObjects) {
		summary.clear();
		for(String[] listObject:listObjects) {
			addToSummaryList(listObject);
			objectDetails.put(listObject[0], listObject);
		}
	}

	/**
	 * Creates a {@link S3ObjectSummary} with the given details and adds it to the summary object list
	 * @param listObject
	 */
	private static void addToSummaryList(final String[] listObject) {
		summary.add(
			new S3ObjectSummary() {

				@Override
				public long getSize() {
					return 0;
				}

				@Override
				public Date getLastModified() {
					return null;
				}

				@Override
				public String getKey() {
					return listObject[0];
				}

				@Override
				public String getETag() {
					if(listObject[3] != null) {
						return listObject[3];
					}
					else {
						byte[] b64 = Base64.decodeBase64((listObject[2]).getBytes());
						return Hex.encodeHexString(b64);
					}

				}

				@Override
				public String getBucketName() {
					return null;
				}
			}
		);
	}

}
