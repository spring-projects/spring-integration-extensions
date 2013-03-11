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

package org.springframework.integration.aws.s3.config.xml;

import static org.junit.Assert.assertEquals;
import static org.springframework.integration.test.util.TestUtils.getPropertyValue;

import java.io.File;
import java.net.URI;

import org.junit.Test;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.aws.s3.AmazonS3InboundSynchronizationMessageSource;
import org.springframework.integration.aws.s3.core.AmazonS3Operations;
import org.springframework.integration.aws.s3.core.DefaultAmazonS3Operations;
import org.springframework.integration.endpoint.SourcePollingChannelAdapter;

/**
 * The test case class for S3 inbound channel adapter
 *
 * @author Amol Nayak
 *
 * @since 0.5
 *
 */
public class AmazonS3InboundChannelAdapterParserTests {


	/**
	 * Tests the inbound channel adapter definition with a valid combination of attributes
	 */
	@Test
	public void withValidAttributeValues() {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:s3-valid-inbound-cases.xml");
		SourcePollingChannelAdapter valid = ctx.getBean("validInbound", SourcePollingChannelAdapter.class);
		AmazonS3InboundSynchronizationMessageSource source = getPropertyValue(valid, "source", AmazonS3InboundSynchronizationMessageSource.class);
		assertEquals("TestBucket", getPropertyValue(source, "bucket"));
		assertEquals(".temp", getPropertyValue(source, "temporarySuffix"));
		assertEquals(new File(System.getProperty("java.io.tmpdir")), getPropertyValue(source, "directory"));
		assertEquals("remote", getPropertyValue(source, "remoteDirectory"));
		assertEquals(true, getPropertyValue(source, "acceptSubFolders", Boolean.class).booleanValue());
		assertEquals(100, getPropertyValue(source, "maxObjectsPerBatch", Integer.class).intValue());
		assertEquals("[A-Za-z0-9]+\\\\.txt", getPropertyValue(source, "fileNameRegex"));

		//test the second definition with custom attributes
		valid = ctx.getBean("validInboundWithCustomOps", SourcePollingChannelAdapter.class);
		source = getPropertyValue(valid, "source", AmazonS3InboundSynchronizationMessageSource.class);
		AmazonS3Operations s3Operations = getPropertyValue(source, "s3Operations", AmazonS3Operations.class);
		assertEquals(AmazonS3DummyOperations.class, s3Operations.getClass());

		//test with aws endpoint set
		valid = ctx.getBean("withAWSEndpoint", SourcePollingChannelAdapter.class);
		source = getPropertyValue(valid, "source", AmazonS3InboundSynchronizationMessageSource.class);
		s3Operations = getPropertyValue(source, "s3Operations", AmazonS3Operations.class);
		assertEquals(DefaultAmazonS3Operations.class, s3Operations.getClass());
		assertEquals("https://s3-eu-west-1.amazonaws.com", getPropertyValue(s3Operations, "client.endpoint", URI.class).toString());


		ctx.close();

	}

	/**
	 * Tests with a definition where none of directory and directory-expression attributes are provided
	 */
	@Test(expected=BeanDefinitionStoreException.class)
	public void withNoneOfDirectoryExprAndDirectory() {
		new ClassPathXmlApplicationContext("classpath:s3-with-none-of-direxpr-and-dir.xml");
	}
}
