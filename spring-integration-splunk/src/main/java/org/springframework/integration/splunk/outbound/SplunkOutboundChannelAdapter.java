/*
 * Copyright 2002-2012 the original author or authors.
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

package org.springframework.integration.splunk.outbound;

import org.springframework.integration.handler.AbstractReplyProducingMessageHandler;
import org.springframework.integration.splunk.support.SplunkExecutor;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;


/**
 * Handle message and write data into Splunk
 *
 * @author Jarred Li
 * @since 1.0
 *
 */
public class SplunkOutboundChannelAdapter extends AbstractReplyProducingMessageHandler {

	private final SplunkExecutor splunkExecutor;
	private boolean producesReply = true; //false for outbound-channel-adapter, true for outbound-gateway

	/**
	 * Constructor taking an {@link SplunkExecutor} that wraps common
	 * Splunk Operations.
	 *
	 * @param splunkExecutor Must not be null
	 *
	 */
	public SplunkOutboundChannelAdapter(SplunkExecutor splunkExecutor) {
		Assert.notNull(splunkExecutor, "splunkExecutor must not be null.");
		this.splunkExecutor = splunkExecutor;
	}


	@Override
	protected Object handleRequestMessage(Message<?> requestMessage) {
		final Object result;
		result = this.splunkExecutor.write(requestMessage);
		if (result == null || !producesReply) {
			return null;
		}
		return MessageBuilder.withPayload(result).copyHeaders(requestMessage.getHeaders()).build();

	}

	/**
	 * If set to 'false', this component will act as an Outbound Channel Adapter.
	 * If not explicitly set this property will default to 'true'.
	 *
	 * @param producesReply Defaults to 'true'.
	 *
	 */
	public void setProducesReply(boolean producesReply) {
		this.producesReply = producesReply;
	}

}
