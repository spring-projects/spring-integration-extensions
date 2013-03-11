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
package org.springframework.integration.aws.common;

import org.junit.Assert;

import org.junit.Test;
import org.springframework.integration.aws.core.AbstractAWSClientFactory;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.sqs.AmazonSQSClient;

/**
 * The test class for {@link AbstractAmazonWSClientFactory}
 *
 * @author Amol Nayak
 *
 * @since 0.5
 *
 */
public class AWSClientFactoryTest {

	AbstractAWSClientFactory<AmazonSQSClient> factory = new AbstractAWSClientFactory<AmazonSQSClient>() {

		@Override
		protected AmazonSQSClient getClientImplementation() {
			return new AmazonSQSClient((AWSCredentials)null);
		}

	};

	@Test
	public void getRegionSpecificEndpoints() {
		//TODO: Use Spring integration test to inspect and assert the variables within
		AmazonSQSClient usEastClient = factory.getClient("https://queue.amazonaws.com/123456789012/MyTestQueue");
		Assert.assertNotNull(usEastClient);
		Assert.assertEquals(factory.getClientMap().size(),1);
		AmazonSQSClient usEastClient1 = factory.getClient("https://queue.amazonaws.com/123456789012/MyTestQueue1");
		Assert.assertNotNull(usEastClient1);
		Assert.assertEquals(usEastClient,usEastClient1);
		Assert.assertEquals(factory.getClientMap().size(),1);
		AmazonSQSClient apacClient1 = factory.getClient("https://ap-southeast-1.queue.amazonaws.com/123456789012/MyApacTestQueue");
		Assert.assertNotNull(apacClient1);
		Assert.assertEquals(factory.getClientMap().size(),2);

	}
}
