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
import javax.jms.ExceptionListener;

import org.springframework.jms.listener.AbstractMessageListenerContainer;
import org.springframework.util.ErrorHandler;

/**
 * @author Artem Bilan
 */
public class JmsListenerContainerSpec<C extends AbstractMessageListenerContainer>
		extends JmsDestinationAccessorSpec<JmsListenerContainerSpec<C>, C> {

	JmsListenerContainerSpec(Class<C> aClass) throws Exception {
		super(aClass.newInstance());
	}

	JmsListenerContainerSpec<C> destination(Destination destination) {
		target.setDestination(destination);
		return _this();
	}

	JmsListenerContainerSpec<C> destination(String destinationName) {
		target.setDestinationName(destinationName);
		return _this();
	}

	public JmsListenerContainerSpec<C> messageSelector(String messageSelector) {
		target.setMessageSelector(messageSelector);
		return _this();
	}

	public JmsListenerContainerSpec<C> subscriptionDurable(boolean subscriptionDurable) {
		target.setSubscriptionDurable(subscriptionDurable);
		return _this();
	}

	public JmsListenerContainerSpec<C> durableSubscriptionName(String durableSubscriptionName) {
		target.setDurableSubscriptionName(durableSubscriptionName);
		return _this();
	}

	public JmsListenerContainerSpec<C> exceptionListener(ExceptionListener exceptionListener) {
		target.setExceptionListener(exceptionListener);
		return _this();
	}

	public JmsListenerContainerSpec<C> errorHandler(ErrorHandler errorHandler) {
		target.setErrorHandler(errorHandler);
		return _this();
	}

	public JmsListenerContainerSpec<C> exposeListenerSession(boolean exposeListenerSession) {
		target.setExposeListenerSession(exposeListenerSession);
		return _this();
	}

	public JmsListenerContainerSpec<C> acceptMessagesWhileStopping(boolean acceptMessagesWhileStopping) {
		target.setAcceptMessagesWhileStopping(acceptMessagesWhileStopping);
		return _this();
	}

	public JmsListenerContainerSpec<C> clientId(String clientId) {
		target.setClientId(clientId);
		return _this();
	}

}
