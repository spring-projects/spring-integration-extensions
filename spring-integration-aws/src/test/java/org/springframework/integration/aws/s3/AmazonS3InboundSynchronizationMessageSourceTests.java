/*
 * Copyright 2002-2014 the original author or authors.
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
import static org.junit.Assert.assertNull;
import static org.springframework.integration.aws.common.AWSTestUtils.md5Hash;
import static org.springframework.integration.aws.s3.AmazonS3OperationsMockingUtil.mockAmazonS3Operations;
import static org.springframework.integration.aws.s3.AmazonS3OperationsMockingUtil.mockS3Operations;
import static org.springframework.integration.test.util.TestUtils.getPropertyValue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.integration.aws.core.BasicAWSCredentials;
import org.springframework.integration.aws.s3.core.AbstractAmazonS3Operations;
import org.springframework.integration.aws.s3.core.AmazonS3Object;
import org.springframework.integration.aws.s3.core.AmazonS3ObjectACL;
import org.springframework.integration.aws.s3.core.AmazonS3Operations;
import org.springframework.integration.aws.s3.core.PaginatedObjectsView;
import org.springframework.messaging.Message;

/**
 * The test class for {@link AmazonS3InboundSynchronizationMessageSource}
 *
 * @author Amol Nayak
 * @author Rob Harrop
 *
 * @since 0.5
 *
 */
public class AmazonS3InboundSynchronizationMessageSourceTests {

	@Rule
	public TemporaryFolder temp = new TemporaryFolder();

	private static AmazonS3Operations operations;


	@BeforeClass
	public static void setup() {
		operations = mockS3Operations();
	}


	/**
	 * Tests by providing null credentials.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void withNullCredentials() {
		AmazonS3InboundSynchronizationMessageSource src = new AmazonS3InboundSynchronizationMessageSource();
		src.setCredentials(null);
	}

	/**
	 * Tests by providing null temporary suffix
	 */
	@Test(expected = IllegalArgumentException.class)
	public void withNullTempSuffix() {
		AmazonS3InboundSynchronizationMessageSource src = new AmazonS3InboundSynchronizationMessageSource();
		src.setTemporarySuffix(null);
	}

	/**
	 * Tests by providing null wildcard
	 */
	@Test(expected = IllegalArgumentException.class)
	public void withNullWildcard() {
		AmazonS3InboundSynchronizationMessageSource src = new AmazonS3InboundSynchronizationMessageSource();
		src.setFileNameWildcard(null);
	}

	/**
	 * Tests by providing null regex
	 */
	@Test(expected = IllegalArgumentException.class)
	public void withNullRegex() {
		AmazonS3InboundSynchronizationMessageSource src = new AmazonS3InboundSynchronizationMessageSource();
		src.setFileNameRegex(null);
	}

	/**
	 * Tests by providing both regex and wildcard
	 */
	@Test(expected = IllegalArgumentException.class)
	public void withBothRegexAndWildcard() {
		AmazonS3InboundSynchronizationMessageSource src = new AmazonS3InboundSynchronizationMessageSource();
		src.setFileNameRegex("[a-z]+\\.txt");
		src.setFileNameWildcard("*.txt");
	}

	/**
	 * Tests by providing both wildcard and regex, unlike previous one, this sets the wildcard first
	 */
	@Test(expected = IllegalArgumentException.class)
	public void withBothWildcardAndRegex() {
		AmazonS3InboundSynchronizationMessageSource src = new AmazonS3InboundSynchronizationMessageSource();
		src.setFileNameWildcard("*.txt");
		src.setFileNameRegex("[a-z]+\\.txt");
	}


	/**
	 * Tests providing null remote directory
	 */
	@Test(expected = IllegalArgumentException.class)
	public void withNullRemoteDirectory() {
		AmazonS3InboundSynchronizationMessageSource src = new AmazonS3InboundSynchronizationMessageSource();
		src.setRemoteDirectory(null);
	}

	/**
	 *Tests with a non existent local directory
	 */
	@Test(expected = IllegalArgumentException.class)
	public void withNonExistentLocalDirectory() {
		AmazonS3InboundSynchronizationMessageSource src = new AmazonS3InboundSynchronizationMessageSource();
		src.setDirectory(new LiteralExpression("SomeNotExistentDir"));
		src.setBeanFactory(Mockito.mock(BeanFactory.class));
		src.afterPropertiesSet();
	}

	/**
	 * Tests with a {@link File} instance that is not a directory
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void withNonDirectory() throws IOException {
		File file = temp.newFile("SomeFile.txt");
		AmazonS3InboundSynchronizationMessageSource src = new AmazonS3InboundSynchronizationMessageSource();
		src.setDirectory(new LiteralExpression(file.getAbsolutePath()));
		src.setBeanFactory(Mockito.mock(BeanFactory.class));
		src.afterPropertiesSet();
	}

	/**
	 * Tests with a null s3 operation.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void withNullS3Operations() {
		AmazonS3InboundSynchronizationMessageSource src = new AmazonS3InboundSynchronizationMessageSource();
		src.setS3Operations(null);
	}

	/**
	 * Doesn't set the {@link AmazonS3Operations} instance and relies on the default one.
	 * sets the temp suffix and the thread pool executor.
	 */
	@Test
	public void withDefaultS3Service() {
		AmazonS3InboundSynchronizationMessageSource src = new AmazonS3InboundSynchronizationMessageSource();
		BasicAWSCredentials credentials = new BasicAWSCredentials("dummy", "dummy");
		src.setTemporarySuffix(".temp");
		src.setCredentials(credentials);
		src.setDirectory(new LiteralExpression(temp.getRoot().getAbsolutePath()));
		src.setBeanFactory(Mockito.mock(BeanFactory.class));
		src.afterPropertiesSet();
		assertEquals(".temp", getPropertyValue(src, "s3Operations.temporaryFileSuffix", String.class));
	}


