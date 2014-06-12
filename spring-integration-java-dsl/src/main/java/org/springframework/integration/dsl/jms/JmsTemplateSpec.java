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

import org.springframework.integration.jms.DynamicJmsTemplate;
import org.springframework.jms.support.converter.MessageConverter;

/**
 * @author Artem Bilan
 */
public class JmsTemplateSpec extends JmsDestinationAccessorSpec<JmsTemplateSpec, DynamicJmsTemplate> {

	JmsTemplateSpec() {
		super(new DynamicJmsTemplate());
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

}
