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
package org.springframework.integration.splunk.inbound;

import java.util.List;

import org.springframework.integration.context.IntegrationObjectSupport;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.splunk.event.SplunkEvent;
import org.springframework.integration.splunk.support.SplunkExecutor;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;

/**
 * Polling data from Splunk to generate <code>Message</code>
 *
 * @author Jarred Li
 * @since 1.0
 *
 */
public class SplunkPollingChannelAdapter extends IntegrationObjectSupport implements MessageSource<List<SplunkEvent>> {

	private final SplunkExecutor splunkExecutor;

	/**
	 * Constructor taking a {@link SplunkExecutor} that provide all required Splunk
	 * functionality.
	 *
	 * @param splunkExecutor Must not be null.
	 */
	public SplunkPollingChannelAdapter(SplunkExecutor splunkExecutor) {
		super();
		Assert.notNull(splunkExecutor, "splunkExecutor must not be null.");
		this.splunkExecutor = splunkExecutor;
	}

	/**
	 * Check for mandatory attributes
	 */
	@Override
	protected void onInit() throws Exception {
		super.onInit();
	}

	/**
	 * Uses {@link SplunkExecutor#poll()} to executes the Splunk operation.
	 *
	 * If {@link SplunkExecutor#poll()} returns null, this method will return
	 * <code>null</code>. Otherwise, a new {@link Message} is constructed and returned.
	 */
	public Message<List<SplunkEvent>> receive() {
		List<SplunkEvent> payload = splunkExecutor.poll();
		if (payload == null) {
			return null;
		}
		return MessageBuilder.withPayload(payload).build();
	}

	@Override
	public String getComponentType() {
		return "splunk:inbound-channel-adapter";
	}

}
