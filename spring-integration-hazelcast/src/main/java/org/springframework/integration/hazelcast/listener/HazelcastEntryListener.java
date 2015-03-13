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

import java.io.Serializable;
import java.util.EventObject;

import com.hazelcast.core.AbstractIMapEvent;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.MapEvent;

import org.springframework.integration.hazelcast.inbound.HazelcastMessageProducer;

/**
 * This is an Entry listener for {@link com.hazelcast.core.IMap},
 * {@link com.hazelcast.core.MultiMap} and {@link com.hazelcast.core.ReplicatedMap} to get
 * notified when a map entry is added, removed, updated or evicted. It listens events,
 * checks and sends to related channel.
 *
 * @author Eren Avsarogullari
 * @since 1.0.0
 *
 */
public class HazelcastEntryListener<K, V> extends AbstractHazelcastEventListener implements EntryListener<K, V>, Serializable {

	public HazelcastEntryListener(HazelcastMessageProducer hazelcastInboundChannelAdapter) {
		super(hazelcastInboundChannelAdapter);
	}

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
		if (getHazelcastInboundChannelAdapter().getCacheEventTypeSet().contains(
				((AbstractIMapEvent) event).getEventType().toString())) {
			sendMessage(event,
					((AbstractIMapEvent) event).getMember().getSocketAddress(),
					getHazelcastInboundChannelAdapter().getCacheListeningPolicy());
		}
	}

}
