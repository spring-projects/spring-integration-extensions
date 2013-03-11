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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.integration.aws.core.AWSCredentials;
import org.springframework.integration.aws.core.AbstractAWSClientFactory;
import org.springframework.util.Assert;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.CanonicalGrantee;
import com.amazonaws.services.s3.model.EmailAddressGrantee;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.GroupGrantee;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.Permission;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerConfiguration;
import com.amazonaws.services.s3.transfer.Upload;


/**
 * The default, out of the box implementation of the {@link AmazonS3Operations} that is implemented
 * using AWS SDK.
 *
 * @author Amol Nayak
 *
 * @since 0.5
 *
 */
public class DefaultAmazonS3Operations extends AbstractAmazonS3Operations {

	private final AWSCredentials credentials;

	private AmazonS3Client client;

	private volatile TransferManager transferManager;	//Used to upload to S3

	private volatile ThreadPoolExecutor threadPoolExecutor;

	private volatile AbstractAWSClientFactory<AmazonS3Client> s3Factory;

	/**
	 * Constructor
	 * @param credentials
	 */
	public DefaultAmazonS3Operations(final AWSCredentials credentials) {
		super(credentials);
		this.credentials = credentials;
		s3Factory = new AbstractAWSClientFactory<AmazonS3Client>() {
			@Override
			protected AmazonS3Client getClientImplementation() {
				String accessKey = credentials.getAccessKey();
				String secretKey = credentials.getSecretKey();
				BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
				return new AmazonS3Client(credentials);
			}

		};
	}

	@Override
	protected void init() {

		client = s3Factory.getClient(getAwsEndpoint());

		if(threadPoolExecutor == null) {
			//Will use the Default Executor,
			//See com.amazonaws.services.s3.transfer.internal.TransferManagerUtils for more details
			transferManager = new TransferManager(client);
		}
		else {
			transferManager = new TransferManager(client, threadPoolExecutor);
		}

		//As per amazon it is recommended to use Multi part upload above 100 MB
		long multipartUploadThreshold = getMultipartUploadThreshold();
		if(multipartUploadThreshold > 0) {
			TransferManagerConfiguration config = new TransferManagerConfiguration();
			if(multipartUploadThreshold > Integer.MAX_VALUE) {
				config.setMultipartUploadThreshold(Integer.MAX_VALUE);		//2GB
			}
			else {
				config.setMultipartUploadThreshold((int)multipartUploadThreshold);
			}
			transferManager.setConfiguration(config);
		}
		//If none is set, we use the default
	}

	/**
	 * The implementation that uses the AWS SDK to list objects from the given bucket
	 *
	 * @param bucketName The bucket in which we want to list the objects in
	 * @param nextMarker The number of objects can be very large and this serves as the marker
	 * 					 for remembering the last record fetch in the last retrieve operation.
	 * @param pageSize The max number of records to be retrieved in one list object operation.
	 * @param prefix The prefix for the list operation, this can serve as the folder whose contents
	 * 				 are to be listed.
	 */
	@Override
	protected PaginatedObjectsView doListObjects(String bucketName,
			String nextMarker, int pageSize, String prefix) {

		ListObjectsRequest listObjectsRequest =
			new ListObjectsRequest()
			.withBucketName(bucketName)
			.withPrefix(prefix)
			.withMarker(nextMarker);

		if(pageSize > 0) {
			listObjectsRequest.withMaxKeys(pageSize);
		}

		ObjectListing listing = client.listObjects(listObjectsRequest);
		PaginatedObjectsView view = null;
		List<com.amazonaws.services.s3.model.S3ObjectSummary> summaries = listing.getObjectSummaries();
		if(summaries != null && !summaries.isEmpty()) {
			List<S3ObjectSummary> objectSummaries = new ArrayList<S3ObjectSummary>();
			for(final com.amazonaws.services.s3.model.S3ObjectSummary summary:summaries) {
				S3ObjectSummary summ = new S3ObjectSummary() {

					public long getSize() {
						return summary.getSize();
					}

					public Date getLastModified() {
						return summary.getLastModified();
					}

					public String getKey() {
						return summary.getKey();
					}

					public String getETag() {
						return summary.getETag();
					}

					public String getBucketName() {
						return summary.getBucketName();
					}
				};
				objectSummaries.add(summ);
			}
			view = new PagninatedObjectsViewImpl(objectSummaries,listing.getNextMarker());
		}
		return view;
	}

	/**
	 * Gets the object from the given bucket with the given key using the AWS SDK implementation
	 *
	 * @param bucketName
	 * @param key
	 * @return The Amazon S3 Object representing the Object in S3, may be null.
	 */
	@Override
	protected AmazonS3Object doGetObject(String bucketName, String key) {
		GetObjectRequest request = new GetObjectRequest(bucketName, key);
		S3Object s3Object;
		try {
			s3Object = client.getObject(request);
		} catch (AmazonS3Exception e) {
			if("NoSuchKey".equals(e.getErrorCode())) {
				//If the key is not found, return null rather than throwing the exception
				return null;
			}
			else {
				//throw the exception to caller in all other cases
				throw e;
			}
		}
		return new AmazonS3Object(s3Object.getObjectMetadata().getUserMetadata(),
				s3Object.getObjectMetadata().getRawMetadata(),
				s3Object.getObjectContent(),
				null);
	}

