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
package org.springframework.integration.aws.core;

import java.net.URI;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.springframework.integration.test.util.TestUtils;

import com.amazonaws.AmazonWebServiceClient;

/**
 * The abstract test class for Amazon WebService client factory tests
 * @author Amol Nayak
 *
 * @since 0.5
 *
 */
public abstract class AbstractAWSClientFactoryTests<T extends AmazonWebServiceClient> {

	protected AbstractAWSClientFactory<T> factory;

	@Before
	public final void setup() {
		 factory = getFactory();
	}

	/**
	 * The subclass is responsible for returning the appropriate factory implementation
	 * @return
	 */
	protected abstract AbstractAWSClientFactory<T> getFactory();

	/**
	 * Gets the endpoint for the service in US-EAST-1 region
	 * @return
	 */
	protected abstract String getUSEast1Endpoint();


	/**
	 * Gets the endpoint for the service in US-EAST-1 region
	 * @return
	 */
	protected abstract String getEUWest1Endpoint();

	/**
	 * Gets the Suffix for the endpoint URL which is service specific
	 * @return
	 */
	protected abstract String getSuffix();

	/**
	 * The test case for giving a US east endpoint without protocol
	 */
	@Test
	public void withUSEast1EndpointWithoutProtocol() {
		String usEast1 = getUSEast1Endpoint();
		factory.clear();
		T client = factory.getClient(usEast1 + getSuffix());
		Map<String, T> map = factory.getClientMap();
		Assert.assertNotNull(map);
		Assert.assertEquals(1, map.size());
		Assert.assertTrue("Expected one key with value https://" + usEast1, map.containsKey("https://" + usEast1));
		URI endpoint = TestUtils.getPropertyValue(client, "endpoint", URI.class);
		Assert.assertNotNull(endpoint);
		Assert.assertEquals(usEast1,endpoint.getHost());
		Assert.assertEquals("https",endpoint.getScheme());
	}

	/**
	 * Tests the factory by providing an endpoint in US east with protocol as http
	 */
	@Test
	public void withUSEast1EndpointWithProtocol() {
		String usEast1 = getUSEast1Endpoint();
		factory.clear();
		T client = factory.getClient("http://" + usEast1 + getSuffix());
		Map<String, T> map = factory.getClientMap();
		Assert.assertNotNull(map);
		Assert.assertEquals(1, map.size());
		Assert.assertTrue("Expected one key with value http://" + usEast1, map.containsKey("http://" + usEast1));
		URI endpoint = TestUtils.getPropertyValue(client, "endpoint", URI.class);
		Assert.assertNotNull(endpoint);
		Assert.assertEquals(usEast1,endpoint.getHost());
		Assert.assertEquals("http",endpoint.getScheme());
	}


	/**
	 * Calls the getClient multiple times to get the same client instance on each invocation
	 */
	@Test
	public void withMultipleCallsToSameEndpoint() {
		String usEast1 = getUSEast1Endpoint();
		factory.clear();
		T client = factory.getClient("https://" + usEast1 + getSuffix());
		Map<String, T> map = factory.getClientMap();
		Assert.assertNotNull(map);
		Assert.assertEquals(1, map.size());
		Assert.assertTrue("Expected one key with value http://" + usEast1, map.containsKey("https://" + usEast1));
		//default to https
		T client1 = factory.getClient(usEast1 + getSuffix());
		map = factory.getClientMap();
		Assert.assertEquals(1, map.size());
		Assert.assertTrue("Expected one key with value http://" + usEast1, map.containsKey("https://" + usEast1));
		Assert.assertTrue("Expecting to get the same instance of the client, but was not", client == client1);
	}


	/**
	 *Calls to different endpoints on the same client, expected to return a client with
	 *appropriate endpoint URI set
	 */
	@Test
	public void withMultipleCallsToDifferentEndpoints() {
		String usEast1 = getUSEast1Endpoint();
		String euWest1 = getEUWest1Endpoint();
		factory.clear();
		T client = factory.getClient(usEast1 + getSuffix());
		Map<String, T> map = factory.getClientMap();
		Assert.assertNotNull(map);
		Assert.assertEquals(1, map.size());
		Assert.assertTrue("Expected one key with value https://" + usEast1, map.containsKey("https://" + usEast1));
		URI endpoint = TestUtils.getPropertyValue(client, "endpoint", URI.class);
		Assert.assertNotNull(endpoint);
		Assert.assertEquals(usEast1,endpoint.getHost());
		Assert.assertEquals("https",endpoint.getScheme());
		client = factory.getClient(euWest1 + getSuffix());
		map = factory.getClientMap();
		Assert.assertNotNull(map);
		Assert.assertEquals(2, map.size());
		Assert.assertTrue("Expected one key with value https://" + euWest1, map.containsKey("https://" + euWest1));
		endpoint = TestUtils.getPropertyValue(client, "endpoint", URI.class);
		Assert.assertNotNull(endpoint);
		Assert.assertEquals(euWest1,endpoint.getHost());
		Assert.assertEquals("https",endpoint.getScheme());
	}
}
