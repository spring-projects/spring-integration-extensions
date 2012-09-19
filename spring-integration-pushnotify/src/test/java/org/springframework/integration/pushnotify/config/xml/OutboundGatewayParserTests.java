/*
 * Copyright 2002-2012 the original author or authors.
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
package org.springframework.integration.pushnotify.config.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.integration.test.util.TestUtils.getPropertyValue;

import org.junit.Test;
import org.springframework.beans.factory.parsing.BeanDefinitionParsingException;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.integration.endpoint.EventDrivenConsumer;
import org.springframework.integration.pushnotify.PushNotifyService;
import org.springframework.integration.pushnotify.gcm.GCMPushNotifyServiceImpl;
import org.springframework.integration.pushnotify.gcm.outbound.GCMOutboundGateway;

/**
 * Test cases for parser definitions
 *
 * @author Amol Nayak
 *
 */
public class OutboundGatewayParserTests {

	@Test
	public void withDefaultService() {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("pushnotify-simple-valid-definition.xml");
		EventDrivenConsumer consumer = ctx.getBean("validOne", EventDrivenConsumer.class);
		GCMOutboundGateway gateway = getPropertyValue(consumer, "handler", GCMOutboundGateway.class);
		assertNotNull(gateway);
		PushNotifyService service = getPropertyValue(gateway, "service", PushNotifyService.class);
		assertNotNull(service);
		assertEquals(GCMPushNotifyServiceImpl.class, service.getClass());
		assertEquals("abc", getPropertyValue(service, "senderId", String.class));
		ctx.close();
	}

	@Test(expected=BeanDefinitionParsingException.class)
	public void withoutSenderId() {
		new ClassPathXmlApplicationContext("pushnotify-without-senderid.xml");
	}

	@Test(expected=BeanDefinitionParsingException.class)
	public void withBothSenderIdAndRef() {
		new ClassPathXmlApplicationContext("pushnotify-with-both-senderid-and-ref.xml");
	}

	@Test(expected=BeanDefinitionParsingException.class)
	public void withBothSenderIdAndServiceRef() {
		new ClassPathXmlApplicationContext("pushnotify-with-service-ref-and-senderid.xml");
	}

	@Test
	public void withServiceRef() {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("pushnotify-with-service-ref.xml");
		EventDrivenConsumer consumer = ctx.getBean("validOne", EventDrivenConsumer.class);
		GCMOutboundGateway gateway = getPropertyValue(consumer, "handler", GCMOutboundGateway.class);
		assertNotNull(gateway);
		PushNotifyService service = getPropertyValue(gateway, "service", PushNotifyService.class);
		assertNotNull(service);
		assertEquals(DummyPushNotifyService.class, service.getClass());
		ctx.close();
	}

	@Test
	public void withAllAttributes() {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("pushnotify-with-all-attrs.xml");
		EventDrivenConsumer consumer = ctx.getBean("validOne", EventDrivenConsumer.class);
		GCMOutboundGateway gateway = getPropertyValue(consumer, "handler", GCMOutboundGateway.class);
		assertNotNull(gateway);

		Expression expr = getPropertyValue(gateway, "delayWhileIdleExpression", Expression.class);
		assertNotNull(expr);
		assertEquals(SpelExpression.class,expr.getClass());
		assertEquals("headers['dwi']", getPropertyValue(expr, "expression"));

		expr = getPropertyValue(gateway, "collapseKeyExpression", Expression.class);
		assertNotNull(expr);
		assertEquals(LiteralExpression.class,expr.getClass());
		assertEquals("ColKey", getPropertyValue(expr, "literalValue"));

		expr = getPropertyValue(gateway, "timeToLiveExpression", Expression.class);
		assertNotNull(expr);
		assertEquals(LiteralExpression.class,expr.getClass());
		assertEquals("10000", getPropertyValue(expr, "literalValue"));

		ctx.close();
	}

	@Test(expected=BeanDefinitionParsingException.class)
	public void withBothBothLiteralAndExpressionAttribute() {
		new ClassPathXmlApplicationContext("pushnotify-with-literal-and-expr.xml");
	}

	@Test
	public void withLiteralReceiverId() {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("pushnotify-with-literal-receiverid.xml");
		EventDrivenConsumer consumer = ctx.getBean("validOne", EventDrivenConsumer.class);
		GCMOutboundGateway gateway = getPropertyValue(consumer, "handler", GCMOutboundGateway.class);
		assertNotNull(gateway);

		Expression expr = getPropertyValue(gateway, "receiverIdsExpression", Expression.class);
		assertNotNull(expr);
		assertEquals(LiteralExpression.class,expr.getClass());
		assertEquals("SomeRecId", getPropertyValue(expr, "literalValue"));
	}

	@Test
	public void withExpressionReceiverId() {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("pushnotify-with-receiverid-expr.xml");
		EventDrivenConsumer consumer = ctx.getBean("validOne", EventDrivenConsumer.class);
		GCMOutboundGateway gateway = getPropertyValue(consumer, "handler", GCMOutboundGateway.class);
		assertNotNull(gateway);

		Expression expr = getPropertyValue(gateway, "receiverIdsExpression", Expression.class);
		assertNotNull(expr);
		assertEquals(SpelExpression.class,expr.getClass());
		assertEquals("headers['recid']", getPropertyValue(expr, "expression"));
	}
}
