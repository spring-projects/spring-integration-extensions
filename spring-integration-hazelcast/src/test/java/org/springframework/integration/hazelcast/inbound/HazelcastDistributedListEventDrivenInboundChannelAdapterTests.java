/*
 * Copyright 2015-2017 the original author or authors.
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

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.hazelcast.HazelcastHeaders;
import org.springframework.integration.hazelcast.HazelcastIntegrationTestUser;
import org.springframework.integration.hazelcast.inbound.util.HazelcastInboundChannelAdapterTestUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.PollableChannel;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.hazelcast.core.EntryEventType;
import com.hazelcast.core.IList;

/**
 * Hazelcast Distributed List Event Driven Inbound Channel Adapter Test Class
 *
 * @author Eren Avsarogullari
 * @since 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@DirtiesContext
public class HazelcastDistributedListEventDrivenInboundChannelAdapterTests {

	@Autowired
	private PollableChannel edListChannel1;

	@Autowired
	private PollableChannel edListChannel2;

	@Autowired
	private PollableChannel edListChannel3;

	@Resource
	private IList<HazelcastIntegrationTestUser> edDistributedList1;

	@Resource
	private IList<HazelcastIntegrationTestUser> edDistributedList2;

	@Resource
	private IList<HazelcastIntegrationTestUser> edDistributedList3;

	@Test
	public void testEventDrivenForOnlyADDEDEntryEvent() {
		edDistributedList1.add(new HazelcastIntegrationTestUser(1, "TestName1", "TestSurname1"));
		Message<?> msg = edListChannel1.receive(HazelcastInboundChannelAdapterTestUtils.TIMEOUT);
		assertNotNull(msg);
		assertNotNull(msg.getPayload());
		assertNotNull(msg.getHeaders().get(HazelcastHeaders.MEMBER));
		assertEquals(EntryEventType.ADDED.toString(), msg.getHeaders().get(HazelcastHeaders.EVENT_TYPE).toString());
		assertEquals(1, ((HazelcastIntegrationTestUser) msg.getPayload()).getId());
		assertEquals("TestName1", ((HazelcastIntegrationTestUser) msg.getPayload()).getName());
		assertEquals("TestSurname1", ((HazelcastIntegrationTestUser) msg.getPayload()).getSurname());
	}

	@Test
	public void testEventDrivenForOnlyREMOVEDEntryEvent() {
		HazelcastIntegrationTestUser user = new HazelcastIntegrationTestUser(2, "TestName2", "TestSurname2");
		edDistributedList2.add(user);
		edDistributedList2.remove(user);
		Message<?> msg = edListChannel2.receive(HazelcastInboundChannelAdapterTestUtils.TIMEOUT);
		assertNotNull(msg);
		assertNotNull(msg.getPayload());
		assertNotNull(msg.getHeaders().get(HazelcastHeaders.MEMBER));
		assertEquals(EntryEventType.REMOVED.toString(), msg.getHeaders().get(HazelcastHeaders.EVENT_TYPE).toString());
		assertEquals(2, ((HazelcastIntegrationTestUser) msg.getPayload()).getId());
		assertEquals("TestName2", ((HazelcastIntegrationTestUser) msg.getPayload()).getName());
		assertEquals("TestSurname2", ((HazelcastIntegrationTestUser) msg.getPayload()).getSurname());
	}

	@Test
	public void testEventDrivenForALLEntryEvent() {
		HazelcastInboundChannelAdapterTestUtils
				.testEventDrivenForDistributedCollectionItemEvents(edDistributedList3, edListChannel3);
	}

}
