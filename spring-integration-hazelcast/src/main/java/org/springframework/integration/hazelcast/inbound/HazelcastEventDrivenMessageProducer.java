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

import com.hazelcast.core.IList;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;
import com.hazelcast.core.ISet;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.MultiMap;
import com.hazelcast.core.ReplicatedMap;

import org.springframework.integration.hazelcast.listener.HazelcastEntryListener;
import org.springframework.integration.hazelcast.listener.HazelcastItemListener;
import org.springframework.integration.hazelcast.listener.HazelcastMessageListener;

/**
 * Hazelcast Event Driven Message Producer is a message producer which enables
 * Hazelcast Entry/Item/Message Listeners in order to listen related cache events and
 * sends events to related channel.
 * 
 * @author Eren Avsarogullari
 * @since 1.0.0
 *
 */
public class HazelcastEventDrivenMessageProducer extends HazelcastMessageProducer {
		
	@Override
	protected void doStart() {
		if(getDistributedObject() instanceof IMap) {
			((IMap<?, ?>)getDistributedObject()).addEntryListener(new HazelcastEntryListener(this), true);
		} 
		else if(getDistributedObject() instanceof MultiMap) {
			((MultiMap<?, ?>)getDistributedObject()).addEntryListener(new HazelcastEntryListener(this), true);
		} 
		else if(getDistributedObject() instanceof IList) {
			((IList<?>)getDistributedObject()).addItemListener(new HazelcastItemListener(this), true);
		} 
		else if(getDistributedObject() instanceof ISet) {
			((ISet<?>)getDistributedObject()).addItemListener(new HazelcastItemListener(this), true);
		} 
		else if(getDistributedObject() instanceof IQueue) {
			((IQueue<?>)getDistributedObject()).addItemListener(new HazelcastItemListener(this), true);
		} 
		else if(getDistributedObject() instanceof ITopic) {
			((ITopic<?>)getDistributedObject()).addMessageListener(new HazelcastMessageListener(this));
		} 
		else if(getDistributedObject() instanceof ReplicatedMap) {
			((ReplicatedMap<?, ?>)getDistributedObject()).addEntryListener(new HazelcastEntryListener(this));
		} 
	}

}
