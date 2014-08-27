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

import java.util.Properties;

import javax.activation.FileTypeMap;

import org.springframework.integration.dsl.core.MessageHandlerSpec;
import org.springframework.integration.dsl.support.PropertiesBuilder;
import org.springframework.integration.dsl.support.PropertiesBuilder.PropertiesConfigurer;
import org.springframework.integration.mail.MailSendingMessageHandler;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * @author Gary Russell
 * @author Artem Bilan
 */
public class MailSendingMessageHandlerSpec
		extends MessageHandlerSpec<MailSendingMessageHandlerSpec, MailSendingMessageHandler> {

	private final JavaMailSenderImpl sender = new JavaMailSenderImpl();

	MailSendingMessageHandlerSpec (String host) {
		this.sender.setHost(host);
		this.target = new MailSendingMessageHandler(this.sender);
	}

	public MailSendingMessageHandlerSpec javaMailProperties(Properties javaMailProperties) {
		this.sender.setJavaMailProperties(javaMailProperties);
		return this;
	}

	public MailSendingMessageHandlerSpec javaMailProperties(PropertiesConfigurer propertiesConfigurer) {
		PropertiesBuilder properties = new PropertiesBuilder();
		propertiesConfigurer.configure(properties);
		return javaMailProperties(properties.get());
	}

	public MailSendingMessageHandlerSpec protocol(String protocol) {
		this.sender.setProtocol(protocol);
		return this;
	}

	public MailSendingMessageHandlerSpec port(int port) {
		this.sender.setPort(port);
		return this;
	}

	public MailSendingMessageHandlerSpec credentials(String username, String password) {
		this.sender.setUsername(username);
		this.sender.setPassword(password);
		return this;
	}

	public MailSendingMessageHandlerSpec defaultEncoding(String defaultEncoding) {
		this.sender.setDefaultEncoding(defaultEncoding);
		return this;
	}

	public MailSendingMessageHandlerSpec defaultFileTypeMap(FileTypeMap defaultFileTypeMap) {
		this.sender.setDefaultFileTypeMap(defaultFileTypeMap);
		return this;
	}

	@Override
	protected MailSendingMessageHandler doGet() {
		throw new UnsupportedOperationException();
	}

}
