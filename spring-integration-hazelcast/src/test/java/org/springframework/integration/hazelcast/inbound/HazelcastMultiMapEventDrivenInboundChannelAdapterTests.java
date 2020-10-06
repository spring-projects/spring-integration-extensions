/*
 * Copyright 2015-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.integration.hazelcast.inbound;

import javax.annotation.Resource;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.hazelcast.HazelcastHeaders;
import org.springframework.integration.hazelcast.HazelcastIntegrationTestUser;
import org.springframework.integration.hazelcast.inbound.util.HazelcastInboundChannelAdapterTestUtils;
import org.springframework.integration.hazelcast.message.EntryEventMessagePayload;
import org.springframework.messaging.Message;
import org.springframework.messaging.PollableChannel;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.hazelcast.core.EntryEventType;
import com.hazelcast.instance.impl.HazelcastInstanceFactory;
import com.hazelcast.multimap.MultiMap;

/**
 * Hazelcast MultiMap Event Driven Inbound Channel Adapter Test
 *
 * @author Eren Avsarogullari
 * @author Artem Bilan
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

	@AfterClass
	public static void shutdown() {
		HazelcastInstanceFactory.terminateAll();
	}

	@Test
	public void testEventDrivenForOnlyADDEDEntryEvent() {
		edMultiMap1
				.put(1, new HazelcastIntegrationTestUser(1, "TestName1", "TestSurname1"));
		Message<?> msg =
				edMultiMapChannel1.receive(HazelcastInboundChannelAdapterTestUtils.TIMEOUT);
		Assert.assertNotNull(msg);
		Assert.assertNotNull(msg.getPayload());
		Assert.assertTrue(msg.getPayload() instanceof EntryEventMessagePayload);
		Assert.assertNotNull(msg.getHeaders().get(HazelcastHeaders.MEMBER));
		Assert.assertEquals(EntryEventType.ADDED.name(),
				msg.getHeaders().get(HazelcastHeaders.EVENT_TYPE));
		Assert.assertEquals("edMultiMap1",
				msg.getHeaders().get(HazelcastHeaders.CACHE_NAME));

		Assert.assertEquals(Integer.valueOf(1),
				((EntryEventMessagePayload<Integer, HazelcastIntegrationTestUser>) msg
						.getPayload()).key);
		Assert.assertEquals(1,
				(((EntryEventMessagePayload<Integer, HazelcastIntegrationTestUser>) msg
						.getPayload()).value).getId());
		Assert.assertEquals("TestName1",
				(((EntryEventMessagePayload<Integer, HazelcastIntegrationTestUser>) msg
						.getPayload()).value).getName());
		Assert.assertEquals("TestSurname1",
				(((EntryEventMessagePayload<Integer, HazelcastIntegrationTestUser>) msg
						.getPayload()).value).getSurname());
	}

	@Test
	public void testEventDrivenForOnlyREMOVEDEntryEvent() {
		edMultiMap2
				.put(1, new HazelcastIntegrationTestUser(1, "TestName1", "TestSurname1"));
		edMultiMap2
				.put(2, new HazelcastIntegrationTestUser(2, "TestName2", "TestSurname2"));
		edMultiMap2.remove(2);
		Message<?> msg =
				edMultiMapChannel2.receive(HazelcastInboundChannelAdapterTestUtils.TIMEOUT);
		Assert.assertNotNull(msg);
		Assert.assertNotNull(msg.getPayload());
		Assert.assertTrue(msg.getPayload() instanceof EntryEventMessagePayload);
		Assert.assertNotNull(msg.getHeaders().get(HazelcastHeaders.MEMBER));
		Assert.assertEquals(EntryEventType.REMOVED.name(),
				msg.getHeaders().get(HazelcastHeaders.EVENT_TYPE));
		Assert.assertEquals("edMultiMap2",
				msg.getHeaders().get(HazelcastHeaders.CACHE_NAME));

		Assert.assertEquals(Integer.valueOf(2),
				((EntryEventMessagePayload<Integer, HazelcastIntegrationTestUser>) msg
						.getPayload()).key);
		Assert.assertNull(((EntryEventMessagePayload<?, ?>) msg.getPayload()).value);
		Assert.assertEquals(2,
				(((EntryEventMessagePayload<Integer, HazelcastIntegrationTestUser>) msg
						.getPayload()).oldValue).getId());
		Assert.assertEquals("TestName2",
				(((EntryEventMessagePayload<Integer, HazelcastIntegrationTestUser>) msg
						.getPayload()).oldValue).getName());
		Assert.assertEquals("TestSurname2",
				(((EntryEventMessagePayload<Integer, HazelcastIntegrationTestUser>) msg
						.getPayload()).oldValue).getSurname());
	}

	@Test
	public void testEventDrivenForALLEntryEvent() {
		HazelcastInboundChannelAdapterTestUtils
				.testEventDrivenForMultiMapEntryEvents(edMultiMap3, edMultiMapChannel3,
						"edMultiMap3");
	}

}
