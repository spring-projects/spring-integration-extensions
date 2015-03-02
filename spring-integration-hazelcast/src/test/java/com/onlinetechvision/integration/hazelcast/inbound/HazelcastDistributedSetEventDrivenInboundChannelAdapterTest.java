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

import com.hazelcast.core.EntryEventType;
import com.hazelcast.core.ISet;
import com.hazelcast.core.ItemEvent;

/**
 * HazelcastDistributedSetEventDrivenInboundChannelAdapterTest
 * 
 * @author Eren Avsarogullari
 * @since 1.0.0
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/HazelcastDistributedSetEventDrivenInboundChannelAdapterTest-context.xml"})
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class HazelcastDistributedSetEventDrivenInboundChannelAdapterTest {

	@Autowired
	private PollableChannel edSetChannel1;
	
	@Autowired
	private PollableChannel edSetChannel2;
	
	@Autowired
	private PollableChannel edSetChannel3;
			
	@Resource
	private ISet<User> edDistributedSet1;
	
	@Resource
	private ISet<User> edDistributedSet2;
	
	@Resource
	private ISet<User> edDistributedSet3;
		
	@Test
	public void testEventDrivenForOnlyADDEDEntryEvent() {
		edDistributedSet1.add(new User(1, "TestName1", "TestSurname1"));
		Message<?> msg = edSetChannel1.receive(2_000);
		Assert.assertNotNull(msg);
		Assert.assertNotNull(msg.getPayload());
		Assert.assertTrue(msg.getPayload() instanceof ItemEvent);
		Assert.assertEquals(EntryEventType.ADDED.toString(), ((ItemEvent<?>)msg.getPayload()).getEventType().ADDED.toString());
		Assert.assertEquals(1, ((User)((ItemEvent<?>)msg.getPayload()).getItem()).getId());
		Assert.assertEquals("TestName1", ((User)((ItemEvent<?>)msg.getPayload()).getItem()).getName());
		Assert.assertEquals("TestSurname1", ((User)((ItemEvent<?>)msg.getPayload()).getItem()).getSurname());
	}
	
	@Test
	public void testEventDrivenForOnlyREMOVEDEntryEvent() {
		User user = new User(2, "TestName2", "TestSurname2");
		edDistributedSet2.add(user);
		edDistributedSet2.remove(user);
		Message<?> msg = edSetChannel2.receive(2_000);
		Assert.assertNotNull(msg);
		Assert.assertNotNull(msg.getPayload());
		Assert.assertTrue(msg.getPayload() instanceof ItemEvent);
		Assert.assertEquals(EntryEventType.REMOVED.toString(), ((ItemEvent<?>)msg.getPayload()).getEventType().REMOVED.toString());
		Assert.assertEquals(2, ((User)((ItemEvent<?>)msg.getPayload()).getItem()).getId());
		Assert.assertEquals("TestName2", ((User)((ItemEvent<?>)msg.getPayload()).getItem()).getName());
		Assert.assertEquals("TestSurname2", ((User)((ItemEvent<?>)msg.getPayload()).getItem()).getSurname());
	}
	
	@Test
	public void testEventDrivenForALLEntryEvent() {
		User user = new User(1, "TestName1", "TestSurname1");
		edDistributedSet3.add(user);
		Message<?> msg = edSetChannel3.receive(2_000);
		verify(msg, EntryEventType.ADDED);
		
		edDistributedSet3.remove(user);
		msg = edSetChannel3.receive(2_000);
		verify(msg, EntryEventType.REMOVED);
		
		user = new User(2, "TestName2", "TestSurname2");
		edDistributedSet3.add(user);
		msg = edSetChannel3.receive(2_000);
		verify(msg, EntryEventType.ADDED);
	}
		
	private void verify(Message<?> msg, EntryEventType type) {
		Assert.assertNotNull(msg);
		Assert.assertNotNull(msg.getPayload());
		Assert.assertTrue(msg.getPayload() instanceof ItemEvent);
		Assert.assertEquals(type.toString(), ((ItemEvent)msg.getPayload()).getEventType().toString());
	}

}