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

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.integration.hazelcast.CacheEventType;
import org.springframework.integration.hazelcast.CacheListeningPolicyType;
import org.springframework.integration.hazelcast.HazelcastHeaders;
import org.springframework.integration.hazelcast.HazelcastIntegrationDefinitionValidator;
import org.springframework.integration.hazelcast.HazelcastLocalInstanceRegistrar;
import org.springframework.integration.hazelcast.message.EntryEventMessagePayload;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.hazelcast.core.AbstractIMapEvent;
import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MapEvent;
import com.hazelcast.core.MultiMap;

/**
 * Hazelcast Base Event-Driven Message Producer.
 *
 * @author Eren Avsarogullari
 * @author Artem Bilan
 * @since 1.0.0
 */
public abstract class AbstractHazelcastMessageProducer extends MessageProducerSupport {

	protected final DistributedObject distributedObject;

	private volatile CacheListeningPolicyType cacheListeningPolicy = CacheListeningPolicyType.SINGLE;

	private volatile String hazelcastRegisteredEventListenerId;

	private Set<String> cacheEvents = Collections.singleton(CacheEventType.ADDED.name());

	public AbstractHazelcastMessageProducer(DistributedObject distributedObject) {
		Assert.notNull(distributedObject, "'distributedObject' must not be null");
		this.distributedObject = distributedObject;
	}

	protected Set<String> getCacheEvents() {
		return cacheEvents;
	}

	public void setCacheEventTypes(String cacheEventTypes) {
		HazelcastIntegrationDefinitionValidator.validateEnumType(CacheEventType.class, cacheEventTypes);
		Set<String> cacheEvents = StringUtils.commaDelimitedListToSet(cacheEventTypes);
		Assert.notEmpty(cacheEvents, "'cacheEvents' must have elements");
		HazelcastIntegrationDefinitionValidator.validateCacheEventsByDistributedObject(
				this.distributedObject, cacheEvents);
		this.cacheEvents = cacheEvents;
	}

	protected CacheListeningPolicyType getCacheListeningPolicy() {
		return cacheListeningPolicy;
	}

	public void setCacheListeningPolicy(CacheListeningPolicyType cacheListeningPolicy) {
		Assert.notNull(cacheListeningPolicy, "'cacheListeningPolicy' must not be null");
		this.cacheListeningPolicy = cacheListeningPolicy;
	}

	protected String getHazelcastRegisteredEventListenerId() {
		return hazelcastRegisteredEventListenerId;
	}

	protected void setHazelcastRegisteredEventListenerId(String hazelcastRegisteredEventListenerId) {
		this.hazelcastRegisteredEventListenerId = hazelcastRegisteredEventListenerId;
	}

	protected abstract class AbstractHazelcastEventListener<E> {

		protected abstract void processEvent(E event);

		protected abstract Message<?> toMessage(E event);

		protected void sendMessage(E event, InetSocketAddress socketAddress,
				CacheListeningPolicyType cacheListeningPolicyType) {
			if (CacheListeningPolicyType.ALL == cacheListeningPolicyType || isEventAcceptable(socketAddress)) {
				AbstractHazelcastMessageProducer.this.sendMessage(toMessage(event));
			}
		}

		private boolean isEventAcceptable(final InetSocketAddress socketAddress) {
			final Set<HazelcastInstance> hazelcastInstanceSet = Hazelcast.getAllHazelcastInstances();
			final Set<SocketAddress> localSocketAddressesSet = getLocalSocketAddresses(hazelcastInstanceSet);
			return (!localSocketAddressesSet.isEmpty())
					&& (localSocketAddressesSet.contains(socketAddress) ||
					isEventComingFromNonRegisteredHazelcastInstance(hazelcastInstanceSet.iterator().next(),
							localSocketAddressesSet, socketAddress));

		}

		private Set<SocketAddress> getLocalSocketAddresses(final Set<HazelcastInstance> hazelcastInstanceSet) {
			final Set<SocketAddress> localSocketAddressesSet = new HashSet<>();
			for (HazelcastInstance hazelcastInstance : hazelcastInstanceSet) {
				localSocketAddressesSet.add(hazelcastInstance.getLocalEndpoint().getSocketAddress());
			}

			return localSocketAddressesSet;
		}

		private boolean isEventComingFromNonRegisteredHazelcastInstance(
				final HazelcastInstance hazelcastInstance,
				final Set<SocketAddress> localSocketAddressesSet,
				final InetSocketAddress socketAddressOfEvent) {
			final MultiMap<SocketAddress, SocketAddress> configMultiMap = hazelcastInstance
					.getMultiMap(HazelcastLocalInstanceRegistrar.SPRING_INTEGRATION_INTERNAL_CLUSTER_MULTIMAP);
			return configMultiMap.size() > 0
					&& !configMultiMap.values().contains(socketAddressOfEvent)
					&& localSocketAddressesSet.contains(configMultiMap.keySet().iterator().next());

		}

	}

	protected final class HazelcastEntryListener<K, V> extends
			AbstractHazelcastEventListener<AbstractIMapEvent> implements EntryListener<K, V> {

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
		protected void processEvent(AbstractIMapEvent event) {
			if (getCacheEvents().contains(event.getEventType().toString())) {
				sendMessage(event, event.getMember().getSocketAddress(), getCacheListeningPolicy());
			}

			if (logger.isDebugEnabled()) {
				logger.debug("Received Event : " + event);
			}
		}

		@Override
		@SuppressWarnings("unchecked")
		protected Message<?> toMessage(AbstractIMapEvent event) {
			final Map<String, Object> headers = new HashMap<String, Object>();
			headers.put(HazelcastHeaders.EVENT_TYPE, event.getEventType().name());
			headers.put(HazelcastHeaders.MEMBER, event.getMember().getSocketAddress());
			headers.put(HazelcastHeaders.CACHE_NAME, event.getName());

			if (event instanceof EntryEvent) {
				EntryEvent<K, V> entryEvent = (EntryEvent<K, V>) event;
				EntryEventMessagePayload<K, V> messagePayload = new EntryEventMessagePayload<>(entryEvent.getKey(),
						entryEvent.getValue(), entryEvent.getOldValue());
				return getMessageBuilderFactory().withPayload(messagePayload).copyHeaders(headers).build();
			}
			else if (event instanceof MapEvent) {
				return getMessageBuilderFactory()
						.withPayload(((MapEvent) event).getNumberOfEntriesAffected()).copyHeaders(headers).build();
			}
			else {
				throw new IllegalStateException("Invalid event is received. Event : " + event);
			}
		}

	}

}
