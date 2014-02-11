/*
 * Copyright 2014 the original author or authors.
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

package org.springframework.integration.dsl.channel;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Lock;

import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.store.MessageGroupQueue;
import org.springframework.integration.store.MessageGroupStore;
import org.springframework.messaging.Message;

/**
 * @author Artem Bilan
 */
public class QueueChannelSpec extends ChannelSpecSupport<QueueChannelSpec, QueueChannel> {

	protected BlockingQueue<Message<?>> queue;

	protected Integer capacity;

	QueueChannelSpec() {
	}

	QueueChannelSpec(BlockingQueue<Message<?>> queue) {
		this.queue = queue;
	}

	QueueChannelSpec(Integer capacity) {
		this.capacity = capacity;
	}

	@Override
	public QueueChannel get() {
		if (this.queue != null) {
			this.channel = new QueueChannel(this.queue);
		}
		else if (this.capacity != null) {
			this.channel = new QueueChannel(this.capacity);
		}
		else {
			this.channel = new QueueChannel();
		}
		return super.get();
	}

	public static class MessageStoreSpec extends QueueChannelSpec {

		private final MessageGroupStore messageGroupStore;

		private final Object groupId;

		private Lock storeLock;

		MessageStoreSpec(MessageGroupStore messageGroupStore, Object groupId) {
			super();
			this.messageGroupStore = messageGroupStore;
			this.groupId = groupId;
		}

		public MessageStoreSpec capacity(Integer capacity) {
			this.capacity = capacity;
			return this;
		}

		public MessageStoreSpec storeLock(Lock storeLock) {
			this.storeLock = storeLock;
			return this;
		}

		@Override
		public QueueChannel get() {
			if (this.capacity != null) {
				if (this.storeLock != null) {
					this.queue = new MessageGroupQueue(messageGroupStore, groupId, this.capacity, this.storeLock);
				}
				else {
					this.queue = new MessageGroupQueue(messageGroupStore, groupId, this.capacity);
				}
			}
			else if (this.storeLock != null) {
				this.queue = new MessageGroupQueue(messageGroupStore, groupId, this.storeLock);
			}
			else {
				this.queue = new MessageGroupQueue(messageGroupStore, groupId);
			}
			return super.get();
		}

	}

}
