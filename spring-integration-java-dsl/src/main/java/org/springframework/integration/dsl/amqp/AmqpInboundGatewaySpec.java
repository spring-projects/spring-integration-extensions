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

import java.util.concurrent.Executor;

import org.aopalliance.aop.Advice;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.integration.amqp.inbound.AmqpInboundGateway;
import org.springframework.integration.amqp.support.AmqpHeaderMapper;
import org.springframework.integration.amqp.support.DefaultAmqpHeaderMapper;
import org.springframework.integration.dsl.core.MessagingGatewaySpec;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.ErrorHandler;

/**
 * @author Artem Bilan
 */
public class AmqpInboundGatewaySpec extends MessagingGatewaySpec<AmqpInboundGatewaySpec, AmqpInboundGateway> {

	private final SimpleMessageListenerContainer listenerContainer;

	private final DefaultAmqpHeaderMapper headerMapper = new DefaultAmqpHeaderMapper();

	AmqpInboundGatewaySpec(SimpleMessageListenerContainer listenerContainer) {
		super(new AmqpInboundGateway(listenerContainer));
		this.listenerContainer = listenerContainer;
		this.target.setHeaderMapper(headerMapper);
	}

	public AmqpInboundGatewaySpec acknowledgeMode(AcknowledgeMode acknowledgeMode) {
		this.listenerContainer.setAcknowledgeMode(acknowledgeMode);
		return this;
	}

	public AmqpInboundGatewaySpec addQueueNames(String... queueName) {
		this.listenerContainer.addQueueNames(queueName);
		return this;
	}

	public AmqpInboundGatewaySpec addQueues(Queue... queues) {
		this.listenerContainer.addQueues(queues);
		return this;
	}

	public AmqpInboundGatewaySpec errorHandler(ErrorHandler errorHandler) {
		this.listenerContainer.setErrorHandler(errorHandler);
		return this;
	}

	public AmqpInboundGatewaySpec channelTransacted(boolean transactional) {
		this.listenerContainer.setChannelTransacted(transactional);
		return this;
	}

	public AmqpInboundGatewaySpec adviceChain(Advice... adviceChain) {
		this.listenerContainer.setAdviceChain(adviceChain);
		return this;
	}

	public AmqpInboundGatewaySpec recoveryInterval(long recoveryInterval) {
		this.listenerContainer.setRecoveryInterval(recoveryInterval);
		return this;
	}

	public AmqpInboundGatewaySpec concurrentConsumers(int concurrentConsumers) {
		this.listenerContainer.setConcurrentConsumers(concurrentConsumers);
		return this;
	}

	public AmqpInboundGatewaySpec maxConcurrentConsumers(int maxConcurrentConsumers) {
		this.listenerContainer.setMaxConcurrentConsumers(maxConcurrentConsumers);
		return this;
	}

	public AmqpInboundGatewaySpec exclusive(boolean exclusive) {
		this.listenerContainer.setExclusive(exclusive);
		return this;
	}

	public AmqpInboundGatewaySpec startConsumerMinInterval(long startConsumerMinInterval) {
		this.listenerContainer.setStartConsumerMinInterval(startConsumerMinInterval);
		return this;
	}

	public AmqpInboundGatewaySpec stopConsumerMinInterval(long stopConsumerMinInterval) {
		this.listenerContainer.setStopConsumerMinInterval(stopConsumerMinInterval);
		return this;
	}

	public AmqpInboundGatewaySpec consecutiveActiveTrigger(int consecutiveActiveTrigger) {
		this.listenerContainer.setConsecutiveActiveTrigger(consecutiveActiveTrigger);
		return this;
	}

	public AmqpInboundGatewaySpec consecutiveIdleTrigger(int consecutiveIdleTrigger) {
		this.listenerContainer.setConsecutiveIdleTrigger(consecutiveIdleTrigger);
		return this;
	}

	public AmqpInboundGatewaySpec receiveTimeout(long receiveTimeout) {
		this.listenerContainer.setReceiveTimeout(receiveTimeout);
		return this;
	}

	public AmqpInboundGatewaySpec shutdownTimeout(long shutdownTimeout) {
		this.listenerContainer.setShutdownTimeout(shutdownTimeout);
		return this;
	}

	public AmqpInboundGatewaySpec taskExecutor(Executor taskExecutor) {
		this.listenerContainer.setTaskExecutor(taskExecutor);
		return this;
	}

	public AmqpInboundGatewaySpec prefetchCount(int prefetchCount) {
		this.listenerContainer.setPrefetchCount(prefetchCount);
		return this;
	}

	public AmqpInboundGatewaySpec txSize(int txSize) {
		this.listenerContainer.setTxSize(txSize);
		return this;
	}

	public AmqpInboundGatewaySpec transactionManager(PlatformTransactionManager transactionManager) {
		this.listenerContainer.setTransactionManager(transactionManager);
		return this;
	}

	public AmqpInboundGatewaySpec defaultRequeueRejected(boolean defaultRequeueRejected) {
		this.listenerContainer.setDefaultRequeueRejected(defaultRequeueRejected);
		return this;
	}

	public AmqpInboundGatewaySpec messageConverter(MessageConverter messageConverter) {
		this.target.setMessageConverter(messageConverter);
		return this;
	}

	public AmqpInboundGatewaySpec headerMapper(AmqpHeaderMapper headerMapper) {
		this.target.setHeaderMapper(headerMapper);
		return this;
	}

	public AmqpInboundGatewaySpec mappedRequestHeaders(String... headers) {
		this.headerMapper.setRequestHeaderNames(headers);
		return this;
	}

	public AmqpInboundGatewaySpec mappedReplyHeaders(String... headers) {
		this.headerMapper.setReplyHeaderNames(headers);
		return this;
	}

}
