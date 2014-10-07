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

package org.springframework.integration.dsl.jms;

import javax.jms.ConnectionFactory;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.AbstractMessageListenerContainer;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

/**
 * @author Artem Bilan
 */
public abstract class Jms {

	public static <S extends JmsPollableMessageChannelSpec<S>> JmsPollableMessageChannelSpec<S>
	pollableChannel(ConnectionFactory connectionFactory) {
		return pollableChannel(null, connectionFactory);
	}

	public static
	<S extends JmsPollableMessageChannelSpec<S>> JmsPollableMessageChannelSpec<S> pollableChannel(String id,
			ConnectionFactory connectionFactory) {
		return new JmsPollableMessageChannelSpec<S>(connectionFactory).id(id);
	}

	public static
	<S extends JmsMessageChannelSpec<S>> JmsMessageChannelSpec<S> channel(ConnectionFactory connectionFactory) {
		return channel(null, connectionFactory);
	}

	public static <S extends JmsMessageChannelSpec<S>> JmsMessageChannelSpec<S> channel(String id,
			ConnectionFactory connectionFactory) {
		return new JmsMessageChannelSpec<S>(connectionFactory).id(id);
	}

	public static JmsPublishSubscribeMessageChannelSpec publishSubscribeChannel(ConnectionFactory connectionFactory) {
		return publishSubscribeChannel(null, connectionFactory);
	}

	public static JmsPublishSubscribeMessageChannelSpec publishSubscribeChannel(String id,
			ConnectionFactory connectionFactory) {
		return new JmsPublishSubscribeMessageChannelSpec(connectionFactory).id(id);
	}

	public static <S extends JmsOutboundChannelAdapterSpec<S>> JmsOutboundChannelAdapterSpec<S>
	outboundAdapter(JmsTemplate jmsTemplate) {
		return new JmsOutboundChannelAdapterSpec<S>(jmsTemplate);
	}

	public static JmsOutboundChannelAdapterSpec.JmsOutboundChannelSpecTemplateAware
	outboundAdapter(ConnectionFactory connectionFactory) {
		return new JmsOutboundChannelAdapterSpec.JmsOutboundChannelSpecTemplateAware(connectionFactory);
	}

	public static <S extends JmsInboundChannelAdapterSpec<S>> JmsInboundChannelAdapterSpec<S>
	inboundAdapter(JmsTemplate jmsTemplate) {
		return new JmsInboundChannelAdapterSpec<S>(jmsTemplate);
	}

	public static JmsInboundChannelAdapterSpec.JmsInboundChannelSpecTemplateAware
	inboundAdapter(ConnectionFactory connectionFactory) {
		return new JmsInboundChannelAdapterSpec.JmsInboundChannelSpecTemplateAware(connectionFactory);
	}

	public static JmsOutboundGatewaySpec outboundGateway(ConnectionFactory connectionFactory) {
		return new JmsOutboundGatewaySpec(connectionFactory);
	}

	public static <S extends JmsInboundGatewaySpec<S>> JmsInboundGatewaySpec<S>
	inboundGateway(AbstractMessageListenerContainer listenerContainer) {
		return new JmsInboundGatewaySpec<S>(listenerContainer);
	}

	public static JmsInboundGatewaySpec.JmsInboundGatewayListenerContainerSpec<DefaultMessageListenerContainer>
	inboundGateway(ConnectionFactory connectionFactory) {
		return inboundGateway(connectionFactory, DefaultMessageListenerContainer.class);
	}

	public static <C extends AbstractMessageListenerContainer>
	JmsInboundGatewaySpec.JmsInboundGatewayListenerContainerSpec<C> inboundGateway(ConnectionFactory connectionFactory,
			Class<C> containerClass) {
		try {
			JmsListenerContainerSpec<C> spec = new JmsListenerContainerSpec<C>(containerClass)
					.connectionFactory(connectionFactory);
			return new JmsInboundGatewaySpec.JmsInboundGatewayListenerContainerSpec<C>(spec);
		}
		catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public static <S extends JmsMessageDrivenChannelAdapterSpec<S>> JmsMessageDrivenChannelAdapterSpec<S>
	messageDriverChannelAdapter(AbstractMessageListenerContainer listenerContainer) {
		return new JmsMessageDrivenChannelAdapterSpec<S>(listenerContainer);
	}

	public static
	JmsMessageDrivenChannelAdapterSpec.JmsMessageDrivenChannelAdapterListenerContainerSpec<DefaultMessageListenerContainer>
	messageDriverChannelAdapter(ConnectionFactory connectionFactory) {
		return messageDriverChannelAdapter(connectionFactory, DefaultMessageListenerContainer.class);
	}

	public static <C extends AbstractMessageListenerContainer>
	JmsMessageDrivenChannelAdapterSpec.JmsMessageDrivenChannelAdapterListenerContainerSpec<C>
	messageDriverChannelAdapter(ConnectionFactory connectionFactory, Class<C> containerClass) {
		try {
			JmsListenerContainerSpec<C> spec = new JmsListenerContainerSpec<C>(containerClass)
					.connectionFactory(connectionFactory);
			return new JmsMessageDrivenChannelAdapterSpec.JmsMessageDrivenChannelAdapterListenerContainerSpec<C>(spec);
		}
		catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

}
