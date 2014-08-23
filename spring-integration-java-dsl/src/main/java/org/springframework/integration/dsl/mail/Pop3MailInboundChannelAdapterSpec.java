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

import org.springframework.integration.mail.Pop3MailReceiver;

/**
 * @author Gary Russell
 *
 */
public class Pop3MailInboundChannelAdapterSpec
		extends MailInboundChannelAdapterSpec<Pop3MailInboundChannelAdapterSpec, Pop3MailReceiver> {

	Pop3MailInboundChannelAdapterSpec() {
		this.receiver = new Pop3MailReceiver();
	}

	Pop3MailInboundChannelAdapterSpec(String url) {
		this.receiver = new Pop3MailReceiver(url);
	}

	Pop3MailInboundChannelAdapterSpec(String host, String username, String password) {
		this.receiver = new Pop3MailReceiver(host, username, password);
	}

	Pop3MailInboundChannelAdapterSpec(String host, int port, String username, String password) {
		this.receiver = new Pop3MailReceiver(host, port, username, password);
	}

}
