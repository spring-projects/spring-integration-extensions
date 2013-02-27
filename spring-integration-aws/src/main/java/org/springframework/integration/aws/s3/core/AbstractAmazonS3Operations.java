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

import static org.springframework.integration.aws.core.AWSCommonUtils.encodeHex;
import static org.springframework.integration.aws.core.AWSCommonUtils.getContentsMD5AsBytes;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.integration.aws.core.AWSCredentials;
import org.springframework.util.Assert;

/**
 * The common super class for any implementation of {@link AmazonS3Operations}. The sub class
 * has to implement all the functionality for performing the actual work to add, remove, update
 * delete, list the objects in an S3 bucket
 *
 * @author Amol Nayak
 *
 * @since 0.5
 *
 */
public abstract class AbstractAmazonS3Operations implements AmazonS3Operations,InitializingBean {

	protected final Log logger = LogFactory.getLog(getClass());

	private volatile long multipartUploadThreshold;

	private volatile File temporaryDirectory = new  File(System.getProperty("java.io.tmpdir"));

	private volatile String temporaryFileSuffix = ".writing";

	public final String PATH_SEPARATOR = "/";

	private final AWSCredentials credentials;

	private String awsEndpoint;



	/**
	 * The constructor that accepts the {@link AmazonWSCredentials}
	 * @param credentials
	 */
	protected AbstractAmazonS3Operations(AWSCredentials credentials) {
		Assert.notNull(credentials,"null 'credentials' provided");
		this.credentials = credentials;
	}


	/**
	 * Get the threshold value in bytes above which multi part upload will be used
	 * @return
	 */
	public long getMultipartUploadThreshold() {
		return multipartUploadThreshold;
	}


	/**
	 * The threshold value in bytes above which the service will use multi part upload.
	 * All the uploads below this value will be uploaded in a single thread
	 * Minimum value for the threshold is 5120 Bytes (5 KB).
	 * It is recommended by Amazon to use Multi part uploads for all the uploads
	 * above 100 MB
	 * If the value is set to a number above Integer.MAX_VALUE, the value will be
	 * set to  Integer.MAX_VALUE.
	 *
	 * @param multipartUploadThreshold
	 */
	public void setMultipartUploadThreshold(long multipartUploadThreshold) {
		Assert.isTrue(multipartUploadThreshold >= 5120,
				"Minimum threshold for multipart upload is 5120 bytes");
		this.multipartUploadThreshold = multipartUploadThreshold;
	}


	/**
	 * Gets the temporary directory
	 * @return
	 */
	public File getTemporaryDirectory() {
		return temporaryDirectory;
	}

	/**
	 * The temporary directory that will be used to write the files received over stream
	 * @param temporaryDirectory
	 */
	public void setTemporaryDirectory(File temporaryDirectory) {
		Assert.notNull(temporaryDirectory, "Provided temporaryDirectory is null");
		Assert.isTrue(temporaryDirectory.exists(),"The given temporary directory does not exist");
		Assert.isTrue(temporaryDirectory.isDirectory(), "The given temporary directory path has to be a directory");
		this.temporaryDirectory = temporaryDirectory;
	}


	/**
	 * The temporary directory that will be used to write the files received over stream
	 * @param temporaryDirectory
	 */
	public void setTemporaryDirectory(String temporaryDirectory) {
		Assert.hasText(temporaryDirectory,"Provided temporary directory string is null or empty string");
		setTemporaryDirectory(new File(temporaryDirectory));
	}


	/**
	 * Gets the temporary file suffix that is appended to the file while writing to
	 * the temporary directory
	 * @return
	 */
	public String getTemporaryFileSuffix() {
		return temporaryFileSuffix;
	}


	/**
	 * Gets the temporary file suffix
	 * @param temporaryFileSuffix
	 */
	public void setTemporaryFileSuffix(String temporaryFileSuffix) {
		Assert.hasText(temporaryFileSuffix, "The temporary file suffix must not be null or empty");
		if(!temporaryFileSuffix.startsWith(".")) {
			temporaryFileSuffix = "." + temporaryFileSuffix;
		}

		this.temporaryFileSuffix = temporaryFileSuffix;
	}

	/**
	 * Gets the AWS endpoint to use for all the operations, by default if none is set then us-east-1 is
	 * assumed.
	 *
	 * @return
	 */
	public String getAwsEndpoint() {
		return awsEndpoint;
	}


	/**
	 * Sets the valid AWS endpoint to be used by the client to connect to appropriate
	 * region to perform the operations
	 *
	 * @param awsEndpoint
	 */
	public void setAwsEndpoint(String awsEndpoint) {
		Assert.hasText(awsEndpoint, "Given AWS Endpoint has to be non null and non empty string");
		this.awsEndpoint = awsEndpoint;
	}


	/**
	 * The implemented afterPropertiesSet method
	 */
	public final void afterPropertiesSet() throws Exception {
		//TODO: protect by lock?
		init();
	}

	/**
	 * The subclass needs to override this method if it desires to perform any initializing
	 * of the class
	 */
	protected void init() {

	}

