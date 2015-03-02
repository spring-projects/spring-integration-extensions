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
package com.onlinetechvision.integration.hazelcast.inbound;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.PollableChannel;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.hazelcast.core.AbstractIMapEvent;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryEventType;
import com.hazelcast.core.IMap;

/**
 * HazelcastContinuousQueryInboundChannelAdapterTest
 * 
 * @author Eren Avsarogullari
 * @since 1.0.0
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/HazelcastCQDistributedMapInboundChannelAdapterTest-context.xml"})
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class HazelcastCQDistributedMapInboundChannelAdapterTest {

	@Autowired
	private PollableChannel cqMapChannel1;
	
	@Autowired
	private PollableChannel cqMapChannel2;
	
	@Autowired
	private PollableChannel cqMapChannel3;
	
	@Autowired
	private PollableChannel cqMapChannel4;
		
	@Resource
	private IMap<Integer, User> cqDistributedMap1;
	
	@Resource
	private IMap<Integer, User> cqDistributedMap2;
	
	@Resource
	private IMap<Integer, User> cqDistributedMap3;
	
	@Resource
	private IMap<Integer, User> cqDistributedMap4;
	
	@Test
	public void testContinuousQueryForOnlyADDEDEntryEvent() {
		cqDistributedMap1.put(1, new User(1, "TestName1", "TestSurname1"));
		cqDistributedMap1.remove(1);
		cqDistributedMap1.put(2, new User(2, "TestName2", "TestSurname2"));
		Message<?> msg = cqMapChannel1.receive(2_000);
		Assert.assertNotNull(msg);
		Assert.assertNotNull(msg.getPayload());
		Assert.assertTrue(msg.getPayload() instanceof EntryEvent);
		Assert.assertEquals(EntryEventType.ADDED, ((EntryEvent<?, ?>)msg.getPayload()).getEventType());
		Assert.assertEquals("cqDistributedMap1", ((EntryEvent<?, ?>)msg.getPayload()).getName());
		Assert.assertEquals(1, ((EntryEvent<?, ?>)msg.getPayload()).getKey());
		Assert.assertEquals(1, ((User)((EntryEvent<?, ?>)msg.getPayload()).getValue()).getId());
		Assert.assertEquals("TestName1", ((User)((EntryEvent<?, ?>)msg.getPayload()).getValue()).getName());
		Assert.assertEquals("TestSurname1", ((User)((EntryEvent<?, ?>)msg.getPayload()).getValue()).getSurname());
	}
	
	@Test
	public void testContinuousQueryForOnlyREMOVEDEntryEvent() {
		cqDistributedMap2.put(1, new User(1, "TestName1", "TestSurname1"));
		cqDistributedMap2.put(2, new User(2, "TestName2", "TestSurname2"));
		cqDistributedMap2.remove(2);
		Message<?> msg = cqMapChannel2.receive(2_000);
		Assert.assertNotNull(msg);
		Assert.assertNotNull(msg.getPayload());
		Assert.assertTrue(msg.getPayload() instanceof EntryEvent);
		Assert.assertEquals(EntryEventType.REMOVED, ((EntryEvent<?, ?>)msg.getPayload()).getEventType());
		Assert.assertEquals("cqDistributedMap2", ((EntryEvent<?, ?>)msg.getPayload()).getName());
		Assert.assertEquals(2, ((EntryEvent<?, ?>)msg.getPayload()).getKey());
		Assert.assertEquals(2, ((User)((EntryEvent<?, ?>)msg.getPayload()).getOldValue()).getId());
		Assert.assertEquals("TestName2", ((User)((EntryEvent<?, ?>)msg.getPayload()).getOldValue()).getName());
		Assert.assertEquals("TestSurname2", ((User)((EntryEvent<?, ?>)msg.getPayload()).getOldValue()).getSurname());
	}
	
	@Test
	public void testContinuousQueryForALLEntryEvent() {
		cqDistributedMap3.put(1, new User(1, "TestName1", "TestSurname1"));
		Message<?> msg = cqMapChannel3.receive(2_000);
		verify(msg, "cqDistributedMap3", EntryEventType.ADDED);
		
		cqDistributedMap3.put(1, new User(1, "TestName1", "TestSurnameUpdated"));
		msg = cqMapChannel3.receive(2_000);
		verify(msg, "cqDistributedMap3", EntryEventType.UPDATED);
		
		cqDistributedMap3.remove(1);
		msg = cqMapChannel3.receive(2_000);
		verify(msg, "cqDistributedMap3", EntryEventType.REMOVED);
		
		cqDistributedMap3.put(2, new User(2, "TestName2", "TestSurname2"));
		msg = cqMapChannel3.receive(2_000);
		verify(msg, "cqDistributedMap3", EntryEventType.ADDED);
		
		cqDistributedMap3.clear();
		msg = cqMapChannel3.receive(2_000);
		verify(msg, "cqDistributedMap3", EntryEventType.CLEAR_ALL);
	}
	
	@Test
	public void testContinuousQueryForOnlyUPDATEDEntryEvent() {
		cqDistributedMap4.put(1, new User(1, "TestName1", "TestSurname1"));
		cqDistributedMap4.put(1, new User(2, "TestName2", "TestSurname2"));
		Message<?> msg = cqMapChannel4.receive(2_000);
		Assert.assertNotNull(msg);
		Assert.assertNotNull(msg.getPayload());
		Assert.assertTrue(msg.getPayload() instanceof EntryEvent);
		Assert.assertEquals(EntryEventType.UPDATED, ((EntryEvent<?, ?>)msg.getPayload()).getEventType());
		Assert.assertEquals("cqDistributedMap4", ((EntryEvent<?, ?>)msg.getPayload()).getName());
		Assert.assertEquals(1, ((EntryEvent<?, ?>)msg.getPayload()).getKey());
		Assert.assertEquals(1, ((User)((EntryEvent<?, ?>)msg.getPayload()).getOldValue()).getId());
		Assert.assertEquals("TestName1", ((User)((EntryEvent<?, ?>)msg.getPayload()).getOldValue()).getName());
		Assert.assertEquals("TestSurname1", ((User)((EntryEvent<?, ?>)msg.getPayload()).getOldValue()).getSurname());
		Assert.assertEquals(2, ((User)((EntryEvent<?, ?>)msg.getPayload()).getValue()).getId());
		Assert.assertEquals("TestName2", ((User)((EntryEvent<?, ?>)msg.getPayload()).getValue()).getName());
		Assert.assertEquals("TestSurname2", ((User)((EntryEvent<?, ?>)msg.getPayload()).getValue()).getSurname());
	}
		
	private void verify(Message<?> msg, String cacheName, EntryEventType type) {
		Assert.assertNotNull(msg);
		Assert.assertNotNull(msg.getPayload());
		Assert.assertTrue(msg.getPayload() instanceof AbstractIMapEvent);
		Assert.assertEquals(cacheName, ((AbstractIMapEvent)msg.getPayload()).getName());
		Assert.assertEquals(type, ((AbstractIMapEvent)msg.getPayload()).getEventType());
	}
	
	
	
}