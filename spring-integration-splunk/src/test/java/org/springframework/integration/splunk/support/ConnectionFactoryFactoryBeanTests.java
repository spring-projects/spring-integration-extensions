/*
 * Copyright 2011-2012 the original author or authors.
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
package org.springframework.integration.splunk.support;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.integration.splunk.core.ConnectionFactory;
import org.springframework.integration.splunk.entity.SplunkServer;

import com.splunk.Service;

/**
 * @author Jarred Li
 * @since 1.0
 *
 */
public class ConnectionFactoryFactoryBeanTests {

	private ConnectionFactoryFactoryBean<Service> factoryBean;


	/**
	 * Test method for {@link org.springframework.integration.splunk.support.ConnectionFactoryFactoryBean#ConnectionFactoryFactoryBean(org.springframework.integration.splunk.core.ConnectionFactory, boolean)}.
	 * @throws Exception
	 */
	@Test
	public void testConnectionFactoryFactoryBean() throws Exception {
		SplunkServer server = new SplunkServer();
		SplunkConnectionFactory factory = new SplunkConnectionFactory(server);
		factoryBean = new ConnectionFactoryFactoryBean<Service>(factory, false);

		ConnectionFactory<Service> generatedByFactoryBean = factoryBean.getObject();
		Assert.assertTrue(generatedByFactoryBean instanceof SplunkConnectionFactory);

	}

	@Test
	public void testConnectionFactoryFactoryBean_withPoll() throws Exception {
		SplunkServer server = new SplunkServer();
		SplunkConnectionFactory factory = new SplunkConnectionFactory(server);
		factoryBean = new ConnectionFactoryFactoryBean<Service>(factory, true);

		ConnectionFactory<Service> generatedByFactoryBean = factoryBean.getObject();
		Assert.assertTrue(generatedByFactoryBean instanceof PoolingConnectionFactory);

	}


	/**
	 * Test method for {@link org.springframework.integration.splunk.support.ConnectionFactoryFactoryBean#getObjectType()}.
	 */
	@Test
	public void testGetObjectType() {
		SplunkServer server = new SplunkServer();
		SplunkConnectionFactory factory = new SplunkConnectionFactory(server);
		factoryBean = new ConnectionFactoryFactoryBean<Service>(factory, true);

		Class<?> clazz = factoryBean.getObjectType();
		Assert.assertEquals(PoolingConnectionFactory.class, clazz);
	}

	/**
	 * Test method for {@link org.springframework.integration.splunk.support.ConnectionFactoryFactoryBean#isSingleton()}.
	 */
	@Test
	public void testIsSingleton() {
		SplunkServer server = new SplunkServer();
		SplunkConnectionFactory factory = new SplunkConnectionFactory(server);
		factoryBean = new ConnectionFactoryFactoryBean<Service>(factory, false);

		Assert.assertTrue(factoryBean.isSingleton());
	}

}
