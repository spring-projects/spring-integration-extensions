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

package org.springframework.integration.dsl;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.integration.gateway.GatewayProxyFactoryBean;
import org.springframework.integration.gateway.RequestReplyExchanger;
import org.springframework.integration.handler.AbstractReplyProducingMessageHandler;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.util.StringUtils;

/**
 * @author Artem Bilan
 */
class GatewayMessageHandler extends AbstractReplyProducingMessageHandler {

	private final GatewayProxyFactoryBean gatewayProxyFactoryBean;

	private RequestReplyExchanger exchanger;

	private String requestChannel;

	private String replyChannel;

	private String errorChannel;

	GatewayMessageHandler() {
		this.gatewayProxyFactoryBean = new GatewayProxyFactoryBean();
		this.gatewayProxyFactoryBean.setServiceInterface(RequestReplyExchanger.class);
	}

	void setRequestChannel(MessageChannel requestChannel) {
		this.gatewayProxyFactoryBean.setDefaultRequestChannel(requestChannel);
	}

	void setRequestChannelName(String requestChannel) {
		this.requestChannel = requestChannel;
	}

	public void setReplyChannel(MessageChannel replyChannel) {
		this.gatewayProxyFactoryBean.setDefaultReplyChannel(replyChannel);
	}

	public void setReplyChannelName(String replyChannel) {
		this.replyChannel = replyChannel;
	}

	public void setErrorChannel(MessageChannel errorChannel) {
		this.gatewayProxyFactoryBean.setErrorChannel(errorChannel);
	}

	public void setErrorChannelName(String errorChannel) {
		this.errorChannel = errorChannel;
	}

	public void setRequestTimeout(Long requestTimeout) {
		this.gatewayProxyFactoryBean.setDefaultRequestTimeout(requestTimeout);
	}

	public void setReplyTimeout(Long replyTimeout) {
		this.gatewayProxyFactoryBean.setDefaultReplyTimeout(replyTimeout);
	}

	@Override
	protected Object handleRequestMessage(Message<?> requestMessage) {
		if (this.exchanger == null) {
			synchronized (this) {
				if (this.exchanger == null) {
					initialize();
				}
			}
		}
		return this.exchanger.exchange(requestMessage);
	}

	private void initialize() {
		BeanFactory beanFactory = getBeanFactory();

		if (StringUtils.hasText(this.requestChannel)) {
			this.gatewayProxyFactoryBean.setDefaultRequestChannel(beanFactory.getBean(this.requestChannel,
					MessageChannel.class));
		}

		if (StringUtils.hasText(this.replyChannel)) {
			this.gatewayProxyFactoryBean.setDefaultReplyChannel(beanFactory.getBean(this.replyChannel,
					MessageChannel.class));
		}

		if (StringUtils.hasText(this.errorChannel)) {
			this.gatewayProxyFactoryBean.setErrorChannel(beanFactory.getBean(this.errorChannel,
					MessageChannel.class));
		}

		if (beanFactory instanceof ConfigurableListableBeanFactory) {
			((ConfigurableListableBeanFactory) beanFactory).initializeBean(this.gatewayProxyFactoryBean, null);
		}
		try {
			this.exchanger = (RequestReplyExchanger) this.gatewayProxyFactoryBean.getObject();
		}
		catch (Exception e) {
			throw new BeanCreationException("Can't instantiate the GatewayProxyFactoryBean: " + this, e);
		}
	}

}
