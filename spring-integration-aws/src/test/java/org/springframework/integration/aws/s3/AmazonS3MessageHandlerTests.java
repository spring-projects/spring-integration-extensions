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
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.integration.aws.s3.AmazonS3MessageHeaders.FILE_NAME;
import static org.springframework.integration.aws.s3.AmazonS3MessageHeaders.METADATA;
import static org.springframework.integration.aws.s3.AmazonS3MessageHeaders.OBJECT_ACLS;
import static org.springframework.integration.aws.s3.AmazonS3MessageHeaders.USER_METADATA;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.integration.Message;
import org.springframework.integration.MessageHandlingException;
import org.springframework.integration.aws.core.BasicAWSCredentials;
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

	private static AmazonS3Operations operations;
	private static PutObjectParameterHolder holder = new PutObjectParameterHolder();

	@BeforeClass
	public static void setup() {
		operations = Mockito.mock(AmazonS3Operations.class);

		doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock inv) {
				Object[] args = inv.getArguments();
				holder.setBucket((String)args[0]);
				holder.setFolder((String)args[1]);
				holder.setObjectName((String)args[2]);
				holder.setS3Object((AmazonS3Object)args[3]);
				return null;
			}
		}).
		when(operations)
		.putObject(anyString(), anyString(), anyString(), any(AmazonS3Object.class));


	}
	private AmazonS3MessageHandler getHandler() {
		AmazonS3MessageHandler handler = new AmazonS3MessageHandler(new BasicAWSCredentials(), operations);
		//set the remote directory to root by default
		handler.setRemoteDirectoryExpression(new LiteralExpression("/"));
		handler.setBucket("TestBucket");
		handler.afterPropertiesSet();
		return handler;
	}

	private static class PutObjectParameterHolder {
		private String bucket;
		private String folder;
		private String objectName;
		private AmazonS3Object s3Object;

		public String getBucket() {
			return bucket;
		}
		public void setBucket(String bucket) {
			this.bucket = bucket;
		}
		public String getFolder() {
			return folder;
		}
		public void setFolder(String folder) {
			this.folder = folder;
		}
		public String getObjectName() {
			return objectName;
		}
		public void setObjectName(String objectName) {
			this.objectName = objectName;
		}
		public AmazonS3Object getS3Object() {
			return s3Object;
		}
		public void setS3Object(AmazonS3Object s3Object) {
			this.s3Object = s3Object;
		}
	}

	/**
	 * Tests with a message payload of type {@link String}
	 */
	@Test
	public void withStringPayload() {
		Message<String> message = MessageBuilder.withPayload("Test String").build();
		AmazonS3MessageHandler handler = getHandler();
		handler.handleMessage(message);
		AmazonS3Object object = holder.getS3Object();
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
		AmazonS3Object object = holder.getS3Object();
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
		AmazonS3Object object = holder.getS3Object();
		Assert.assertNotNull(object.getInputStream());
		Assert.assertNull(object.getFileSource());
		assertCommonValues(message,object);
	}

	/**
	 * Tests with a message with payload of type {@link File} which is a file with temporary suffix
	 */
	@Test
	public void withTempFileTypePayload() throws Exception {
		File file = new File(System.getProperty("java.io.tmpdir") + "TempFile.txt.writing");
		messageWithFileTypePayload(file);
	}

	/**
	 * Tests with a message with payload of type {@link File} which is a file without temporary suffix
	 */
	@Test
	public void withFileTypePayload() throws Exception {
		File file = new File(System.getProperty("java.io.tmpdir") + "TempFile.txt");
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
		Assert.assertEquals("TestBucket", holder.getBucket());
		Assert.assertEquals("TestFileName.txt", holder.getObjectName());
		Assert.assertEquals("/remote", holder.getFolder());
		AmazonS3Object object = holder.getS3Object();
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
		AmazonS3Object object = holder.getS3Object();
		Assert.assertEquals("TempFile.txt", holder.getObjectName());
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
		Assert.assertEquals(message.getHeaders().getId().toString() + ".ext", holder.getObjectName());
		Assert.assertEquals("/", holder.getFolder());
		Assert.assertEquals("TestBucket", holder.getBucket());
		Assert.assertNotNull(object);
		Assert.assertNull(object.getMetaData());
		Assert.assertNull(object.getObjectACL());
		Assert.assertNull(object.getUserMetaData());
	}

}
