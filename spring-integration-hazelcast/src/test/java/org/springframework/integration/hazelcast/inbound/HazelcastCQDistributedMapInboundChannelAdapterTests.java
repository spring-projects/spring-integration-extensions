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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.hazelcast.HazelcastIntegrationTestUtils;
import org.springframework.integration.hazelcast.HazelcastHeaders;
import org.springframework.integration.hazelcast.HazelcastIntegrationTestUser;
import org.springframework.integration.hazelcast.message.EntryEventMessagePayload;
import org.springframework.messaging.Message;
import org.springframework.messaging.PollableChannel;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.hazelcast.core.EntryEventType;
import com.hazelcast.core.IMap;

/**
 * Hazelcast Continuous Query Inbound Channel Adapter Unit Test Class
 *
 * @author Eren Avsarogullari
 * @since 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@DirtiesContext
@SuppressWarnings("unchecked")
public class HazelcastCQDistributedMapInboundChannelAdapterTests {

	@Autowired
	private PollableChannel cqMapChannel1;

	@Autowired
	private PollableChannel cqMapChannel2;

	@Autowired
	private PollableChannel cqMapChannel3;

	@Autowired
	private PollableChannel cqMapChannel4;

	@Autowired
	private PollableChannel cqMapChannel5;

	@Resource
	private IMap<Integer, HazelcastIntegrationTestUser> cqDistributedMap1;

	@Resource
	private IMap<Integer, HazelcastIntegrationTestUser> cqDistributedMap2;

	@Resource
	private IMap<Integer, HazelcastIntegrationTestUser> cqDistributedMap3;

	@Resource
	private IMap<Integer, HazelcastIntegrationTestUser> cqDistributedMap4;

	@Resource
	private IMap<Integer, HazelcastIntegrationTestUser> cqDistributedMap5;

	@Test
	public void testContinuousQueryForOnlyADDEDEntryEvent() {
		cqDistributedMap1.put(1, new HazelcastIntegrationTestUser(1, "TestName1", "TestSurname1"));
		cqDistributedMap1.remove(1);
		cqDistributedMap1.put(2, new HazelcastIntegrationTestUser(2, "TestName2", "TestSurname2"));
		Message<?> msg = cqMapChannel1.receive(HazelcastIntegrationTestUtils.TIMEOUT);
		assertNotNull(msg);
		assertNotNull(msg.getPayload());
		assertTrue(msg.getPayload() instanceof EntryEventMessagePayload);
		assertNotNull(msg.getHeaders().get(HazelcastHeaders.MEMBER));
		assertEquals(EntryEventType.ADDED.name(), msg.getHeaders().get(HazelcastHeaders.EVENT_TYPE));
		assertEquals("cqDistributedMap1", msg.getHeaders().get(HazelcastHeaders.CACHE_NAME));

		assertEquals(Integer.valueOf(1),
				((EntryEventMessagePayload<Integer, HazelcastIntegrationTestUser>) msg
						.getPayload()).key);
		assertEquals(1,
				(((EntryEventMessagePayload<Integer, HazelcastIntegrationTestUser>) msg
						.getPayload()).value).getId());
		assertEquals("TestName1",
				(((EntryEventMessagePayload<Integer, HazelcastIntegrationTestUser>) msg
						.getPayload()).value).getName());
		assertEquals("TestSurname1",
				(((EntryEventMessagePayload<Integer, HazelcastIntegrationTestUser>) msg
						.getPayload()).value).getSurname());
	}

	@Test
	public void testContinuousQueryForOnlyREMOVEDEntryEvent() {
		cqDistributedMap2.put(1, new HazelcastIntegrationTestUser(1, "TestName1", "TestSurname1"));
		cqDistributedMap2.put(2, new HazelcastIntegrationTestUser(2, "TestName2", "TestSurname2"));
		cqDistributedMap2.remove(2);
		Message<?> msg = cqMapChannel2.receive(HazelcastIntegrationTestUtils.TIMEOUT);
		assertNotNull(msg);
		assertNotNull(msg.getPayload());
		assertTrue(msg.getPayload() instanceof EntryEventMessagePayload);
		assertNotNull(msg.getHeaders().get(HazelcastHeaders.MEMBER));
		assertEquals(EntryEventType.REMOVED.name(), msg.getHeaders().get(HazelcastHeaders.EVENT_TYPE));
		assertEquals("cqDistributedMap2", msg.getHeaders().get(HazelcastHeaders.CACHE_NAME));

		assertEquals(Integer.valueOf(2),
				((EntryEventMessagePayload<Integer, HazelcastIntegrationTestUser>) msg
						.getPayload()).key);
		assertEquals(2,
				(((EntryEventMessagePayload<Integer, HazelcastIntegrationTestUser>) msg
						.getPayload()).oldValue).getId());
		assertEquals("TestName2",
				(((EntryEventMessagePayload<Integer, HazelcastIntegrationTestUser>) msg
						.getPayload()).oldValue).getName());
		assertEquals("TestSurname2",
				(((EntryEventMessagePayload<Integer, HazelcastIntegrationTestUser>) msg
						.getPayload()).oldValue).getSurname());
	}

	@Test
	public void testContinuousQueryForALLEntryEvent() {
		cqDistributedMap3.put(1, new HazelcastIntegrationTestUser(1, "TestName1", "TestSurname1"));
		Message<?> msg = cqMapChannel3.receive(HazelcastIntegrationTestUtils.TIMEOUT);
		HazelcastIntegrationTestUtils.verifyEntryEvent(msg, "cqDistributedMap3", EntryEventType.ADDED);

		cqDistributedMap3.put(1, new HazelcastIntegrationTestUser(1, "TestName1", "TestSurnameUpdated"));
		msg = cqMapChannel3.receive(HazelcastIntegrationTestUtils.TIMEOUT);
		HazelcastIntegrationTestUtils.verifyEntryEvent(msg, "cqDistributedMap3", EntryEventType.UPDATED);

		cqDistributedMap3.remove(1);
		msg = cqMapChannel3.receive(HazelcastIntegrationTestUtils.TIMEOUT);
		HazelcastIntegrationTestUtils.verifyEntryEvent(msg, "cqDistributedMap3", EntryEventType.REMOVED);

		cqDistributedMap3.put(2, new HazelcastIntegrationTestUser(2, "TestName2", "TestSurname2"));
		msg = cqMapChannel3.receive(HazelcastIntegrationTestUtils.TIMEOUT);
		HazelcastIntegrationTestUtils.verifyEntryEvent(msg, "cqDistributedMap3", EntryEventType.ADDED);

		cqDistributedMap3.clear();
		msg = cqMapChannel3.receive(HazelcastIntegrationTestUtils.TIMEOUT);
		HazelcastIntegrationTestUtils.verifyEntryEvent(msg, "cqDistributedMap3", EntryEventType.CLEAR_ALL);
	}

	@Test
	public void testContinuousQueryForOnlyUPDATEDEntryEvent() {
		cqDistributedMap4.put(1, new HazelcastIntegrationTestUser(1, "TestName1", "TestSurname1"));
		cqDistributedMap4.put(1, new HazelcastIntegrationTestUser(2, "TestName2", "TestSurname2"));
		Message<?> msg = cqMapChannel4.receive(HazelcastIntegrationTestUtils.TIMEOUT);
		assertNotNull(msg);
		assertNotNull(msg.getPayload());
		assertTrue(msg.getPayload() instanceof EntryEventMessagePayload);
		assertNotNull(msg.getHeaders().get(HazelcastHeaders.MEMBER));
		assertEquals(EntryEventType.UPDATED.name(), msg.getHeaders().get(HazelcastHeaders.EVENT_TYPE));
		assertEquals("cqDistributedMap4", msg.getHeaders().get(HazelcastHeaders.CACHE_NAME));

		assertEquals(Integer.valueOf(1),
				((EntryEventMessagePayload<Integer, HazelcastIntegrationTestUser>) msg
						.getPayload()).key);
		assertEquals(1,
				(((EntryEventMessagePayload<Integer, HazelcastIntegrationTestUser>) msg
						.getPayload()).oldValue).getId());
		assertEquals("TestName1",
				(((EntryEventMessagePayload<Integer, HazelcastIntegrationTestUser>) msg
						.getPayload()).oldValue).getName());
		assertEquals("TestSurname1",
				(((EntryEventMessagePayload<Integer, HazelcastIntegrationTestUser>) msg
						.getPayload()).oldValue).getSurname());
		assertEquals(2,
				(((EntryEventMessagePayload<Integer, HazelcastIntegrationTestUser>) msg
						.getPayload()).value).getId());
		assertEquals("TestName2",
				(((EntryEventMessagePayload<Integer, HazelcastIntegrationTestUser>) msg
						.getPayload()).value).getName());
		assertEquals("TestSurname2",
				(((EntryEventMessagePayload<Integer, HazelcastIntegrationTestUser>) msg
						.getPayload()).value).getSurname());
	}

	@Test
	public void testContinuousQueryForOnlyUPDATEDEntryEventWhenIncludeValueIsFalse() {
		cqDistributedMap5.put(1, new HazelcastIntegrationTestUser(1, "TestName1", "TestSurname1"));
		cqDistributedMap5.put(1, new HazelcastIntegrationTestUser(2, "TestName2", "TestSurname2"));
		Message<?> msg = cqMapChannel5.receive(HazelcastIntegrationTestUtils.TIMEOUT);
		assertNotNull(msg);
		assertNotNull(msg.getPayload());
		assertTrue(msg.getPayload() instanceof EntryEventMessagePayload);
		assertNotNull(msg.getHeaders().get(HazelcastHeaders.MEMBER));
		assertEquals(EntryEventType.UPDATED.name(), msg.getHeaders().get(HazelcastHeaders.EVENT_TYPE));
		assertEquals("cqDistributedMap5", msg.getHeaders().get(HazelcastHeaders.CACHE_NAME));

		assertEquals(Integer.valueOf(1),
				((EntryEventMessagePayload<Integer, HazelcastIntegrationTestUser>) msg.getPayload()).key);
		assertNull(((EntryEventMessagePayload<Integer, HazelcastIntegrationTestUser>) msg.getPayload()).oldValue);
		assertNull(((EntryEventMessagePayload<Integer, HazelcastIntegrationTestUser>) msg.getPayload()).value);
	}

}
