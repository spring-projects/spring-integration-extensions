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

package org.springframework.integration.hazelcast.inbound.config;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.hazelcast.HazelcastIntegrationTestUser;
import org.springframework.integration.hazelcast.HazelcastIntegrationTestUtils;
import org.springframework.messaging.PollableChannel;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import com.hazelcast.core.IList;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;
import com.hazelcast.core.ISet;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.MultiMap;
import com.hazelcast.core.ReplicatedMap;

/**
 * Hazelcast Event Driven Inbound Channel Adapter JavaConfig driven Unit Test Class
 *
 * @author Eren Avsarogullari
 * @since 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = HazelcastIntegrationInboundTestConfiguration.class, 
						loader = AnnotationConfigContextLoader.class)
@DirtiesContext
public class HazelcastEventDrivenInboundChannelAdapterConfigTests {

	@Autowired
	private PollableChannel distributedMapChannel;

	@Autowired
	private PollableChannel distributedMapChannel2;

	@Autowired
	private PollableChannel distributedListChannel;

	@Autowired
	private PollableChannel distributedSetChannel;

	@Autowired
	private PollableChannel distributedQueueChannel;

	@Autowired
	private PollableChannel topicChannel;

	@Autowired
	private PollableChannel replicatedMapChannel;

	@Autowired
	private PollableChannel multiMapChannel;

	@Resource
	private IMap<Integer, HazelcastIntegrationTestUser> testDistributedMap;

	@Resource
	private IMap<Integer, HazelcastIntegrationTestUser> testDistributedMap2;

	@Resource
	private IList<HazelcastIntegrationTestUser> testDistributedList;

	@Resource
	private ISet<HazelcastIntegrationTestUser> testDistributedSet;

	@Resource
	private IQueue<HazelcastIntegrationTestUser> testDistributedQueue;

	@Resource
	private ITopic<HazelcastIntegrationTestUser> testTopic;

	@Resource
	private ReplicatedMap<Integer, HazelcastIntegrationTestUser> testReplicatedMap;

	@Resource
	private MultiMap<Integer, HazelcastIntegrationTestUser> testMultiMap;

	@Test
	public void testEventDrivenForADDEDEntryEvent() {
		HazelcastIntegrationTestUtils.testEventDrivenForADDEDDistributedMapEntryEvent(
				testDistributedMap, distributedMapChannel, "Test_Distributed_Map");
	}

	@Test
	public void testEventDrivenForEntryEvents() {
		HazelcastIntegrationTestUtils.testEventDrivenForDistributedMapEntryEvents(
				testDistributedMap2, distributedMapChannel2, "Test_Distributed_Map2");
	}

	@Test
	public void testEventDrivenForDistributedListItemEvents() {
		HazelcastIntegrationTestUtils.testEventDrivenForDistributedCollectionItemEvents(
				testDistributedList, distributedListChannel);
	}

	@Test
	public void testEventDrivenForDistributedSetItemEvents() {
		HazelcastIntegrationTestUtils.testEventDrivenForDistributedCollectionItemEvents(
				testDistributedSet, distributedSetChannel);
	}

	@Test
	public void testEventDrivenForDistributedQueueItemEvents() {
		HazelcastIntegrationTestUtils.testEventDrivenForDistributedCollectionItemEvents(
				testDistributedQueue, distributedQueueChannel);
	}

	@Test
	public void testEventDrivenForADDEDMessageEvent() {
		HazelcastIntegrationTestUtils.testEventDrivenForTopicMessageEvent(testTopic,
				topicChannel);
	}

	@Test
	public void testEventDrivenForReplicatedMapEntryEvents() {
		HazelcastIntegrationTestUtils.testEventDrivenForReplicatedMapEntryEvents(
				testReplicatedMap, replicatedMapChannel, "Test_Replicated_Map");
	}

	@Test
	public void testEventDrivenForMultiMapEntryEvents() {
		HazelcastIntegrationTestUtils.testEventDrivenForMultiMapEntryEvents(testMultiMap,
				multiMapChannel, "Test_Multi_Map");
	}

}
