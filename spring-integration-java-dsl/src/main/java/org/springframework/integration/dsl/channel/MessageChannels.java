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
import java.util.concurrent.Executor;

import org.springframework.integration.dispatcher.LoadBalancingStrategy;
import org.springframework.integration.store.MessageGroupStore;
import org.springframework.messaging.Message;

/**
 * @author Artem Bilan
 */
public final class MessageChannels {

	public static DirectChannelSpec direct() {
		return new DirectChannelSpec();
	}

	public static DirectChannelSpec direct(LoadBalancingStrategy loadBalancingStrategy) {
		return direct().loadBalancer(loadBalancingStrategy);
	}

	public static QueueChannelSpec queue() {
		return new QueueChannelSpec();
	}

	public static QueueChannelSpec queue(BlockingQueue<Message<?>> queue) {
		return new QueueChannelSpec(queue);
	}

	public static QueueChannelSpec queue(Integer capacity) {
		return new QueueChannelSpec(capacity);
	}

	public static QueueChannelSpec.MessageStoreSpec queue(MessageGroupStore messageGroupStore, Object groupId) {
		return new QueueChannelSpec.MessageStoreSpec(messageGroupStore, groupId);
	}

	public static ExecutorChannelSpec executor(Executor executor) {
		return new ExecutorChannelSpec(executor);
	}

	public static RendezvousChannelSpec rendezvous() {
		return new RendezvousChannelSpec();
	}

	public static PriorityChannelSpec priority() {
		return new PriorityChannelSpec();
	}

	public static PublishSubscribeChannelSpec publishSubscribe() {
		return new PublishSubscribeChannelSpec();
	}

	public static PublishSubscribeChannelSpec publishSubscribe(Executor executor) {
		return new PublishSubscribeChannelSpec(executor);
	}

	private MessageChannels() {
	}

}
