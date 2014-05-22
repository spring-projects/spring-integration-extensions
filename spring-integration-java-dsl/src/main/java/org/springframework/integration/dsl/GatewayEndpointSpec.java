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

import org.springframework.integration.dsl.core.ConsumerEndpointSpec;
import org.springframework.messaging.MessageChannel;

/**
 * @author Artem Bilan
 */
public final class GatewayEndpointSpec extends ConsumerEndpointSpec<GatewayEndpointSpec, GatewayMessageHandler> {

	GatewayEndpointSpec(MessageChannel requestChannel) {
		super(new GatewayMessageHandler());
		this.target.getT2().setRequestChannel(requestChannel);
	}

	GatewayEndpointSpec(String requestChannel) {
		super(new GatewayMessageHandler());
		this.target.getT2().setRequestChannelName(requestChannel);
	}

	public GatewayEndpointSpec replyChannel(MessageChannel replyChannel) {
		this.target.getT2().setReplyChannel(replyChannel);
		return this;
	}

	public GatewayEndpointSpec replyChannel(String replyChannel) {
		this.target.getT2().setReplyChannelName(replyChannel);
		return this;
	}

	public GatewayEndpointSpec errorChannel(MessageChannel errorChannel) {
		this.target.getT2().setErrorChannel(errorChannel);
		return this;
	}

	public GatewayEndpointSpec errorChannel(String errorChannel) {
		this.target.getT2().setErrorChannelName(errorChannel);
		return this;
	}

	public GatewayEndpointSpec requestTimeout(Long requestTimeout) {
		this.target.getT2().setRequestTimeout(requestTimeout);
		return this;
	}

	public GatewayEndpointSpec replyTimeout(Long replyTimeout) {
		this.target.getT2().setReplyTimeout(replyTimeout);
		return this;
	}

}
