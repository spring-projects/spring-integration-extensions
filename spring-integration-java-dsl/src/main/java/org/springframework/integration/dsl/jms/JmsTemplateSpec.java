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

import org.springframework.integration.dsl.core.IntegrationComponentSpec;
import org.springframework.integration.jms.DynamicJmsTemplate;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.destination.DestinationResolver;

/**
 * @author Artem Bilan
 */
public class JmsTemplateSpec extends IntegrationComponentSpec<JmsTemplateSpec, DynamicJmsTemplate> {

	public JmsTemplateSpec() {
		this.target = new DynamicJmsTemplate();
	}

	JmsTemplateSpec connectionFactory(ConnectionFactory connectionFactory) {
		this.target.setConnectionFactory(connectionFactory);
		return _this();
	}

	public JmsTemplateSpec destinationResolver(DestinationResolver destinationResolver) {
		this.target.setDestinationResolver(destinationResolver);
		return _this();
	}

	public JmsTemplateSpec pubSubDomain(boolean pubSubDomain) {
		this.target.setPubSubDomain(pubSubDomain);
		return _this();
	}

	public JmsTemplateSpec jmsMessageConverter(MessageConverter messageConverter) {
		this.target.setMessageConverter(messageConverter);
		return _this();
	}

	public JmsTemplateSpec deliveryPersistent(boolean deliveryPersistent) {
		this.target.setDeliveryPersistent(deliveryPersistent);
		return _this();
	}

	public JmsTemplateSpec explicitQosEnabled(boolean explicitQosEnabled) {
		this.target.setExplicitQosEnabled(explicitQosEnabled);
		return _this();
	}

	public JmsTemplateSpec priority(int priority) {
		this.target.setPriority(priority);
		return _this();
	}

	public JmsTemplateSpec timeToLive(long timeToLive) {
		this.target.setTimeToLive(timeToLive);
		return _this();
	}

	public JmsTemplateSpec receiveTimeout(long receiveTimeout) {
		this.target.setReceiveTimeout(receiveTimeout);
		return _this();
	}

	/**
	 * @param sessionAcknowledgeMode the acknowledgement mode constant
	 * @return the current {@link org.springframework.integration.dsl.channel.MessageChannelSpec}
	 * @see javax.jms.Session#AUTO_ACKNOWLEDGE etc.
	 */
	public JmsTemplateSpec sessionAcknowledgeMode(int sessionAcknowledgeMode) {
		this.target.setSessionAcknowledgeMode(sessionAcknowledgeMode);
		return _this();
	}

	public JmsTemplateSpec sessionTransacted(boolean sessionTransacted) {
		this.target.setSessionTransacted(sessionTransacted);
		return _this();
	}

	@Override
	protected DynamicJmsTemplate doGet() {
		throw new UnsupportedOperationException();
	}

}
