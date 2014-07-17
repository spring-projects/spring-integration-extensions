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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.context.OrderlyShutdownCapable;
import org.springframework.integration.gateway.MessagingGatewaySupport;
import org.springframework.integration.jms.ChannelPublishingJmsMessageListener;
import org.springframework.integration.jms.JmsMessageDrivenEndpoint;
import org.springframework.jms.listener.AbstractMessageListenerContainer;
import org.springframework.messaging.MessageChannel;

/**
 * @author Artem Bilan
 */
public class JmsInboundGateway extends MessagingGatewaySupport implements
		DisposableBean, OrderlyShutdownCapable {

	private final JmsMessageDrivenEndpoint endpoint;

	private final ChannelPublishingJmsMessageListener listener;

	public JmsInboundGateway(AbstractMessageListenerContainer listenerContainer,
			ChannelPublishingJmsMessageListener listener) {
		this.endpoint = new JmsMessageDrivenEndpoint(listenerContainer, listener);
		this.listener = listener;
	}

	@Override
	public void setRequestChannel(MessageChannel requestChannel) {
		this.listener.setRequestChannel(requestChannel);
	}

	@Override
	public void setReplyChannel(MessageChannel replyChannel) {
		this.listener.setReplyChannel(replyChannel);
	}

	@Override
	public void setErrorChannel(MessageChannel errorChannel) {
		this.listener.setErrorChannel(errorChannel);
	}

	@Override
	public void setRequestTimeout(long requestTimeout) {
		this.listener.setRequestTimeout(requestTimeout);
	}

	@Override
	public void setReplyTimeout(long replyTimeout) {
		this.listener.setReplyTimeout(replyTimeout);
	}

	@Override
	public void setShouldTrack(boolean shouldTrack) {
		this.listener.setShouldTrack(shouldTrack);
	}

	@Override
	public String getComponentType() {
		return "jms:inbound-gateway";
	}

	@Override
	public void setComponentName(String componentName) {
		super.setComponentName(componentName);
		this.endpoint.setComponentName(getComponentName());
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		super.setApplicationContext(applicationContext);
		this.endpoint.setApplicationContext(applicationContext);
		this.endpoint.setBeanFactory(applicationContext);
		this.listener.setBeanFactory(applicationContext);
	}

	@Override
	protected void onInit() throws Exception {
		this.endpoint.afterPropertiesSet();
	}

	ChannelPublishingJmsMessageListener getListener() {
		return this.listener;
	}

	@Override
	protected void doStart() {
		this.endpoint.start();
	}

	@Override
	protected void doStop() {
		this.endpoint.stop();
	}

	@Override
	public void destroy() throws Exception {
		this.endpoint.destroy();
	}

	@Override
	public int beforeShutdown() {
		return this.endpoint.beforeShutdown();
	}

	@Override
	public int afterShutdown() {
		return this.endpoint.afterShutdown();
	}

}
