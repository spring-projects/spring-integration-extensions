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

import java.net.SocketAddress;
import java.util.EventObject;
import java.util.Set;

import org.springframework.integration.hazelcast.inbound.HazelcastEventDrivenMessageProducer;

import com.hazelcast.core.ItemEvent;
import com.hazelcast.core.ItemListener;

/**
 * HazelcastItemListener
 * 
 * @author Eren Avsarogullari
 * @since 1.0.0
 *
 */
public class HazelcastItemListener<E> extends AbstractHazelcastEventListener implements ItemListener<E> {

	public HazelcastItemListener(HazelcastEventDrivenMessageProducer hazelcastEventDrivenInboundChannelAdapter) {
		super(hazelcastEventDrivenInboundChannelAdapter);
	}

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
		if(getHazelcastInboundChannelAdapter().getCacheEventTypeSet().contains(((ItemEvent<E>)event).getEventType().toString())) {
			Set<SocketAddress> localSocketAddressesSet = getLocalSocketAddresses();
			if((!localSocketAddressesSet.isEmpty()) 
					&& (localSocketAddressesSet.contains(((ItemEvent<E>)event).getMember().getSocketAddress())
							|| isEventAcceptable(localSocketAddressesSet, ((ItemEvent<E>)event).getMember().getSocketAddress()))) {
				
				createAndSendMessage(event);
	
			}
		}
	}

}
