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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.integration.aws.common.AWSTestUtils.md5Hash;
import static org.springframework.integration.aws.s3.AmazonS3OperationUtils.FILE_NAME;
import static org.springframework.integration.aws.s3.AmazonS3OperationUtils.HEADER_LIST_NEXT_MARKER;
import static org.springframework.integration.aws.s3.AmazonS3OperationUtils.HEADER_LIST_PAGE_SIZE;
import static org.springframework.integration.aws.s3.AmazonS3OperationUtils.HEADER_REMOVE_OBJECT_NAME;
import static org.springframework.integration.aws.s3.AmazonS3OperationUtils.METADATA;
import static org.springframework.integration.aws.s3.AmazonS3OperationUtils.OBJECT_ACLS;
import static org.springframework.integration.aws.s3.AmazonS3OperationUtils.USER_METADATA;
import static org.springframework.integration.aws.s3.AmazonS3OperationsMockingUtil.BUCKET;
import static org.springframework.integration.aws.s3.AmazonS3OperationsMockingUtil.getLastListOperation;
import static org.springframework.integration.aws.s3.AmazonS3OperationsMockingUtil.getLastPutOperation;
import static org.springframework.integration.aws.s3.AmazonS3OperationsMockingUtil.mockAmazonS3Operations;
import static org.springframework.integration.aws.s3.AmazonS3OutboundGateway.COMMAND_GET;
import static org.springframework.integration.aws.s3.AmazonS3OutboundGateway.COMMAND_LIST;
import static org.springframework.integration.aws.s3.AmazonS3OutboundGateway.COMMAND_PUT;
import static org.springframework.integration.aws.s3.AmazonS3OutboundGateway.COMMAND_REMOVE;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.expression.Expression;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.MessagingException;
import org.springframework.integration.aws.core.AWSCredentials;
import org.springframework.integration.aws.core.BasicAWSCredentials;
import org.springframework.integration.aws.s3.AmazonS3OperationsMockingUtil.LastListOperationCall;
import org.springframework.integration.aws.s3.AmazonS3OperationsMockingUtil.LastPutOperationCall;
import org.springframework.integration.aws.s3.AmazonS3OperationsMockingUtil.LastRemoveOperationCall;
import org.springframework.integration.aws.s3.core.AmazonS3Object;
import org.springframework.integration.aws.s3.core.AmazonS3Operations;
import org.springframework.integration.aws.s3.core.PaginatedObjectsView;
import org.springframework.integration.handler.ExpressionEvaluatingMessageProcessor;
import org.springframework.integration.support.MessageBuilder;

/**
 * The test class for {@link AmazonS3OutboundGateway}
 *
 * @author Amol Nayak
 * @since 0.5
 *
 */
public class AmazonS3OutboundGatewayTests {

	private static final AmazonS3Operations s3Operations = AmazonS3OperationsMockingUtil.mockS3Operations();
	private static final AWSCredentials credentials = new BasicAWSCredentials("ak", "sk");

	@BeforeClass
	public static void setup() {
		mockAmazonS3Operations(Arrays.asList(
				new String[]{"test.txt","test.txt",md5Hash("test.txt"),null},
				new String[]{"sub1/test.txt","sub1/test.txt",md5Hash("sub1/test.txt"),null},
				new String[]{"sub1/sub11/test.txt","sub1/sub11/test.txt",md5Hash("sub1/sub11/test.txt"),null},
				new String[]{"sub2/test.txt","sub2/test.txt",md5Hash("sub2/test.txt"),null}
			));
	}
	/**
	 *
	 */
	@Test(expected=IllegalArgumentException.class)
	public void withNullCredentials() {
		new AmazonS3OutboundGateway(null, s3Operations);
	}

	/**
	 *
	 */
	@Test(expected=IllegalArgumentException.class)
	public void withNullS3Operations() {
		new AmazonS3OutboundGateway(credentials,
					null);
	}


	/**
	 *
	 */
	@Test(expected=IllegalArgumentException.class)
	public void withNullBucketName() {
		AmazonS3OutboundGateway gateway = new AmazonS3OutboundGateway(credentials,
					s3Operations);
		gateway.setRemoteDirectoryExpression(new LiteralExpression("test"));
		gateway.setRemoteCommandExpression(new LiteralExpression("test"));
		gateway.afterPropertiesSet();
	}

	/**
	 *
	 */
	@Test(expected=IllegalArgumentException.class)
	public void withNullRemoteDirectoryProcessor() {
		AmazonS3OutboundGateway gateway = new AmazonS3OutboundGateway(credentials,s3Operations);
		gateway.setBucket(BUCKET);
		gateway.setRemoteCommandExpression(new LiteralExpression("test"));
		gateway.afterPropertiesSet();
	}

