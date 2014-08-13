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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
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
	private static LastRemoveOperationCall lastRemoveOperation = new LastRemoveOperationCall();
	private static LastPutOperationCall lastPutOperation = new LastPutOperationCall();
	private static LastListOperationCall lastListOperation = new LastListOperationCall();

	private AmazonS3OperationsMockingUtil() {
		throw new AssertionError("Cannot instantiate utility class");
	}

	public static final AmazonS3Operations mockS3Operations() {

		AmazonS3Operations operations;

		final PaginatedObjectsView view = new PaginatedObjectsView() {

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
		.then(new Answer<PaginatedObjectsView>() {

			@Override
			public PaginatedObjectsView answer(InvocationOnMock invocation)
					throws Throwable {
				Object[] args = invocation.getArguments();
				lastListOperation.setBucket((String)args[0]);
				lastListOperation.setRemoteDirectory((String)args[1]);
				lastListOperation.setNextMarker((String)args[2]);
				lastListOperation.setPageSize(((Number)args[3]).intValue());
				return view;
			}
		});

		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				lastRemoveOperation.setBucket((String)args[0]);
				lastRemoveOperation.setRemoteDirectory((String)args[1]);
				lastRemoveOperation.setObjectName((String)args[2]);
				return null;
			}

		}).when(operations).removeObject(anyString(), anyString(), anyString());

		doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock inv) {
				Object[] args = inv.getArguments();
				lastPutOperation.setBucket((String)args[0]);
				lastPutOperation.setFolder((String)args[1]);
				lastPutOperation.setObjectName((String)args[2]);
				lastPutOperation.setS3Object((AmazonS3Object)args[3]);
				return null;
			}
		}).
		when(operations)
		.putObject(anyString(), anyString(), anyString(), any(AmazonS3Object.class));

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
					if(StringUtils.hasText(folderName) && !folderName.endsWith("/")) {
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

	/**
	 * The utility class that would be used to hold the value of the invocation of the
	 * last remove call, doesn't bother about thread safety
	 *
	 *
	 */
	public static class LastRemoveOperationCall {
		private String bucket;
		private String remoteDirectory;
		private String objectName;

		/**
		 *
		 */
		public String getBucket() {
			return bucket;
		}

		/**
		 *
		 * @param bucket
		 */
		public void setBucket(String bucket) {
			this.bucket = bucket;
		}

		/**
		 *
		 * @return
		 */
		public String getRemoteDirectory() {
			return remoteDirectory;
		}

		/**
		 *
		 * @param remoteDirectory
		 */
		public void setRemoteDirectory(String remoteDirectory) {
			this.remoteDirectory = remoteDirectory;
		}
		/**
		 *
		 * @return
		 */
		public String getObjectName() {
			return objectName;
		}
		/**
		 *
		 * @param objectName
		 */
		public void setObjectName(String objectName) {
			this.objectName = objectName;
		}
	}

	/**
	 * The utility class that would be used to hold the value of the invocation of the
	 * last list operation call
	 *
	 *
	 */
	public static class LastListOperationCall {
		private String bucket;
		private String remoteDirectory;
		private String nextMarker;
		private int pageSize;

		/**
		 *
		 */
		public String getBucket() {
			return bucket;
		}

		/**
		 *
		 * @param bucket
		 */
		public void setBucket(String bucket) {
			this.bucket = bucket;
		}

		/**
		 *
		 * @return
		 */
		public String getRemoteDirectory() {
			return remoteDirectory;
		}

		/**
		 *
		 * @param remoteDirectory
		 */
		public void setRemoteDirectory(String remoteDirectory) {
			this.remoteDirectory = remoteDirectory;
		}
		/**
		 *
		 * @return
		 */
		public String getNextMarker() {
			return nextMarker;
		}
		/**
		 *
		 * @param objectName
		 */
		public void setNextMarker(String nextMarker) {
			this.nextMarker = nextMarker;
		}

		/**
		 *
		 * @return
		 */
		public int getPageSize() {
			return pageSize;
		}

		/**
		 *
		 * @param pageSize
		 */
		public void setPageSize(int pageSize) {
			this.pageSize = pageSize;
		}
	}

	/**
	 *
	 *
	 *
	 */
	public static class LastPutOperationCall {

		private String bucket;
		private String folder;
		private String objectName;
		private AmazonS3Object s3Object;

		/**
		 *
		 * @return
		 */
		public String getBucket() {
			return bucket;
		}

		/**
		 *
		 * @param bucket
		 */
		public void setBucket(String bucket) {
			this.bucket = bucket;
		}

		/**
		 *
		 * @return
		 */
		public String getFolder() {
			return folder;
		}

		/**
		 *
		 * @param folder
		 */
		public void setFolder(String folder) {
			this.folder = folder;
		}

		/**
		 *
		 * @return
		 */
		public String getObjectName() {
			return objectName;
		}

		/**
		 *
		 * @param objectName
		 */
		public void setObjectName(String objectName) {
			this.objectName = objectName;
		}

		/**
		 *
		 * @return
		 */
		public AmazonS3Object getS3Object() {
			return s3Object;
		}

		/**
		 *
		 * @param s3Object
		 */
		public void setS3Object(AmazonS3Object s3Object) {
			this.s3Object = s3Object;
		}
	}


	/**
	 *
	 * @return
	 */
	public static LastRemoveOperationCall getLastRemoveOperation() {
		return lastRemoveOperation;
	}

	/**
	 *
	 * @return
	 */
	public static LastPutOperationCall getLastPutOperation() {
		return lastPutOperation;
	}

	/**
	 *
	 * @return
	 */
	public static LastListOperationCall getLastListOperation() {
		return lastListOperation;
	}
}
