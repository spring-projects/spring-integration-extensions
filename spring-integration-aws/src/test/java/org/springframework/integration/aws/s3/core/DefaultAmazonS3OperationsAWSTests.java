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

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.integration.aws.core.PropertiesAWSCredentials;

/**
 * The test class for the {@link DefaultAmazonS3Operations}, the default implementation that
 * uses the AWS SDK to implement the functionality. The tests are present in the superclass
 * {@link AbstractAmazonS3OperationsImplAWSTests}
 *
 * Please note that that this test needs connectivity with the AWS S3 service
 * to be successfully executed. It is excluded from the maven's test execution by default
 *
 * To run this test you need to have your AWSAccess key and Secret key in the
 * file awscredentials.properties in the classpath. This file is not present in the
 * repository and you need to add one yourselves to src/test/resources folder and have
 * two properties accessKey and secretKey in it containing the access and the secret key
 *
 *
 * @author Amol Nayak
 *
 * @since 0.5
 *
 */
public class DefaultAmazonS3OperationsAWSTests extends AbstractAmazonS3OperationsImplAWSTests {


	private static DefaultAmazonS3Operations impl;

	@BeforeClass
	public static void setupS3Operations() throws Exception {
		PropertiesAWSCredentials credentials =
			new PropertiesAWSCredentials("classpath:awscredentials.properties");
		credentials.afterPropertiesSet();
		impl = new DefaultAmazonS3Operations(credentials);
	}
	/**
	 * Sets the thread pool executor to a non null value, execution should
	 * complete successfully
	 *
	 */
	@Test
	public void withNonNullThreadPoolExecutor() {
		ThreadPoolExecutor executor = (ThreadPoolExecutor)Executors.newFixedThreadPool(10);
		impl.setThreadPoolExecutor(executor);
		Assert.assertEquals(executor, impl.getThreadPoolExecutor());
	}

	/**
	 * Sets the thread pool executor to a null value, should throw an
	 * {@link IllegalArgumentException}
	 */
	@Test(expected=IllegalArgumentException.class)
	public void withNullThreadPoolExecutor() {
		impl.setThreadPoolExecutor(null);
	}

	@Override
	protected AbstractAmazonS3Operations getS3OperationsImplementation() {
		return impl;
	}
}