	/**
	 * Instantiates with a custom implementation of {@link AmazonS3Operations}
	 * which extends from {@link AbstractAmazonS3Operations}. Also sets the following
	 * s3Operations, directory, fileNameRegex, remoteDirectory, maxObjectsPerBatch and temporarySuffix
	 * attributes.
	 */
	@Test
	public void withCustomS3Operations() {
		BasicAWSCredentials credentials = new BasicAWSCredentials("dummy", "dummy");
		AbstractAmazonS3Operations ops = new AbstractAmazonS3Operations(credentials) {

			@Override
			protected void doPut(String bucket, String key, File file,
					AmazonS3ObjectACL objectACL, Map<String, String> userMetadata,
					String stringContentMD5) {

			}

			@Override
			protected PaginatedObjectsView doListObjects(String bucketName,
					String nextMarker, int pageSize, String prefix) {
				return null;
			}

			@Override
			protected AmazonS3Object doGetObject(String bucketName, String key) {
				return null;
			}
		};

		AmazonS3InboundSynchronizationMessageSource src = new AmazonS3InboundSynchronizationMessageSource();
		src.setS3Operations(ops);
		src.setBucket("testbucket");
		src.setDirectory(new LiteralExpression(temp.getRoot().getAbsolutePath()));
		src.setFileNameRegex("[a-z]+\\.txt");
		src.setRemoteDirectory("remotedirectory");
		src.setMaxObjectsPerBatch(15);
		src.setTemporarySuffix(".temp");
		src.setAcceptSubFolders(true);
		src.setDirectory(new LiteralExpression(temp.getRoot().getAbsolutePath()));
		src.setBeanFactory(Mockito.mock(BeanFactory.class));
		src.afterPropertiesSet();

		assertEquals(ops, getPropertyValue(src, "s3Operations", AmazonS3Operations.class));
		assertEquals(".temp", getPropertyValue(src, "s3Operations.temporaryFileSuffix", String.class));
		assertEquals("testbucket", getPropertyValue(src, "bucket", String.class));
		assertEquals(temp.getRoot(), getPropertyValue(src, "directory"));
		assertEquals("[a-z]+\\.txt", getPropertyValue(src, "synchronizer.fileNameRegex", String.class));
		assertEquals(true, getPropertyValue(src, "synchronizer.acceptSubFolders", Boolean.class));
		assertEquals(15, getPropertyValue(src, "synchronizer.maxObjectsPerBatch", Integer.class).intValue());
		assertEquals("remotedirectory", getPropertyValue(src, "remoteDirectory", String.class));
	}

	/**
	 * Synchronizes the local directory to a remote bucket
	 */
	@Test
	public void synchronizeWithLocalDirectory() {
		mockAmazonS3Operations(Arrays.asList(
				new String[] {"test.txt", "test.txt", md5Hash("test.txt"), null},
				new String[] {"sub1/test.txt", "sub1/test.txt", md5Hash("sub1/test.txt"), null},
				new String[] {"sub1/sub11/test.txt", "sub1/sub11/test.txt", md5Hash("sub1/sub11/test.txt"), null},
				new String[] {"sub2/test.txt", "sub2/test.txt", md5Hash("sub2/test.txt"), null}
		));
		AmazonS3InboundSynchronizationMessageSource src = new AmazonS3InboundSynchronizationMessageSource();
		src.setS3Operations(operations);
		src.setBucket(AmazonS3OperationsMockingUtil.BUCKET);
		src.setDirectory(new LiteralExpression(temp.getRoot().getAbsolutePath()));
		src.setFileNameRegex("[a-z]+\\.txt");
		src.setRemoteDirectory("/sub1");
		src.setMaxObjectsPerBatch(15);
		src.setTemporarySuffix(".temp");
		src.setAcceptSubFolders(true);
		src.setDirectory(new LiteralExpression(temp.getRoot().getAbsolutePath()));
		src.setBeanFactory(Mockito.mock(BeanFactory.class));
		src.afterPropertiesSet();
		File file = src.receive().getPayload();
		assertEquals(temp.getRoot().getAbsoluteFile() +
				File.separator + "sub1" + File.separator + "test.txt", file.getAbsolutePath());
		file = src.receive().getPayload();
		assertEquals(temp.getRoot().getAbsoluteFile() +
				File.separator + "sub1" + File.separator + "sub11" + File.separator + "test.txt", file.getAbsolutePath());
		Message<File> message = src.receive();
		assertNull(message);
	}



}
