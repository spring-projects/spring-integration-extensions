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

import com.hazelcast.core.AbstractIMapEvent;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryEventType;
import com.hazelcast.core.ReplicatedMap;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.hazelcast.HazelcastIntegrationTestUser;
import org.springframework.messaging.Message;
import org.springframework.messaging.PollableChannel;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Hazelcast Replicated Map Event Driven Inbound Channel Adapter Test
 *
 * @author Eren Avsarogullari
 * @since 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@DirtiesContext
public class HazelcastReplicatedMapEventDrivenInboundChannelAdapterTests {

	@Autowired
	private PollableChannel edReplicatedMapChannel1;

	@Autowired
	private PollableChannel edReplicatedMapChannel2;

	@Autowired
	private PollableChannel edReplicatedMapChannel3;

	@Autowired
	private PollableChannel edReplicatedMapChannel4;

	@Resource
	private ReplicatedMap<Integer, HazelcastIntegrationTestUser> edReplicatedMap1;

	@Resource
	private ReplicatedMap<Integer, HazelcastIntegrationTestUser> edReplicatedMap2;

	@Resource
	private ReplicatedMap<Integer, HazelcastIntegrationTestUser> edReplicatedMap3;

	@Resource
	private ReplicatedMap<Integer, HazelcastIntegrationTestUser> edReplicatedMap4;

	@Test
	public void testEventDrivenForOnlyADDEDEntryEvent() {
		edReplicatedMap1.put(1, new HazelcastIntegrationTestUser(1, "TestName1", "TestSurname1"));
		Message<?> msg = edReplicatedMapChannel1.receive(2_000);
		Assert.assertNotNull(msg);
		Assert.assertNotNull(msg.getPayload());
		Assert.assertTrue(msg.getPayload() instanceof EntryEvent);
		Assert.assertEquals(EntryEventType.ADDED,
				((EntryEvent<?, ?>) msg.getPayload()).getEventType());
		Assert.assertEquals("edReplicatedMap1",
				((EntryEvent<?, ?>) msg.getPayload()).getName());
		Assert.assertEquals(1, ((EntryEvent<?, ?>) msg.getPayload()).getKey());
		Assert.assertEquals(1,
				((HazelcastIntegrationTestUser) ((EntryEvent<?, ?>) msg.getPayload()).getValue()).getId());
		Assert.assertEquals("TestName1",
				((HazelcastIntegrationTestUser) ((EntryEvent<?, ?>) msg.getPayload()).getValue()).getName());
		Assert.assertEquals("TestSurname1",
				((HazelcastIntegrationTestUser) ((EntryEvent<?, ?>) msg.getPayload()).getValue()).getSurname());
	}

	@Test
	public void testEventDrivenForOnlyUPDATEDEntryEvent() {
		edReplicatedMap2.put(2, new HazelcastIntegrationTestUser(1, "TestName1", "TestSurname1"));
		edReplicatedMap2.put(2, new HazelcastIntegrationTestUser(2, "TestName2", "TestSurname2"));
		Message<?> msg = edReplicatedMapChannel2.receive(2_000);
		Assert.assertNotNull(msg);
		Assert.assertNotNull(msg.getPayload());
		Assert.assertTrue(msg.getPayload() instanceof EntryEvent);
		Assert.assertEquals(EntryEventType.UPDATED,
				((EntryEvent<?, ?>) msg.getPayload()).getEventType());
		Assert.assertEquals("edReplicatedMap2",
				((EntryEvent<?, ?>) msg.getPayload()).getName());
		Assert.assertEquals(2, ((EntryEvent<?, ?>) msg.getPayload()).getKey());
		Assert.assertEquals(1,
				((HazelcastIntegrationTestUser) ((EntryEvent<?, ?>) msg.getPayload()).getOldValue()).getId());
		Assert.assertEquals("TestName1",
				((HazelcastIntegrationTestUser) ((EntryEvent<?, ?>) msg.getPayload()).getOldValue()).getName());
		Assert.assertEquals("TestSurname1",
				((HazelcastIntegrationTestUser) ((EntryEvent<?, ?>) msg.getPayload()).getOldValue()).getSurname());
		Assert.assertEquals(2,
				((HazelcastIntegrationTestUser) ((EntryEvent<?, ?>) msg.getPayload()).getValue()).getId());
		Assert.assertEquals("TestName2",
				((HazelcastIntegrationTestUser) ((EntryEvent<?, ?>) msg.getPayload()).getValue()).getName());
		Assert.assertEquals("TestSurname2",
				((HazelcastIntegrationTestUser) ((EntryEvent<?, ?>) msg.getPayload()).getValue()).getSurname());
	}

