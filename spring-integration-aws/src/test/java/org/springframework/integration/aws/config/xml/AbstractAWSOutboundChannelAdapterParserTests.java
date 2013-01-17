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
package org.springframework.integration.aws.config.xml;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.aws.core.AWSCredentials;
import org.springframework.integration.core.MessageHandler;
import org.springframework.integration.endpoint.AbstractEndpoint;
import org.springframework.integration.test.util.TestUtils;

/**
 * The Abstract test class for the AWS outbound adapter parsers
 *
 * @author Amol Nayak
 *
 * @since 0.5
 *
 */
public abstract class AbstractAWSOutboundChannelAdapterParserTests<T extends MessageHandler> {

	protected static ClassPathXmlApplicationContext ctx;
	private static boolean isInitialized;

	/**
	 * The bean name expected to be present in the context which would be used when the
	 * element is defined with the propertiesFile attribute
	 */
	private final String CREDENTIALS_TEST_ONE = "credentialsTestOne";

	/**
	 * The bean name expected to be present in the context which would be used when the
	 * element is defined with the accessKey and the secretKey definition
	 */
	private final String CREDENTIALS_TEST_TWO = "credentialsTestTwo";


	@Before
	public void setup() {
		if(!isInitialized) {
			String contextLocation = getConfigFilePath();
			Assert.assertNotNull("Non null path value expected", contextLocation);
			ctx = new ClassPathXmlApplicationContext(contextLocation);
			Assert.assertTrue("Bean with id " + CREDENTIALS_TEST_ONE
					+ " expected to be present in the context for credentials test",
					ctx.containsBean(CREDENTIALS_TEST_ONE));
			Assert.assertTrue("Bean with id " + CREDENTIALS_TEST_TWO
					+ " expected to be present in the context for credentials test",
					ctx.containsBean(CREDENTIALS_TEST_TWO));
			isInitialized = true;

		}
	}

	@AfterClass
	public static void destroy() {
		ctx.close();
	}

	/**
	 * Gets the Message handler implementation for the  given bean id
	 * @param beanId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected T getMessageHandlerForBeanDefinition(String beanId) {
		AbstractEndpoint endpoint = (AbstractEndpoint)ctx.getBean(beanId);
		return (T)TestUtils.getPropertyValue(endpoint, "handler");
	}


	/**
	 * Gets the config file path that would be used to create the {@link ApplicationContext} instance
	 * sub class should implement this method to return a value of the config to be used to create the
	 * context
	 *
	 * @return
	 */
	protected abstract String getConfigFilePath();

	/**
	 * The subclass should return the {@link AmazonWSCredentials} instance that is being used
	 * to configure the adapters
	 *
	 * @return
	 */
	protected abstract AWSCredentials getCredentials();

	/**
	 * The test class for the  AWS Credentials test that used property files
	 */
	@Test
	public final void awsCredentialsTestWithPropFiles() {
		MessageHandler handler = getMessageHandlerForBeanDefinition(CREDENTIALS_TEST_ONE);
		AWSCredentials credentials = getCredentials();
		String accessKey = credentials.getAccessKey();
		String secretKey = credentials.getSecretKey();
		AWSCredentials configuredCredentials =
			TestUtils.getPropertyValue(handler, "credentials",AWSCredentials.class);
		Assert.assertEquals(accessKey, configuredCredentials.getAccessKey());
		Assert.assertEquals(secretKey, configuredCredentials.getSecretKey());
	}

	/**
	 * The test class for the  AWS Credentials test that used accessKey and secretKey elements
	 */
	@Test
	public final void awsCredentialsTestWithoutPropFiles() {
		MessageHandler handler = getMessageHandlerForBeanDefinition(CREDENTIALS_TEST_TWO);
		AWSCredentials credentials = getCredentials();
		String accessKey = credentials.getAccessKey();
		String secretKey = credentials.getSecretKey();
		AWSCredentials configuredCredentials =
			TestUtils.getPropertyValue(handler, "credentials",AWSCredentials.class);
		Assert.assertEquals(accessKey, configuredCredentials.getAccessKey());
		Assert.assertEquals(secretKey, configuredCredentials.getSecretKey());
	}
}
