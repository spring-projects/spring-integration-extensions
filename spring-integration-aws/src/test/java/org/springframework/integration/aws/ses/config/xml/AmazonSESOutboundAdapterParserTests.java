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
package org.springframework.integration.aws.ses.config.xml;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.beans.factory.parsing.BeanDefinitionParsingException;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.aws.core.AWSCredentials;
import org.springframework.integration.core.MessageHandler;
import org.springframework.integration.endpoint.EventDrivenConsumer;
import org.springframework.integration.test.util.TestUtils;

/**
 * The test class for the AmazonSESOutboundAdapterParser
 *
 * @author Amol Nayak
 *
 * @since 1.0
 *
 */
public class AmazonSESOutboundAdapterParserTests {


	/**
	 * Tests the creation of context with a valid definition by specifying the credentials
	 * in a properties file
	 *
	 */
	@Test
	public void propFileValidOutboundAdapter() {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:ses-propfile-valid-test.xml");
		EventDrivenConsumer consumer = ctx.getBean("validDefinition",EventDrivenConsumer.class);
		assertValidDefinition(consumer);
		ctx.close();
	}

	/**
	 * Tests the creation of context with a valid definition by specifying the credentials
	 * as individual attributes of the xml
	 *
	 */
	@Test
	public void propsValidOutboundAdapter() {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:ses-props-valid-test.xml");
		EventDrivenConsumer consumer = ctx.getBean("validDefinition",EventDrivenConsumer.class);
		assertValidDefinition(consumer);
		ctx.close();
	}

	/**
	 * Tests the creation of context with a valid definition by specifying the credentials
	 * as individual attributes of the xml
	 *
	 */
	@Test
	public void refValidOutboundAdapter() {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:ses-cred-ref-valid-test.xml");
		EventDrivenConsumer consumer = ctx.getBean("validDefinition",EventDrivenConsumer.class);
		assertValidDefinition(consumer);
		ctx.close();
	}

	private void assertValidDefinition(EventDrivenConsumer consumer) {
		Assert.assertNotNull("Expected a non null EventDrivenConsumer", consumer);
		MessageHandler handler = TestUtils.getPropertyValue(consumer, "handler", MessageHandler.class);
		Assert.assertNotNull("Expected a non null messagehandler", handler);
		AWSCredentials credentials =  TestUtils.getPropertyValue(handler, "mailSender.credentials", AWSCredentials.class);
		Assert.assertNotNull("Expected a non null instance of credentials", credentials);
		Assert.assertEquals("dummy", credentials.getAccessKey());
		Assert.assertEquals("dummy", credentials.getSecretKey());
	}

	/**
	 * A Test case that tests by specifying both the aws credentials properties file
	 * and the individual properties to set the credentials
	 */
	@Test(expected=BeanDefinitionParsingException.class)
	public void invalidDefinitionWithBothPropsAndPropFile() {
		new ClassPathXmlApplicationContext("classpath:ses-both-awscred-props-property.xml");
	}
}
