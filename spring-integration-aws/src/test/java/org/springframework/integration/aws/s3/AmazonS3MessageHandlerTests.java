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

import static org.springframework.integration.aws.s3.AmazonS3OperationUtils.FILE_NAME;
import static org.springframework.integration.aws.s3.AmazonS3OperationUtils.METADATA;
import static org.springframework.integration.aws.s3.AmazonS3OperationUtils.OBJECT_ACLS;
import static org.springframework.integration.aws.s3.AmazonS3OperationUtils.USER_METADATA;
import static org.springframework.integration.aws.s3.AmazonS3OperationsMockingUtil.getLastPutOperation;
import static org.springframework.integration.aws.s3.AmazonS3OperationsMockingUtil.mockS3Operations;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.integration.Message;
import org.springframework.integration.MessageHandlingException;
import org.springframework.integration.aws.core.BasicAWSCredentials;
import org.springframework.integration.aws.s3.AmazonS3OperationsMockingUtil.LastPutOperationCall;
import org.springframework.integration.aws.s3.core.AmazonS3Object;
import org.springframework.integration.aws.s3.core.AmazonS3Operations;
import org.springframework.integration.support.MessageBuilder;

/**
 * The test class for {@link AmazonS3MessageHandler}, we rely on mock of {@link AmazonS3Operations}
 * to test the behavior.
 *
 * @author Amol Nayak
 *
 * @since 0.5
 *
 */
public class AmazonS3MessageHandlerTests {

	private static final AmazonS3Operations operations = mockS3Operations();

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	private AmazonS3MessageHandler getHandler() {
		AmazonS3MessageHandler handler = new AmazonS3MessageHandler(new BasicAWSCredentials(), operations);
		//set the remote directory to root by default
		handler.setRemoteDirectoryExpression(new LiteralExpression("/"));
		handler.setBucket("TestBucket");
		handler.afterPropertiesSet();
		return handler;
	}

	/**
	 * Tests with a message payload of type {@link String}
	 */
	@Test
	public void withStringPayload() {
		Message<String> message = MessageBuilder.withPayload("Test String").build();
		AmazonS3MessageHandler handler = getHandler();
		handler.handleMessage(message);
		LastPutOperationCall lastOperation = getLastPutOperation();
		AmazonS3Object object = lastOperation.getS3Object();
		Assert.assertNotNull(object.getInputStream());
		Assert.assertNull(object.getFileSource());
		assertCommonValues(message,object);
	}

	/**
	 * Tests with a message with payload of type {@link InputStream}
	 */
	@Test
	public void withInputStreamPayload() {
		InputStream bin = new ByteArrayInputStream("SomeString".getBytes());
		Message<InputStream> message = MessageBuilder.withPayload(bin).build();
		AmazonS3MessageHandler handler = getHandler();
		handler.handleMessage(message);
		LastPutOperationCall lastOperation = getLastPutOperation();
		AmazonS3Object object = lastOperation.getS3Object();
		Assert.assertNotNull(object.getInputStream());
		Assert.assertNull(object.getFileSource());
		assertCommonValues(message,object);
	}

	/**
	 * Tests with a message with payload of type byte[]
	 */
	@Test
	public void withByteArrayPayload() {
		Message<byte[]> message = MessageBuilder.withPayload("String".getBytes()).build();
		AmazonS3MessageHandler handler = getHandler();
		handler.handleMessage(message);
		LastPutOperationCall lastOperation = getLastPutOperation();
		AmazonS3Object object = lastOperation.getS3Object();
		Assert.assertNotNull(object.getInputStream());
		Assert.assertNull(object.getFileSource());
		assertCommonValues(message,object);
	}

	/**
	 * Tests with a message with payload of type {@link File} which is a file with temporary suffix
	 */
	@Test
	public void withTempFileTypePayload() throws Exception {
		final File file = tempFolder.newFile("TempFile.txt.writing");
		messageWithFileTypePayload(file);
	}

	/**
	 * Tests with a message with payload of type {@link File} which is a file without temporary suffix
	 */
	@Test
	public void withFileTypePayload() throws Exception {
		final File file = tempFolder.newFile("TempFile.txt");
		messageWithFileTypePayload(file);
	}

	/**
	 *Test case to with message of an incompatible type, {@link Integer} in this case.
	 *
	 */
	@Test(expected=MessageHandlingException.class)
	public void withIncompatiblePayload() {
		Message<Integer> message = MessageBuilder.withPayload(1).build();
		AmazonS3MessageHandler handler = getHandler();
		handler.handleMessage(message);
	}

	/**
	 * Tests with all the header provided in the message
	 */
	@Test
	public void withAllHeaders() {
		Map<String, Collection<String>> acls = new HashMap<String, Collection<String>>();
		acls.put("test@test.com", Arrays.asList("Read", "Write acp"));
		Message<String> message = MessageBuilder.withPayload("Test Content")
									.setHeader(FILE_NAME, "TestFileName.txt")
									.setHeader(USER_METADATA, Collections.singletonMap("UserMD", "UserMD"))
									.setHeader(METADATA, Collections.singletonMap("Metadata", "Metadata"))
									.setHeader(OBJECT_ACLS, acls)
									.setHeader("remoteDirectory", "/remote")
									.build();
		AmazonS3MessageHandler handler = getHandler();
		SpelExpressionParser parser = new SpelExpressionParser();
		handler.setRemoteDirectoryExpression(parser.parseExpression("headers['remoteDirectory']"));
		handler.handleMessage(message);
		LastPutOperationCall lastOperation = getLastPutOperation();
		Assert.assertEquals("TestBucket", lastOperation.getBucket());
		Assert.assertEquals("TestFileName.txt", lastOperation.getObjectName());
		Assert.assertEquals("/remote", lastOperation.getFolder());
		AmazonS3Object object = lastOperation.getS3Object();
		Assert.assertNotNull(object);
		Assert.assertNotNull(object.getInputStream());
		Assert.assertNotNull(object.getMetaData());
		Assert.assertNotNull(object.getUserMetaData());
		Assert.assertNotNull(object.getObjectACL());
		Assert.assertEquals(2,object.getObjectACL().getGrants().size());
	}

	/**
	 * The common method to test messages with payload of type {@link File}
	 * @param file
	 */
	private void messageWithFileTypePayload(File file) throws Exception {
		file.createNewFile();
		Message<File> message = MessageBuilder.withPayload(file).build();
		AmazonS3MessageHandler handler = getHandler();
		handler.handleMessage(message);
		LastPutOperationCall lastOperation = getLastPutOperation();
		AmazonS3Object object = lastOperation.getS3Object();
		Assert.assertEquals("TempFile.txt", lastOperation.getObjectName());
		Assert.assertNotNull(object.getFileSource());
		Assert.assertNull(object.getInputStream());
		Assert.assertNull(object.getMetaData());
		Assert.assertNull(object.getObjectACL());
		Assert.assertNull(object.getUserMetaData());
		file.delete();
	}


	/**
	 * The method used to assert the values for tests with String, InputStream and byte[] parameters
	 * @param message
	 */
	private void assertCommonValues(Message<?> message,AmazonS3Object object) {
		LastPutOperationCall lastOperation = getLastPutOperation();
		Assert.assertEquals(message.getHeaders().getId().toString() + ".ext", lastOperation.getObjectName());
		Assert.assertEquals("/", lastOperation.getFolder());
		Assert.assertEquals("TestBucket", lastOperation.getBucket());
		Assert.assertNotNull(object);
		Assert.assertNull(object.getMetaData());
		Assert.assertNull(object.getObjectACL());
		Assert.assertNull(object.getUserMetaData());
	}

}
