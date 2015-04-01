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

import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.IList;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;
import com.hazelcast.core.ISet;

import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.integration.hazelcast.common.HazelcastIntegrationDefinitionValidator;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;

/**
 * MessageHandler implementation that writes {@link Message} payload to defined Hazelcast
 * distributed cache object. Currently, it supports {@link java.util.Map},
 * {@link java.util.List}, {@link java.util.Set} and {@link java.util.Queue} data
 * structures.
 *
 * @author Eren Avsarogullari
 * @since 1.0.0
 */
public class HazelcastCacheWritingMessageHandler extends AbstractMessageHandler {

	private final DistributedObject distributedObject;

	public HazelcastCacheWritingMessageHandler(DistributedObject distributedObject) {
		Assert.notNull(distributedObject, "cache must not be null");
		this.distributedObject = distributedObject;
	}

	@Override
	protected void onInit() throws Exception {
		super.onInit();
		HazelcastIntegrationDefinitionValidator.validateCacheTypeForCacheWritingMessageHandler(this.distributedObject);
	}

	@Override
	protected void handleMessageInternal(Message<?> message) throws Exception {
		writeToCache(message);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void writeToCache(Message<?> message) {
		if (this.distributedObject instanceof IMap) {
			((IMap<?, ?>) this.distributedObject).putAll((Map) message.getPayload());
		}
		else if (this.distributedObject instanceof IList) {
			((IList<?>) this.distributedObject).addAll((List) message.getPayload());
		}
		else if (this.distributedObject instanceof ISet) {
			((ISet<?>) this.distributedObject).addAll((Set) message.getPayload());
		}
		else if (this.distributedObject instanceof IQueue) {
			((IQueue<?>) this.distributedObject).addAll((Queue) message.getPayload());
		}
	}

}
