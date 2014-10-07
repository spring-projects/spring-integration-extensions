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

import javax.jms.Destination;

import org.springframework.integration.dsl.core.MessageProducerSpec;
import org.springframework.integration.dsl.support.Consumer;
import org.springframework.integration.jms.ChannelPublishingJmsMessageListener;
import org.springframework.integration.jms.JmsHeaderMapper;
import org.springframework.jms.listener.AbstractMessageListenerContainer;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.util.Assert;

/**
 * @author Artem Bilan
 */
public class JmsMessageDrivenChannelAdapterSpec<S extends JmsMessageDrivenChannelAdapterSpec<S>>
		extends MessageProducerSpec<S, JmsMessageDrivenChannelAdapter> {

	JmsMessageDrivenChannelAdapterSpec(AbstractMessageListenerContainer listenerContainer) {
		super(new JmsMessageDrivenChannelAdapter(listenerContainer, new ChannelPublishingJmsMessageListener()));
		this.target.getListener().setExpectReply(false);
	}

	public S jmsMessageConverter(MessageConverter messageConverter) {
		this.target.getListener().setMessageConverter(messageConverter);
		return _this();
	}

	public S setHeaderMapper(JmsHeaderMapper headerMapper) {
		this.target.getListener().setHeaderMapper(headerMapper);
		return _this();
	}

	public S extractPayload(boolean extractRequestPayload) {
		this.target.getListener().setExtractRequestPayload(extractRequestPayload);
		return _this();
	}


	public static class
			JmsMessageDrivenChannelAdapterListenerContainerSpec<C extends AbstractMessageListenerContainer> extends
			JmsMessageDrivenChannelAdapterSpec<JmsMessageDrivenChannelAdapterListenerContainerSpec<C>> {

		private final JmsListenerContainerSpec<C> spec;

		JmsMessageDrivenChannelAdapterListenerContainerSpec(JmsListenerContainerSpec<C> spec) {
			super(spec.get());
			this.spec = spec;
			this.spec.get().setAutoStartup(false);
		}

		public JmsMessageDrivenChannelAdapterListenerContainerSpec<C> destination(Destination destination) {
			spec.destination(destination);
			return _this();
		}

		public JmsMessageDrivenChannelAdapterListenerContainerSpec<C> destination(String destinationName) {
			spec.destination(destinationName);
			return _this();
		}

		public JmsMessageDrivenChannelAdapterListenerContainerSpec<C> configureListenerContainer(
				Consumer<JmsListenerContainerSpec<C>> configurer) {
			Assert.notNull(configurer);
			configurer.accept(this.spec);
			return _this();
		}

	}

}
