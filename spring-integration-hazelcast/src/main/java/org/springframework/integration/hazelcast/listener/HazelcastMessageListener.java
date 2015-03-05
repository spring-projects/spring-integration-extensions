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

import java.util.EventObject;

import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

import org.springframework.integration.hazelcast.inbound.HazelcastEventDrivenMessageProducer;

/**
 * This is a listener for ITopic to get notified when a message is published. It listens
 * message events and sends to related channel.
 * 
 * @author Eren Avsarogullari
 * @since 1.0.0
 *
 */
public class HazelcastMessageListener<E> extends AbstractHazelcastEventListener implements MessageListener<E> {

	public HazelcastMessageListener(HazelcastEventDrivenMessageProducer hazelcastEventDrivenInboundChannelAdapter) {
		super(hazelcastEventDrivenInboundChannelAdapter);
	}

	@Override
	public void onMessage(Message<E> message) {
		processEvent(message);
	}

	@Override
	protected void processEvent(EventObject event) {
		sendMessage(event, ((Message<E>) event).getPublishingMember().getSocketAddress(), null);
	}

}
