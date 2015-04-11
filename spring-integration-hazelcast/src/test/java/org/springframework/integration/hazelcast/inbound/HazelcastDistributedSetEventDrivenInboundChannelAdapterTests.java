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
import org.springframework.integration.hazelcast.message.HazelcastHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.PollableChannel;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.hazelcast.core.EntryEventType;
import com.hazelcast.core.ISet;

/**
 * Hazelcast Distributed Set Event Driven Inbound Channel Adapter Test
 *
 * @author Eren Avsarogullari
 * @since 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@DirtiesContext
public class HazelcastDistributedSetEventDrivenInboundChannelAdapterTests {

	@Autowired
	private PollableChannel edSetChannel1;

	@Autowired
	private PollableChannel edSetChannel2;

	@Autowired
	private PollableChannel edSetChannel3;

	@Resource
	private ISet<HazelcastIntegrationTestUser> edDistributedSet1;

	@Resource
	private ISet<HazelcastIntegrationTestUser> edDistributedSet2;

	@Resource
	private ISet<HazelcastIntegrationTestUser> edDistributedSet3;

	@Test
	public void testEventDrivenForOnlyADDEDEntryEvent() {
		edDistributedSet1.add(new HazelcastIntegrationTestUser(1, "TestName1", "TestSurname1"));
		Message<?> msg = edSetChannel1.receive(2_000);
		Assert.assertNotNull(msg);
		Assert.assertNotNull(msg.getPayload());
		Assert.assertNotNull(msg.getHeaders().get(HazelcastHeaders.MEMBER));
		Assert.assertEquals(EntryEventType.ADDED.toString(), msg.getHeaders().get(HazelcastHeaders.EVENT).toString());
		Assert.assertEquals(1, (((HazelcastIntegrationTestUser) msg.getPayload()).getId()));
		Assert.assertEquals("TestName1", (((HazelcastIntegrationTestUser) msg.getPayload()).getName()));
		Assert.assertEquals("TestSurname1", (((HazelcastIntegrationTestUser) msg.getPayload()).getSurname()));
	}

	@Test
	public void testEventDrivenForOnlyREMOVEDEntryEvent() {
		HazelcastIntegrationTestUser user = new HazelcastIntegrationTestUser(2, "TestName2", "TestSurname2");
		edDistributedSet2.add(user);
		edDistributedSet2.remove(user);
		Message<?> msg = edSetChannel2.receive(2_000);
		Assert.assertNotNull(msg);
		Assert.assertNotNull(msg.getPayload());
		Assert.assertNotNull(msg.getHeaders().get(HazelcastHeaders.MEMBER));
		Assert.assertEquals(EntryEventType.REMOVED.toString(), msg.getHeaders().get(HazelcastHeaders.EVENT).toString());
		Assert.assertEquals(2, (((HazelcastIntegrationTestUser) msg.getPayload()).getId()));
		Assert.assertEquals("TestName2", (((HazelcastIntegrationTestUser) msg.getPayload()).getName()));
		Assert.assertEquals("TestSurname2", (((HazelcastIntegrationTestUser) msg.getPayload()).getSurname()));
	}

	@Test
	public void testEventDrivenForALLEntryEvent() {
		HazelcastIntegrationTestUser user = new HazelcastIntegrationTestUser(1, "TestName1", "TestSurname1");
		edDistributedSet3.add(user);
		Message<?> msg = edSetChannel3.receive(2_000);
		verify(msg, EntryEventType.ADDED);

		edDistributedSet3.remove(user);
		msg = edSetChannel3.receive(2_000);
		verify(msg, EntryEventType.REMOVED);

		user = new HazelcastIntegrationTestUser(2, "TestName2", "TestSurname2");
		edDistributedSet3.add(user);
		msg = edSetChannel3.receive(2_000);
		verify(msg, EntryEventType.ADDED);
	}

	private void verify(Message<?> msg, EntryEventType event) {
		Assert.assertNotNull(msg);
		Assert.assertNotNull(msg.getPayload());
		Assert.assertNotNull(msg.getHeaders().get(HazelcastHeaders.MEMBER));
		Assert.assertEquals(event.toString(), msg.getHeaders().get(HazelcastHeaders.EVENT).toString());
	}

}
