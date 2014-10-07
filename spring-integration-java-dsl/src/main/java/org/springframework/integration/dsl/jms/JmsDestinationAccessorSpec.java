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
import org.springframework.jms.support.destination.DestinationResolver;
import org.springframework.jms.support.destination.JmsDestinationAccessor;

/**
 * @author Artem Bilan
 */
public abstract class
		JmsDestinationAccessorSpec<S extends JmsDestinationAccessorSpec<S, A>, A extends JmsDestinationAccessor>
		extends IntegrationComponentSpec<S, A> {

	protected JmsDestinationAccessorSpec(A accessor) {
		this.target = accessor;
	}

	S connectionFactory(ConnectionFactory connectionFactory) {
		this.target.setConnectionFactory(connectionFactory);
		return _this();
	}

	public S destinationResolver(DestinationResolver destinationResolver) {
		this.target.setDestinationResolver(destinationResolver);
		return _this();
	}

	public S pubSubDomain(boolean pubSubDomain) {
		target.setPubSubDomain(pubSubDomain);
		return _this();
	}

	/**
	 * @param sessionAcknowledgeMode the acknowledgement mode constant
	 * @return the current {@link org.springframework.integration.dsl.channel.MessageChannelSpec}
	 * @see javax.jms.Session#AUTO_ACKNOWLEDGE etc.
	 */
	public S sessionAcknowledgeMode(int sessionAcknowledgeMode) {
		this.target.setSessionAcknowledgeMode(sessionAcknowledgeMode);
		return _this();
	}

	public S sessionAcknowledgeModeName(String constantName) {
		target.setSessionAcknowledgeModeName(constantName);
		return _this();
	}

	public S sessionTransacted(boolean sessionTransacted) {
		this.target.setSessionTransacted(sessionTransacted);
		return _this();
	}

	@Override
	protected A doGet() {
		throw new UnsupportedOperationException();
	}

}
