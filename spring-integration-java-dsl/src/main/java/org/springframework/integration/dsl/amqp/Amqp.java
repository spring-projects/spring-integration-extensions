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

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.integration.amqp.inbound.AmqpInboundGateway;
import org.springframework.integration.dsl.core.MessagingGatewaySpec;

/**
 * @author Artem Bilan
 * @since 4.0
 */
public abstract class Amqp {

	public static MessagingGatewaySpec<AmqpInboundGatewaySpec, AmqpInboundGateway> inboundGateway(
			SimpleMessageListenerContainer listenerContainer) {
		return new AmqpInboundGatewaySpec(listenerContainer);
	}

	public static AmqpInboundGatewaySpec inboundGateway(ConnectionFactory connectionFactory, String... queueNames) {
		SimpleMessageListenerContainer listenerContainer = new SimpleMessageListenerContainer(connectionFactory);
		listenerContainer.setQueueNames(queueNames);
		return new AmqpInboundGatewaySpec(listenerContainer);
	}

	public static AmqpInboundGatewaySpec inboundGateway(ConnectionFactory connectionFactory, Queue... queues) {
		SimpleMessageListenerContainer listenerContainer = new SimpleMessageListenerContainer(connectionFactory);
		listenerContainer.setQueues(queues);
		return new AmqpInboundGatewaySpec(listenerContainer);
	}

}
