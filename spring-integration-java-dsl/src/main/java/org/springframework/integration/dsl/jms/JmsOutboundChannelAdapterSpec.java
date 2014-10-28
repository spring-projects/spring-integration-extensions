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

import org.springframework.integration.dsl.core.MessageHandlerSpec;
import org.springframework.integration.dsl.support.Consumer;
import org.springframework.integration.dsl.support.Function;
import org.springframework.integration.dsl.support.FunctionExpression;
import org.springframework.integration.jms.JmsHeaderMapper;
import org.springframework.integration.jms.JmsSendingMessageHandler;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;

/**
 * @author Artem Bilan
 */
public class JmsOutboundChannelAdapterSpec<S extends JmsOutboundChannelAdapterSpec<S>>
		extends MessageHandlerSpec<S, JmsSendingMessageHandler> {

	protected final JmsTemplateSpec jmsTemplateSpec = new JmsTemplateSpec();

	JmsOutboundChannelAdapterSpec(JmsTemplate jmsTemplate) {
		this.target = new JmsSendingMessageHandler(jmsTemplate);
	}

	private JmsOutboundChannelAdapterSpec(ConnectionFactory connectionFactory) {
		this.target = new JmsSendingMessageHandler(this.jmsTemplateSpec.connectionFactory(connectionFactory).get());
	}

	public S extractPayload(boolean extractPayload) {
		this.target.setExtractPayload(extractPayload);
		return _this();
	}

	public S headerMapper(JmsHeaderMapper headerMapper) {
		this.target.setHeaderMapper(headerMapper);
		return _this();
	}

	public S destination(Destination destination) {
		this.target.setDestination(destination);
		return _this();
	}

	public S destination(String destination) {
		this.target.setDestinationName(destination);
		return _this();
	}

	public S destinationExpression(String destination) {
		this.target.setDestinationExpression(PARSER.parseExpression(destination));
		return _this();
	}

	public <P> S destination(Function<Message<P>, ?> destinationFunction) {
		this.target.setDestinationExpression(new FunctionExpression<Message<P>>(destinationFunction));
		return _this();
	}

	@Override
	protected JmsSendingMessageHandler doGet() {
		return null;
	}

	public static class JmsOutboundChannelSpecTemplateAware extends
			JmsOutboundChannelAdapterSpec<JmsOutboundChannelSpecTemplateAware> {

		JmsOutboundChannelSpecTemplateAware(ConnectionFactory connectionFactory) {
			super(connectionFactory);
		}

		public JmsOutboundChannelSpecTemplateAware configureJmsTemplate(Consumer<JmsTemplateSpec> configurer) {
			Assert.notNull(configurer);
			configurer.accept(this.jmsTemplateSpec);
			return _this();
		}

	}

}
