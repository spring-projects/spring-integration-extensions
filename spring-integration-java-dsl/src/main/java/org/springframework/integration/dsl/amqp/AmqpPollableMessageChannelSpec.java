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

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.support.MessagePropertiesConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.integration.amqp.channel.AbstractAmqpChannel;
import org.springframework.integration.amqp.config.AmqpChannelFactoryBean;
import org.springframework.integration.dsl.channel.MessageChannelSpec;
import org.springframework.util.Assert;

/**
 * @author Artem Bilan
 */
public class AmqpPollableMessageChannelSpec<S extends AmqpPollableMessageChannelSpec<S>>
		extends MessageChannelSpec<S, AbstractAmqpChannel> {

	protected final AmqpChannelFactoryBean amqpChannelFactoryBean;

	AmqpPollableMessageChannelSpec(ConnectionFactory connectionFactory) {
		this(new AmqpChannelFactoryBean(false), connectionFactory);
	}

	AmqpPollableMessageChannelSpec(AmqpChannelFactoryBean amqpChannelFactoryBean, ConnectionFactory connectionFactory) {
		this.amqpChannelFactoryBean = amqpChannelFactoryBean;
		this.amqpChannelFactoryBean.setConnectionFactory(connectionFactory);
		this.amqpChannelFactoryBean.setSingleton(false);
		this.amqpChannelFactoryBean.setPubSub(false);
		this.amqpChannelFactoryBean.setBeanFactory(new DefaultListableBeanFactory());
	}

	@Override
	protected S id(String id) {
		this.amqpChannelFactoryBean.setBeanName(id);
		return super.id(id);
	}

	public S queueName(String queueName) {
		if (this.id == null) {
			id(queueName + ".channel");
		}
		this.amqpChannelFactoryBean.setQueueName(queueName);
		return _this();
	}

	public S encoding(String encoding) {
		this.amqpChannelFactoryBean.setEncoding(encoding);
		return _this();
	}

	public S amqpMessageConverter(MessageConverter messageConverter) {
		this.amqpChannelFactoryBean.setMessageConverter(messageConverter);
		return _this();
	}

	public S channelTransacted(boolean channelTransacted) {
		this.amqpChannelFactoryBean.setChannelTransacted(channelTransacted);
		return _this();
	}

	public S messagePropertiesConverter(MessagePropertiesConverter messagePropertiesConverter) {
		this.amqpChannelFactoryBean.setMessagePropertiesConverter(messagePropertiesConverter);
		return _this();
	}

	@Override
	protected AbstractAmqpChannel doGet() {
		Assert.notNull(this.id, "The 'id' or 'queueName' must be specified");
		try {
			this.channel = this.amqpChannelFactoryBean.getObject();
		}
		catch (Exception e) {
			throw new BeanCreationException("Cannot create the AMQP MessageChannel", e);
		}
		return super.doGet();
	}

}
