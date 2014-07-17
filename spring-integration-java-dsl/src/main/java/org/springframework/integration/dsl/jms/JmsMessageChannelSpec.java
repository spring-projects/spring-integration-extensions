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

import org.springframework.beans.DirectFieldAccessor;
import org.springframework.integration.jms.AbstractJmsChannel;
import org.springframework.integration.jms.config.JmsChannelFactoryBean;
import org.springframework.jms.listener.AbstractMessageListenerContainer;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.ErrorHandler;

/**
 * @author Artem Bilan
 */
public class JmsMessageChannelSpec<S extends JmsMessageChannelSpec<S>> extends JmsPollableMessageChannelSpec<S> {

	private Integer cacheLevel;

	JmsMessageChannelSpec(ConnectionFactory connectionFactory) {
		super(new JmsChannelFactoryBean(true), connectionFactory);
	}

	public S containerType(Class<? extends AbstractMessageListenerContainer> containerType) {
		this.jmsChannelFactoryBean.setContainerType(containerType);
		return _this();
	}

	public S concurrentConsumers(int concurrentConsumers) {
		this.jmsChannelFactoryBean.setConcurrentConsumers(concurrentConsumers);
		return _this();
	}

	public S maxSubscribers(int maxSubscribers) {
		this.jmsChannelFactoryBean.setMaxSubscribers(maxSubscribers);
		return _this();
	}

	public S autoStartup(boolean autoStartup) {
		this.jmsChannelFactoryBean.setAutoStartup(autoStartup);
		return _this();
	}

	public S phase(int phase) {
		this.jmsChannelFactoryBean.setPhase(phase);
		return _this();
	}

	public S errorHandler(ErrorHandler errorHandler) {
		this.jmsChannelFactoryBean.setErrorHandler(errorHandler);
		return _this();
	}

	public S exposeListenerSession(boolean exposeListenerSession) {
		this.jmsChannelFactoryBean.setExposeListenerSession(exposeListenerSession);
		return _this();
	}

	public S acceptMessagesWhileStopping(boolean acceptMessagesWhileStopping) {
		this.jmsChannelFactoryBean.setAcceptMessagesWhileStopping(acceptMessagesWhileStopping);
		return _this();
	}

	public S idleTaskExecutionLimit(int idleTaskExecutionLimit) {
		this.jmsChannelFactoryBean.setIdleTaskExecutionLimit(idleTaskExecutionLimit);
		return _this();
	}

	public S maxMessagesPerTask(int maxMessagesPerTask) {
		this.jmsChannelFactoryBean.setMaxMessagesPerTask(maxMessagesPerTask);
		return _this();
	}

	public S recoveryInterval(long recoveryInterval) {
		this.jmsChannelFactoryBean.setRecoveryInterval(recoveryInterval);
		return _this();
	}

	public S taskExecutor(Executor taskExecutor) {
		this.jmsChannelFactoryBean.setTaskExecutor(taskExecutor);
		return _this();
	}

	public S transactionManager(PlatformTransactionManager transactionManager) {
		this.jmsChannelFactoryBean.setTransactionManager(transactionManager);
		return _this();
	}

	public S transactionName(String transactionName) {
		this.jmsChannelFactoryBean.setTransactionName(transactionName);
		return _this();
	}

	public S transactionTimeout(int transactionTimeout) {
		this.jmsChannelFactoryBean.setTransactionTimeout(transactionTimeout);
		return _this();
	}

	/**
	 * @param cacheLevel the value for {@code DefaultMessageListenerContainer.cacheLevel}
	 * @return the current {@link org.springframework.integration.dsl.channel.MessageChannelSpec}
	 * @see org.springframework.jms.listener.DefaultMessageListenerContainer#CACHE_AUTO etc.
	 */
	public S cacheLevel(Integer cacheLevel) {
		this.cacheLevel = cacheLevel;
		return _this();
	}

	@Override
	protected AbstractJmsChannel doGet() {
		AbstractJmsChannel jmsChannel = super.doGet();
		if (this.cacheLevel != null) {
			//TODO till INT-3435
			DirectFieldAccessor dfa = new DirectFieldAccessor(jmsChannel);
			Object container = dfa.getPropertyValue("container");
			if (container instanceof DefaultMessageListenerContainer) {
				((DefaultMessageListenerContainer) container).setCacheLevel(this.cacheLevel);
			}

		}
		return jmsChannel;
	}

}
