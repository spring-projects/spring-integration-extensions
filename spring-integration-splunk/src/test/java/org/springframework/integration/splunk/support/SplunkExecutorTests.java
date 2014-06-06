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
package org.springframework.integration.splunk.support;

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.springframework.integration.splunk.core.DataReader;
import org.springframework.integration.splunk.core.DataWriter;
import org.springframework.integration.splunk.event.SplunkEvent;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

/**
 * @author Jarred Li
 * @since 1.0
 *
 */
public class SplunkExecutorTests {

	private SplunkExecutor executor;

	private DataReader reader = mock(DataReader.class);

	private DataWriter writer = mock(DataWriter.class);

	@Before
	public void before() {
		executor = new SplunkExecutor();
		executor.setReader(reader);
		executor.setWriter(writer);
	}


	/**
	 * Test method for {@link org.springframework.integration.splunk.support.SplunkExecutor#handleMessage(org.springframework.integration.Message)}.
	 * @throws Exception
	 */
	@Test
	public void testHandleMessage() throws Exception {
		SplunkEvent sd = new SplunkEvent("spring", "spring:example");
		sd.setCommonDesc("description");
		Message<SplunkEvent> message = MessageBuilder.withPayload(sd).build();
		executor.handleMessage(message);
		verify(writer).write(sd);
	}

	/**
	 * Test method for {@link org.springframework.integration.splunk.support.SplunkExecutor#poll()}.
	 * @throws Exception
	 */
	@Test
	public void testPoll() throws Exception {
		List<SplunkEvent> data = new ArrayList<SplunkEvent>();
		SplunkEvent sd = new SplunkEvent("spring", "spring:example");
		sd.setCommonDesc("description");
		data.add(sd);

		sd = new SplunkEvent("spring", "spring:example");
		sd.setCommonDesc("description");
		data.add(sd);
		when(reader.read()).thenReturn(data);

		List<SplunkEvent> result = executor.poll();
		Assert.assertEquals(2, result.size());

	}

}
