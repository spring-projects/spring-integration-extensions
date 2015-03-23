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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.integration.hazelcast.common.HazelcastIntegrationDefinitionValidator;
import org.springframework.util.Assert;

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

	private final Log logger = LogFactory.getLog(this.getClass());

	private HazelcastEventDrivenMessageProducer(DistributedObject distributedObject) {
		super(distributedObject);
	}

	@Override
	protected void onInit() {
		super.onInit();
		HazelcastIntegrationDefinitionValidator.validateCacheTypeForEventDrivenMessageProducer(getDistributedObject());
	}

	@Override
	protected void doStart() {
		if(getDistributedObject() instanceof IMap) {
			setHazelcastRegisteredEventListenerId(((IMap<?, ?>)getDistributedObject()).addEntryListener(new HazelcastEntryListener(), true));
		}
		else if(getDistributedObject() instanceof MultiMap) {
			setHazelcastRegisteredEventListenerId(((MultiMap<?, ?>)getDistributedObject()).addEntryListener(new HazelcastEntryListener(), true));
		}
		else if(getDistributedObject() instanceof ReplicatedMap) {
			setHazelcastRegisteredEventListenerId(((ReplicatedMap<?, ?>)getDistributedObject()).addEntryListener(new HazelcastEntryListener()));
		}
		else if(getDistributedObject() instanceof IList) {
			setHazelcastRegisteredEventListenerId(((IList<?>)getDistributedObject()).addItemListener(new HazelcastItemListener(), true));
		}
		else if(getDistributedObject() instanceof ISet) {
			setHazelcastRegisteredEventListenerId(((ISet<?>)getDistributedObject()).addItemListener(new HazelcastItemListener(), true));
		}
		else if(getDistributedObject() instanceof IQueue) {
			setHazelcastRegisteredEventListenerId(((IQueue<?>)getDistributedObject()).addItemListener(new HazelcastItemListener(), true));
		}
		else if(getDistributedObject() instanceof ITopic) {
			setHazelcastRegisteredEventListenerId(((ITopic<?>)getDistributedObject()).addMessageListener(new HazelcastMessageListener()));
		}
	}

	@Override
	protected void doStop() {
		if(getDistributedObject() instanceof IMap) {
			((IMap<?, ?>)getDistributedObject()).removeEntryListener(getHazelcastRegisteredEventListenerId());
		}
		else if(getDistributedObject() instanceof MultiMap) {
			((MultiMap<?, ?>)getDistributedObject()).removeEntryListener(getHazelcastRegisteredEventListenerId());
		}
		else if(getDistributedObject() instanceof ReplicatedMap) {
			((ReplicatedMap<?, ?>)getDistributedObject()).removeEntryListener(getHazelcastRegisteredEventListenerId());
		}
		else if(getDistributedObject() instanceof IList) {
			((IList<?>)getDistributedObject()).removeItemListener(getHazelcastRegisteredEventListenerId());
		}
		else if(getDistributedObject() instanceof ISet) {
			((ISet<?>)getDistributedObject()).removeItemListener(getHazelcastRegisteredEventListenerId());
		}
		else if(getDistributedObject() instanceof IQueue) {
			((IQueue<?>)getDistributedObject()).removeItemListener(getHazelcastRegisteredEventListenerId());
		}
		else if(getDistributedObject() instanceof ITopic) {
			((ITopic<?>)getDistributedObject()).removeMessageListener(getHazelcastRegisteredEventListenerId());
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

			if (getCacheEventSet().contains(((ItemEvent<E>) event).getEventType().toString())) {
				sendMessage(event, ((ItemEvent<E>) event).getMember().getSocketAddress(), getCacheListeningPolicy());
			}

			if (logger.isDebugEnabled()){
				logger.debug("Received ItemEvent : " + event);
			}
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

	}

}
