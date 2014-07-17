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
import javax.jms.Destination;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.integration.dsl.channel.MessageChannelSpec;
import org.springframework.integration.jms.AbstractJmsChannel;
import org.springframework.integration.jms.config.JmsChannelFactoryBean;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.destination.DestinationResolver;

/**
 * @author Artem Bilan
 */
public class JmsPollableMessageChannelSpec<S extends JmsPollableMessageChannelSpec<S>>
		extends MessageChannelSpec<S, AbstractJmsChannel> {

	protected final JmsChannelFactoryBean jmsChannelFactoryBean;

	JmsPollableMessageChannelSpec(ConnectionFactory connectionFactory) {
		this(new JmsChannelFactoryBean(false), connectionFactory);
	}

	JmsPollableMessageChannelSpec(JmsChannelFactoryBean jmsChannelFactoryBean, ConnectionFactory connectionFactory) {
		this.jmsChannelFactoryBean = jmsChannelFactoryBean;
		this.jmsChannelFactoryBean.setConnectionFactory(connectionFactory);
		this.jmsChannelFactoryBean.setSingleton(false);
		this.jmsChannelFactoryBean.setBeanFactory(new DefaultListableBeanFactory());
	}

	@Override
	protected S id(String id) {
		this.jmsChannelFactoryBean.setBeanName(id);
		return super.id(id);
	}

	public S destination(String destination) {
		this.jmsChannelFactoryBean.setDestinationName(destination);
		return _this();
	}

	public S destinationResolver(DestinationResolver destinationResolver) {
		this.jmsChannelFactoryBean.setDestinationResolver(destinationResolver);
		return _this();
	}

	public S destination(Destination destination) {
		this.jmsChannelFactoryBean.setDestination(destination);
		return _this();
	}

	public S messageSelector(String messageSelector) {
		this.jmsChannelFactoryBean.setMessageSelector(messageSelector);
		return _this();
	}

	public S jmsMessageConverter(MessageConverter messageConverter) {
		this.jmsChannelFactoryBean.setMessageConverter(messageConverter);
		return _this();
	}

	public S deliveryPersistent(boolean deliveryPersistent) {
		this.jmsChannelFactoryBean.setDeliveryPersistent(deliveryPersistent);
		return _this();
	}

	public S explicitQosEnabled(boolean explicitQosEnabled) {
		this.jmsChannelFactoryBean.setExplicitQosEnabled(explicitQosEnabled);
		return _this();
	}

	public S messageIdEnabled(boolean messageIdEnabled) {
		this.jmsChannelFactoryBean.setMessageIdEnabled(messageIdEnabled);
		return _this();
	}

	public S messageTimestampEnabled(boolean messageTimestampEnabled) {
		this.jmsChannelFactoryBean.setMessageTimestampEnabled(messageTimestampEnabled);
		return _this();
	}

	public S priority(int priority) {
		this.jmsChannelFactoryBean.setPriority(priority);
		return _this();
	}

	public S timeToLive(long timeToLive) {
		this.jmsChannelFactoryBean.setTimeToLive(timeToLive);
		return _this();
	}

	public S receiveTimeout(long receiveTimeout) {
		this.jmsChannelFactoryBean.setReceiveTimeout(receiveTimeout);
		return _this();
	}

	/**
	 * @param sessionAcknowledgeMode the acknowledgement mode constant
	 * @return the current {@link MessageChannelSpec}
	 * @see javax.jms.Session#AUTO_ACKNOWLEDGE etc.
	 */
	public S sessionAcknowledgeMode(int sessionAcknowledgeMode) {
		this.jmsChannelFactoryBean.setSessionAcknowledgeMode(sessionAcknowledgeMode);
		return _this();
	}

	public S sessionTransacted(boolean sessionTransacted) {
		this.jmsChannelFactoryBean.setSessionTransacted(sessionTransacted);
		return _this();
	}

	@Override
	protected AbstractJmsChannel doGet() {
		try {
			this.channel = this.jmsChannelFactoryBean.getObject();
		}
		catch (Exception e) {
			throw new BeanCreationException("Cannot create the JMS MessageChannel", e);
		}
		return super.doGet();
	}

}
