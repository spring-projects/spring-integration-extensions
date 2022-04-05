/*
 * Copyright 2015-2020 the original author or authors.
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

package org.springframework.integration.hazelcast.outbound;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.hazelcast.HazelcastHeaders;
import org.springframework.integration.hazelcast.HazelcastIntegrationTestUser;
import org.springframework.integration.hazelcast.HazelcastTestRequestHandlerAdvice;
import org.springframework.integration.hazelcast.outbound.util.HazelcastOutboundChannelAdapterTestUtils;
import org.springframework.integration.support.DefaultMessageBuilderFactory;
import org.springframework.integration.support.MessageBuilderFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandlingException;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.hazelcast.instance.impl.HazelcastInstanceFactory;
import com.hazelcast.multimap.MultiMap;
import com.hazelcast.replicatedmap.ReplicatedMap;
import com.hazelcast.topic.ITopic;

/**
 * Hazelcast Outbound Channel Adapter Test Class.
 *
 * @author Eren Avsarogullari
 * @author Artem Bilan
 *
 * @since 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@DirtiesContext
@SuppressWarnings({ "rawtypes", "unchecked" })
public class HazelcastOutboundChannelAdapterTests {

	private static final String DISTRIBUTED_MAP = "distributedMap";

	private static final String CACHE_HEADER = "CACHE_HEADER";

	private final MessageBuilderFactory messageBuilderFactory =
			new DefaultMessageBuilderFactory();

	@Autowired
	@Qualifier("firstMapChannel")
	private MessageChannel firstMapChannel;

	@Autowired
	@Qualifier("secondMapChannel")
	private MessageChannel secondMapChannel;

	@Autowired
	@Qualifier("thirdMapChannel")
	private MessageChannel thirdMapChannel;

	@Autowired
	@Qualifier("fourthMapChannel")
	private MessageChannel fourthMapChannel;

	@Autowired
	@Qualifier("fifthMapChannel")
	private MessageChannel fifthMapChannel;

	@Autowired
	@Qualifier("sixthMapChannel")
	private MessageChannel sixthMapChannel;

	@Autowired
	@Qualifier("bulkMapChannel")
	private MessageChannel bulkMapChannel;

	@Autowired
	@Qualifier("multiMapChannel")
	private MessageChannel multiMapChannel;

	@Autowired
	@Qualifier("replicatedMapChannel")
	private MessageChannel replicatedMapChannel;

	@Autowired
	@Qualifier("bulkReplicatedMapChannel")
	private MessageChannel bulkReplicatedMapChannel;

	@Autowired
	@Qualifier("listChannel")
	private MessageChannel listChannel;

	@Autowired
	@Qualifier("bulkListChannel")
	private MessageChannel bulkListChannel;

	@Autowired
	@Qualifier("setChannel")
	private MessageChannel setChannel;

	@Autowired
	@Qualifier("bulkSetChannel")
	private MessageChannel bulkSetChannel;

	@Autowired
	@Qualifier("queueChannel")
	private MessageChannel queueChannel;

	@Autowired
	@Qualifier("bulkQueueChannel")
	private MessageChannel bulkQueueChannel;

	@Autowired
	@Qualifier("topicChannel")
	private MessageChannel topicChannel;

	@Autowired
	@Qualifier("lockChannel")
	private MessageChannel lockChannel;

	@Resource
	private Map<?, ?> distributedMap;

	@Resource
	private Map<?, ?> distributedBulkMap;

	@Resource
	private MultiMap<Integer, HazelcastIntegrationTestUser> multiMap;

	@Resource
	private ReplicatedMap<Integer, HazelcastIntegrationTestUser> replicatedMap;

	@Resource
	private ReplicatedMap<Integer, HazelcastIntegrationTestUser> bulkReplicatedMap;

	@Resource
	private List<HazelcastIntegrationTestUser> distributedList;

	@Resource
	private List<HazelcastIntegrationTestUser> distributedBulkList;

	@Resource
	private Set<HazelcastIntegrationTestUser> distributedSet;

	@Resource
	private Set<HazelcastIntegrationTestUser> distributedBulkSet;

	@Resource
	private Queue<HazelcastIntegrationTestUser> distributedQueue;

	@Resource
	private Queue<HazelcastIntegrationTestUser> distributedBulkQueue;

	@Resource
	private ITopic<HazelcastIntegrationTestUser> topic;

	@Autowired
	@Qualifier("testFirstMapRequestHandlerAdvice")
	private HazelcastTestRequestHandlerAdvice testFirstMapRequestHandlerAdvice;

	@Autowired
	@Qualifier("testSecondMapRequestHandlerAdvice")
	private HazelcastTestRequestHandlerAdvice testSecondMapRequestHandlerAdvice;

	@Autowired
	@Qualifier("testThirdMapRequestHandlerAdvice")
	private HazelcastTestRequestHandlerAdvice testThirdMapRequestHandlerAdvice;

	@Autowired
	@Qualifier("testFourthMapRequestHandlerAdvice")
	private HazelcastTestRequestHandlerAdvice testFourthMapRequestHandlerAdvice;

	@Autowired
	@Qualifier("testBulkMapRequestHandlerAdvice")
	private HazelcastTestRequestHandlerAdvice testBulkMapRequestHandlerAdvice;

	@Autowired
	@Qualifier("testMultiMapRequestHandlerAdvice")
	private HazelcastTestRequestHandlerAdvice testMultiMapRequestHandlerAdvice;

	@Autowired
	@Qualifier("testReplicatedMapRequestHandlerAdvice")
	private HazelcastTestRequestHandlerAdvice testReplicatedMapRequestHandlerAdvice;

	@Autowired
	@Qualifier("testBulkReplicatedMapRequestHandlerAdvice")
	private HazelcastTestRequestHandlerAdvice testBulkReplicatedMapRequestHandlerAdvice;

	@Autowired
	@Qualifier("testListRequestHandlerAdvice")
	private HazelcastTestRequestHandlerAdvice testListRequestHandlerAdvice;

	@Autowired
	@Qualifier("testBulkListRequestHandlerAdvice")
	private HazelcastTestRequestHandlerAdvice testBulkListRequestHandlerAdvice;

	@Autowired
	@Qualifier("testSetRequestHandlerAdvice")
	private HazelcastTestRequestHandlerAdvice testSetRequestHandlerAdvice;

	@Autowired
	@Qualifier("testBulkSetRequestHandlerAdvice")
	private HazelcastTestRequestHandlerAdvice testBulkSetRequestHandlerAdvice;

	@Autowired
	@Qualifier("testQueueRequestHandlerAdvice")
	private HazelcastTestRequestHandlerAdvice testQueueRequestHandlerAdvice;

	@Autowired
	@Qualifier("testBulkQueueRequestHandlerAdvice")
	private HazelcastTestRequestHandlerAdvice testBulkQueueRequestHandlerAdvice;

	@Autowired
	@Qualifier("testTopicRequestHandlerAdvice")
	private HazelcastTestRequestHandlerAdvice testTopicRequestHandlerAdvice;

	@AfterClass
	public static void shutdown() {
		HazelcastInstanceFactory.terminateAll();
	}

	@Before
	public void setUp() {
		this.distributedMap.clear();
		this.distributedBulkMap.clear();
		this.distributedList.clear();
		this.distributedBulkList.clear();
		this.distributedSet.clear();
		this.distributedBulkSet.clear();
		this.distributedQueue.clear();
		this.distributedBulkQueue.clear();
		this.multiMap.clear();
		this.replicatedMap.clear();
		this.bulkReplicatedMap.clear();
	}

	@Test
	public void testWriteToDistributedMap() {
		HazelcastOutboundChannelAdapterTestUtils
				.testWriteToDistributedMap(this.firstMapChannel, this.distributedMap,
						this.testFirstMapRequestHandlerAdvice);
	}

	@Test
	public void testBulkWriteToDistributedMap() {
		HazelcastOutboundChannelAdapterTestUtils
				.testBulkWriteToDistributedMap(this.bulkMapChannel, this.distributedBulkMap,
						this.testBulkMapRequestHandlerAdvice);
	}

	@Test
	public void testWriteToDistributedMapWhenCacheExpressionIsSet()
			throws InterruptedException {
		sendMessageWithCacheHeaderToChannel(this.secondMapChannel, CACHE_HEADER,
				DISTRIBUTED_MAP);
		assertThat(this.testSecondMapRequestHandlerAdvice.executeLatch
				.await(10, TimeUnit.SECONDS)).isTrue();
		HazelcastOutboundChannelAdapterTestUtils
				.verifyMapForPayload(new TreeMap(this.distributedMap));
	}

	@Test
	public void testWriteToDistributedMapWhenHazelcastHeaderIsSet()
			throws InterruptedException {
		sendMessageWithCacheHeaderToChannel(this.thirdMapChannel,
				HazelcastHeaders.CACHE_NAME, DISTRIBUTED_MAP);
		assertThat(this.testThirdMapRequestHandlerAdvice.executeLatch
				.await(10, TimeUnit.SECONDS)).isTrue();
		HazelcastOutboundChannelAdapterTestUtils
				.verifyMapForPayload(new TreeMap(this.distributedMap));
	}

	@Test
	public void testWriteToDistributedMapWhenExtractPayloadIsFalse()
			throws InterruptedException {
		sendMessageWithCacheHeaderToChannel(this.fourthMapChannel,
				HazelcastHeaders.CACHE_NAME, DISTRIBUTED_MAP);
		assertThat(this.testFourthMapRequestHandlerAdvice.executeLatch
				.await(10, TimeUnit.SECONDS)).isTrue();
		verifyMapForMessage(new TreeMap(this.distributedMap));
	}

	@Test
	public void testWriteToMultiMap() {
		HazelcastOutboundChannelAdapterTestUtils
				.testWriteToMultiMap(this.multiMapChannel, this.multiMap,
						this.testMultiMapRequestHandlerAdvice);
	}

	@Test
	public void testWriteToReplicatedMap() {
		HazelcastOutboundChannelAdapterTestUtils
				.testWriteToReplicatedMap(this.replicatedMapChannel, this.replicatedMap,
						this.testReplicatedMapRequestHandlerAdvice);
	}

	@Test
	public void testBulkWriteToReplicatedMap() throws InterruptedException {
		Map<Integer, HazelcastIntegrationTestUser> userMap =
				new HashMap<>(HazelcastOutboundChannelAdapterTestUtils.DATA_COUNT);
		for (int index = 1;
				index <= HazelcastOutboundChannelAdapterTestUtils.DATA_COUNT; index++) {
			userMap
					.put(index, HazelcastOutboundChannelAdapterTestUtils.getTestUser(index));
		}

		this.bulkReplicatedMapChannel.send(new GenericMessage<>(userMap));

		assertThat(this.testBulkReplicatedMapRequestHandlerAdvice.executeLatch
				.await(10, TimeUnit.SECONDS)).isTrue();
		HazelcastOutboundChannelAdapterTestUtils
				.verifyMapForPayload(new TreeMap(this.bulkReplicatedMap));
	}


	@Test
	public void testWriteToDistributedList() {
		HazelcastOutboundChannelAdapterTestUtils
				.testWriteToDistributedList(this.listChannel, this.distributedList,
						this.testListRequestHandlerAdvice);
	}

	@Test
	public void testBulkWriteToDistributedList() throws InterruptedException {
		List<HazelcastIntegrationTestUser> userList =
				new ArrayList<>(HazelcastOutboundChannelAdapterTestUtils.DATA_COUNT);
		for (int index = 1;
				index <= HazelcastOutboundChannelAdapterTestUtils.DATA_COUNT; index++) {
			userList.add(HazelcastOutboundChannelAdapterTestUtils.getTestUser(index));
		}

		this.bulkListChannel.send(new GenericMessage<>(userList));

		assertThat(this.testBulkListRequestHandlerAdvice.executeLatch
				.await(10, TimeUnit.SECONDS)).isTrue();
		HazelcastOutboundChannelAdapterTestUtils
				.verifyCollection(this.distributedBulkList,
						HazelcastOutboundChannelAdapterTestUtils.DATA_COUNT);
	}

	@Test
	public void testWriteToDistributedSet() {
		HazelcastOutboundChannelAdapterTestUtils
				.testWriteToDistributedSet(this.setChannel, this.distributedSet,
						this.testSetRequestHandlerAdvice);
	}

	@Test
	public void testBulkWriteToDistributedSet() throws InterruptedException {
		Set<HazelcastIntegrationTestUser> userSet =
				new HashSet<>(HazelcastOutboundChannelAdapterTestUtils.DATA_COUNT);
		for (int index = 1;
				index <= HazelcastOutboundChannelAdapterTestUtils.DATA_COUNT; index++) {
			userSet.add(HazelcastOutboundChannelAdapterTestUtils.getTestUser(index));
		}

		this.bulkSetChannel.send(new GenericMessage<>(userSet));

		assertThat(this.testBulkSetRequestHandlerAdvice.executeLatch
				.await(10, TimeUnit.SECONDS)).isTrue();
		final List<HazelcastIntegrationTestUser> list =
				new ArrayList(this.distributedBulkSet);
		Collections.sort(list);
		HazelcastOutboundChannelAdapterTestUtils
				.verifyCollection(list, HazelcastOutboundChannelAdapterTestUtils.DATA_COUNT);
	}

	@Test
	public void testWriteToDistributedQueue() {
		HazelcastOutboundChannelAdapterTestUtils
				.testWriteToDistributedQueue(this.queueChannel, this.distributedQueue,
						this.testQueueRequestHandlerAdvice);
	}

	@Test
	public void testBulkWriteToDistributedQueue() throws InterruptedException {
		Queue<HazelcastIntegrationTestUser> userQueue =
				new ArrayBlockingQueue(HazelcastOutboundChannelAdapterTestUtils.DATA_COUNT);
		for (int index = 1;
				index <= HazelcastOutboundChannelAdapterTestUtils.DATA_COUNT; index++) {
			userQueue.add(HazelcastOutboundChannelAdapterTestUtils.getTestUser(index));
		}

		this.bulkQueueChannel.send(new GenericMessage<>(userQueue));

		assertThat(this.testBulkQueueRequestHandlerAdvice.executeLatch
				.await(10, TimeUnit.SECONDS)).isTrue();
		HazelcastOutboundChannelAdapterTestUtils
				.verifyCollection(this.distributedBulkQueue,
						HazelcastOutboundChannelAdapterTestUtils.DATA_COUNT);
	}

	@Test
	public void testWriteToTopic() {
		HazelcastOutboundChannelAdapterTestUtils
				.testWriteToTopic(this.topicChannel, this.topic,
						this.testTopicRequestHandlerAdvice);
	}

	@Test(expected = MessageHandlingException.class)
	public void testWriteToDistributedMapWhenCacheIsNotSet() {
		this.fifthMapChannel.send(new GenericMessage<>(
				HazelcastOutboundChannelAdapterTestUtils.getTestUser(1)));
	}

	@Test(expected = MessageHandlingException.class)
	public void testWriteToDistributedMapWhenKeyExpressionIsNotSet() {
		Message<HazelcastIntegrationTestUser> message = this.messageBuilderFactory
				.withPayload(HazelcastOutboundChannelAdapterTestUtils.getTestUser(1))
				.setHeader(HazelcastHeaders.CACHE_NAME, DISTRIBUTED_MAP).build();
		this.sixthMapChannel.send(message);
	}

	@Test(expected = MessageHandlingException.class)
	public void testWriteToLock() {
		this.lockChannel.send(new GenericMessage<>("foo"));
	}

	private void sendMessageWithCacheHeaderToChannel(final MessageChannel channel,
			final String headerName, final String distributedObjectName) {
		for (int index = 1;
				index <= HazelcastOutboundChannelAdapterTestUtils.DATA_COUNT; index++) {
			Message<HazelcastIntegrationTestUser> message = this.messageBuilderFactory
					.withPayload(HazelcastOutboundChannelAdapterTestUtils.getTestUser(index))
					.setHeader(headerName, distributedObjectName).build();
			channel.send(message);
		}
	}

	private void verifyMapForMessage(
			final Map<Integer, Message<HazelcastIntegrationTestUser>> map) {
		int index = 1;
		assertThat(map).isNotNull();
		assertThat(map.size()).isEqualTo(HazelcastOutboundChannelAdapterTestUtils.DATA_COUNT);
		for (Entry<Integer, Message<HazelcastIntegrationTestUser>> entry : map.entrySet()) {
			assertThat(entry).isNotNull();
			assertThat(entry.getKey().intValue()).isEqualTo(index);
			assertThat(entry.getValue().getHeaders().size() > 0).isTrue();
			HazelcastOutboundChannelAdapterTestUtils
					.verifyHazelcastIntegrationTestUser(entry.getValue().getPayload(), index);
			index++;
		}
	}

}
