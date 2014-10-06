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

import org.springframework.integration.dsl.core.MessageSourceSpec;
import org.springframework.integration.dsl.support.Consumer;
import org.springframework.integration.jms.JmsDestinationPollingSource;
import org.springframework.integration.jms.JmsHeaderMapper;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.util.Assert;

/**
 * @author Artem Bilan
 */
public class JmsInboundChannelAdapterSpec<S extends JmsInboundChannelAdapterSpec<S>>
		extends MessageSourceSpec<S, JmsDestinationPollingSource> {

	final JmsTemplateSpec jmsTemplateSpec = new JmsTemplateSpec();

	JmsInboundChannelAdapterSpec(JmsTemplate jmsTemplate) {
		this.target = new JmsDestinationPollingSource(jmsTemplate);
	}

	private JmsInboundChannelAdapterSpec(ConnectionFactory connectionFactory) {
		this.target = new JmsDestinationPollingSource(this.jmsTemplateSpec.connectionFactory(connectionFactory).get());
	}

	public S messageSelector(String messageSelector) {
		this.target.setMessageSelector(messageSelector);
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

	@Override
	protected JmsDestinationPollingSource doGet() {
		throw new UnsupportedOperationException();
	}

	public static class JmsInboundChannelSpecTemplateAware extends
			JmsInboundChannelAdapterSpec<JmsInboundChannelSpecTemplateAware> {

		JmsInboundChannelSpecTemplateAware(ConnectionFactory connectionFactory) {
			super(connectionFactory);
		}

		public JmsInboundChannelSpecTemplateAware configureJmsTemplate(Consumer<JmsTemplateSpec> configurer) {
			Assert.notNull(configurer);
			configurer.accept(this.jmsTemplateSpec);
			return _this();
		}

	}

}
