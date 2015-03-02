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
package org.springframework.integration.hazelcast.inbound;

import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.integration.context.ApplicationContextStartEventHandler;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.integration.hazelcast.common.CacheListeningPolicyType;
import org.springframework.integration.hazelcast.common.HazelcastIntegrationDefinitionValidator;
import org.springframework.integration.hazelcast.common.HazelcastIntegrationUtil;
import org.springframework.messaging.Message;

import reactor.util.StringUtils;

import com.hazelcast.core.DistributedObject;

/**
 * HazelcastMessageProducer
 * 
 * @author Eren Avsarogullari
 * @since 1.0.0
 *
 */
public class HazelcastMessageProducer extends MessageProducerSupport implements BeanFactoryPostProcessor, DisposableBean {

	private DistributedObject distributedObject;
	private String cacheEventTypes;
	private CacheListeningPolicyType cacheListeningPolicy;
	
	@Override
	protected void onInit() {
		if(HazelcastIntegrationDefinitionValidator.validateCacheEventByDistributedObject(getDistributedObject(), getCacheEventTypeSet())) {
			super.onInit();
		}
	}

	public Set<String> getCacheEventTypeSet() {
		return StringUtils.commaDelimitedListToSet(getCacheEventTypes());
	}
	
	@Override
	public void sendMessage(Message<?> message) {
		super.sendMessage(message);
	}
	
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		if(!beanFactory.containsBean(ApplicationContextStartEventHandler.class.getName())) {
			beanFactory.registerSingleton(ApplicationContextStartEventHandler.class.getName(), new ApplicationContextStartEventHandler());
		}
	}
	
	@Override
	public void destroy() throws Exception {
		HazelcastIntegrationUtil.shutdownAllHazelcastInstances();
	}

	public DistributedObject getDistributedObject() {
		return distributedObject;
	}

	public void setDistributedObject(DistributedObject distributedObject) {
		this.distributedObject = distributedObject;
	}

	public String getCacheEventTypes() {
		return cacheEventTypes;
	}

	public void setCacheEventTypes(String cacheEventTypes) {
		this.cacheEventTypes = cacheEventTypes;
	}

	public CacheListeningPolicyType getCacheListeningPolicy() {
		return cacheListeningPolicy;
	}

	public void setCacheListeningPolicy(CacheListeningPolicyType cacheListeningPolicy) {
		this.cacheListeningPolicy = cacheListeningPolicy;
	}
	
}
