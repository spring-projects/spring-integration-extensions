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
package org.springframework.integration.pushnotify.gcm.outbound;

import static org.springframework.integration.pushnotify.gcm.GCMPushNotifyService.COLLAPSE_KEY;
import static org.springframework.integration.pushnotify.gcm.GCMPushNotifyService.DELAY_WHILE_IDLE;
import static org.springframework.integration.pushnotify.gcm.GCMPushNotifyService.TIME_TO_LIVE;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.core.convert.ConversionService;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.integration.Message;
import org.springframework.integration.MessagingException;
import org.springframework.integration.handler.AbstractReplyProducingMessageHandler;
import org.springframework.integration.pushnotify.PushNotifyService;
import org.springframework.integration.pushnotify.gcm.GCMPushNotifyService;
import org.springframework.integration.pushnotify.gcm.GCMPushResponse;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * The Outbound gateway class for sending the payload of the incoming message to the GCM push notification
 * service. The GCM push notify service accepts the messages in key value format. The message coming to the
 * adapter thus should have the Either of the following
 * 	1. A Payload of type java.util.Map with both the keys and values as Strings.
 *  2. A String payload, in which case it will be sent to the GCM Service with the key whose
 *  value is the {@link #defaultKey}
 *  3. Any Object that can be converted to {@link String} using Spring's conversion services
 *
 * @author Amol Nayak
 *
 * @since 1.0
 *
 */
public class GCMOutboundGateway extends AbstractReplyProducingMessageHandler {

	private static final String DEFAULT_KEY = "Data";

	private volatile String defaultKey;

	private final PushNotifyService service;

	private volatile Expression receiverIdsExpression;

	private volatile Expression collapseKeyExpression;

	private volatile Expression timeToLiveExpression;

	private volatile Expression delayWhileIdleExpression;

	private final StandardEvaluationContext context;

	private final String defaultReceiverIdsExpression = "headers['receiverIds']";



	/**
	 * The default constructor that instantiates the gateway with an instance of {@link GCMPushNotifyService}
	 *
	 * @param service
	 */
	public GCMOutboundGateway(PushNotifyService service) {
		Assert.notNull(service, "Provided 'service' is null");
		this.service = service;
		context = new StandardEvaluationContext();
	}

	@Override
	protected void onInit() {
		super.onInit();
		BeanFactory factory = getBeanFactory();
		if(factory != null) {
			context.setBeanResolver(new BeanFactoryResolver(factory));
		}
		if(receiverIdsExpression == null) {
			receiverIdsExpression = new SpelExpressionParser().parseExpression(defaultReceiverIdsExpression);
		}
		defaultKey = DEFAULT_KEY;
	}

	/* (non-Javadoc)
	 * @see org.springframework.integration.handler.AbstractReplyProducingMessageHandler#handleRequestMessage(org.springframework.integration.Message)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected Object handleRequestMessage(Message<?> requestMessage) {
		Object payload = requestMessage.getPayload();
		ConversionService conversionService = getConversionService();
		Map<String, Object> message;
		if(Map.class.isAssignableFrom(payload.getClass())) {
			message = (Map<String, Object>)payload;
		}
		else if(String.class.isAssignableFrom(payload.getClass())) {
			message = Collections.singletonMap(defaultKey, payload);
		}
		else if(conversionService.canConvert(payload.getClass(), String.class)) {
			message = Collections.singletonMap(defaultKey, (Object)conversionService.convert(payload, String.class));
		}
		else {
			throw new MessagingException("Only messages with payload of Map, String or an " +
					"object that can be converted to String are allowed");
		}
		String[] receiverIds = getReceiverIds(requestMessage);

		Map<String, String> attributes = getAttributes(requestMessage);
		GCMPushResponse response;
		try {
			response = (GCMPushResponse)service.push(message, attributes, receiverIds);
		} catch (IOException e) {
			throw new MessagingException(requestMessage, "Caught IOException while pushing to using GCM", e);
		}
		return response;
	}


	/**
	 * Sets the default key to be used in case a {@link Map} is not provided as the payload
	 * in the incoming message. This value is used as the key of the message that is sent out to the
	 * GCM service to be pushed to the Android device.
	 *
	 * @param defaultKey
	 */
	public void setDefaultKey(String defaultKey) {
		Assert.hasText(defaultKey, "Provided 'defaultKey' is either null or empty text");
		this.defaultKey = defaultKey;
	}

	/**
	 * The Expression that would be executed on the incoming message to get the receiver ids.
	 * The the permitted return values on expression evaluation are String, String[]
	 * or {@link Collection<String>}
	 *
	 * @param receiverIdsExpression
	 */
	public void setReceiverIdsExpression(Expression receiverIdsExpression) {
		Assert.notNull(receiverIdsExpression, "Provided 'receiverIdsExpression' is null");
		this.receiverIdsExpression = receiverIdsExpression;
	}

	/**
	 * Sets the collapse key expression to be used for the message that will be sent.
	 *
	 * @param collapseKeyExpression
	 */
	public void setCollapseKeyExpression(Expression collapseKeyExpression) {
		Assert.notNull(collapseKeyExpression, "'collapseKeyExpression' is null");
		this.collapseKeyExpression = collapseKeyExpression;
	}

	/**
	 * Sets the expression to find the time to live attribute.
	 * @param timeToLiveExpression
	 */
	public void setTimeToLiveExpression(Expression timeToLiveExpression) {
		Assert.notNull(timeToLiveExpression, "'timeToLiveExpression' is null");
		this.timeToLiveExpression = timeToLiveExpression;
	}

	/**
	 * Sets the expression for finding the value of the delay if idle attribute,
	 * A value that evaluates to 1 or true (case insensitive) will be assumed to be
	 * true.
	 *
	 * @param delayWhileIdleExpression
	 */
	public void setDelayWhileIdleExpression(Expression delayWhileIdleExpression) {
		Assert.notNull(delayWhileIdleExpression, "'delayWhileIdleExpression' is null");
		this.delayWhileIdleExpression = delayWhileIdleExpression;
	}

	/**
	 * Populates the attributes in the map by evaluating the expressions
	 *
	 * @param requestMessage
	 * @return
	 */
	private Map<String, String> getAttributes(Message<?> requestMessage) {
		Map<String, String> attributes = null;
		if(timeToLiveExpression != null
				|| collapseKeyExpression != null
				|| delayWhileIdleExpression != null) {

			attributes = new HashMap<String, String>();

			if(timeToLiveExpression != null) {
				String ttl = timeToLiveExpression.getValue(context, requestMessage, String.class);
				if(StringUtils.hasText(ttl)) {
					attributes.put(TIME_TO_LIVE, ttl);
				}
			}

			if(collapseKeyExpression != null) {
				String collapseKey = collapseKeyExpression.getValue(context, requestMessage, String.class);
				if(StringUtils.hasText(collapseKey)) {
					attributes.put(COLLAPSE_KEY, collapseKey);
				}
			}

			if(delayWhileIdleExpression != null) {
				String delayWhileIdle = delayWhileIdleExpression.getValue(context, requestMessage, String.class);
				if(StringUtils.hasText(delayWhileIdle)) {
					attributes.put(DELAY_WHILE_IDLE, delayWhileIdle);
				}
			}
		}

		return attributes != null && attributes.size() > 0 ? attributes : null;
	}
	/**
	 * Evaluates the {@link #receiverIdsExpression} on the message to get the receiver ids
	 * @param requestMessage
	 * @return
	 */
	private String[] getReceiverIds(Message<?> requestMessage) {
		Object executionValue = receiverIdsExpression.getValue(context, requestMessage);
		String[] receiverIds;
		if(executionValue == null) {
			throw new MessagingException(requestMessage, "Execution of receiverIdsExpression on message didn't yield any receiver ids");
		}
		if(executionValue instanceof String) {
			receiverIds = new String[]{(String)executionValue};
		}
		else if(executionValue instanceof String[]) {
			receiverIds = (String[])executionValue;
		}
		else if(executionValue instanceof Collection<?>) {
			@SuppressWarnings("rawtypes")
			Collection coll = (Collection)executionValue;
			if(coll.size() != 0) {
				receiverIds = new String[coll.size()];
				int index = 0;
				for(Object receiverId:coll) {
					if(receiverId instanceof String) {
						receiverIds[index++] = (String)receiverId;
					}
					else {
						throw new MessagingException(requestMessage, "The collection of receiverIds is expected to contain String values only, " +
								"found an object of type " + receiverId.getClass().getName());
					}
				}
			}
			else {
				receiverIds = new String[0];
			}
		}
		else {
			throw new MessagingException(requestMessage, "Only String, String[] or Collection<String> are the allowed values, " +
					"the expression evaluated to type " + executionValue.getClass().getName());
		}

		if(receiverIds.length == 0) {
			throw new MessagingException(requestMessage, "Execution of receiverIdsExpression on message didn't yield any receiver ids");
		}

		return receiverIds;
	}
}
