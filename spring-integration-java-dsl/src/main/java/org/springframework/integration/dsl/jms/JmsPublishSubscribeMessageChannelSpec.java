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

/**
 * @author Artem Bilan
 */
public class JmsPublishSubscribeMessageChannelSpec
		extends JmsMessageChannelSpec<JmsPublishSubscribeMessageChannelSpec> {

	JmsPublishSubscribeMessageChannelSpec(ConnectionFactory connectionFactory) {
		super(connectionFactory);
		this.jmsChannelFactoryBean.setPubSubDomain(true);
	}

	public JmsPublishSubscribeMessageChannelSpec subscriptionDurable(boolean durable) {
		this.jmsChannelFactoryBean.setSubscriptionDurable(durable);
		return _this();
	}

	public JmsPublishSubscribeMessageChannelSpec durableSubscriptionName(String durableSubscriptionName) {
		this.jmsChannelFactoryBean.setDurableSubscriptionName(durableSubscriptionName);
		return _this();
	}

	public JmsPublishSubscribeMessageChannelSpec clientId(String clientId) {
		this.jmsChannelFactoryBean.setClientId(clientId);
		return _this();
	}

	public JmsPublishSubscribeMessageChannelSpec pubSubNoLocal(boolean pubSubNoLocal) {
		this.jmsChannelFactoryBean.setPubSubNoLocal(pubSubNoLocal);
		return _this();
	}

}
