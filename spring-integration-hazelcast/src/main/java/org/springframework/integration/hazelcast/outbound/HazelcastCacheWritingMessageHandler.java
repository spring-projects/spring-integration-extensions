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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.integration.hazelcast.common.HazelcastIntegrationDefinitionValidator;
import org.springframework.integration.hazelcast.common.HazelcastIntegrationUtils;
import org.springframework.messaging.Message;

/**
 * This listens defined channel, handles messages and write defined cache objects.
 * Currently it supports java.util.Map, List, Set and Queue data strctures.
 * 
 * @author Eren Avsarogullari
 * @since 1.0.0
 *
 */
public class HazelcastCacheWritingMessageHandler extends AbstractMessageHandler implements BeanPostProcessor, DisposableBean {

	private DistributedObject distributedObject;

	@Override
	protected void handleMessageInternal(Message<?> message) throws Exception {
		writeToCache(message);
	}

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

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		if (!HazelcastIntegrationDefinitionValidator.validateCacheType(this.distributedObject)) {
			throw new IllegalStateException(
					"Invalid 'cache' type is set. Only IMap, IList, ISet and IQueue cache objects are acceptable.");
		}

		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Override
	public void destroy() throws Exception {
		HazelcastIntegrationUtils.shutdownAllHazelcastInstances();
	}

	public void setDistributedObject(DistributedObject distributedObject) {
		this.distributedObject = distributedObject;
	}

}
