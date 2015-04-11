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
import com.hazelcast.core.IMap;

/**
 * Hazelcast Distributed Map Event Driven Inbound Channel Adapter Test
 *
 * @author Eren Avsarogullari
 * @since 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@DirtiesContext
@SuppressWarnings("unchecked")
public class HazelcastDistributedMapEventDrivenInboundChannelAdapterTests {

	@Autowired
	private PollableChannel edMapChannel1;

	@Autowired
	private PollableChannel edMapChannel2;

	@Autowired
	private PollableChannel edMapChannel3;

	@Autowired
	private PollableChannel edMapChannel4;

	@Resource
	private IMap<Integer, HazelcastIntegrationTestUser> edDistributedMap1;

	@Resource
	private IMap<Integer, HazelcastIntegrationTestUser> edDistributedMap2;

	@Resource
	private IMap<Integer, HazelcastIntegrationTestUser> edDistributedMap3;

	@Resource
	private IMap<Integer, HazelcastIntegrationTestUser> edDistributedMap4;

	@Test
	public void testEventDrivenForOnlyADDEDEntryEvent() {
		edDistributedMap1.put(1, new HazelcastIntegrationTestUser(1, "TestName1", "TestSurname1"));
		Message<?> msg = edMapChannel1.receive(2_000);
		Assert.assertNotNull(msg);
		Assert.assertNotNull(msg.getPayload());
		Assert.assertTrue(msg.getPayload() instanceof EntryEventMessagePayload);
		Assert.assertNotNull(msg.getHeaders().get(HazelcastHeaders.MEMBER));
		Assert.assertEquals(EntryEventType.ADDED, msg.getHeaders().get(HazelcastHeaders.EVENT));
		Assert.assertEquals("edDistributedMap1", msg.getHeaders().get(HazelcastHeaders.NAME));

		Assert.assertEquals(Integer.valueOf(1),
				((EntryEventMessagePayload<Integer, HazelcastIntegrationTestUser>) msg.getPayload()).getKey());
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
	public void testEventDrivenForOnlyUPDATEDEntryEvent() {
		edDistributedMap2.put(2, new HazelcastIntegrationTestUser(1, "TestName1", "TestSurname1"));
		edDistributedMap2.put(2, new HazelcastIntegrationTestUser(2, "TestName2", "TestSurname2"));
		Message<?> msg = edMapChannel2.receive(2_000);
		Assert.assertNotNull(msg);
		Assert.assertNotNull(msg.getPayload());
		Assert.assertTrue(msg.getPayload() instanceof EntryEventMessagePayload);
		Assert.assertNotNull(msg.getHeaders().get(HazelcastHeaders.MEMBER));
		Assert.assertEquals(EntryEventType.UPDATED, msg.getHeaders().get(HazelcastHeaders.EVENT));
		Assert.assertEquals("edDistributedMap2", msg.getHeaders().get(HazelcastHeaders.NAME));

		Assert.assertEquals(Integer.valueOf(2),
				((EntryEventMessagePayload<Integer, HazelcastIntegrationTestUser>) msg
						.getPayload()).getKey());
		Assert.assertEquals(1,
				(((EntryEventMessagePayload<Integer, HazelcastIntegrationTestUser>) msg
						.getPayload()).getOldValue()).getId());
		Assert.assertEquals("TestName1",
				(((EntryEventMessagePayload<Integer, HazelcastIntegrationTestUser>) msg
						.getPayload()).getOldValue()).getName());
		Assert.assertEquals("TestSurname1",
				(((EntryEventMessagePayload<Integer, HazelcastIntegrationTestUser>) msg
						.getPayload()).getOldValue()).getSurname());
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
	public void testEventDrivenForOnlyREMOVEDEntryEvent() {
		edDistributedMap3.put(1, new HazelcastIntegrationTestUser(1, "TestName1", "TestSurname1"));
		edDistributedMap3.put(2, new HazelcastIntegrationTestUser(2, "TestName2", "TestSurname2"));
		edDistributedMap3.remove(2);
		Message<?> msg = edMapChannel3.receive(2_000);
		Assert.assertNotNull(msg);
		Assert.assertNotNull(msg.getPayload());
		Assert.assertTrue(msg.getPayload() instanceof EntryEventMessagePayload);
		Assert.assertNotNull(msg.getHeaders().get(HazelcastHeaders.MEMBER));
		Assert.assertEquals(EntryEventType.REMOVED, msg.getHeaders().get(HazelcastHeaders.EVENT));
		Assert.assertEquals("edDistributedMap3", msg.getHeaders().get(HazelcastHeaders.NAME));

		Assert.assertEquals(Integer.valueOf(2),
				((EntryEventMessagePayload<Integer, HazelcastIntegrationTestUser>) msg
						.getPayload()).getKey());
		Assert.assertEquals(2,
				(((EntryEventMessagePayload<Integer, HazelcastIntegrationTestUser>) msg
						.getPayload()).getOldValue()).getId());
		Assert.assertEquals("TestName2",
				(((EntryEventMessagePayload<Integer, HazelcastIntegrationTestUser>) msg
						.getPayload()).getOldValue()).getName());
		Assert.assertEquals("TestSurname2",
				(((EntryEventMessagePayload<Integer, HazelcastIntegrationTestUser>) msg
						.getPayload()).getOldValue()).getSurname());
	}

	@Test
	public void testEventDrivenForALLEntryEvent() {
		edDistributedMap4.put(1, new HazelcastIntegrationTestUser(1, "TestName1", "TestSurname1"));
		Message<?> msg = edMapChannel4.receive(2_000);
		verify(msg, "edDistributedMap4", EntryEventType.ADDED);

		edDistributedMap4.put(1, new HazelcastIntegrationTestUser(1, "TestName1", "TestSurnameUpdated"));
		msg = edMapChannel4.receive(2_000);
		verify(msg, "edDistributedMap4", EntryEventType.UPDATED);

		edDistributedMap4.remove(1);
		msg = edMapChannel4.receive(2_000);
		verify(msg, "edDistributedMap4", EntryEventType.REMOVED);

		edDistributedMap4.put(2, new HazelcastIntegrationTestUser(2, "TestName2", "TestSurname2"));
		msg = edMapChannel4.receive(2_000);
		verify(msg, "edDistributedMap4", EntryEventType.ADDED);

		edDistributedMap4.clear();
		msg = edMapChannel4.receive(2_000);
		verify(msg, "edDistributedMap4", EntryEventType.CLEAR_ALL);
	}

	private void verify(Message<?> msg, String cacheName, EntryEventType event) {
		Assert.assertNotNull(msg);
		Assert.assertNotNull(msg.getPayload());
		Assert.assertTrue(msg.getPayload() instanceof EntryEventMessagePayload);
		Assert.assertEquals(cacheName, msg.getHeaders().get(HazelcastHeaders.NAME));
		Assert.assertEquals(event, msg.getHeaders().get(HazelcastHeaders.EVENT));
	}

}
