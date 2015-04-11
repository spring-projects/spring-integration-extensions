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

package org.springframework.integration.hazelcast.inbound;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.hazelcast.HazelcastIntegrationTestUser;
import org.springframework.integration.hazelcast.message.EntryEventMessagePayload;
import org.springframework.integration.hazelcast.message.HazelcastHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.PollableChannel;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.hazelcast.core.EntryEventType;
import com.hazelcast.core.MultiMap;

/**
 * Hazelcast MultiMap Event Driven Inbound Channel Adapter Test
 *
 * @author Eren Avsarogullari
 * @since 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@DirtiesContext
@SuppressWarnings("unchecked")
public class HazelcastMultiMapEventDrivenInboundChannelAdapterTests {

	@Autowired
	private PollableChannel edMultiMapChannel1;

	@Autowired
	private PollableChannel edMultiMapChannel2;

	@Autowired
	private PollableChannel edMultiMapChannel3;

	@Resource
	private MultiMap<Integer, HazelcastIntegrationTestUser> edMultiMap1;

	@Resource
	private MultiMap<Integer, HazelcastIntegrationTestUser> edMultiMap2;

	@Resource
	private MultiMap<Integer, HazelcastIntegrationTestUser> edMultiMap3;

	@Test
	public void testEventDrivenForOnlyADDEDEntryEvent() {
		edMultiMap1.put(1, new HazelcastIntegrationTestUser(1, "TestName1", "TestSurname1"));
		Message<?> msg = edMultiMapChannel1.receive(2_000);
		Assert.assertNotNull(msg);
		Assert.assertNotNull(msg.getPayload());
		Assert.assertTrue(msg.getPayload() instanceof EntryEventMessagePayload);
		Assert.assertNotNull(msg.getHeaders().get(HazelcastHeaders.MEMBER));
		Assert.assertEquals(EntryEventType.ADDED, msg.getHeaders().get(HazelcastHeaders.EVENT));
		Assert.assertEquals("edMultiMap1", msg.getHeaders().get(HazelcastHeaders.NAME));

		Assert.assertEquals(Integer.valueOf(1),
				((EntryEventMessagePayload<Integer, HazelcastIntegrationTestUser>) msg
						.getPayload()).getKey());
		Assert.assertEquals(1,
				(((EntryEventMessagePayload<Integer, HazelcastIntegrationTestUser>) msg
						.getPayload()).getValue()).getId());
		Assert.assertEquals("TestName1",
				(((EntryEventMessagePayload<Integer, HazelcastIntegrationTestUser>) msg
						.getPayload()).getValue()).getName());
		Assert.assertEquals("TestSurname1",
				(((EntryEventMessagePayload<Integer, HazelcastIntegrationTestUser>) msg
						.getPayload()).getValue()).getSurname());
	}

	@Test
	public void testEventDrivenForOnlyREMOVEDEntryEvent() {
		edMultiMap2.put(1, new HazelcastIntegrationTestUser(1, "TestName1", "TestSurname1"));
		edMultiMap2.put(2, new HazelcastIntegrationTestUser(2, "TestName2", "TestSurname2"));
		edMultiMap2.remove(2);
		Message<?> msg = edMultiMapChannel2.receive(2_000);
		Assert.assertNotNull(msg);
		Assert.assertNotNull(msg.getPayload());
		Assert.assertTrue(msg.getPayload() instanceof EntryEventMessagePayload);
		Assert.assertNotNull(msg.getHeaders().get(HazelcastHeaders.MEMBER));
		Assert.assertEquals(EntryEventType.REMOVED, msg.getHeaders().get(HazelcastHeaders.EVENT));
		Assert.assertEquals("edMultiMap2", msg.getHeaders().get(HazelcastHeaders.NAME));

		Assert.assertEquals(Integer.valueOf(2),
				((EntryEventMessagePayload<Integer, HazelcastIntegrationTestUser>) msg
						.getPayload()).getKey());
		Assert.assertEquals(2,
				(((EntryEventMessagePayload<Integer, HazelcastIntegrationTestUser>) msg
						.getPayload()).getValue()).getId());
		Assert.assertEquals("TestName2",
				(((EntryEventMessagePayload<Integer, HazelcastIntegrationTestUser>) msg
						.getPayload()).getValue()).getName());
		Assert.assertEquals("TestSurname2",
				(((EntryEventMessagePayload<Integer, HazelcastIntegrationTestUser>) msg
						.getPayload()).getValue()).getSurname());
	}

	@Test
	public void testEventDrivenForALLEntryEvent() {
		edMultiMap3.put(1, new HazelcastIntegrationTestUser(1, "TestName1", "TestSurname1"));
		Message<?> msg = edMultiMapChannel3.receive(2_000);
		verify(msg, "edMultiMap3", EntryEventType.ADDED);

		edMultiMap3.put(1, new HazelcastIntegrationTestUser(1, "TestName1", "TestSurnameUpdated"));
		msg = edMultiMapChannel3.receive(2_000);
		verify(msg, "edMultiMap3", EntryEventType.ADDED);

		edMultiMap3.remove(1);
		msg = edMultiMapChannel3.receive(2_000);
		verify(msg, "edMultiMap3", EntryEventType.REMOVED);
		msg = edMultiMapChannel3.receive(2_000);
		verify(msg, "edMultiMap3", EntryEventType.REMOVED);

		edMultiMap3.put(2, new HazelcastIntegrationTestUser(2, "TestName2", "TestSurname2"));
		msg = edMultiMapChannel3.receive(2_000);
		verify(msg, "edMultiMap3", EntryEventType.ADDED);

		edMultiMap3.clear();
		msg = edMultiMapChannel3.receive(2_000);
		verify(msg, "edMultiMap3", EntryEventType.CLEAR_ALL);
	}

	private void verify(Message<?> msg, String cacheName, EntryEventType event) {
		Assert.assertNotNull(msg);
		Assert.assertNotNull(msg.getPayload());
		Assert.assertTrue(msg.getPayload() instanceof EntryEventMessagePayload);
		Assert.assertEquals(cacheName, msg.getHeaders().get(HazelcastHeaders.NAME));
		Assert.assertEquals(event, msg.getHeaders().get(HazelcastHeaders.EVENT));
	}

}
