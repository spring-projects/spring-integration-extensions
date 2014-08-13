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

import static org.springframework.integration.aws.s3.AmazonS3OperationUtils.getObject;
import static org.springframework.integration.aws.s3.AmazonS3OperationUtils.listObjects;
import static org.springframework.integration.aws.s3.AmazonS3OperationUtils.putObject;
import static org.springframework.integration.aws.s3.AmazonS3OperationUtils.removeObject;
import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

import org.springframework.expression.Expression;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.MessagingException;
import org.springframework.integration.aws.core.AWSCredentials;
import org.springframework.integration.aws.s3.core.AmazonS3Operations;
import org.springframework.integration.handler.AbstractReplyProducingMessageHandler;
import org.springframework.integration.handler.ExpressionEvaluatingMessageProcessor;
import org.springframework.integration.handler.MessageProcessor;
import org.springframework.util.StringUtils;

/**
 * The outbound gateway implementation for performing operations like upload an object,
 * delete an object, remove and object and list objects in a bucket.
 *
 * @author Amol Nayak
 * @since 0.5
 *
 */
public class AmazonS3OutboundGateway extends AbstractReplyProducingMessageHandler {

	private final AWSCredentials credentials;
	private volatile MessageProcessor<String> remoteDirectoryProcessor;
	private volatile MessageProcessor<String> remoteCommandProcessor;
	private final AmazonS3Operations s3Operations;
	private volatile String bucket;
	private volatile String charset = "UTF-8";
	private volatile FileNameGenerationStrategy fileNameGenerator = new DefaultFileNameGenerationStrategy();
	private volatile Expression remoteDirectoryExpression;
	private volatile Expression remoteCommandExpression;

	/**
	 * Command to be issued to list the contents of folder in the bucket
	 */
	public static final String COMMAND_LIST = "list";

	/**
	 * Command to be used to remove an object from the bucket
	 */
	public static final String COMMAND_REMOVE = "remove";

	/**
	 * Command issued to remove an object from the bucket
	 */
	public static final String COMMAND_PUT = "put";

	/**
	 * Command to be issued to get the contents of an object in the bucket
	 */
	public static final String COMMAND_GET = "get";


	public AmazonS3OutboundGateway(
			AWSCredentials credentials,
			AmazonS3Operations s3Operations) {
		notNull(credentials, "credentials must not be null");
		notNull(s3Operations, "s3Operations instance must not be null");
		this.credentials = credentials;
		this.s3Operations = s3Operations;
	}

	@Override
	protected void onInit() {
		hasText(bucket, "bucket name can not be null or empty string");
		notNull(remoteDirectoryExpression == null && remoteDirectoryProcessor == null,
				"both remote directory expression and processor can not be null");
		notNull(remoteCommandExpression == null && remoteCommandProcessor == null,
				"both remote command expression and processor can not be null");
		isTrue(remoteDirectoryExpression != null ^ remoteDirectoryProcessor != null,
				"can not specify both remore directory expression and processor");
		isTrue(remoteCommandExpression != null ^ remoteCommandProcessor != null,
				"can not specify both remote command expression and processor");
		if(remoteDirectoryExpression != null) {
			this.remoteDirectoryProcessor = new ExpressionEvaluatingMessageProcessor<String>(remoteDirectoryExpression);
		}
		if(remoteCommandExpression != null) {
			this.remoteCommandProcessor = new ExpressionEvaluatingMessageProcessor<String>(remoteCommandExpression);
		}
	}


	@Override
	protected Object handleRequestMessage(Message<?> requestMessage) {
		String command = remoteCommandProcessor.processMessage(requestMessage);
		if(!StringUtils.hasText(command)) {
			throw new MessagingException(requestMessage, "Message evaluated to a null command");
		}

		if(COMMAND_LIST.equalsIgnoreCase(command)) {
			return listObjects(s3Operations,
					bucket,
					credentials.getAccessKey(),
					remoteDirectoryProcessor,
					requestMessage);
		}
		else if(COMMAND_REMOVE.equalsIgnoreCase(command)) {
			removeObject(s3Operations,
							bucket,
							credentials.getAccessKey(),
							remoteDirectoryProcessor,
							requestMessage);
			return true;
		}
		else if(COMMAND_PUT.equalsIgnoreCase(command)) {
			try {
				putObject(s3Operations,
							bucket,
							credentials.getAccessKey(),
							charset,
							remoteDirectoryProcessor,
							fileNameGenerator,
							requestMessage);
				return true;
			} catch (Exception e) {
				logger.error("Exception while putting message to the bucket", e);
				throw new MessagingException(requestMessage, e);
			}
		}
		else if(COMMAND_GET.equalsIgnoreCase(command)) {
			return getObject(
					s3Operations,
					bucket,
					credentials.getAccessKey(),
					remoteDirectoryProcessor,
					requestMessage);
		}
		else {
			throw new MessagingException(requestMessage, "Unknown command " + command + " provided");
		}
	}

	/**
	 *
	 * @param fileNameGenerator
	 */
	public void setFileNameGenerator(FileNameGenerationStrategy fileNameGenerator) {
		this.fileNameGenerator = fileNameGenerator;
	}

	/**
	 *
	 * @param remoteDirectoryProcessor
	 */
	public void setRemoteDirectoryProcessor(
			MessageProcessor<String> remoteDirectoryProcessor) {
		this.remoteDirectoryProcessor = remoteDirectoryProcessor;
	}

	/**
	 *
	 * @param bucket
	 */
	public void setBucket(String bucket) {
		this.bucket = bucket;
	}

	/**
	 * The charset of the message bytes coming in as the payload. Relevant only for put operations
	 * ignored for other operations
	 *
	 * @param charset
	 */
	public void setCharset(String charset) {
		this.charset = charset;
	}

	/**
	 *
	 * @param remoteDirectoryExpression
	 */
	public void setRemoteDirectoryExpression(Expression remoteDirectoryExpression) {
		this.remoteDirectoryExpression = remoteDirectoryExpression;
	}


	/**
	 *
	 * @param remoteCommandExpression
	 */
	public void setRemoteCommandExpression(Expression remoteCommandExpression) {
		this.remoteCommandExpression = remoteCommandExpression;
	}

	/**
	 *
	 * @param remoteCommandProcessor
	 */
	public void setRemoteCommandProcessor(
			MessageProcessor<String> remoteCommandProcessor) {
		this.remoteCommandProcessor = remoteCommandProcessor;
	}

    /**
     * The channel on which the reply of the gateway is will be
     * @param replyChannel
     */
    public void setReplyChannel(MessageChannel replyChannel) {
       setOutputChannel(replyChannel);
    }
}
