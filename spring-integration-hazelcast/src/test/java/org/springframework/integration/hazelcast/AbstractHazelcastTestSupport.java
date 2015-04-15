/*
 * Copyright 2015 the original author or authors.
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

package org.springframework.integration.hazelcast;

import org.junit.Assert;

import org.springframework.integration.hazelcast.message.EntryEventMessagePayload;
import org.springframework.messaging.Message;

import com.hazelcast.core.EntryEventType;

/**
 * Base Class for Hazelcast Test Support
 *
 * @author Eren Avsarogullari
 * @since 1.0.0
 */
public class AbstractHazelcastTestSupport {

	protected void verifyEntryEvent(Message<?> msg, String cacheName, EntryEventType event) {
		Assert.assertNotNull(msg);
		Assert.assertNotNull(msg.getPayload());
		if (event == EntryEventType.CLEAR_ALL || event == EntryEventType.EVICT_ALL) {
			Assert.assertTrue(msg.getPayload() instanceof Integer);
		}
		else {
			Assert.assertTrue(msg.getPayload() instanceof EntryEventMessagePayload);
		}

		Assert.assertEquals(cacheName, msg.getHeaders().get(HazelcastHeaders.CACHE_NAME));
		Assert.assertEquals(event.name(), msg.getHeaders().get(HazelcastHeaders.EVENT_TYPE));
		Assert.assertNotNull(msg.getHeaders().get(HazelcastHeaders.MEMBER));
	}

	protected void verifyItemEvent(Message<?> msg, EntryEventType event) {
		Assert.assertNotNull(msg);
		Assert.assertNotNull(msg.getPayload());
		Assert.assertNotNull(msg.getHeaders().get(HazelcastHeaders.MEMBER));
		Assert.assertEquals(event.toString(), msg.getHeaders().get(HazelcastHeaders.EVENT_TYPE).toString());
	}

}
