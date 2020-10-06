/*
 * Copyright 2015-2019 the original author or authors.
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
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.hazelcast.inbound.util.HazelcastInboundChannelAdapterTestUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.PollableChannel;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.hazelcast.client.Client;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.LifecycleEvent;
import com.hazelcast.core.LifecycleEvent.LifecycleState;
import com.hazelcast.instance.impl.HazelcastInstanceFactory;
import com.hazelcast.internal.nio.ConnectionType;

/**
 * Hazelcast Cluster Monitor Inbound Channel Adapter Unit Test Class
 *
 * @author Eren Avsarogullari
 * @author Artem Bilan
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@DirtiesContext
@Ignore("Hard to reach CP consensus with limited number of members in two clusters")
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

	@AfterClass
	public static void shutdown() {
		HazelcastInstanceFactory.terminateAll();
	}

	@Test
	public void testDistributedObjectEvent() {
		HazelcastInboundChannelAdapterTestUtils
				.testDistributedObjectEventByChannelAndHazelcastInstance(cmChannel2,
						hazelcastInstance, "Test_Distributed_Map4");
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
		assertEquals(ConnectionType.JAVA_CLIENT, ((Client) msg.getPayload()).getClientType());
		assertNotNull(((Client) msg.getPayload()).getSocketAddress());
	}

}
