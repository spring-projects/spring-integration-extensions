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

import java.io.File;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.integration.Message;
import org.springframework.integration.aws.core.AWSCredentials;
import org.springframework.integration.aws.s3.core.AbstractAmazonS3Operations;
import org.springframework.integration.aws.s3.core.AmazonS3Operations;
import org.springframework.integration.aws.s3.core.DefaultAmazonS3Operations;
import org.springframework.integration.context.IntegrationObjectSupport;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * The message source used to receive the File instances stored on the local file system
 * synchronized from the S3
 *
 * @author Amol Nayak
 *
 * @since 0.5
 *
 */
public class AmazonS3InboundSynchronizationMessageSource extends
		IntegrationObjectSupport implements MessageSource<File>,FileEventHandler {


	private volatile InboundFileSynchronizer synchronizer;
	private volatile String bucket;
	private volatile String remoteDirectory;
	private volatile File directory;
	private volatile AWSCredentials credentials;
	private volatile String temporarySuffix = ".writing";
	private volatile int maxObjectsPerBatch;
	private volatile String fileNameWildcard;
	private volatile String fileNameRegex;
	private volatile BlockingQueue<File> filesQueue;
	private volatile AmazonS3Operations s3Operations;
	private volatile boolean acceptSubFolders;
	private volatile String awsEndpoint;
	//We will hard code the queue capacity here
	private final int QUEUE_CAPACITY = 1024;
	private volatile StandardEvaluationContext ctx;
	private volatile Expression directoryExpression;


	public Message<File> receive() {
		File headElement = filesQueue.poll();
		if(headElement == null) {
			synchronizer.synchronizeToLocalDirectory(directory, bucket, remoteDirectory);
			//Now check the queue again
			headElement = filesQueue.poll();
		}
		if(headElement != null) {
			return MessageBuilder.withPayload(headElement).build();
		}
		else {
			return null;
		}
	}


	@Override
	protected void onInit() throws Exception {
		Assert.notNull(directoryExpression, "Local directory to synchronize to is not set");

		ctx = new StandardEvaluationContext();
		BeanFactory factory = getBeanFactory();
		if(factory != null) {
			ctx.setBeanResolver(new BeanFactoryResolver(factory));
		}
		String directoryPath = directoryExpression.getValue(ctx,String.class);
		directory = new File(directoryPath);

		Assert.notNull(directory, "Please provide a valid local directory to synchronize the remote files");
//		TODO: Uncomment this once we start supporting auto-create-local-directory
//		Assert.isTrue(directory.exists(),
//				String.format("Provided directory %s does not exist", directoryPath));
		Assert.isTrue(directory.isDirectory(),
				String.format("Provided path %s is not a directory", directoryPath));

		//instantiate the S3Operations instance
		if(s3Operations == null) {
			s3Operations = new DefaultAmazonS3Operations(credentials);
		}

		if(AbstractAmazonS3Operations.class.isAssignableFrom(s3Operations.getClass())) {
			AbstractAmazonS3Operations abstractOperation = (AbstractAmazonS3Operations)s3Operations;
			abstractOperation.setTemporaryFileSuffix(temporarySuffix);
			if(StringUtils.hasText(awsEndpoint)) {
				abstractOperation.setAwsEndpoint(awsEndpoint);
			}
			abstractOperation.afterPropertiesSet();
		}

		//Now the file operations class
		InboundLocalFileOperationsImpl fileOperations = new InboundLocalFileOperationsImpl();
		fileOperations.setTemporaryFileSuffix(temporarySuffix);
		fileOperations.addEventListener(this);

		InboundFileSynchronizationImpl synchronizationImpl =
			new InboundFileSynchronizationImpl(s3Operations, fileOperations);
		synchronizationImpl.setSynchronizingBatchSize(maxObjectsPerBatch);
		if(StringUtils.hasText(fileNameWildcard)) {
			synchronizationImpl.setFileWildcard(fileNameWildcard);
		}
		if(StringUtils.hasText(fileNameRegex)) {
			synchronizationImpl.setFileNamePattern(fileNameRegex);
		}
		synchronizationImpl.setAcceptSubFolders(acceptSubFolders);
		synchronizationImpl.afterPropertiesSet();
		this.synchronizer = synchronizationImpl;

		filesQueue = new ArrayBlockingQueue<File>(QUEUE_CAPACITY);
	}

	//-- For Spring DI

	/**
	 * Sets the AWSCredential instance to be used
	 */
	public void setCredentials(AWSCredentials credentials) {
		Assert.notNull(credentials, "null 'credentials' provided");
		this.credentials = credentials;
	}


	/**
	 * The temporary suffix that would be used to indicate that the file is being writtem and the operation
	 * is not yet complete
	 *
	 * @param temporarySuffix
	 */
	public void setTemporarySuffix(String temporarySuffix) {
		Assert.hasText(temporarySuffix,"Provide a non null non empty string as temporary suffix");
		this.temporarySuffix = temporarySuffix;
	}

	/**
	 * The maximum number of objects those will be retrieved in one batch from Amazon S3 bucket
	 * as part of the listOperation
	 *
	 * @param maxObjectsPerBatch
	 */
	public void setMaxObjectsPerBatch(int maxObjectsPerBatch) {
		Assert.isTrue(maxObjectsPerBatch > 0, "Provide a non sero, non negative number for max objects per batch");
		this.maxObjectsPerBatch = maxObjectsPerBatch;
	}

	/**
	 * Sets the file's wildcard pattern that would be used to match the objects in S3 bucket
	 * This attribute is mutually exclusive to fileName regex.
	 *
	 * @param fileNameWildcard Must not be empty.
	 */
	public void setFileNameWildcard(String fileNameWildcard) {
		Assert.hasText(fileNameWildcard, "Provided file wildcard is null or empty string");
		Assert.isTrue(!StringUtils.hasText(fileNameRegex), "File name regex and wildcard are mutually exclusive");
		this.fileNameWildcard = fileNameWildcard;
	}

	/**
	 * Sets the regex to be used to match the objects in S3 bucket. This attribute is mutually exclusive
	 * to fileName regex.
	 *
	 * @param fileNameRegex
	 */
	public void setFileNameRegex(String fileNameRegex) {
		Assert.hasText(fileNameRegex, "Provided file regex is null or empty string");
		Assert.isTrue(!StringUtils.hasText(fileNameWildcard), "File name regex and wildcard are mutually exclusive");
		this.fileNameRegex = fileNameRegex;
	}

	/**
	 * Sets the  bucket with which the data in local directory is synchronized with.
	 *
	 * @param bucket
	 */
	public void setBucket(String bucket) {
		Assert.hasText(bucket, "Provided 'bucket' is null or empty string");
		this.bucket = bucket;
	}

	/**
	 * Sets the remote directory, this is the directory relative to the provided bucket
	 * in S3.
	 *
	 * @param remoteDirectory
	 */
	public void setRemoteDirectory(String remoteDirectory) {
		Assert.hasText(remoteDirectory, "Provided 'remoteDirectory' is null or empty string");
		this.remoteDirectory = remoteDirectory;
	}

	/**
	 * Sets the expression to find the local directory where the remote files are synchronized with.
	 *
	 * @param directoryExpression Must not be null
	 */
	public void setDirectory(Expression directoryExpression) {
		Assert.notNull(directoryExpression, "provided 'directoryExpression' is null");
		this.directoryExpression = directoryExpression;
	}

	/**
	 * Sets the {@link AmazonS3Operations} instance that would be used for the receiving
	 * the objects and listing the objects in the bucket.
	 *
	 * @param s3Operations
	 */
	public void setS3Operations(AmazonS3Operations s3Operations) {
		Assert.notNull(s3Operations, "null 's3Operations' instance provided");
		this.s3Operations = s3Operations;
	}

	/**
	 * Set to true if you want the subfolders of the given remote folder to be synchronized to the
	 * local directory.
	 *
	 * @param acceptSubFolders
	 */
	public void setAcceptSubFolders(boolean acceptSubFolders) {
		this.acceptSubFolders = acceptSubFolders;
	}

	/**
	 * The AWS region's endpoint whose bucket(and the subfolder if any) will be synchronized
	 * by this adapter
	 *
	 * @param awsEndpoint
	 */
	public void setAwsEndpoint(String awsEndpoint) {
		Assert.hasText(awsEndpoint, "Given AWS Endpoint has to be non null and non empty string");
		this.awsEndpoint = awsEndpoint;
	}


	//----





	public void onEvent(FileEvent event) {
		//We are interested in Create new file events only
		if(FileOperationType.CREATE.equals(event.getFileOperation())) {
			try {
				filesQueue.put(event.getFile());
				//The call hierarchy is
				//if, no file found in queue, then
				// receive()
				//	-> InboundFileSynchronizer.synchronizeToLocalDirectory()
				//	->InboundLocalFileOperations.writeToFile()
				//	->onEvent()
				//If the Queue is full and the thread blocks, the lock in synchronizeToLocalDirectory
				//stays and hence preventing further concurrent synchronization
			} catch (InterruptedException e) {
				logger.error("Interrupted while waiting to put the event on the filesQueue", e);
			}
		}
	}
}