	/**
	 *
	 */
	@Test(expected=IllegalArgumentException.class)
	public void withBothRemoteDirectoryProcessorAndExpression() {
		AmazonS3OutboundGateway gateway = new AmazonS3OutboundGateway(credentials,s3Operations);
		gateway.setBucket(BUCKET);
		gateway.setRemoteCommandExpression(new LiteralExpression("test"));
		Expression expr = new LiteralExpression("/");
		gateway.setRemoteDirectoryExpression(expr);
		gateway.setRemoteDirectoryProcessor(new ExpressionEvaluatingMessageProcessor<String>(expr));
		gateway.afterPropertiesSet();
	}


	/**
	 *
	 */
	@Test(expected=IllegalArgumentException.class)
	public void withNullRemoteCommandProcessor() {
		AmazonS3OutboundGateway gateway = new AmazonS3OutboundGateway(credentials, s3Operations);
		gateway.setBucket(BUCKET);
		gateway.setRemoteDirectoryExpression(new LiteralExpression("test"));
		gateway.afterPropertiesSet();
	}

	/**
	 *
	 */
	@Test(expected=IllegalArgumentException.class)
	public void withBothRemoteCommandProcessorAndExpression() {
		AmazonS3OutboundGateway gateway = new AmazonS3OutboundGateway(credentials, s3Operations);
		gateway.setBucket(BUCKET);
		gateway.setRemoteDirectoryExpression(new LiteralExpression("test"));
		Expression expr = new LiteralExpression("put");
		gateway.setRemoteCommandExpression(expr);
		gateway.setRemoteCommandProcessor(new ExpressionEvaluatingMessageProcessor<String>(expr));
		gateway.afterPropertiesSet();
	}


	/**
	 *
	 */
	@Test
	public void withNullCommandExpression() {
		AmazonS3OutboundGateway gateway =
			new AmazonS3OutboundGateway(credentials, s3Operations);
		gateway.setBucket(BUCKET);
		gateway.setRemoteCommandExpression(new LiteralExpression(null));
		gateway.setRemoteDirectoryExpression(new LiteralExpression(null));
		gateway.afterPropertiesSet();
		try {
			gateway.handleMessage(MessageBuilder.withPayload(" ").build());
		} catch (MessagingException e) {
			assertEquals("Message evaluated to a null command", e.getMessage());
		}
	}

	/**
	 *
	 */
	@Test
	public void withInvalidCommand() {
		AmazonS3OutboundGateway gateway =
			new AmazonS3OutboundGateway(credentials, s3Operations);
		gateway.setBucket(BUCKET);
		gateway.setRemoteDirectoryExpression(new LiteralExpression(null));
		gateway.setRemoteCommandExpression(new LiteralExpression("unknown"));
		gateway.afterPropertiesSet();
		try {
			gateway.handleMessage(MessageBuilder.withPayload(" ").build());
		} catch (MessagingException e) {
			assertEquals("Unknown command unknown provided", e.getMessage());
		}
	}

	/**
	 *
	 */
	@Test
	public void withMissingObjectNameForRemove() {
		AmazonS3OutboundGateway gateway =
			new AmazonS3OutboundGateway(credentials, s3Operations);
		gateway.setBucket(BUCKET);
		gateway.setRemoteCommandExpression(new LiteralExpression(AmazonS3OutboundGateway.COMMAND_REMOVE));
		gateway.setRemoteDirectoryExpression(new LiteralExpression("/child"));
		gateway.afterPropertiesSet();
		try {
		gateway.handleMessage(MessageBuilder
				.withPayload("")
				.build());
		}catch (MessagingException e) {
			assertEquals("remove operation needs to specify the mandatory " +
					"header object_name in the request message", e.getMessage());
		}
	}

	/**
	 *
	 */
	@Test
	public void withValidRemoveInvocation() {
		AmazonS3OutboundGateway gateway =
			new AmazonS3OutboundGateway(credentials, s3Operations);
		gateway.setBucket(BUCKET);
		gateway.setRemoteDirectoryExpression(new LiteralExpression("/child"));
		gateway.setRemoteCommandExpression(new LiteralExpression(COMMAND_REMOVE));
		gateway.afterPropertiesSet();
		gateway.handleMessage(MessageBuilder
				.withPayload("")
				.setHeader(HEADER_REMOVE_OBJECT_NAME, "ObjectName.txt")
				.setReplyChannel(new MessageChannel() {
					public boolean send(Message<?> message, long timeout) {
						assertEquals(true, message.getPayload());
						return true;
					}
					public boolean send(Message<?> message) {
						return send(message, -1);
					}
				})
				.build());
		LastRemoveOperationCall lastRemove = AmazonS3OperationsMockingUtil.getLastRemoveOperation();
		assertEquals(BUCKET, lastRemove.getBucket());
		assertEquals("/child", lastRemove.getRemoteDirectory());
		assertEquals("ObjectName.txt", lastRemove.getObjectName());
	}

