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
package org.springframework.integration.hazelcast.listener;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Set;

import org.springframework.integration.context.ApplicationContextStartEventHandler;
import org.springframework.integration.hazelcast.inbound.HazelcastMessageProducer;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MultiMap;

/**
 * AbstractHazelcastEventListener
 * 
 * @author Eren Avsarogullari
 * @since 1.0.0
 *
 */
public abstract class AbstractHazelcastEventListener {

	private HazelcastMessageProducer hazelcastInboundChannelAdapter;
	
	public AbstractHazelcastEventListener(HazelcastMessageProducer hazelcastInboundChannelAdapter) {
		this.hazelcastInboundChannelAdapter = hazelcastInboundChannelAdapter;
	}

	public HazelcastMessageProducer getHazelcastInboundChannelAdapter() {
		return hazelcastInboundChannelAdapter;
	}

	protected abstract void processEvent(EventObject event);
	
	protected Set<SocketAddress> getLocalSocketAddresses() {
		Set<SocketAddress> localSocketAddressesSet = new HashSet<>();
		for(HazelcastInstance hazelcastInstance : Hazelcast.getAllHazelcastInstances()) {
			localSocketAddressesSet.add(hazelcastInstance.getLocalEndpoint().getSocketAddress());
		}
		
		return localSocketAddressesSet;
	}
	
	protected boolean isEventAcceptable(Set<SocketAddress> localSocketAddressesSet, InetSocketAddress socketAddressOfEvent) {
		HazelcastInstance hazelcastInstance = Hazelcast.getAllHazelcastInstances().iterator().next();
		MultiMap<SocketAddress, SocketAddress> configMultiMap = hazelcastInstance.getMultiMap(ApplicationContextStartEventHandler.HZ_INTERNAL_CONFIGURATION_MULTI_MAP);
		if (configMultiMap.size() > 0 
				&& !configMultiMap.values().contains(socketAddressOfEvent) 
				&& localSocketAddressesSet.contains(configMultiMap.keySet().iterator().next())) {
			return true;
		}
		
		return false;
	}
	
	protected void createAndSendMessage(Object payload) {
		Message<?> message = MessageBuilder.withPayload(payload).build();
		getHazelcastInboundChannelAdapter().sendMessage(message);
	}
}
