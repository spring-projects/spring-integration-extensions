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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.springframework.integration.test.util.TestUtils.getPropertyValue;

import java.net.URI;

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


	/**
	 * Test case for the xml definition with a custom implementation of {@link AmazonS3Operations}
	 *
	 */
	@Test
	public void withCustomOperations() {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:s3-valid-outbound-cases.xml");
		EventDrivenConsumer consumer = ctx.getBean("withCustomService",EventDrivenConsumer.class);
		AmazonS3MessageHandler handler = getPropertyValue(consumer, "handler", AmazonS3MessageHandler.class);
		assertEquals(AmazonS3DummyOperations.class, getPropertyValue(handler, "operations").getClass());
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
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:s3-valid-outbound-cases.xml");
		EventDrivenConsumer consumer = ctx.getBean("withDefaultServices",EventDrivenConsumer.class);
		AmazonS3MessageHandler handler = getPropertyValue(consumer, "handler", AmazonS3MessageHandler.class);
		assertEquals(DefaultAmazonS3Operations.class, getPropertyValue(handler, "operations").getClass());
		Expression expression =
			getPropertyValue(handler, "remoteDirectoryProcessor.expression",Expression.class);
		assertNotNull(expression);
		assertEquals(SpelExpression.class, expression.getClass());
		assertEquals("headers['remoteDirectory']", getPropertyValue(expression, "expression", String.class));
		assertEquals("TestBucket", getPropertyValue(handler, "bucket", String.class));
		assertEquals("US-ASCII", getPropertyValue(handler, "charset", String.class));
		assertEquals("dummy", getPropertyValue(handler, "credentials.accessKey", String.class));
		assertEquals("dummy", getPropertyValue(handler, "credentials.secretKey", String.class));
		assertEquals("dummy", getPropertyValue(handler, "operations.credentials.accessKey", String.class));
		assertEquals("dummy", getPropertyValue(handler, "operations.credentials.secretKey", String.class));
		assertEquals(5120, getPropertyValue(handler, "operations.multipartUploadThreshold", Long.class).longValue());
		assertEquals(".write", getPropertyValue(handler, "operations.temporaryFileSuffix", String.class));
		assertEquals(".write", getPropertyValue(handler, "fileNameGenerator.temporarySuffix", String.class));
		assertEquals("headers['name']", getPropertyValue(handler, "fileNameGenerator.fileNameExpression", String.class));
		assertEquals(ctx.getBean("executor"), getPropertyValue(handler, "operations.threadPoolExecutor"));
		ctx.destroy();
	}

	/**
	 * Test case for the xml definition with a custom implementation of {@link FileNameGenerationStrategy}
	 *
	 */
	@Test
	public void withCustomNameGenerator() {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("s3-valid-outbound-cases.xml");
		EventDrivenConsumer consumer = ctx.getBean("withCustomNameGenerator",EventDrivenConsumer.class);
		AmazonS3MessageHandler handler = getPropertyValue(consumer, "handler", AmazonS3MessageHandler.class);
		assertEquals(DummyFileNameGenerator.class, getPropertyValue(handler, "fileNameGenerator").getClass());
		ctx.destroy();
	}

	/**
	 * Test case for the xml definition with a custom AWS endpoint
	 *
	 */
	@Test
	public void withCustomEndpoint() {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("s3-valid-outbound-cases.xml");
		EventDrivenConsumer consumer = ctx.getBean("withCustomEndpoint",EventDrivenConsumer.class);
		AmazonS3MessageHandler handler = getPropertyValue(consumer, "handler", AmazonS3MessageHandler.class);
		assertEquals("http://s3-eu-west-1.amazonaws.com",
				getPropertyValue(handler, "operations.client.endpoint", URI.class).toString());
		ctx.destroy();
	}

	/**
	 * Multi part upload should have a size of 5120 and above, any value less than 5120 will
	 * thrown an exception
	 */
	@Test(expected=BeanCreationException.class)
	public void withMultiUploadLessthan5120() {
		new ClassPathXmlApplicationContext("s3-multiupload-lessthan-5120.xml");
	}

	/**
	 * Test with both the custom file generator and expression attribute set.
	 */
	@Test(expected=BeanDefinitionStoreException.class)
	public void withBothFileGeneratorAndExpression() {
		new ClassPathXmlApplicationContext("s3-both-customfilegenerator-and-expression.xml");
	}

	/**
	 * When custom implementation of {@link AmazonS3Operations} is provided, the attributes
	 * multipart-upload-threshold, temporary-directory, temporary-suffix and thread-pool-executor
	 * are not allowed
	 */
	@Test(expected=BeanDefinitionStoreException.class)
	public void withCustomOperationsAndDisallowedAttributes() {
		new ClassPathXmlApplicationContext("s3-custom-operations-with-disallowed-attributes.xml");
	}


	public static class DummyFileNameGenerator implements FileNameGenerationStrategy {

		@Override
		public String generateFileName(Message<?> message) {
			return null;
		}
	}
}
