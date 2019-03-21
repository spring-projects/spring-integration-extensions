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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.hazelcast.inbound.util.HazelcastInboundChannelAdapterTestUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.PollableChannel;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.core.Client;
import com.hazelcast.core.ClientType;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.LifecycleEvent;
import com.hazelcast.core.LifecycleEvent.LifecycleState;
import com.hazelcast.core.MigrationEvent;

/**
 * Hazelcast Cluster Monitor Inbound Channel Adapter Unit Test Class
 *
 * @author Eren Avsarogullari
 * @since 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@DirtiesContext
public class HazelcastClusterMonitorInboundChannelAdapterTests {

	private static final String TEST_GROUP_NAME1 = "Test_Group_Name1";

	@Autowired
	private PollableChannel cmChannel1;

	@Autowired
	private PollableChannel cmChannel2;

	@Autowired
	private PollableChannel cmChannel3;

	@Autowired
	private PollableChannel cmChannel4;

	@Autowired
	private PollableChannel cmChannel5;

	@Autowired
	private PollableChannel cmChannel6;

	@Autowired
	private HazelcastInstance hazelcastInstance;

	@Autowired
	private HazelcastInstance hazelcastInstance2;

	@Autowired
	private HazelcastInstance hazelcastInstance3;

	@Test
	public void testMembershipEvent() {
		HazelcastInboundChannelAdapterTestUtils
				.testMembershipEvent(hazelcastInstance, cmChannel1, "testKey1", "testValue1");
	}

	@Test
	public void testDistributedObjectEvent() {
		HazelcastInboundChannelAdapterTestUtils
				.testDistributedObjectEventByChannelAndHazelcastInstance(cmChannel2,
						hazelcastInstance, "Test_Distributed_Map4");
	}

	@Test
	public void testMigrationEvent()  {
		final IMap<Integer, String> distributedMap =
				hazelcastInstance3.getMap("Test_Distributed_Map2");
		distributedMap.put(1, "TestValue1");
		distributedMap.put(2, "TestValue2");

		hazelcastInstance3.getLifecycleService().terminate();
		final Message<?> msg = this.cmChannel3.receive(HazelcastInboundChannelAdapterTestUtils.TIMEOUT);
		verifyMigrationEvent(msg);
	}

	@Test
	public void testLifecycleEvent() {
		hazelcastInstance2.getLifecycleService().terminate();

		Message<?> msg =
				cmChannel4.receive(HazelcastInboundChannelAdapterTestUtils.TIMEOUT);
		verifyLifecycleEvent(msg, LifecycleState.SHUTTING_DOWN);

		msg = cmChannel4.receive(HazelcastInboundChannelAdapterTestUtils.TIMEOUT);
		verifyLifecycleEvent(msg, LifecycleState.SHUTDOWN);
	}

	@Test
	public void testClientEvent() {
		testClientEventByChannelAndGroupName(cmChannel5, TEST_GROUP_NAME1);
	}

	@Test
	public void testMultipleMonitorTypes() {
		HazelcastInboundChannelAdapterTestUtils
				.testDistributedObjectEventByChannelAndHazelcastInstance(cmChannel6,
						hazelcastInstance, "Test_Distributed_Map5");

		HazelcastInboundChannelAdapterTestUtils
				.testMembershipEvent(hazelcastInstance, cmChannel6, "testKey2", "testValue2");
	}

	private void testClientEventByChannelAndGroupName(final PollableChannel channel,
			final String groupName) {
		final HazelcastInstance client = getHazelcastClientByGroupName(groupName);

		Message<?> msg = channel.receive(HazelcastInboundChannelAdapterTestUtils.TIMEOUT);
		verifyClientEvent(msg);

		client.getLifecycleService().terminate();

		msg = channel.receive(HazelcastInboundChannelAdapterTestUtils.TIMEOUT);
		verifyClientEvent(msg);
	}

	private HazelcastInstance getHazelcastClientByGroupName(final String groupName) {
		final GroupConfig groupConfig = new GroupConfig();
		groupConfig.setName(groupName);
		groupConfig.setPassword("dev-pass");
		final ClientConfig cfg = new ClientConfig();
		cfg.setGroupConfig(groupConfig);
		cfg.getNetworkConfig().addAddress("127.0.0.1:5701");

		return HazelcastClient.newHazelcastClient(cfg);
	}

	private void verifyMigrationEvent(final Message<?> msg) {
		assertNotNull(msg);
		assertNotNull(msg.getPayload());
		assertTrue(msg.getPayload() instanceof MigrationEvent);
		assertNotNull(((MigrationEvent) msg.getPayload()).getStatus());
		assertNotNull(((MigrationEvent) msg.getPayload()).getNewOwner());

		// Newer Hazelcast versions doesn't populated 'oldOwner' if we drop member on the same host
		assertNull(((MigrationEvent) msg.getPayload()).getOldOwner());
	}

	private void verifyLifecycleEvent(final Message<?> msg,
			final LifecycleState lifecycleState) {
		assertNotNull(msg);
		assertNotNull(msg.getPayload());
		assertTrue(msg.getPayload() instanceof LifecycleEvent);
		assertEquals(lifecycleState, ((LifecycleEvent) msg.getPayload()).getState());
	}

	private void verifyClientEvent(final Message<?> msg) {
		assertNotNull(msg);
		assertNotNull(msg.getPayload());
		assertTrue(msg.getPayload() instanceof Client);
		assertEquals(ClientType.JAVA, ((Client) msg.getPayload()).getClientType());
		assertNotNull(((Client) msg.getPayload()).getSocketAddress());
	}

}
