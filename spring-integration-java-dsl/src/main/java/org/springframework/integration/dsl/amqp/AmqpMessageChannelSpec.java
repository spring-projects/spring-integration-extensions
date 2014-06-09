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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;

import org.aopalliance.aop.Advice;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.integration.amqp.channel.AbstractAmqpChannel;
import org.springframework.integration.amqp.config.AmqpChannelFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.util.ErrorHandler;

/**
 * @author Artem Bilan
 */
public class AmqpMessageChannelSpec<S extends AmqpMessageChannelSpec<S>> extends AmqpPollableMessageChannelSpec<S> {

	private final List<Advice> adviceChain = new LinkedList<Advice>();

	AmqpMessageChannelSpec(ConnectionFactory connectionFactory) {
		super(new AmqpChannelFactoryBean(true), connectionFactory);
	}

	public S maxSubscribers(int maxSubscribers) {
		this.amqpChannelFactoryBean.setMaxSubscribers(maxSubscribers);
		return _this();
	}

	public S acknowledgeMode(AcknowledgeMode acknowledgeMode) {
		this.amqpChannelFactoryBean.setAcknowledgeMode(acknowledgeMode);
		return _this();
	}

	public S advice(Advice... advice) {
		this.adviceChain.addAll(Arrays.asList(advice));
		return _this();
	}

	public S autoStartup(boolean autoStartup) {
		this.amqpChannelFactoryBean.setAutoStartup(autoStartup);
		return _this();
	}

	public S concurrentConsumers(int concurrentConsumers) {
		this.amqpChannelFactoryBean.setConcurrentConsumers(concurrentConsumers);
		return _this();
	}

	public S errorHandler(ErrorHandler errorHandler) {
		this.amqpChannelFactoryBean.setErrorHandler(errorHandler);
		return _this();
	}

	public S exposeListenerChannel(boolean exposeListenerChannel) {
		this.amqpChannelFactoryBean.setExposeListenerChannel(exposeListenerChannel);
		return _this();
	}

	public S phase(int phase) {
		this.amqpChannelFactoryBean.setPhase(phase);
		return _this();
	}

	public S prefetchCount(int prefetchCount) {
		this.amqpChannelFactoryBean.setPrefetchCount(prefetchCount);
		return _this();
	}

	public S receiveTimeout(long receiveTimeout) {
		this.amqpChannelFactoryBean.setReceiveTimeout(receiveTimeout);
		return _this();
	}

	public S recoveryInterval(long recoveryInterval) {
		this.amqpChannelFactoryBean.setRecoveryInterval(recoveryInterval);
		return _this();
	}

	public S shutdownTimeout(long shutdownTimeout) {
		this.amqpChannelFactoryBean.setShutdownTimeout(shutdownTimeout);
		return _this();
	}

	public S taskExecutor(Executor taskExecutor) {
		this.amqpChannelFactoryBean.setTaskExecutor(taskExecutor);
		return _this();
	}

	public S transactionAttribute(TransactionAttribute transactionAttribute) {
		this.amqpChannelFactoryBean.setTransactionAttribute(transactionAttribute);
		return _this();
	}

	public S transactionManager(PlatformTransactionManager transactionManager) {
		this.amqpChannelFactoryBean.setTransactionManager(transactionManager);
		return _this();
	}

	public S txSize(int txSize) {
		this.amqpChannelFactoryBean.setTxSize(txSize);
		return _this();
	}

	@Override
	protected AbstractAmqpChannel doGet() {
		this.amqpChannelFactoryBean.setAdviceChain(this.adviceChain.toArray(new Advice[this.adviceChain.size()]));
		return super.doGet();
	}

}