	/**
	 * Reads the stream provided and writes the file to the temp location
	 * @param in the Stream from which the data of the Object is to be read
	 * @param objectName the name of the object that would be used to upload the file
	 */
	private File getTempFile(InputStream in,String bucketName,String objectName) {
		InputStream inStream;
		if(!(in instanceof BufferedInputStream) && !(in instanceof ByteArrayInputStream)) {
			inStream = new BufferedInputStream(in);
		}
		else {
			inStream = in;
		}
		String fileName;
		if(objectName.contains(PATH_SEPARATOR)) {
			String[] splits = objectName.split(PATH_SEPARATOR);
			fileName = splits[splits.length - 1];
		}
		else {
			fileName = objectName;
		}
		File temporaryDirectory = getTemporaryDirectory();
		String temporaryFileSuffix = getTemporaryFileSuffix();

		String filePath = temporaryDirectory.getAbsoluteFile() + File.separator + fileName + temporaryFileSuffix;

		if(logger.isDebugEnabled()) {
			logger.debug("Temporary file path is " + filePath);
		}

		//Write data to temporary file
		File tempFile = new File(filePath);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(tempFile);
			byte[] bytes = new byte[1024];
			int read = 0;
			while(true) {
				read = inStream.read(bytes);
				if(read == -1) {
					break;
				}
				fos.write(bytes, 0, read);
			}
		} catch (FileNotFoundException e) {
			throw new AmazonS3OperationException(credentials.getAccessKey(),
					 bucketName,
					 objectName,
					 "Exception caught while writing the temporary file from input stream", e);
		} catch(IOException ioe) {
			throw new AmazonS3OperationException(credentials.getAccessKey(),
					 bucketName,
					 objectName,
					 "Exception caught while reading from the provided input stream", ioe);
		} finally {
			if(fos != null) {
				try {
					fos.flush();
					fos.close();
				} catch (IOException e) {
					//just log
					logger.error("Unable to close the stream to the temp file being created", e);
				}
				try {
					in.close();
				} catch (IOException e) {
					//just log
					logger.error("Unable to close the input stream source", e);
				}
			}
		}
		return tempFile;
	}


	/**
	 * The implementation of the core common operations that would be performed
	 * before and after the subclass does the actual work of putting the object to
	 * the given bucket
	 *
	 * @param bucketName The bucket to which the object is to be put
	 * @param folder The folder to which the object is to be uploaded
	 * @param objectName The name of the object in the given bucket
	 * @param s3Object The {@link AmazonS3Object} instance that represents the object
	 * 					to be uploaded.
	 */
	@Override
	public final void putObject(String bucketName, String folder, String objectName,
			AmazonS3Object s3Object) {
		Assert.hasText(bucketName,"null or empty bucketName provided");
		Assert.hasText(objectName,"null or empty object name provided");
		Assert.notNull(s3Object, "null s3 object provided for upload");


		File file = s3Object.getFileSource();
		InputStream in = s3Object.getInputStream();
		Assert.isTrue(file != null ^ in != null,
				"Exactly one of file or inpuut stream needed in the provided s3 object");

		boolean isTempFile = false;
		if(in != null) {
			//We don't know the source of the stream and hence we read the content
			//and write to the temporary file.
			file = getTempFile(in,bucketName,objectName);
			isTempFile = true;
		}

		String key = getKeyFromFolder(folder, objectName);

		//if the size of the file is greater than the threshold for multipart upload,
		//set the Content-MD5 header for this upload. This header will also come handy
		//later in inbound-channel-adapter where we cant find the MD5 sum of the
		//multipart upload file from its ETag

		String stringContentMD5 = null;
		try {
			stringContentMD5 =
				encodeHex(getContentsMD5AsBytes(file));
		} catch (UnsupportedEncodingException e) {
			logger.error("Exception while generating the content's MD5 of the file " + file.getAbsolutePath(), e);
		}

		try {
			doPut(bucketName, key, file, s3Object.getObjectACL(),
					s3Object.getUserMetaData(), stringContentMD5);
		} catch (Exception e) {
			throw new AmazonS3OperationException(
					credentials.getAccessKey(), bucketName,
					key,
					"Encountered exception while putting an object, see root cause for more details",
					e);

		}

		if(isTempFile) {
			//Delete the temp file
			if(logger.isDebugEnabled()) {
				logger.debug("Deleting temp file: " + file.getName());
			}
			boolean deleteSuccessful = file.delete();
			if(!deleteSuccessful) {
				logger.warn("Unable to delete file '" + file.getName() + "'");
			}
		}
	}


	/**
	 * The private method that takes the folder and the object name as the parameters and
	 * constructs the key that would be for the item to be retrieved or put in the bucket
	 *
	 * @param folder
	 * @param objectName
	 * @return
	 */
	private String getKeyFromFolder(String folder, String objectName) {
		if(objectName.startsWith(PATH_SEPARATOR)) {
			//remove the leading / of the object name
			objectName = objectName.substring(1);
		}
		String key;
		if(folder != null) {
			key = folder.endsWith(PATH_SEPARATOR)?
					folder + objectName:folder + PATH_SEPARATOR + objectName;
		}
		else {
			key = objectName;
		}

		//check if the foldername begins with a /, if yes, remove it as well as it created
		//one directory with blank name

		if(key.startsWith(PATH_SEPARATOR)) {
			key = key.substring(1);
		}
		return key;
	}

	/**
	 * Deletes the object with the given name from the provided bucket and folder.
	 *
	 */
	public boolean removeObject(String bucketName, String folder,
			String objectName) {
		throw new UnsupportedOperationException("Operation not et supported");
	}

	/**
	 * List the objects in a given bucket in the given folder.
	 *
	 * @param bucketName The bucket in which we want to list the objects in
	 * @param nextMarker The number of objects can be very large and this serves as the marker
	 * 					 for remembering the last record fetch in the last retrieve operation.
	 * @param pageSize The max number of records to be retrieved in one list object operation.
	 * @param prefix The prefix for the list operation, this can serve as the folder whose contents
	 * 				 are to be listed.
	 *
	 * @return
	 */
	public final PaginatedObjectsView listObjects(String bucketName, String folder, String nextMarker,int pageSize) {
		Assert.hasText(bucketName, "Bucket name should be non null and non empty");
		Assert.isTrue(pageSize >= 0, "Page size should be a non negative number");

		if(logger.isDebugEnabled()) {
			logger.debug("Listing objects from bucket " + bucketName + " and folder " + folder);
			logger.debug("Next marker is " + nextMarker  + " and pageSize is " + pageSize);
		}


		String prefix = null;
		if(folder != null && !PATH_SEPARATOR.equals(folder)) {
			prefix = folder;
		}
		//check if the prefix begins with /
		if(prefix != null && prefix.startsWith(PATH_SEPARATOR)) {
			prefix = prefix.substring(1);
		}

		return doListObjects(bucketName, nextMarker, pageSize, prefix);
	}

	/**
	 * Gets the object from the given bucket, folder and the name.
	 *
	 * @param bucketName The bucket from which to retrieve the object
	 * @param folder The folder name
	 * @param objectName The name of the object to retrieve
	 */
	public final AmazonS3Object getObject(String bucketName, String folder,
			String objectName) {
		Assert.hasText(bucketName, "Bucket name should be non null and non empty");
		if(logger.isDebugEnabled()) {
			logger.debug("Getting from bucket " + bucketName +
					", from folder " + folder + " the  object name " + objectName);
		}
		String key = getKeyFromFolder(folder, objectName);
		try {
			return doGetObject(bucketName, key);
		} catch (Exception e) {
			throw new AmazonS3OperationException(
					credentials.getAccessKey(), bucketName,
					key,
					"Encountered exception while putting an object, see root cause for more details",
					e);
		}
	}

	/**
	 * Gets the object from the given bucket with the given key using the AWS SDK implementation
	 *
	 * @param bucketName
	 * @param key
	 * @return
	 */
	protected abstract AmazonS3Object doGetObject(String bucketName, String key);


	/**
	 * The implementation should use the appropriate API to list objects from the given bucket
	 *
	 * @param bucketName The bucket in which we want to list the objects in
	 * @param nextMarker The number of objects can be very large and this serves as the marker
	 * 					 for remembering the last record fetch in the last retrieve operation.
	 * @param pageSize The max number of records to be retrieved in one list object operation.
	 * @param prefix The prefix for the list operation, this can serve as the folder whose contents
	 * 				 are to be listed.
	 * @return
	 */
	protected abstract PaginatedObjectsView doListObjects(String bucketName,
			String nextMarker, int pageSize, String prefix);


	/**
	 * The abstract method to be implemented by the subclass that would be doing the job
	 * of uploading the given file against the given key in the given bucket
	 *
	 * @param bucketName The bucket on S3 where this object is to be put
	 * @param key The key against which this Object is to be stored in S3
	 * @param file resource to be uploaded to S3
	 * @param objectACL the Object's Access controls for the object to be uploaded
	 * @param userMetadata The user's metadata to be associated with the object uploaded
	 * @param The MD5 sum of the contents of the file to be uploaded
	 *
	 */
	protected abstract void doPut(String bucket,String key,File file,
			AmazonS3ObjectACL objectACL, Map<String, String> userMetadata,String stringContentMD5);

}

class PagninatedObjectsViewImpl implements PaginatedObjectsView {

	private final List<S3ObjectSummary> objectSummary;
	private final String nextMarker;

	public PagninatedObjectsViewImpl(List<S3ObjectSummary> objectSummary,
			String nextMarker) {
		this.objectSummary = objectSummary;
		this.nextMarker = nextMarker;
	}


	public List<S3ObjectSummary> getObjectSummary() {
		return objectSummary != null?objectSummary:new ArrayList<S3ObjectSummary>();
	}


	public boolean hasMoreResults() {
		return nextMarker != null;
	}


	public String getNextMarker() {
		return nextMarker;
	}
}
