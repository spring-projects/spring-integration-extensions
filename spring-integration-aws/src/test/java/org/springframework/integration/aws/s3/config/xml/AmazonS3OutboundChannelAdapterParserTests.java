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
import static org.junit.Assert.assertNotNull;
import static org.springframework.integration.test.util.TestUtils.getPropertyValue;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.integration.Message;
import org.springframework.integration.aws.s3.AmazonS3MessageHandler;
import org.springframework.integration.aws.s3.FileNameGenerationStrategy;
import org.springframework.integration.aws.s3.core.AmazonS3Operations;
import org.springframework.integration.aws.s3.core.DefaultAmazonS3Operations;
import org.springframework.integration.core.MessageHandler;
import org.springframework.integration.endpoint.EventDrivenConsumer;

/**
 * The test case for the aws-s3 namespace's {@link AmazonS3OutboundChannelAdapterParser} class
 *
 * @author Amol Nayak
 *
 * @since 0.5
 *
 */
public class AmazonS3OutboundChannelAdapterParserTests {

	private static Map<String, String> adapterConfigMap = new HashMap<String, String>();

	@BeforeClass
	public static void init() {
		adapterConfigMap.put("withCustomOperations", "s3-valid-outbound-cases.xml");
		adapterConfigMap.put("withDefaultOperationsImplementation", "s3-valid-outbound-cases.xml");
		adapterConfigMap.put("withCustomNameGenerator", "s3-valid-outbound-cases.xml");
		adapterConfigMap.put("withCustomEndpoint", "s3-valid-outbound-cases.xml");
		adapterConfigMap.put("withMultiUploadLessthan5120", "s3-multiupload-lessthan-5120.xml");
		adapterConfigMap.put("withBothFileGeneratorAndExpression", "s3-both-customfilegenerator-and-expression.xml");
		adapterConfigMap.put("withCustomOperationsAndDisallowedAttributes", "s3-custom-operations-with-disallowed-attributes.xml");
	}

	/**
	 * Test case for the xml definition with a custom implementation of {@link AmazonS3Operations}
	 *
	 */
	@Test
	public void withCustomOperations() {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(getConfigForIdentifier("withCustomOperations"));
		EventDrivenConsumer consumer = ctx.getBean("withCustomService",EventDrivenConsumer.class);
		MessageHandler handler = getPropertyValue(consumer, "handler", getMessageHandlerClass());
		assertEquals(AmazonS3DummyOperations.class, getPropertyValue(handler, "s3Operations").getClass());
		Expression expression =
			getPropertyValue(handler, "remoteDirectoryProcessor.expression",Expression.class);
		assertNotNull(expression);
		assertEquals(LiteralExpression.class, expression.getClass());
		assertEquals("/", getPropertyValue(expression, "literalValue", String.class));
		ctx.destroy();
	}

	/**
	 * Test case for the xml definition with the default implementation of {@link AmazonS3Operations}
	 */
	@Test
	public void withDefaultOperationsImplementation() {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(getConfigForIdentifier("withDefaultOperationsImplementation"));
		EventDrivenConsumer consumer = ctx.getBean("withDefaultServices",EventDrivenConsumer.class);
		MessageHandler handler = getPropertyValue(consumer, "handler", getMessageHandlerClass());
		assertEquals(DefaultAmazonS3Operations.class, getPropertyValue(handler, "s3Operations").getClass());
		Expression expression =
			getPropertyValue(handler, "remoteDirectoryProcessor.expression",Expression.class);
		assertNotNull(expression);
		assertEquals(SpelExpression.class, expression.getClass());
		assertEquals("headers['remoteDirectory']", getPropertyValue(expression, "expression", String.class));
		assertEquals("TestBucket", getPropertyValue(handler, "bucket", String.class));
		assertEquals("US-ASCII", getPropertyValue(handler, "charset", String.class));
		assertEquals("dummy", getPropertyValue(handler, "credentials.accessKey", String.class));
		assertEquals("dummy", getPropertyValue(handler, "credentials.secretKey", String.class));
		assertEquals("dummy", getPropertyValue(handler, "s3Operations.credentials.accessKey", String.class));
		assertEquals("dummy", getPropertyValue(handler, "s3Operations.credentials.secretKey", String.class));
		assertEquals(5120, getPropertyValue(handler, "s3Operations.multipartUploadThreshold", Long.class).longValue());
		assertEquals(".write", getPropertyValue(handler, "s3Operations.temporaryFileSuffix", String.class));
		assertEquals(".write", getPropertyValue(handler, "fileNameGenerator.temporaryFileSuffix", String.class));
		assertEquals("headers['name']", getPropertyValue(handler, "fileNameGenerator.fileNameExpression", String.class));
		assertEquals(ctx.getBean("executor"), getPropertyValue(handler, "s3Operations.threadPoolExecutor"));
		ctx.destroy();
	}

	/**
	 * Test case for the xml definition with a custom implementation of {@link FileNameGenerationStrategy}
	 *
	 */
	@Test
	public void withCustomNameGenerator() {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(getConfigForIdentifier("withCustomNameGenerator"));
		EventDrivenConsumer consumer = ctx.getBean("withCustomNameGenerator",EventDrivenConsumer.class);
		MessageHandler handler = getPropertyValue(consumer, "handler", getMessageHandlerClass());
		assertEquals(DummyFileNameGenerator.class, getPropertyValue(handler, "fileNameGenerator").getClass());
		ctx.destroy();
	}

	/**
	 * Test case for the xml definition with a custom AWS endpoint
	 *
	 */
	@Test
	public void withCustomEndpoint() {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(getConfigForIdentifier("withCustomEndpoint"));
		EventDrivenConsumer consumer = ctx.getBean("withCustomEndpoint",EventDrivenConsumer.class);
		MessageHandler handler = getPropertyValue(consumer, "handler", getMessageHandlerClass());
		assertEquals("http://s3-eu-west-1.amazonaws.com",
				getPropertyValue(handler, "s3Operations.client.endpoint", URI.class).toString());
		ctx.destroy();
	}

	/**
	 * Multi part upload should have a size of 5120 and above, any value less than 5120 will
	 * thrown an exception
	 */
	@Test(expected=BeanCreationException.class)
	public void withMultiUploadLessthan5120() {
		new ClassPathXmlApplicationContext(getConfigForIdentifier("withMultiUploadLessthan5120"));
	}

	/**
	 * Test with both the custom file generator and expression attribute set.
	 */
	@Test(expected=BeanDefinitionStoreException.class)
	public void withBothFileGeneratorAndExpression() {
		new ClassPathXmlApplicationContext(getConfigForIdentifier("withBothFileGeneratorAndExpression"));
	}

	/**
	 * When custom implementation of {@link AmazonS3Operations} is provided, the attributes
	 * multipart-upload-threshold, temporary-directory, temporary-suffix and thread-pool-executor
	 * are not allowed
	 */
	@Test(expected=BeanDefinitionStoreException.class)
	public void withCustomOperationsAndDisallowedAttributes() {
		new ClassPathXmlApplicationContext(getConfigForIdentifier("withCustomOperationsAndDisallowedAttributes"));
	}


	public static class DummyFileNameGenerator implements FileNameGenerationStrategy {

		@Override
		public String generateFileName(Message<?> message) {
			return null;
		}
	}

	/**
	 *
	 * @return
	 */
	protected Class<? extends MessageHandler> getMessageHandlerClass() {
		return AmazonS3MessageHandler.class;
	}


	/**
	 *
	 * @param identifier
	 * @return
	 */
	protected String getConfigForIdentifier(String identifier) {
		return adapterConfigMap.get(identifier);
	}
}