	/**
	 * The implementation puts the given {@link File} instance to the provided bucket against
	 * the given key.
	 *
	 * @param bucketName The bucket on S3 where this object is to be put
	 * @param key The key against which this Object is to be stored in S3
	 * @param file resource to be uploaded to S3
	 * @param objectACL the Object's Access controls for the object to be uploaded
	 * @param userMetadata The user's metadata to be associated with the object uploaded
	 * @param stringContentMD5 The MD5 sum of the contents of the file to be uploaded
	 */
	@Override
	public void doPut(String bucketName, String key, File file, AmazonS3ObjectACL objectACL,
						Map<String, String> userMetadata,String stringContentMD5) {

		ObjectMetadata metadata = new ObjectMetadata();
		PutObjectRequest request = new PutObjectRequest(bucketName, key, file);

		request.withMetadata(metadata);

		if(stringContentMD5 != null) {
			metadata.setContentMD5(stringContentMD5);
		}

		if(userMetadata != null) {
			metadata.setUserMetadata(userMetadata);
		}

		Upload upload;
		try {
			upload = transferManager.upload(request);
		} catch (Exception e) {
			throw new AmazonS3OperationException(
					credentials.getAccessKey(), bucketName,
					key,
					"Encountered Exception while invoking upload on multipart/single thread file, " +
					"see nested exceptions for more details",
					e);
		}
		//Wait till the upload completes, the call to putObject is synchronous
		try {
			if(logger.isInfoEnabled()) {
				logger.info("Waiting for Upload to complete");
			}
			upload.waitForCompletion();
			if(logger.isInfoEnabled()) {
				logger.info("Upload completed");
			}
		} catch (Exception e) {
			throw new AmazonS3OperationException(
					credentials.getAccessKey(), bucketName,
					key,
					"Encountered Exception while uploading the multipart/single thread file, " +
					"see nested exceptions for more details",
					e);
		}
		//Now since the object is present on S3, set the AccessControl list on it
		//Please note that it is not possible to set the object ACL with the
		//put object request, and hence both these operations cannot be atomic
		//it is possible the objects is uploaded and the ACl not set due to some
		//failure

		if(objectACL != null) {
			if(logger.isInfoEnabled()) {
				logger.info("Setting Access control list for key " + key);
			}
			try {
				client.setObjectAcl(bucketName, key,
						getAccessControlList(bucketName, key, objectACL));
			} catch (Exception e) {
				throw new AmazonS3OperationException(
						credentials.getAccessKey(), bucketName,
						key,
						"Encountered Exception while setting the Object ACL for key , " + key +
						"see nested exceptions for more details",
						e);
			}
			if(logger.isDebugEnabled()) {
				logger.debug("Successfully set the object ACL");
			}
		}
	}

	/**
	 * Gets the {@link AccessControlList} from the given {@link AmazonS3ObjectACL}
	 */
	private AccessControlList getAccessControlList(String bucketName,String key,AmazonS3ObjectACL acl) {
		AccessControlList accessControlList = null;
		if(acl != null) {
			if(!acl.getGrants().isEmpty()) {
				accessControlList = client.getObjectAcl(bucketName, key);
				for(ObjectGrant objGrant:acl.getGrants()) {
					Grantee grantee = objGrant.getGrantee();
					com.amazonaws.services.s3.model.Grantee awsGrantee;
					if(grantee.getGranteeType() == GranteeType.CANONICAL_GRANTEE_TYPE) {
						awsGrantee = new CanonicalGrantee(grantee.getIdentifier());
					}
					else if(grantee.getGranteeType() == GranteeType.EMAIL_GRANTEE_TYPE) {
						awsGrantee = new EmailAddressGrantee(grantee.getIdentifier());
					}
					else {
						awsGrantee = GroupGrantee.parseGroupGrantee(grantee.getIdentifier());
						if(awsGrantee == null) {
							logger.warn("Group grantee with identifier: \"" + grantee.getIdentifier() + "\" not found. skipping this grant");
							continue;
						}
					}
					ObjectPermissions perm = objGrant.getPermission();
					Permission permission;
					if(perm == ObjectPermissions.READ) {
						permission = Permission.Read;
					}
					else if(perm == ObjectPermissions.READ_ACP) {
						permission = Permission.ReadAcp;
					}
					else
						permission = Permission.WriteAcp;

					accessControlList.grantPermission(awsGrantee, permission);
				}
			}
		}
		return accessControlList;
	}


	/**
	 * Gets the thread pool executor that will be used to upload the object in multiparts
	 * concurrently
	 */
	public ThreadPoolExecutor getThreadPoolExecutor() {
		return threadPoolExecutor;
	}


	/**
	 * Used only when we upload the data using multi part upload. The thread pool will be used
	 * to upload the data concurrently
	 *
	 * @param threadPoolExecutor May not be null
	 */
	public void setThreadPoolExecutor(ThreadPoolExecutor threadPoolExecutor) {
		Assert.notNull(threadPoolExecutor, "'threadPoolExecutor' is null");
		this.threadPoolExecutor = threadPoolExecutor;
	}

}
