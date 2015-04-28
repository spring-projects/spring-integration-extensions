/*
 * Copyright 2015 the original author or authors.
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

package org.springframework.integration.hazelcast.outbound;

import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.integration.hazelcast.HazelcastHeaders;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;

import reactor.util.StringUtils;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.IList;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;
import com.hazelcast.core.ISet;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.MultiMap;
import com.hazelcast.core.ReplicatedMap;

/**
 * MessageHandler implementation that writes {@link Message} or payload to defined
 * Hazelcast distributed cache object.
 *
 * @author Eren Avsarogullari
 * @since 1.0.0
 */
public class HazelcastCacheWritingMessageHandler extends AbstractMessageHandler {

	private DistributedObject distributedObject;

	private String cacheExpression;

	private String keyExpression;

	private boolean extractPayload = true;

	public void setDistributedObject(DistributedObject distributedObject) {
		Assert.notNull(distributedObject, "'distributedObject' must not be null");
		this.distributedObject = distributedObject;
	}

	public void setCacheExpression(String cacheExpression) {
		Assert.notNull(cacheExpression, "'cacheExpression' must not be null");
		this.cacheExpression = cacheExpression;
	}

	public void setKeyExpression(String keyExpression) {
		Assert.notNull(keyExpression, "'keyExpression' must not be null");
		this.keyExpression = keyExpression;
	}

	public void setExtractPayload(boolean extractPayload) {
		this.extractPayload = extractPayload;
	}

	@Override
	protected void onInit() throws Exception {
		super.onInit();
	}

	@Override
	protected void handleMessageInternal(final Message<?> message) throws Exception {
		writeToCache(message);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void writeToCache(final Message<?> message) {
		DistributedObject distributedObject = getDistributedObject(message);
		if (distributedObject instanceof IMap) {
			((IMap) distributedObject).put(parseKeyExpression(message),
					getPayloadOrMessage(message));
		}
		else if (distributedObject instanceof MultiMap) {
			((MultiMap) distributedObject).put(parseKeyExpression(message),
					getPayloadOrMessage(message));
		}
		else if (distributedObject instanceof ReplicatedMap) {
			((ReplicatedMap) distributedObject).put(parseKeyExpression(message),
					getPayloadOrMessage(message));
		}
		else if (distributedObject instanceof IList) {
			((IList) distributedObject).add(getPayloadOrMessage(message));
		}
		else if (distributedObject instanceof ISet) {
			((ISet) distributedObject).add(getPayloadOrMessage(message));
		}
		else if (distributedObject instanceof IQueue) {
			((IQueue) distributedObject).add(getPayloadOrMessage(message));
		}
		else if (distributedObject instanceof ITopic) {
			((ITopic) distributedObject).publish(getPayloadOrMessage(message));
		}
	}

	private DistributedObject getDistributedObject(final Message<?> message) {
		if (this.distributedObject != null) {
			return this.distributedObject;
		}
		else if (StringUtils.hasText(this.cacheExpression)) {
			return parseCacheExpression(message);
		}
		else if (message.getHeaders().get(HazelcastHeaders.CACHE_NAME) != null) {
			return this.getBeanFactory().getBean(
					message.getHeaders().get(HazelcastHeaders.CACHE_NAME).toString(),
					DistributedObject.class);
		}
		else {
			throw new IllegalStateException("One of 'cache', 'cache-expression' and "
					+ HazelcastHeaders.CACHE_NAME
					+ " must be set for cache object definition.");
		}
	}

	private DistributedObject parseCacheExpression(final Message<?> message) {
		Object cacheName = parseExpression(this.cacheExpression, message);
		return this.getBeanFactory().getBean(cacheName.toString(),
				DistributedObject.class);
	}

	private Object parseKeyExpression(final Message<?> message) {
		if (StringUtils.hasText(this.keyExpression)) {
			return parseExpression(this.keyExpression, message);
		}
		else {
			throw new IllegalStateException(
					"'key-expression' must be set for IMap, MultiMap and ReplicatedMap");
		}
	}

	private Object parseExpression(final String expressionString, final Message<?> message) {
		Expression expression = new SpelExpressionParser()
				.parseExpression(expressionString);
		return expression.getValue(new StandardEvaluationContext(message));
	}

	private Object getPayloadOrMessage(final Message<?> message) {
		if (this.extractPayload) {
			return message.getPayload();
		}
		else {
			return message;
		}
	}

}
