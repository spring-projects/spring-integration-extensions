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

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * @author Gary Russell
 * @since 4.1
 *
 */
public class JavaMailSenderBuilder {

	private final JavaMailSenderImpl target = new JavaMailSenderImpl();

	public JavaMailSenderBuilder setJavaMailProperties(Properties javaMailProperties) {
		this.target.setJavaMailProperties(javaMailProperties);
		return this;
	}

	public JavaMailSenderBuilder setProtocol(String protocol) {
		this.target.setProtocol(protocol);
		return this;
	}

	public JavaMailSenderBuilder setHost(String host) {
		this.target.setHost(host);
		return this;
	}

	public JavaMailSenderBuilder setPort(int port) {
		this.target.setPort(port);
		return this;
	}

	public JavaMailSenderBuilder setUsername(String username) {
		this.target.setUsername(username);
		return this;
	}

	public JavaMailSenderBuilder setPassword(String password) {
		this.target.setPassword(password);
		return this;
	}

	public JavaMailSenderBuilder setDefaultEncoding(String defaultEncoding) {
		this.target.setDefaultEncoding(defaultEncoding);
		return this;
	}

	public JavaMailSenderBuilder setDefaultFileTypeMap(FileTypeMap defaultFileTypeMap) {
		this.target.setDefaultFileTypeMap(defaultFileTypeMap);
		return this;
	}

	public JavaMailSender get() {
		return this.target;
	}

}
