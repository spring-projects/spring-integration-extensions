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

package org.springframework.integration.dsl;

import javax.jms.ConnectionFactory;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.integration.dsl.amqp.Amqp;
import org.springframework.integration.dsl.amqp.AmqpInboundGatewaySpec;
import org.springframework.integration.dsl.jms.Jms;
import org.springframework.integration.dsl.jms.JmsInboundGatewaySpec;
import org.springframework.jms.listener.AbstractMessageListenerContainer;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

/**
 * @author Artem Bilan
 */
public class MessagingGateways {

	public AmqpInboundGatewaySpec amqp(org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory,
			String... queueNames) {
		return Amqp.inboundGateway(connectionFactory, queueNames);
	}

	public AmqpInboundGatewaySpec amqp(org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory,
			Queue... queues) {
		return Amqp.inboundGateway(connectionFactory, queues);
	}

	public AmqpInboundGatewaySpec amqp(SimpleMessageListenerContainer listenerContainer) {
		return (AmqpInboundGatewaySpec) Amqp.inboundGateway(listenerContainer);
	}

	public JmsInboundGatewaySpec.JmsInboundGatewayListenerContainerSpec<DefaultMessageListenerContainer> jms(
			javax.jms.ConnectionFactory connectionFactory) {
		return Jms.inboundGateway(connectionFactory);
	}

	public <C extends AbstractMessageListenerContainer>
	JmsInboundGatewaySpec.JmsInboundGatewayListenerContainerSpec<C> jms(ConnectionFactory connectionFactory,
			Class<C> containerClass) {
		return Jms.inboundGateway(connectionFactory, containerClass);
	}

	public JmsInboundGatewaySpec<? extends JmsInboundGatewaySpec<?>> jms(
			AbstractMessageListenerContainer listenerContainer) {
		return Jms.inboundGateway(listenerContainer);
	}

	MessagingGateways() {
	}

}
