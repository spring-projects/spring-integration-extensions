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

import javax.jms.Destination;

import org.springframework.integration.dsl.core.MessagingGatewaySpec;
import org.springframework.integration.dsl.support.Consumer;
import org.springframework.integration.jms.ChannelPublishingJmsMessageListener;
import org.springframework.integration.jms.JmsHeaderMapper;
import org.springframework.jms.listener.AbstractMessageListenerContainer;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.destination.DestinationResolver;
import org.springframework.util.Assert;

/**
 * @author Artem Bilan
 */
public class JmsInboundGatewaySpec<S extends JmsInboundGatewaySpec<S>>
		extends MessagingGatewaySpec<S, JmsInboundGateway> {

	JmsInboundGatewaySpec(AbstractMessageListenerContainer listenerContainer) {
		super(new JmsInboundGateway(listenerContainer, new ChannelPublishingJmsMessageListener()));
		this.target.getListener().setExpectReply(true);
	}

	public S defaultReplyDestination(Destination defaultReplyDestination) {
		this.target.getListener().setDefaultReplyDestination(defaultReplyDestination);
		return _this();
	}

	public S defaultReplyQueueName(String destinationName) {
		this.target.getListener().setDefaultReplyQueueName(destinationName);
		return _this();
	}

	public S defaultReplyTopicName(String destinationName) {
		this.target.getListener().setDefaultReplyTopicName(destinationName);
		return _this();
	}

	public S replyTimeToLive(long replyTimeToLive) {
		this.target.getListener().setReplyTimeToLive(replyTimeToLive);
		return _this();
	}

	public S replyPriority(int replyPriority) {
		this.target.getListener().setReplyPriority(replyPriority);
		return _this();
	}

	public S replyDeliveryPersistent(boolean replyDeliveryPersistent) {
		this.target.getListener().setReplyDeliveryPersistent(replyDeliveryPersistent);
		return _this();
	}

	public S correlationKey(String correlationKey) {
		this.target.getListener().setCorrelationKey(correlationKey);
		return _this();
	}

	public S explicitQosEnabledForReplies(boolean explicitQosEnabledForReplies) {
		this.target.getListener().setExplicitQosEnabledForReplies(explicitQosEnabledForReplies);
		return _this();
	}

	public S destinationResolver(DestinationResolver destinationResolver) {
		this.target.getListener().setDestinationResolver(destinationResolver);
		return _this();
	}

	public S jmsMessageConverter(MessageConverter messageConverter) {
		this.target.getListener().setMessageConverter(messageConverter);
		return _this();
	}

	public S setHeaderMapper(JmsHeaderMapper headerMapper) {
		this.target.getListener().setHeaderMapper(headerMapper);
		return _this();
	}

	public S extractRequestPayload(boolean extractRequestPayload) {
		this.target.getListener().setExtractRequestPayload(extractRequestPayload);
		return _this();
	}

	public S extractReplyPayload(boolean extractReplyPayload) {
		this.target.getListener().setExtractReplyPayload(extractReplyPayload);
		return _this();
	}

	public static class JmsInboundGatewayListenerContainerSpec<C extends AbstractMessageListenerContainer> extends
			JmsInboundGatewaySpec<JmsInboundGatewayListenerContainerSpec<C>> {

		private final JmsListenerContainerSpec<C> spec;

		JmsInboundGatewayListenerContainerSpec(JmsListenerContainerSpec<C> spec) {
			super(spec.get());
			this.spec = spec;
			this.spec.get().setAutoStartup(false);
		}

		public JmsInboundGatewayListenerContainerSpec<C> destination(Destination destination) {
			spec.destination(destination);
			return _this();
		}

		public JmsInboundGatewayListenerContainerSpec<C> destination(String destinationName) {
			spec.destination(destinationName);
			return _this();
		}

		public JmsInboundGatewayListenerContainerSpec<C> configureListenerContainer(
				Consumer<JmsListenerContainerSpec<C>> configurer) {
			Assert.notNull(configurer);
			configurer.accept(this.spec);
			return _this();
		}

	}

}
