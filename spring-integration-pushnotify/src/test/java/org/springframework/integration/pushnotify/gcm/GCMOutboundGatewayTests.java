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
package org.springframework.integration.pushnotify.gcm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.integration.pushnotify.gcm.GCMPushNotifyServiceImpl.COLLAPSE_KEY;
import static org.springframework.integration.pushnotify.gcm.GCMPushNotifyServiceImpl.DELAY_WHILE_IDLE;
import static org.springframework.integration.pushnotify.gcm.GCMPushNotifyServiceImpl.TIME_TO_LIVE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.integration.MessagingException;
import org.springframework.integration.pushnotify.PushNotifyService;
import org.springframework.integration.pushnotify.gcm.outbound.GCMOutboundGateway;
import org.springframework.integration.support.MessageBuilder;

/**
 * The test cases for the {@link GCMOutboundGateway}
 *
 * @author Amol Nayak
 *
 * @since 1.0
 *
 */
public class GCMOutboundGatewayTests {

	private static PushNotifyService service;
	private static List<String> receiverIds = new ArrayList<String>();
	private static Map<String, String> payload;
	private static Map<String, String> attributes;
	private static GCMOutboundGateway gateway;

	@SuppressWarnings("unchecked")
	@BeforeClass
	public static void setup() throws IOException {
		service = mock(PushNotifyService.class);
		when(service.push(anyMap(), anyMap(), (String[])anyVararg()))
		.then(new Answer<GCMPushResponse>() {

			@SuppressWarnings("rawtypes")
			@Override
			public GCMPushResponse answer(InvocationOnMock invocation)
					throws Throwable {
				Object[] args = invocation.getArguments();
				GCMOutboundGatewayTests.payload = (Map)args[0];
				GCMOutboundGatewayTests.attributes = (Map)args[1];
				GCMOutboundGatewayTests.receiverIds.clear();
				for(int i = 2; i < args.length; i++) {
					GCMOutboundGatewayTests.receiverIds.add((String)args[i]);
				}
				return null;
			}
		});

		gateway = new GCMOutboundGateway(service);
		gateway.afterPropertiesSet();
	}

	/**
	 * Use a receiver id in the header that is String and with default expression.
	 */
	@Test
	public void withStringReceiverId() {
		gateway.handleMessage(MessageBuilder.withPayload("Test").setHeader("receiverIds", "RecId").build());
		assertEquals(1, receiverIds.size());
		assertEquals("RecId", receiverIds.get(0));
	}


	/**
	 * Use a receiver id in the header that is String[] and with default expression.
	 */
	@Test
	public void withStringReceiverIdArray() {
		gateway.handleMessage(MessageBuilder.withPayload("Test")
				.setHeader("receiverIds", new String[]{"RecId1", "RecId2"}).build());
		assertEquals(2, receiverIds.size());
		assertEquals("RecId1", receiverIds.get(0));
		assertEquals("RecId2", receiverIds.get(1));
	}

	/**
	 * Use a receiver id in the header that is List<String> and with default expression.
	 */
	@Test
	public void withStringReceiverIdList() {
		gateway.handleMessage(MessageBuilder.withPayload("Test")
				.setHeader("receiverIds", Arrays.asList(new String[]{"RecId1", "RecId2"})).build());
		assertEquals(2, receiverIds.size());
		assertEquals("RecId1", receiverIds.get(0));
		assertEquals("RecId2", receiverIds.get(1));
	}

	/**
	 * From a message with no receiver ids, should throw a {@link MessagingException}
	 */
	@Test(expected=MessagingException.class)
	public void withNoReceiverIds() {
		gateway.handleMessage(MessageBuilder.withPayload("Test").build());
	}

	/**
	 * From a message with no receiver ids, should throw a {@link MessagingException}
	 */
	@Test(expected=MessagingException.class)
	public void withEmptyCollectionOfReceiverIds() {
		gateway.handleMessage(MessageBuilder.withPayload("Test")
				.setHeader("receiverIds", Collections.EMPTY_LIST)
				.build());
	}

	/**
	 * From a message that has the receiver ids of incorrect type, java.lang.Integer in this case
	 *
	 */
	@Test(expected=MessagingException.class)
	public void withIncorrectTypeForReceiverId() {
		List<Object> list = new ArrayList<Object>();
		list.add(1);
		gateway.handleMessage(MessageBuilder.withPayload("Test")
				.setHeader("receiverIds", list)
				.build());
	}

	/**
	 * Sends a request with payload as a {@link Map}
	 */
	@Test
	public void withMapPayload() {
		gateway.handleMessage(MessageBuilder.withPayload(Collections.singletonMap("Data", "Test"))
				.setHeader("receiverIds", "RecId")
				.build());
		assertEquals(1, payload.size());
		assertEquals("Test", payload.get("Data"));
	}


	/**
	 * Sends a request with payload as a {@link Map}
	 */
	@Test
	public void withStringPayloadWithDefaultKey() {
		gateway.handleMessage(MessageBuilder.withPayload("Test")
				.setHeader("receiverIds", "RecId")
				.build());
		assertEquals(1, payload.size());
		assertEquals("Test", payload.get("Data"));
	}

	/**
	 * Sends a request with payload as a {@link Map}
	 */
	@Test
	public void withStringPayloadWithCustomKey() {
		gateway.setDefaultKey("DefaultKey");
		gateway.handleMessage(MessageBuilder.withPayload("Test")
				.setHeader("receiverIds", "RecId")
				.build());
		assertEquals(1, payload.size());
		assertEquals("Test", payload.get("DefaultKey"));
	}

	/**
	 * Sets the literal expression for the attributes to be sent to GCM
	 */
	@Test
	public void withAttributesAsLiteralExpressions() {
		gateway.setCollapseKeyExpression(new LiteralExpression("CollapseKey"));
		gateway.setDelayWhileIdleExpression(new LiteralExpression("true"));
		gateway.setTimeToLiveExpression(new LiteralExpression("12345"));
		gateway.handleMessage(MessageBuilder.withPayload("Test")
				.setHeader("receiverIds", "RecId")
				.build());
		assertNotNull(attributes);
		assertEquals("CollapseKey", attributes.get(COLLAPSE_KEY));
		assertEquals("true", attributes.get(DELAY_WHILE_IDLE));
		assertEquals("12345", attributes.get(TIME_TO_LIVE));
	}

	/**
	 * Sets the literal expression for the attributes to be sent to GCM
	 */
	@Test
	public void withAttributesAsSPELExpressions() {
		ExpressionParser parser = new SpelExpressionParser();
		gateway.setCollapseKeyExpression(parser.parseExpression("headers['ck']"));	//Not for Calvin Klein, but for collapse key
		gateway.setDelayWhileIdleExpression(new LiteralExpression("true"));
		gateway.setTimeToLiveExpression(parser.parseExpression("headers['ttl']"));
		gateway.handleMessage(MessageBuilder.withPayload("Test")
				.setHeader("receiverIds", "RecId")
				.setHeader("ck", "CollapseKey")
				.setHeader("ttl", 10000)
				.build());
		assertNotNull(attributes);
		assertEquals("CollapseKey", attributes.get(COLLAPSE_KEY));
		assertEquals("true", attributes.get(DELAY_WHILE_IDLE));
		assertEquals("10000", attributes.get(TIME_TO_LIVE));
	}
}
