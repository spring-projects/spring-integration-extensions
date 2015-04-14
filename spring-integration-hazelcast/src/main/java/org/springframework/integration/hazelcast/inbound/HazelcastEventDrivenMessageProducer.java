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

import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

import org.springframework.integration.hazelcast.HazelcastHeaders;
import org.springframework.integration.hazelcast.HazelcastIntegrationDefinitionValidator;
import org.springframework.util.Assert;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.IList;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;
import com.hazelcast.core.ISet;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.ItemEvent;
import com.hazelcast.core.ItemListener;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import com.hazelcast.core.MultiMap;
import com.hazelcast.core.ReplicatedMap;

/**
 * Hazelcast Event Driven Message Producer is a message producer which enables
 * {@link AbstractHazelcastMessageProducer.HazelcastEntryListener},
 * {@link HazelcastEventDrivenMessageProducer.HazelcastItemListener} and
 * {@link HazelcastEventDrivenMessageProducer.HazelcastMessageListener} listeners in order
 * to listen related cache events and sends events to related channel.
 *
 * @author Eren Avsarogullari
 * @since 1.0.0
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class HazelcastEventDrivenMessageProducer extends AbstractHazelcastMessageProducer {

	public HazelcastEventDrivenMessageProducer(DistributedObject distributedObject) {
		super(distributedObject);
	}

	@Override
	protected void onInit() {
		super.onInit();
		HazelcastIntegrationDefinitionValidator.validateCacheTypeForEventDrivenMessageProducer(this.distributedObject);
	}

	@Override
	protected void doStart() {
		if(this.distributedObject instanceof IMap) {
			setHazelcastRegisteredEventListenerId(((IMap<?, ?>) this.distributedObject)
					.addEntryListener(new HazelcastEntryListener(), true));
		}
		else if(this.distributedObject instanceof MultiMap) {
			setHazelcastRegisteredEventListenerId(((MultiMap<?, ?>) this.distributedObject)
					.addEntryListener(new HazelcastEntryListener(), true));
		}
		else if(this.distributedObject instanceof ReplicatedMap) {
			setHazelcastRegisteredEventListenerId(((ReplicatedMap<?, ?>) this.distributedObject)
					.addEntryListener(new HazelcastEntryListener()));
		}
		else if(this.distributedObject instanceof IList) {
			setHazelcastRegisteredEventListenerId(((IList<?>) this.distributedObject)
					.addItemListener(new HazelcastItemListener(), true));
		}
		else if(this.distributedObject instanceof ISet) {
			setHazelcastRegisteredEventListenerId(((ISet<?>) this.distributedObject)
					.addItemListener(new HazelcastItemListener(), true));
		}
		else if(this.distributedObject instanceof IQueue) {
			setHazelcastRegisteredEventListenerId(((IQueue<?>) this.distributedObject)
					.addItemListener(new HazelcastItemListener(), true));
		}
		else if(this.distributedObject instanceof ITopic) {
			setHazelcastRegisteredEventListenerId(((ITopic<?>) this.distributedObject)
					.addMessageListener(new HazelcastMessageListener()));
		}
	}

	@Override
	protected void doStop() {
		if(this.distributedObject instanceof IMap) {
			((IMap<?, ?>) this.distributedObject).removeEntryListener(getHazelcastRegisteredEventListenerId());
		}
		else if(this.distributedObject instanceof MultiMap) {
			((MultiMap<?, ?>) this.distributedObject).removeEntryListener(getHazelcastRegisteredEventListenerId());
		}
		else if(this.distributedObject instanceof ReplicatedMap) {
			((ReplicatedMap<?, ?>) this.distributedObject).removeEntryListener(getHazelcastRegisteredEventListenerId());
		}
		else if(this.distributedObject instanceof IList) {
			((IList<?>) this.distributedObject).removeItemListener(getHazelcastRegisteredEventListenerId());
		}
		else if(this.distributedObject instanceof ISet) {
			((ISet<?>) this.distributedObject).removeItemListener(getHazelcastRegisteredEventListenerId());
		}
		else if(this.distributedObject instanceof IQueue) {
			((IQueue<?>) this.distributedObject).removeItemListener(getHazelcastRegisteredEventListenerId());
		}
		else if(this.distributedObject instanceof ITopic) {
			((ITopic<?>) this.distributedObject).removeMessageListener(getHazelcastRegisteredEventListenerId());
		}
	}

	@Override
	public String getComponentType() {
		return "hazelcast:inbound-channel-adapter";
	}

	private class HazelcastItemListener<E> extends AbstractHazelcastEventListener implements ItemListener<E> {

		@Override
		public void itemAdded(ItemEvent<E> item) {
			processEvent(item);
		}

		@Override
		public void itemRemoved(ItemEvent<E> item) {
			processEvent(item);
		}

		@Override
		protected void processEvent(EventObject event) {
			Assert.notNull(event, "event must not be null");

			if (getCacheEvents().contains(((ItemEvent<E>) event).getEventType().toString())) {
				sendMessage(event, ((ItemEvent<E>) event).getMember().getSocketAddress(), getCacheListeningPolicy());
			}

			if (logger.isDebugEnabled()){
				logger.debug("Received ItemEvent : " + event);
			}
		}

		@Override
		protected org.springframework.messaging.Message<?> toMessage(EventObject event) {
			ItemEvent itemEvent = (ItemEvent) event;
			Assert.notNull(itemEvent.getItem(), "item must not be null");

			final Map<String, Object> headers = new HashMap<String, Object>();
			headers.put(HazelcastHeaders.EVENT_TYPE, itemEvent.getEventType().name());
			headers.put(HazelcastHeaders.MEMBER, itemEvent.getMember().getSocketAddress());

			return getMessageBuilderFactory().withPayload(itemEvent.getItem()).copyHeaders(headers).build();
		}

	}

	private class HazelcastMessageListener<E> extends AbstractHazelcastEventListener implements MessageListener<E> {

		@Override
		public void onMessage(Message<E> message) {
			processEvent(message);
		}

		@Override
		protected void processEvent(EventObject event) {
			Assert.notNull(event, "event must not be null");
			sendMessage(event, ((Message<E>) event).getPublishingMember().getSocketAddress(), null);

			if (logger.isDebugEnabled()){
				logger.debug("Received Message : " + event);
			}
		}

		@Override
		protected org.springframework.messaging.Message<?> toMessage(EventObject event) {
			Message message = (Message) event;
			Assert.notNull(message.getMessageObject(), "message must not be null");

			final Map<String, Object> headers = new HashMap<String, Object>();
			headers.put(HazelcastHeaders.MEMBER, message.getPublishingMember().getSocketAddress());
			headers.put(HazelcastHeaders.CACHE_NAME, message.getSource());
			headers.put(HazelcastHeaders.PUBLISHING_TIME, message.getPublishTime());

			return getMessageBuilderFactory().withPayload(message.getMessageObject()).copyHeaders(headers).build();
		}

	}

}
