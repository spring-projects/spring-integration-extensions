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

import java.util.concurrent.Executor;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;

import org.springframework.integration.dsl.core.IntegrationComponentSpec;
import org.springframework.integration.dsl.core.MessageHandlerSpec;
import org.springframework.integration.dsl.support.Consumer;
import org.springframework.integration.dsl.support.Function;
import org.springframework.integration.dsl.support.FunctionExpression;
import org.springframework.integration.jms.JmsHeaderMapper;
import org.springframework.integration.jms.JmsOutboundGateway;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.destination.DestinationResolver;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;

/**
 * @author Artem Bilan
 */
public class JmsOutboundGatewaySpec extends MessageHandlerSpec<JmsOutboundGatewaySpec, JmsOutboundGateway> {

	JmsOutboundGatewaySpec(ConnectionFactory connectionFactory) {
		this.target = new JmsOutboundGateway();
		this.target.setConnectionFactory(connectionFactory);
	}

	public JmsOutboundGatewaySpec extractRequestPayload(boolean extractPayload) {
		this.target.setExtractRequestPayload(extractPayload);
		return _this();
	}

	public JmsOutboundGatewaySpec extractReplyPayload(boolean extractPayload) {
		this.target.setExtractReplyPayload(extractPayload);
		return _this();
	}

	public JmsOutboundGatewaySpec headerMapper(JmsHeaderMapper headerMapper) {
		this.target.setHeaderMapper(headerMapper);
		return _this();
	}

	public JmsOutboundGatewaySpec requestDestination(Destination destination) {
		this.target.setRequestDestination(destination);
		return _this();
	}

	public JmsOutboundGatewaySpec requestDestination(String destination) {
		this.target.setRequestDestinationName(destination);
		return _this();
	}

	public JmsOutboundGatewaySpec requestDestinationExpression(String destination) {
		this.target.setRequestDestinationExpression(PARSER.parseExpression(destination));
		return _this();
	}

	public <P> JmsOutboundGatewaySpec requestDestination(Function<Message<P>, ?> destinationFunction) {
		this.target.setRequestDestinationExpression(new FunctionExpression<Message<P>>(destinationFunction));
		return _this();
	}

	public JmsOutboundGatewaySpec replyDestination(Destination destination) {
		this.target.setReplyDestination(destination);
		return _this();
	}

	public JmsOutboundGatewaySpec replyDestination(String destination) {
		this.target.setReplyDestinationName(destination);
		return _this();
	}

	public JmsOutboundGatewaySpec replyDestinationExpression(String destination) {
		this.target.setReplyDestinationExpression(PARSER.parseExpression(destination));
		return _this();
	}

	public <P> JmsOutboundGatewaySpec replyDestination(Function<Message<P>, ?> destinationFunction) {
		this.target.setReplyDestinationExpression(new FunctionExpression<Message<P>>(destinationFunction));
		return _this();
	}

	public JmsOutboundGatewaySpec destinationResolver(DestinationResolver destinationResolver) {
		this.target.setDestinationResolver(destinationResolver);
		return _this();
	}

	public JmsOutboundGatewaySpec jmsMessageConverter(MessageConverter messageConverter) {
		this.target.setMessageConverter(messageConverter);
		return _this();
	}

	public JmsOutboundGatewaySpec correlationKey(String correlationKey) {
		this.target.setCorrelationKey(correlationKey);
		return _this();
	}

	public JmsOutboundGatewaySpec requestPubSubDomain(boolean pubSubDomain) {
		this.target.setRequestPubSubDomain(pubSubDomain);
		return _this();
	}

	public JmsOutboundGatewaySpec replyPubSubDomain(boolean pubSubDomain) {
		this.target.setReplyPubSubDomain(pubSubDomain);
		return _this();
	}

	public JmsOutboundGatewaySpec deliveryPersistent(boolean deliveryPersistent) {
		this.target.setDeliveryPersistent(deliveryPersistent);
		return _this();
	}

	public JmsOutboundGatewaySpec priority(int priority) {
		this.target.setPriority(priority);
		return _this();
	}

	public JmsOutboundGatewaySpec timeToLive(long timeToLive) {
		this.target.setTimeToLive(timeToLive);
		return _this();
	}

	public JmsOutboundGatewaySpec receiveTimeout(long receiveTimeout) {
		this.target.setReceiveTimeout(receiveTimeout);
		return _this();
	}

	public JmsOutboundGatewaySpec explicitQosEnabled(boolean explicitQosEnabled) {
		this.target.setExplicitQosEnabled(explicitQosEnabled);
		return _this();
	}

	public JmsOutboundGatewaySpec replyContainer() {
		this.target.setReplyContainerProperties(new JmsOutboundGateway.ReplyContainerProperties());
		return _this();
	}

	public JmsOutboundGatewaySpec replyContainer(Consumer<ReplyContainerSpec> configurer) {
		Assert.notNull(configurer);
		ReplyContainerSpec spec = new ReplyContainerSpec();
		configurer.accept(spec);
		this.target.setReplyContainerProperties(spec.get());
		return _this();
	}

	@Override
	protected JmsOutboundGateway doGet() {
		throw new UnsupportedOperationException();
	}


	public class ReplyContainerSpec
			extends IntegrationComponentSpec<ReplyContainerSpec, JmsOutboundGateway.ReplyContainerProperties> {

		ReplyContainerSpec() {
			this.target = new JmsOutboundGateway.ReplyContainerProperties();
		}

		public ReplyContainerSpec sessionTransacted(Boolean sessionTransacted) {
			this.target.setSessionTransacted(sessionTransacted);
			return _this();
		}

		public ReplyContainerSpec sessionAcknowledgeMode(Integer sessionAcknowledgeMode) {
			this.target.setSessionAcknowledgeMode(sessionAcknowledgeMode);
			return _this();
		}

		public ReplyContainerSpec receiveTimeout(Long receiveTimeout) {
			this.target.setReceiveTimeout(receiveTimeout);
			return _this();
		}

		public ReplyContainerSpec recoveryInterval(Long recoveryInterval) {
			this.target.setRecoveryInterval(recoveryInterval);
			return _this();
		}

		public ReplyContainerSpec cacheLevel(Integer cacheLevel) {
			this.target.setCacheLevel(cacheLevel);
			return _this();
		}

		public ReplyContainerSpec concurrentConsumers(Integer concurrentConsumers) {
			this.target.setConcurrentConsumers(concurrentConsumers);
			return _this();
		}

		public ReplyContainerSpec maxConcurrentConsumers(Integer maxConcurrentConsumers) {
			this.target.setMaxConcurrentConsumers(maxConcurrentConsumers);
			return _this();
		}

		public ReplyContainerSpec maxMessagesPerTask(Integer maxMessagesPerTask) {
			this.target.setMaxMessagesPerTask(maxMessagesPerTask);
			return _this();
		}

		public ReplyContainerSpec idleConsumerLimit(Integer idleConsumerLimit) {
			this.target.setIdleConsumerLimit(idleConsumerLimit);
			return _this();
		}

		public ReplyContainerSpec idleTaskExecutionLimit(Integer idleTaskExecutionLimit) {
			this.target.setIdleTaskExecutionLimit(idleTaskExecutionLimit);
			return _this();
		}

		public ReplyContainerSpec taskExecutor(Executor taskExecutor) {
			this.target.setTaskExecutor(taskExecutor);
			return _this();
		}

		@Override
		protected JmsOutboundGateway.ReplyContainerProperties doGet() {
			throw new UnsupportedOperationException();
		}

	}

}
