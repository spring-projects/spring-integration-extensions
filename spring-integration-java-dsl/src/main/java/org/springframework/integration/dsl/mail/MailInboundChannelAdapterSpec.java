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
package org.springframework.integration.dsl.mail;

import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Session;

import org.springframework.integration.dsl.core.ComponentsRegistration;
import org.springframework.integration.dsl.core.MessageSourceSpec;
import org.springframework.integration.dsl.support.PropertiesBuilder;
import org.springframework.integration.dsl.support.PropertiesBuilder.PropertiesConfigurer;
import org.springframework.integration.mail.AbstractMailReceiver;
import org.springframework.integration.mail.MailReceivingMessageSource;

/**
 * @author Gary Russell
 *
 */
public abstract class MailInboundChannelAdapterSpec<S extends MailInboundChannelAdapterSpec<S, R>,
		R extends AbstractMailReceiver>
		extends MessageSourceSpec<S, MailReceivingMessageSource>
		implements ComponentsRegistration {

	protected volatile R receiver;

	public S selectorExpression(String selectorExpression) {
		this.receiver.setSelectorExpression(PARSER.parseExpression(selectorExpression));
		return _this();
	}

	public S session(Session session) {
		this.receiver.setSession(session);
		return _this();
	}

	public S javaMailProperties(Properties javaMailProperties) {
		this.receiver.setJavaMailProperties(javaMailProperties);
		return _this();
	}

	public S javaMailProperties(PropertiesConfigurer configurer) {
		PropertiesBuilder properties = new PropertiesBuilder();
		configurer.configure(properties);
		return javaMailProperties(properties.get());
	}

	public S javaMailAuthenticator(Authenticator javaMailAuthenticator) {
		this.receiver.setJavaMailAuthenticator(javaMailAuthenticator);
		return _this();
	}

	public S maxFetchSize(int maxFetchSize) {
		this.receiver.setMaxFetchSize(maxFetchSize);
		return _this();
	}

	public S shouldDeleteMessages(boolean shouldDeleteMessages) {
		this.receiver.setShouldDeleteMessages(shouldDeleteMessages);
		return _this();
	}

	@Override
	public Collection<Object> getComponentsToRegister() {
		return Collections.<Object>singletonList(this.receiver);
	}

	@Override
	public MailReceivingMessageSource doGet() {
		return new MailReceivingMessageSource(this.receiver);
	}

}
