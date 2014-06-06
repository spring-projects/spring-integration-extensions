/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.springframework.integration.splunk.event;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * @author David Turanski
 *
 */
public class SplunkEventTests {
	@Test
	public void testGetEventData() {
		Map<String,String> data = new HashMap<String,String>();
		data.put("foo", "foo");
		data.put("bar", null);
		SplunkEvent event = new SplunkEvent(data);
		Map<String,String> eventData = event.getEventData();
		assertEquals(data.size(),eventData.size());
		for (String key: eventData.keySet()) {
			assertEquals(data.get(key),eventData.get(key));
		}
	}
	public void testGetEventDataEmpty() {
		SplunkEvent event = new SplunkEvent( );
		Map<String,String> eventData = event.getEventData();
		assertEquals(0,eventData.size());
	}

	@Test(expected=RuntimeException.class)
	public void testKeyCannotBeNull() {
		SplunkEvent event = new SplunkEvent();
		event.addPair(null, "foo");
	}

	@Test
	public void testCopyConstructor() {
		Map<String,String> data = new HashMap<String,String>();
		data.put("foo", "foo");
		data.put("bar", "bar");
		SplunkEvent event = new SplunkEvent(data);
		SplunkEvent event2 = new SplunkEvent(event);
		assertEquals(event.quoteValues, event2.quoteValues);
		assertEquals(event.useInternalDate,event2.useInternalDate);
		Map<String,String> eventData = event.getEventData();
		Map<String,String> event2Data = event2.getEventData();

		assertEquals(eventData.size(),event2Data.size());
		for (String key: eventData.keySet()) {
			assertEquals(eventData.get(key),event2Data.get(key));
		}
	}
}
