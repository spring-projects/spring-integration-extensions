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

import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executor;

import javax.mail.Authenticator;
import javax.mail.Session;

import org.aopalliance.aop.Advice;

import org.springframework.beans.DirectFieldAccessor;
import org.springframework.integration.dsl.core.MessagingProducerSpec;
import org.springframework.integration.dsl.support.PropertiesBuilder;
import org.springframework.integration.dsl.support.PropertiesConfigurer;
import org.springframework.integration.mail.ImapIdleChannelAdapter;
import org.springframework.integration.mail.ImapMailReceiver;
import org.springframework.integration.mail.SearchTermStrategy;
import org.springframework.integration.transaction.TransactionSynchronizationFactory;

/**
 * @author Gary Russell
 *
 */
public class ImapIdleChannelAdapterSpec extends MessagingProducerSpec<ImapIdleChannelAdapterSpec, ImapIdleChannelAdapter>  {

	private final ImapMailReceiver receiver;

	public ImapIdleChannelAdapterSpec() {
		super(new ImapIdleChannelAdapter(new ImapMailReceiver()));
		this.receiver = (ImapMailReceiver) new DirectFieldAccessor(this.target).getPropertyValue("mailReceiver");
	}

	public ImapIdleChannelAdapterSpec(String url) {
		super(new ImapIdleChannelAdapter(new ImapMailReceiver(url)));
		this.receiver = (ImapMailReceiver) new DirectFieldAccessor(this.target).getPropertyValue("mailReceiver");
	}

	public ImapIdleChannelAdapterSpec selectorExpression(String selectorExpression) {
		this.receiver.setSelectorExpression(PARSER.parseExpression(selectorExpression));
		return this;
	}

	public ImapIdleChannelAdapterSpec session(Session session) {
		this.receiver.setSession(session);
		return this;
	}

	public ImapIdleChannelAdapterSpec javaMailProperties(Properties javaMailProperties) {
		this.receiver.setJavaMailProperties(javaMailProperties);
		return this;
	}

	public ImapIdleChannelAdapterSpec javaMailProperties(PropertiesConfigurer configurer) {
		PropertiesBuilder properties = new PropertiesBuilder();
		configurer.configure(properties);
		return javaMailProperties(properties.get());
	}

	public ImapIdleChannelAdapterSpec javaMailAuthenticator(Authenticator javaMailAuthenticator) {
		this.receiver.setJavaMailAuthenticator(javaMailAuthenticator);
		return this;
	}

	public ImapIdleChannelAdapterSpec maxFetchSize(int maxFetchSize) {
		this.receiver.setMaxFetchSize(maxFetchSize);
		return this;
	}

	public ImapIdleChannelAdapterSpec shouldDeleteMessages(boolean shouldDeleteMessages) {
		this.receiver.setShouldDeleteMessages(shouldDeleteMessages);
		return this;
	}

	public ImapIdleChannelAdapterSpec transactionSynchronizationFactory(TransactionSynchronizationFactory transactionSynchronizationFactory) {
		this.target.setTransactionSynchronizationFactory(transactionSynchronizationFactory);
		return this;
	}

	public ImapIdleChannelAdapterSpec adviceChain(List<Advice> adviceChain) {
		this.target.setAdviceChain(adviceChain);
		return this;
	}

	public ImapIdleChannelAdapterSpec sendingTaskExecutor(Executor sendingTaskExecutor) {
		this.target.setSendingTaskExecutor(sendingTaskExecutor);
		return this;
	}

	public ImapIdleChannelAdapterSpec shouldReconnectAutomatically(boolean shouldReconnectAutomatically) {
		this.target.setShouldReconnectAutomatically(shouldReconnectAutomatically);
		return this;
	}

	public ImapIdleChannelAdapterSpec searchTermStrategy(SearchTermStrategy searchTermStrategy) {
		this.receiver.setSearchTermStrategy(searchTermStrategy);
		return this;
	}

	public ImapIdleChannelAdapterSpec shouldMarkMessagesAsRead(boolean shouldMarkMessagesAsRead) {
		this.receiver.setShouldMarkMessagesAsRead(shouldMarkMessagesAsRead);
		return this;
	}
}
