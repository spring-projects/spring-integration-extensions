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

package org.springframework.integration.dsl.core;

import org.springframework.integration.gateway.MessagingGatewaySupport;
import org.springframework.integration.mapping.InboundMessageMapper;
import org.springframework.integration.mapping.OutboundMessageMapper;
import org.springframework.messaging.MessageChannel;

/**
 * @author Artem Bilan
 */
public abstract class MessagingGatewaySpec<S extends MessagingGatewaySpec<S, G>, G extends MessagingGatewaySupport>
		extends IntegrationComponentSpec<S, G> {

	public MessagingGatewaySpec(G gateway) {
		this.target = gateway;
	}

	public S id(String id) {
		this.target.setBeanName(id);
		return super.id(id);
	}

	public S phase(int phase) {
		this.target.setPhase(phase);
		return _this();
	}

	public S autoStartup(boolean autoStartup) {
		this.target.setAutoStartup(autoStartup);
		return _this();
	}

	public S replyChannel(MessageChannel replyChannel) {
		this.target.setReplyChannel(replyChannel);
		return _this();
	}

	public S requestChannel(MessageChannel requestChannel) {
		target.setRequestChannel(requestChannel);
		return _this();
	}

	public S errorChannel(MessageChannel errorChannel) {
		target.setErrorChannel(errorChannel);
		return _this();
	}

	public S requestTimeout(long requestTimeout) {
		target.setRequestTimeout(requestTimeout);
		return _this();
	}

	public S replyTimeout(long replyTimeout) {
		target.setReplyTimeout(replyTimeout);
		return _this();
	}

	public S requestMapper(InboundMessageMapper<?> requestMapper) {
		target.setRequestMapper(requestMapper);
		return _this();
	}

	public S replyMapper(OutboundMessageMapper<?> replyMapper) {
		target.setReplyMapper(replyMapper);
		return _this();
	}

	@Override
	protected final G doGet() {
		throw new UnsupportedOperationException();
	}

}
