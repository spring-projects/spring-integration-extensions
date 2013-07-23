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

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.aws.s3.AmazonS3OutboundGateway;
import org.springframework.integration.core.MessageHandler;
import org.springframework.integration.endpoint.EventDrivenConsumer;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.integration.test.util.TestUtils.getPropertyValue;

/**
 * The test case for {@link AmazonS3OutboundGatewayParser}
 * @author Amol Nayak
 * @since 0.5
 *
 */
public class AmazonS3OutboundGatewayParserTests extends
		AmazonS3OutboundChannelAdapterParserTests {

	private final static Map<String, String> gatewayConfigMap  = new HashMap<String, String>();

	@BeforeClass
	public static void init() {
		gatewayConfigMap.put("withCustomOperations", "s3-valid-outbound-gateway.xml");
		gatewayConfigMap.put("withDefaultOperationsImplementation", "s3-valid-outbound-gateway.xml");
		gatewayConfigMap.put("withCustomNameGenerator", "s3-valid-outbound-gateway.xml");
		gatewayConfigMap.put("withCustomEndpoint", "s3-valid-outbound-gateway.xml");
		gatewayConfigMap.put("withMultiUploadLessthan5120", "s3-gateway-multiupload-lessthan-5120.xml");
		gatewayConfigMap.put("withBothFileGeneratorAndExpression", "s3-gateway-both-customfilegenerator-and-expression.xml");
		gatewayConfigMap.put("withCustomOperationsAndDisallowedAttributes", "s3-custom-operations-with-disallowed-attributes.xml");
		gatewayConfigMap.put("withRemoteCommand", "s3-valid-outbound-gateway.xml");
		gatewayConfigMap.put("withRemoteCommandExpression", "s3-valid-outbound-gateway.xml");
        gatewayConfigMap.put("withProvidedReplyTimeout", "s3-valid-outbound-gateway.xml");
        gatewayConfigMap.put("withProvidedReplyChannel", "s3-valid-outbound-gateway.xml");
	}

	/**
	 *
	 * @param identifier
	 * @return
	 */
	@Override
	protected String getConfigForIdentifier(String identifier) {
		return gatewayConfigMap.get(identifier);
	}

	/**
	 *
	 * @return
	 */
	@Override
	protected Class<? extends MessageHandler> getMessageHandlerClass() {
		return AmazonS3OutboundGateway.class;
	}


	/**
	 *
	 */
	@Test
	public void withRemoteCommand() {
		ClassPathXmlApplicationContext ctx =
			new ClassPathXmlApplicationContext(getConfigForIdentifier("withRemoteCommand"));
		EventDrivenConsumer consumer = ctx.getBean("withCustomService", EventDrivenConsumer.class);
		AmazonS3OutboundGateway gateway = getPropertyValue(consumer, "handler", AmazonS3OutboundGateway.class);
		assertNotNull(gateway);
		Expression expression =
			getPropertyValue(gateway, "remoteCommandProcessor.expression",Expression.class);
		assertNotNull(expression);
		assertEquals(LiteralExpression.class, expression.getClass());
		assertEquals("list", getPropertyValue(expression, "literalValue"));
	}


	/**
	 *
	 */
	@Test
	public void withRemoteCommandExpression() {
		ClassPathXmlApplicationContext ctx =
			new ClassPathXmlApplicationContext(getConfigForIdentifier("withRemoteCommandExpression"));
		EventDrivenConsumer consumer = ctx.getBean("withDefaultServices", EventDrivenConsumer.class);
		AmazonS3OutboundGateway gateway = getPropertyValue(consumer, "handler", AmazonS3OutboundGateway.class);
		assertNotNull(gateway);
		Expression expression =
			getPropertyValue(gateway, "remoteCommandProcessor.expression",Expression.class);
		assertNotNull(expression);
		assertEquals(SpelExpression.class, expression.getClass());
		assertEquals("headers['command']", getPropertyValue(expression, "expression"));
	}

    @Test
    public void withProvidedReplyTimeout() {
        ClassPathXmlApplicationContext ctx =
                new ClassPathXmlApplicationContext(getConfigForIdentifier("withRemoteCommandExpression"));
        EventDrivenConsumer consumer = ctx.getBean("withDefaultServices", EventDrivenConsumer.class);
        AmazonS3OutboundGateway gateway = getPropertyValue(consumer, "handler", AmazonS3OutboundGateway.class);
        assertEquals(1000, getPropertyValue(gateway, "messagingTemplate.sendTimeout", Long.class).intValue());
    }

    @Test
    public void withProvidedReplyChannel() {
        ClassPathXmlApplicationContext ctx =
                new ClassPathXmlApplicationContext(getConfigForIdentifier("withProvidedReplyChannel"));
        MessageChannel replyChannel = ctx.getBean("replyChannel", MessageChannel.class);
        EventDrivenConsumer consumer = ctx.getBean("withDefaultServices", EventDrivenConsumer.class);
        AmazonS3OutboundGateway gateway = getPropertyValue(consumer, "handler", AmazonS3OutboundGateway.class);
        assertEquals(replyChannel, getPropertyValue(gateway, "outputChannel", MessageChannel.class));
    }
}
