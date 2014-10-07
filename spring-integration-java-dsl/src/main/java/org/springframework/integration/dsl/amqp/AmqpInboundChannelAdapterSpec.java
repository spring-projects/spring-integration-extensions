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
import org.springframework.integration.amqp.inbound.AmqpInboundChannelAdapter;
import org.springframework.integration.amqp.support.AmqpHeaderMapper;
import org.springframework.integration.amqp.support.DefaultAmqpHeaderMapper;
import org.springframework.integration.dsl.core.MessageProducerSpec;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.ErrorHandler;

/**
 * @author Artem Bilan
 */
public class AmqpInboundChannelAdapterSpec
		extends MessageProducerSpec<AmqpInboundChannelAdapterSpec, AmqpInboundChannelAdapter> {

	private final SimpleMessageListenerContainer listenerContainer;

	private final DefaultAmqpHeaderMapper headerMapper = new DefaultAmqpHeaderMapper();

	public AmqpInboundChannelAdapterSpec(SimpleMessageListenerContainer listenerContainer) {
		super(new AmqpInboundChannelAdapter(listenerContainer));
		this.listenerContainer = listenerContainer;
		this.target.setHeaderMapper(headerMapper);
	}

	public AmqpInboundChannelAdapterSpec acknowledgeMode(AcknowledgeMode acknowledgeMode) {
		this.listenerContainer.setAcknowledgeMode(acknowledgeMode);
		return this;
	}

	public AmqpInboundChannelAdapterSpec addQueueNames(String... queueName) {
		this.listenerContainer.addQueueNames(queueName);
		return this;
	}

	public AmqpInboundChannelAdapterSpec addQueues(Queue... queues) {
		this.listenerContainer.addQueues(queues);
		return this;
	}

	public AmqpInboundChannelAdapterSpec errorHandler(ErrorHandler errorHandler) {
		this.listenerContainer.setErrorHandler(errorHandler);
		return this;
	}

	public AmqpInboundChannelAdapterSpec channelTransacted(boolean transactional) {
		this.listenerContainer.setChannelTransacted(transactional);
		return this;
	}

	public AmqpInboundChannelAdapterSpec adviceChain(Advice... adviceChain) {
		this.listenerContainer.setAdviceChain(adviceChain);
		return this;
	}

	public AmqpInboundChannelAdapterSpec recoveryInterval(long recoveryInterval) {
		this.listenerContainer.setRecoveryInterval(recoveryInterval);
		return this;
	}

	public AmqpInboundChannelAdapterSpec concurrentConsumers(int concurrentConsumers) {
		this.listenerContainer.setConcurrentConsumers(concurrentConsumers);
		return this;
	}

	public AmqpInboundChannelAdapterSpec maxConcurrentConsumers(int maxConcurrentConsumers) {
		this.listenerContainer.setMaxConcurrentConsumers(maxConcurrentConsumers);
		return this;
	}

	public AmqpInboundChannelAdapterSpec exclusive(boolean exclusive) {
		this.listenerContainer.setExclusive(exclusive);
		return this;
	}

	public AmqpInboundChannelAdapterSpec startConsumerMinInterval(long startConsumerMinInterval) {
		this.listenerContainer.setStartConsumerMinInterval(startConsumerMinInterval);
		return this;
	}

	public AmqpInboundChannelAdapterSpec stopConsumerMinInterval(long stopConsumerMinInterval) {
		this.listenerContainer.setStopConsumerMinInterval(stopConsumerMinInterval);
		return this;
	}

	public AmqpInboundChannelAdapterSpec consecutiveActiveTrigger(int consecutiveActiveTrigger) {
		this.listenerContainer.setConsecutiveActiveTrigger(consecutiveActiveTrigger);
		return this;
	}

	public AmqpInboundChannelAdapterSpec consecutiveIdleTrigger(int consecutiveIdleTrigger) {
		this.listenerContainer.setConsecutiveIdleTrigger(consecutiveIdleTrigger);
		return this;
	}

	public AmqpInboundChannelAdapterSpec receiveTimeout(long receiveTimeout) {
		this.listenerContainer.setReceiveTimeout(receiveTimeout);
		return this;
	}

	public AmqpInboundChannelAdapterSpec shutdownTimeout(long shutdownTimeout) {
		this.listenerContainer.setShutdownTimeout(shutdownTimeout);
		return this;
	}

	public AmqpInboundChannelAdapterSpec taskExecutor(Executor taskExecutor) {
		this.listenerContainer.setTaskExecutor(taskExecutor);
		return this;
	}

	public AmqpInboundChannelAdapterSpec prefetchCount(int prefetchCount) {
		this.listenerContainer.setPrefetchCount(prefetchCount);
		return this;
	}

	public AmqpInboundChannelAdapterSpec txSize(int txSize) {
		this.listenerContainer.setTxSize(txSize);
		return this;
	}

	public AmqpInboundChannelAdapterSpec transactionManager(PlatformTransactionManager transactionManager) {
		this.listenerContainer.setTransactionManager(transactionManager);
		return this;
	}

	public AmqpInboundChannelAdapterSpec defaultRequeueRejected(boolean defaultRequeueRejected) {
		this.listenerContainer.setDefaultRequeueRejected(defaultRequeueRejected);
		return this;
	}

	public AmqpInboundChannelAdapterSpec messageConverter(MessageConverter messageConverter) {
		this.target.setMessageConverter(messageConverter);
		return this;
	}

	public AmqpInboundChannelAdapterSpec headerMapper(AmqpHeaderMapper headerMapper) {
		this.target.setHeaderMapper(headerMapper);
		return this;
	}

	public AmqpInboundChannelAdapterSpec mappedRequestHeaders(String... headers) {
		this.headerMapper.setRequestHeaderNames(headers);
		return this;
	}

}
