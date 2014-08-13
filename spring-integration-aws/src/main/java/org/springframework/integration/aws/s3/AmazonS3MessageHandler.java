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

import static org.springframework.integration.aws.s3.AmazonS3OperationUtils.putObject;

import java.io.File;
import java.io.InputStream;

import org.springframework.expression.Expression;
import org.springframework.integration.Message;
import org.springframework.integration.aws.core.AWSCredentials;
import org.springframework.integration.aws.s3.core.AmazonS3Object;
import org.springframework.integration.aws.s3.core.AmazonS3Operations;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.integration.handler.ExpressionEvaluatingMessageProcessor;
import org.springframework.util.Assert;


/**
 * The Message handler for the S3 outbound channel adapter
 *
 * @author Amol Nayak
 *
 * @since 0.5
 *
 */
public class AmazonS3MessageHandler extends AbstractMessageHandler {


	private final AWSCredentials credentials;
	private volatile ExpressionEvaluatingMessageProcessor<String> remoteDirectoryProcessor;
	private final AmazonS3Operations s3Operations;
	private volatile String bucket;
	private volatile String charset = "UTF-8";
	private volatile FileNameGenerationStrategy fileNameGenerator = new DefaultFileNameGenerationStrategy();


	@Override
	protected void onInit() throws Exception {
		super.onInit();
		Assert.hasText(bucket,"Bucket not set'");
		Assert.notNull(remoteDirectoryProcessor, "Remote Directory processor should be present, set the remore directory expression");
	}


	/**
	 * The constructor that initializes {@link AmazonS3MessageHandler} with the provided
	 * implementation of {@link AmazonS3Operations} and using the provided {@link AWSCredentials}
	 *
	 * @param credentials
	 * @param operations
	 */
	public AmazonS3MessageHandler(AWSCredentials credentials,AmazonS3Operations s3Operations) {
		Assert.notNull(s3Operations,"s3 operations is null");
		Assert.notNull(credentials,"AWS Credentials are null");
		this.credentials = credentials;
		this.s3Operations = s3Operations;
	}


	/**
	 * The handler implementation for the Amazon S3 used to put objects in the remote AWS S3 bucket
	 * the message should contain a valid payload of type {@link File}, {@link InputStream},
	 * byte[] or {@link String}. Various predetermined headers as defined in {@link AmazonS3MessageHeaders}
	 * are extracted from the message and an {@link AmazonS3Object} is constructed that is provided to
	 * the {@link AmazonS3Operations} implementation to be uploaded in S3.
	 *
	 * @param message
	 */
	@Override
	protected void handleMessageInternal(Message<?> message) throws Exception {
		putObject(s3Operations,
					bucket,
					credentials.getAccessKey(),
					charset,
					remoteDirectoryProcessor,
					fileNameGenerator,
					message);
	}

	/**
	 * Sets the charset for the String payload received
	 * @param charset
	 */
	public void setCharset(String charset) {
		Assert.hasText(charset,"'charset' should be non null, non empty string");
		this.charset = charset;
	}


	/**
	 * Sets the S3 Bucket to which the files are to be uploaded
	 * @param bucket
	 */
	public void setBucket(String bucket) {
		Assert.hasText(bucket, "'bucket' should be non null, non empty string");
		this.bucket = bucket;
	}

	/**
	 * Sets the directory evaluating expression for finding the remote directory in S3
	 * @param expression
	 */
	public void setRemoteDirectoryExpression(Expression expression) {
		Assert.notNull(expression, "Remote directory expression is null");
		remoteDirectoryProcessor = new ExpressionEvaluatingMessageProcessor<String>(expression);
	}

	/**
	 * Sets the file name generation strategy
	 * @param fileNameGenerator
	 */
	public void setFileNameGenerator(FileNameGenerationStrategy fileNameGenerator) {
		Assert.notNull(fileNameGenerator,"File name generator is null");
		this.fileNameGenerator = fileNameGenerator;
	}
}