	/**
	 *
	 */
	@Test
	public void withValidInvocationOfPut() {
		AmazonS3OutboundGateway gateway =
			new AmazonS3OutboundGateway(credentials, s3Operations);
		gateway.setBucket(BUCKET);
		gateway.setRemoteDirectoryExpression(new LiteralExpression("/child"));
		gateway.setRemoteCommandExpression(new LiteralExpression(COMMAND_PUT));
		gateway.afterPropertiesSet();

		Map<String, Collection<String>> acls = new HashMap<String, Collection<String>>();
		acls.put("test@test.com", Arrays.asList("Read", "Write acp"));
		Message<String> message = MessageBuilder.withPayload("Test Content")
									.setHeader(FILE_NAME, "TestFileName.txt")
									.setHeader(USER_METADATA, Collections.singletonMap("UserMD", "UserMD"))
									.setHeader(METADATA, Collections.singletonMap("Metadata", "Metadata"))
									.setHeader(OBJECT_ACLS, acls)
									.setHeader("remoteDirectory", "/remote")
									.setReplyChannel(new MessageChannel() {
										public boolean send(Message<?> message, long timeout) {
											assertEquals(true, message.getPayload());
											return true;
										}
										public boolean send(Message<?> message) {
											return send(message, -1);
										}
									})
									.build();
		SpelExpressionParser parser = new SpelExpressionParser();
		gateway.setRemoteDirectoryExpression(parser.parseExpression("headers['remoteDirectory']"));
		gateway.handleMessage(message);
		LastPutOperationCall lastOperation = getLastPutOperation();
		Assert.assertEquals(BUCKET, lastOperation.getBucket());
		Assert.assertEquals("TestFileName.txt", lastOperation.getObjectName());
		Assert.assertEquals("/child", lastOperation.getFolder());
		AmazonS3Object object = lastOperation.getS3Object();
		Assert.assertNotNull(object);
		Assert.assertNotNull(object.getInputStream());
		Assert.assertNotNull(object.getMetaData());
		Assert.assertNotNull(object.getUserMetaData());
		Assert.assertNotNull(object.getObjectACL());
		Assert.assertEquals(2,object.getObjectACL().getGrants().size());
	}


	/**
	 *
	 */
	@Test
	public void withValidInvocationOfGetWithObjectNameAsPayload() {
		AmazonS3OutboundGateway gateway =
			new AmazonS3OutboundGateway(credentials, s3Operations);
		gateway.setBucket(BUCKET);
		gateway.setRemoteDirectoryExpression(new LiteralExpression("/sub2"));
		gateway.setRemoteCommandExpression(new LiteralExpression(COMMAND_GET));
		gateway.setRequiresReply(true);
		gateway.afterPropertiesSet();
		Message<String> message = MessageBuilder.withPayload("test.txt")
									.setReplyChannel(new MessageChannel() {
										public boolean send(Message<?> message, long timeout) {
											Object payload = message.getPayload();
											assertEquals(AmazonS3Object.class, payload.getClass());
											AmazonS3Object s3Object = (AmazonS3Object)payload;
											assertEquals(
													s3Object.getUserMetaData().get("Content-MD5"),
													"etBMewKnj4xZrT/xc8m/pw==");
											return true;
										}
										public boolean send(Message<?> message) {
											return send(message, -1);
										}
									})
									.build();
		gateway.handleMessage(message);
	}

	/**
	 *
	 */
	@Test
	public void withValidInvocationOfListWithDirectoryNameAsPayload() {
		AmazonS3OutboundGateway gateway =
			new AmazonS3OutboundGateway(credentials, s3Operations);
		gateway.setBucket(BUCKET);
		gateway.setRemoteDirectoryExpression(new LiteralExpression(null));
		gateway.setRemoteCommandExpression(new LiteralExpression(COMMAND_LIST));
		gateway.setRequiresReply(true);
		gateway.afterPropertiesSet();
		Message<String> message = MessageBuilder.withPayload("/sub2")
									.setHeader(HEADER_LIST_NEXT_MARKER, "nextMarker")
									.setHeader(HEADER_LIST_PAGE_SIZE, 50)
									.setReplyChannel(new MessageChannel() {
										public boolean send(Message<?> message, long timeout) {
											Object payload = message.getPayload();
											assertTrue(payload instanceof PaginatedObjectsView);
											PaginatedObjectsView paginatedView = (PaginatedObjectsView)payload;
											assertTrue(paginatedView.getObjectSummary().size() > 0);
											return true;
										}
										public boolean send(Message<?> message) {
											return send(message, -1);
										}
									})
									.build();
		gateway.handleMessage(message);
		LastListOperationCall lastListOperation = getLastListOperation();
		assertEquals(BUCKET, lastListOperation.getBucket());
		assertEquals("/sub2", lastListOperation.getRemoteDirectory());
		assertEquals("nextMarker", lastListOperation.getNextMarker());
		assertEquals(50, lastListOperation.getPageSize());
	}

}
