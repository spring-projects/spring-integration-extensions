/*
 * Copyright 2011-2012 the original author or authors.
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

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.springframework.integration.splunk.event.SplunkEvent;
import org.springframework.integration.splunk.support.SplunkExecutor;

/**
 * @author Jarred Li
 * @since 1.0
 *
 */
public class SplunkPollingChannelAdapterTests {

	private SplunkPollingChannelAdapter inboundAdapter;

	private SplunkExecutor executor;

	@Before
	public void init() {
		executor = mock(SplunkExecutor.class);
		inboundAdapter = new SplunkPollingChannelAdapter(executor);
	}

	/**
	 * Test method for {@link org.springframework.integration.splunk.inbound.SplunkPollingChannelAdapter#receive()}.
	 */
	@Test
	public void testReceive() {
		List<SplunkEvent> data = new ArrayList<SplunkEvent>();
		SplunkEvent sd = new SplunkEvent("spring", "spring:example");
		sd.setCommonDesc("description");
		data.add(sd);
		when(executor.poll()).thenReturn(data);

		List<SplunkEvent> received = inboundAdapter.receive().getPayload();
		Assert.assertEquals(1, received.size());
	}

	/**
	 * Test method for {@link org.springframework.integration.splunk.inbound.SplunkPollingChannelAdapter#getComponentType()}.
	 */
	@Test
	public void testGetComponentType() {
		Assert.assertEquals("splunk:inbound-channel-adapter", inboundAdapter.getComponentType());
	}

}
