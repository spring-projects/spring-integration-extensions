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

import java.io.File;

import org.springframework.amqp.core.Queue;
import org.springframework.integration.dsl.amqp.Amqp;
import org.springframework.integration.dsl.amqp.AmqpInboundChannelAdapterSpec;
import org.springframework.integration.dsl.file.Files;
import org.springframework.integration.dsl.file.TailAdapterSpec;
import org.springframework.integration.dsl.jms.Jms;
import org.springframework.integration.dsl.jms.JmsMessageDrivenChannelAdapterSpec;
import org.springframework.integration.dsl.mail.ImapIdleChannelAdapterSpec;
import org.springframework.integration.dsl.mail.Mail;
import org.springframework.jms.listener.AbstractMessageListenerContainer;

/**
 * @author Artem Bilan
 */
public class MessageProducers {

	public AmqpInboundChannelAdapterSpec amqp(
			org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory, String... queueNames) {
		return Amqp.inboundAdapter(connectionFactory, queueNames);
	}

	public AmqpInboundChannelAdapterSpec amqp(
			org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory, Queue... queues) {
		return Amqp.inboundAdapter(connectionFactory, queues);
	}

	public TailAdapterSpec tail(File file) {
		return Files.tailAdapter(file);
	}

	public ImapIdleChannelAdapterSpec imap(String url) {
		return Mail.imapIdleAdapter(url);
	}

	public JmsMessageDrivenChannelAdapterSpec<? extends JmsMessageDrivenChannelAdapterSpec<?>> jms(
			AbstractMessageListenerContainer listenerContainer) {
		return Jms.messageDriverChannelAdapter(listenerContainer);
	}

	public JmsMessageDrivenChannelAdapterSpec<? extends JmsMessageDrivenChannelAdapterSpec<?>> jms(
			javax.jms.ConnectionFactory connectionFactory) {
		return Jms.messageDriverChannelAdapter(connectionFactory);
	}

	public <C extends AbstractMessageListenerContainer>
	JmsMessageDrivenChannelAdapterSpec<? extends JmsMessageDrivenChannelAdapterSpec<?>> jms(
			javax.jms.ConnectionFactory connectionFactory,
			Class<C> containerClass) {
		return Jms.messageDriverChannelAdapter(connectionFactory, containerClass);
	}

	MessageProducers() {
	}

}
