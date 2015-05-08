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

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.integration.expression.IntegrationEvaluationContextAware;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.integration.hazelcast.HazelcastHeaders;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;

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
public class HazelcastCacheWritingMessageHandler extends AbstractMessageHandler implements IntegrationEvaluationContextAware {

	private DistributedObject distributedObject;

	private Expression cacheExpression;

	private Expression keyExpression;

	private boolean extractPayload = true;

	private EvaluationContext evaluationContext;

	public void setDistributedObject(DistributedObject distributedObject) {
		Assert.notNull(distributedObject, "'distributedObject' must not be null");
		this.distributedObject = distributedObject;
	}

	public void setCacheExpression(Expression cacheExpression) {
		Assert.notNull(cacheExpression, "'cacheExpression' must not be null");
		this.cacheExpression = cacheExpression;
	}

	public void setKeyExpression(Expression keyExpression) {
		Assert.notNull(keyExpression, "'keyExpression' must not be null");
		this.keyExpression = keyExpression;
	}

	public void setExtractPayload(boolean extractPayload) {
		this.extractPayload = extractPayload;
	}

	@Override
	public void setIntegrationEvaluationContext(EvaluationContext evaluationContext) {
		this.evaluationContext = evaluationContext;
	}

	@Override
	protected void handleMessageInternal(final Message<?> message) throws Exception {
		writeToCache(message, getPayloadOrMessage(message));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void writeToCache(final Message<?> message, Object objectToStore) {
		DistributedObject distributedObject = getDistributedObject(message);
		if (distributedObject instanceof IMap) {
			if (objectToStore instanceof Map) {
				((IMap) distributedObject).putAll((Map) objectToStore);
			}
			else {
				((IMap) distributedObject).put(parseKeyExpression(message), objectToStore);
			}
		}
		else if (distributedObject instanceof MultiMap) {
			((MultiMap) distributedObject).put(parseKeyExpression(message), objectToStore);
		}
		else if (distributedObject instanceof ReplicatedMap) {
			if (objectToStore instanceof Map) {
				((ReplicatedMap) distributedObject).putAll((Map) objectToStore);
			}
			else {
				((ReplicatedMap) distributedObject).put(parseKeyExpression(message), objectToStore);
			}
		}
		else if (distributedObject instanceof ITopic) {
			((ITopic) distributedObject).publish(objectToStore);
		}
		else if (distributedObject instanceof IList) {
			if (objectToStore instanceof List) {
				((IList) distributedObject).addAll((List) objectToStore);
			}
			else {
				((IList) distributedObject).add(objectToStore);
			}
		}
		else if (distributedObject instanceof ISet) {
			if (objectToStore instanceof Set) {
				((ISet) distributedObject).addAll((Set) objectToStore);
			}
			else {
				((ISet) distributedObject).add(objectToStore);
			}
		}
		else if (distributedObject instanceof IQueue) {
			if (objectToStore instanceof Queue) {
				((IQueue) distributedObject).addAll((Queue) objectToStore);
			}
			else {
				((IQueue) distributedObject).add(objectToStore);
			}
		}
	}

	private DistributedObject getDistributedObject(final Message<?> message) {
		if (this.distributedObject != null) {
			return this.distributedObject;
		}
		else if (this.cacheExpression != null) {
			return this.cacheExpression.getValue(this.evaluationContext, message, DistributedObject.class);
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

	private Object parseKeyExpression(final Message<?> message) {
		if (this.keyExpression != null) {
			return this.keyExpression.getValue(message);
		}
		else {
			throw new IllegalStateException(
					"'key-expression' must be set for IMap, MultiMap and ReplicatedMap");
		}
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
