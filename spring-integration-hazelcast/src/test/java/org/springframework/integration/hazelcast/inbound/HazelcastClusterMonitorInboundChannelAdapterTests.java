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
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.PollableChannel;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.core.Client;
import com.hazelcast.core.ClientType;
import com.hazelcast.core.DistributedObjectEvent;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.LifecycleEvent;
import com.hazelcast.core.LifecycleEvent.LifecycleState;
import com.hazelcast.core.MembershipEvent;
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

	private static final String TEST_GROUP_NAME3 = "Test_Group_Name3";

	private static final int TIMEOUT = 2_000;

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

	@Autowired
	private HazelcastInstance hazelcastInstance4;

	@Test
	public void testMembershipEvent() {
		final GroupConfig groupConfig = new GroupConfig();
		groupConfig.setName(TEST_GROUP_NAME1);
		final Config cfg = new Config();
		cfg.setGroupConfig(groupConfig);
		final HazelcastInstance newHzInstance = Hazelcast.newHazelcastInstance(cfg);

		Message<?> msg = cmChannel1.receive(TIMEOUT);
		verifyMembershipEvent(msg, MembershipEvent.MEMBER_ADDED);

		newHzInstance.getLifecycleService().terminate();

		msg = cmChannel1.receive(TIMEOUT);
		verifyMembershipEvent(msg, MembershipEvent.MEMBER_REMOVED);
	}

	@Test
	public void testDistributedObjectEvent() {
		testDistributedObjectEventByChannelAndHazelcastInstance(cmChannel2,
				hazelcastInstance);
	}

	@Test
	public void testMigrationEvent() {
		hazelcastInstance3.getLifecycleService().terminate();
		final Message<?> msg = cmChannel3.receive(TIMEOUT);
		verifyMigrationEvent(msg);
	}

	@Test
	public void testLifecycleEvent() throws InterruptedException {
		hazelcastInstance2.getLifecycleService().terminate();

		Message<?> msg = cmChannel4.receive(TIMEOUT);
		verifyLifecycleEvent(msg, LifecycleState.SHUTTING_DOWN);

		msg = cmChannel4.receive(TIMEOUT);
		verifyLifecycleEvent(msg, LifecycleState.SHUTDOWN);
	}

	@Test
	public void testClientEvent() {
		testClientEventByChannelAndGroupName(cmChannel5, TEST_GROUP_NAME1);
	}

	@Test
	public void testMultipleMonitorTypes() {
		testClientEventByChannelAndGroupName(cmChannel6, TEST_GROUP_NAME3);
		testDistributedObjectEventByChannelAndHazelcastInstance(cmChannel6,
				hazelcastInstance4);
	}

	private void testClientEventByChannelAndGroupName(final PollableChannel channel,
			final String groupName) {
		final HazelcastInstance client = getHazelcastClientByGroupName(groupName);

		Message<?> msg = channel.receive(TIMEOUT);
		verifyClientEvent(msg);

		client.getLifecycleService().terminate();

		msg = channel.receive(TIMEOUT);
		verifyClientEvent(msg);
	}

	private void testDistributedObjectEventByChannelAndHazelcastInstance(
			final PollableChannel channel, final HazelcastInstance hazelcastInstance) {
		final String distributedObjectName = "Test_Distributed_Map";
		final IMap<Integer, String> distributedMap = hazelcastInstance
				.getMap(distributedObjectName);

		Message<?> msg = channel.receive(TIMEOUT);
		verifyDistributedObjectEvent(msg, DistributedObjectEvent.EventType.CREATED,
				distributedObjectName);

		distributedMap.destroy();

		msg = channel.receive(TIMEOUT);
		verifyDistributedObjectEvent(msg, DistributedObjectEvent.EventType.DESTROYED,
				distributedObjectName);
	}

	private HazelcastInstance getHazelcastClientByGroupName(final String groupName) {
		final GroupConfig groupConfig = new GroupConfig();
		groupConfig.setName(groupName);
		final ClientConfig cfg = new ClientConfig();
		cfg.setGroupConfig(groupConfig);

		return HazelcastClient.newHazelcastClient(cfg);
	}

	private void verifyMembershipEvent(final Message<?> msg, final int membershipEvent) {
		assertNotNull(msg);
		assertNotNull(msg.getPayload());
		assertTrue(msg.getPayload() instanceof MembershipEvent);
		assertEquals(((MembershipEvent) msg.getPayload()).getEventType(), membershipEvent);
		assertNotNull(((MembershipEvent) msg.getPayload()).getMember());
	}

	private void verifyDistributedObjectEvent(final Message<?> msg,
			final DistributedObjectEvent.EventType eventType,
			final String distributedObjectName) {
		assertNotNull(msg);
		assertNotNull(msg.getPayload());
		assertTrue(msg.getPayload() instanceof DistributedObjectEvent);
		assertEquals(((DistributedObjectEvent) msg.getPayload()).getEventType(),
				eventType);
		assertNotNull(
				(((DistributedObjectEvent) msg.getPayload()).getDistributedObject())
						.getName(),
				distributedObjectName);
	}

	private void verifyMigrationEvent(final Message<?> msg) {
		assertNotNull(msg);
		assertNotNull(msg.getPayload());
		assertTrue(msg.getPayload() instanceof MigrationEvent);
		assertNotNull(((MigrationEvent) msg.getPayload()).getStatus());
		assertNotNull(((MigrationEvent) msg.getPayload()).getNewOwner());
		assertNotNull(((MigrationEvent) msg.getPayload()).getOldOwner());
	}

	private void verifyLifecycleEvent(final Message<?> msg,
			final LifecycleState lifecycleState) {
		assertNotNull(msg);
		assertNotNull(msg.getPayload());
		assertTrue(msg.getPayload() instanceof LifecycleEvent);
		assertEquals(((LifecycleEvent) msg.getPayload()).getState(), lifecycleState);
	}

	private void verifyClientEvent(final Message<?> msg) {
		assertNotNull(msg);
		assertNotNull(msg.getPayload());
		assertTrue(msg.getPayload() instanceof Client);
		assertEquals(((Client) msg.getPayload()).getClientType(), ClientType.JAVA);
		assertNotNull(((Client) msg.getPayload()).getSocketAddress());
	}

}