	@Test
	public void testEventDrivenForOnlyREMOVEDEntryEvent() {
		edReplicatedMap3.put(1, new HazelcastIntegrationTestUser(1, "TestName1", "TestSurname1"));
		edReplicatedMap3.put(2, new HazelcastIntegrationTestUser(2, "TestName2", "TestSurname2"));
		edReplicatedMap3.remove(2);
		Message<?> msg = edReplicatedMapChannel3.receive(2_000);
		Assert.assertNotNull(msg);
		Assert.assertNotNull(msg.getPayload());
		Assert.assertTrue(msg.getPayload() instanceof EntryEvent);
		Assert.assertEquals(EntryEventType.REMOVED,
				((EntryEvent<?, ?>) msg.getPayload()).getEventType());
		Assert.assertEquals("edReplicatedMap3",
				((EntryEvent<?, ?>) msg.getPayload()).getName());
		Assert.assertEquals(2, ((EntryEvent<?, ?>) msg.getPayload()).getKey());
		Assert.assertEquals(2,
				((HazelcastIntegrationTestUser) ((EntryEvent<?, ?>) msg.getPayload()).getOldValue()).getId());
		Assert.assertEquals("TestName2",
				((HazelcastIntegrationTestUser) ((EntryEvent<?, ?>) msg.getPayload()).getOldValue()).getName());
		Assert.assertEquals("TestSurname2",
				((HazelcastIntegrationTestUser) ((EntryEvent<?, ?>) msg.getPayload()).getOldValue()).getSurname());
	}

	@Test
	public void testEventDrivenForALLEntryEvent() {
		edReplicatedMap4.put(1, new HazelcastIntegrationTestUser(1, "TestName1", "TestSurname1"));
		Message<?> msg = edReplicatedMapChannel4.receive(2_000);
		verify(msg, "edReplicatedMap4", EntryEventType.ADDED);

		edReplicatedMap4.put(1, new HazelcastIntegrationTestUser(1, "TestName1", "TestSurnameUpdated"));
		msg = edReplicatedMapChannel4.receive(2_000);
		verify(msg, "edReplicatedMap4", EntryEventType.UPDATED);

		edReplicatedMap4.remove(1);
		msg = edReplicatedMapChannel4.receive(2_000);
		verify(msg, "edReplicatedMap4", EntryEventType.REMOVED);

		edReplicatedMap4.put(2, new HazelcastIntegrationTestUser(2, "TestName2", "TestSurname2"));
		msg = edReplicatedMapChannel4.receive(2_000);
		verify(msg, "edReplicatedMap4", EntryEventType.ADDED);

	}

	private void verify(Message<?> msg, String cacheName, EntryEventType type) {
		Assert.assertNotNull(msg);
		Assert.assertNotNull(msg.getPayload());
		Assert.assertTrue(msg.getPayload() instanceof AbstractIMapEvent);
		Assert.assertEquals(cacheName, ((AbstractIMapEvent) msg.getPayload()).getName());
		Assert.assertEquals(type, ((AbstractIMapEvent) msg.getPayload()).getEventType());
	}

}
