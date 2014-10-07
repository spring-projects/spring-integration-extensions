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

package org.springframework.integration.dsl.amqp;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.integration.amqp.inbound.AmqpInboundChannelAdapter;
import org.springframework.integration.amqp.inbound.AmqpInboundGateway;
import org.springframework.integration.dsl.core.MessageProducerSpec;
import org.springframework.integration.dsl.core.MessagingGatewaySpec;

/**
 * @author Artem Bilan
 */
public abstract class Amqp {

	public static AmqpInboundGatewaySpec inboundGateway(ConnectionFactory connectionFactory, String... queueNames) {
		SimpleMessageListenerContainer listenerContainer = new SimpleMessageListenerContainer(connectionFactory);
		listenerContainer.setQueueNames(queueNames);
		return (AmqpInboundGatewaySpec) inboundGateway(listenerContainer);
	}

	public static AmqpInboundGatewaySpec inboundGateway(ConnectionFactory connectionFactory, Queue... queues) {
		SimpleMessageListenerContainer listenerContainer = new SimpleMessageListenerContainer(connectionFactory);
		listenerContainer.setQueues(queues);
		return (AmqpInboundGatewaySpec) inboundGateway(listenerContainer);
	}

	public static MessagingGatewaySpec<AmqpInboundGatewaySpec, AmqpInboundGateway> inboundGateway(
			SimpleMessageListenerContainer listenerContainer) {
		return new AmqpInboundGatewaySpec(listenerContainer);
	}

	public static AmqpInboundChannelAdapterSpec inboundAdapter(ConnectionFactory connectionFactory,
			String... queueNames) {
		SimpleMessageListenerContainer listenerContainer = new SimpleMessageListenerContainer(connectionFactory);
		listenerContainer.setQueueNames(queueNames);
		return (AmqpInboundChannelAdapterSpec) inboundAdapter(listenerContainer);
	}

	public static AmqpInboundChannelAdapterSpec inboundAdapter(ConnectionFactory connectionFactory, Queue... queues) {
		SimpleMessageListenerContainer listenerContainer = new SimpleMessageListenerContainer(connectionFactory);
		listenerContainer.setQueues(queues);
		return (AmqpInboundChannelAdapterSpec) inboundAdapter(listenerContainer);
	}

	public static MessageProducerSpec<AmqpInboundChannelAdapterSpec, AmqpInboundChannelAdapter> inboundAdapter(
			SimpleMessageListenerContainer listenerContainer) {
		return new AmqpInboundChannelAdapterSpec(listenerContainer);
	}

	public static AmqpOutboundEndpointSpec outboundAdapter(AmqpTemplate amqpTemplate) {
		return new AmqpOutboundEndpointSpec(amqpTemplate, false);
	}

	public static AmqpOutboundEndpointSpec outboundGateway(AmqpTemplate amqpTemplate) {
		return new AmqpOutboundEndpointSpec(amqpTemplate, true);
	}

	public static <S extends AmqpPollableMessageChannelSpec<S>> AmqpPollableMessageChannelSpec<S>
	pollableChannel(ConnectionFactory connectionFactory) {
		return pollableChannel(null, connectionFactory);
	}

	public static <S extends AmqpPollableMessageChannelSpec<S>> AmqpPollableMessageChannelSpec<S> pollableChannel(
			String id, ConnectionFactory connectionFactory) {
		return new AmqpPollableMessageChannelSpec<S>(connectionFactory).id(id);
	}

	public static <S extends AmqpMessageChannelSpec<S>> AmqpMessageChannelSpec<S> channel(
			ConnectionFactory connectionFactory) {
		return channel(null, connectionFactory);
	}

	public static <S extends AmqpMessageChannelSpec<S>> AmqpMessageChannelSpec<S> channel(String id,
			ConnectionFactory connectionFactory) {
		return new AmqpMessageChannelSpec<S>(connectionFactory).id(id);
	}

	public static AmqpPublishSubscribeMessageChannelSpec publishSubscribeChannel(ConnectionFactory connectionFactory) {
		return publishSubscribeChannel(null, connectionFactory);
	}

	public static AmqpPublishSubscribeMessageChannelSpec publishSubscribeChannel(String id,
			ConnectionFactory connectionFactory) {
		return new AmqpPublishSubscribeMessageChannelSpec(connectionFactory).id(id);
	}

}
