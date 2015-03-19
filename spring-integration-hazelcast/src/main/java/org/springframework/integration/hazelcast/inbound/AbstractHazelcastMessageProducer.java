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

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Set;

import com.hazelcast.core.AbstractIMapEvent;
import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MapEvent;
import com.hazelcast.core.MultiMap;

import reactor.util.StringUtils;

import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.integration.hazelcast.common.CacheEventType;
import org.springframework.integration.hazelcast.common.CacheListeningPolicyType;
import org.springframework.integration.hazelcast.common.HazelcastIntegrationDefinitionValidator;
import org.springframework.integration.hazelcast.config.HazelcastLocalInstanceRegistrar;
import org.springframework.util.Assert;

/**
 * Hazelcast Base Event-Driven Message Producer.
 *
 * @author Eren Avsarogullari
 * @since 1.0.0
 */
public abstract class AbstractHazelcastMessageProducer extends MessageProducerSupport {

	private final DistributedObject distributedObject;

	private String cacheEventTypes;

	private CacheListeningPolicyType cacheListeningPolicy;

	private String hazelcastRegisteredEventListenerId;

	protected AbstractHazelcastMessageProducer(DistributedObject distributedObject) {
		Assert.notNull(distributedObject, "cache must not be null");
		this.distributedObject = distributedObject;
	}

	@Override
	protected void onInit() {
		super.onInit();
		HazelcastIntegrationDefinitionValidator.validateCacheEventsByDistributedObject(
				getDistributedObject(), getCacheEventTypeSet());
	}

	protected Set<String> getCacheEventTypeSet() {
		return StringUtils.commaDelimitedListToSet(getCacheEventTypes());
	}

	protected DistributedObject getDistributedObject() {
		return distributedObject;
	}

	protected String getCacheEventTypes() {
		return cacheEventTypes;
	}

	public void setCacheEventTypes(String cacheEventTypes) {
		HazelcastIntegrationDefinitionValidator.validateEnumType(CacheEventType.class, cacheEventTypes);
		this.cacheEventTypes = cacheEventTypes;
	}

	protected CacheListeningPolicyType getCacheListeningPolicy() {
		return cacheListeningPolicy;
	}

	public void setCacheListeningPolicy(CacheListeningPolicyType cacheListeningPolicy) {
		this.cacheListeningPolicy = cacheListeningPolicy;
	}

	protected String getHazelcastRegisteredEventListenerId() {
		return hazelcastRegisteredEventListenerId;
	}

	protected void setHazelcastRegisteredEventListenerId(String hazelcastRegisteredEventListenerId) {
		this.hazelcastRegisteredEventListenerId = hazelcastRegisteredEventListenerId;
	}

	protected abstract class AbstractHazelcastEventListener {

		protected abstract void processEvent(EventObject event);

		protected void sendMessage(EventObject event, InetSocketAddress socketAddress,
				CacheListeningPolicyType cacheListeningPolicyType) {
			if (CacheListeningPolicyType.ALL == cacheListeningPolicyType || isEventAcceptable(socketAddress)) {
				AbstractHazelcastMessageProducer.this.sendMessage(getMessageBuilderFactory().withPayload(event).build());
			}
		}

		private boolean isEventAcceptable(InetSocketAddress socketAddress) {
			final Set<SocketAddress> localSocketAddressesSet = getLocalSocketAddresses();
			if ((!localSocketAddressesSet.isEmpty())
					&& (localSocketAddressesSet.contains(socketAddress) ||
							isEventComingFromNonRegisteredHazelcastInstance(localSocketAddressesSet, socketAddress))) {
				return true;
			}

			return false;
		}

		private Set<SocketAddress> getLocalSocketAddresses() {
			final Set<SocketAddress> localSocketAddressesSet = new HashSet<>();
			for (HazelcastInstance hazelcastInstance : Hazelcast.getAllHazelcastInstances()) {
				localSocketAddressesSet.add(hazelcastInstance.getLocalEndpoint().getSocketAddress());
			}

			return localSocketAddressesSet;
		}

		private boolean isEventComingFromNonRegisteredHazelcastInstance(
				Set<SocketAddress> localSocketAddressesSet, InetSocketAddress socketAddressOfEvent) {
			final HazelcastInstance hazelcastInstance = Hazelcast.getAllHazelcastInstances().iterator().next();
			MultiMap<SocketAddress, SocketAddress> configMultiMap = hazelcastInstance
					.getMultiMap(HazelcastLocalInstanceRegistrar.HZ_CLUSTER_WIDE_CONFIG_MULTI_MAP);
			if (configMultiMap.size() > 0
					&& !configMultiMap.values().contains(socketAddressOfEvent)
					&& localSocketAddressesSet.contains(configMultiMap.keySet().iterator().next())) {
				return true;
			}

			return false;
		}

	}

	protected final class HazelcastEntryListener<K, V> extends AbstractHazelcastEventListener
			implements EntryListener<K, V>, Serializable {

		private static final long serialVersionUID = -7701718155581762512L;

		@Override
		public void entryAdded(EntryEvent<K, V> event) {
			processEvent(event);
		}

		@Override
		public void entryRemoved(EntryEvent<K, V> event) {
			processEvent(event);
		}

		@Override
		public void entryUpdated(EntryEvent<K, V> event) {
			processEvent(event);
		}

		@Override
		public void entryEvicted(EntryEvent<K, V> event) {
			processEvent(event);
		}

		@Override
		public void mapEvicted(MapEvent event) {
			processEvent(event);
		}

		@Override
		public void mapCleared(MapEvent event) {
			processEvent(event);
		}

		@Override
		protected void processEvent(EventObject event) {
			Assert.notNull(event, "event must not be null");

			if (getCacheEventTypeSet().contains(((AbstractIMapEvent) event).getEventType().toString())) {
				sendMessage(event, ((AbstractIMapEvent) event).getMember().getSocketAddress(), getCacheListeningPolicy());
			}
		}

	}

}
