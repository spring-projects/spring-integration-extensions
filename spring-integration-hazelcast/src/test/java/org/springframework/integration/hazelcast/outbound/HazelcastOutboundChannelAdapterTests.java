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

package org.springframework.integration.hazelcast.outbound;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.hazelcast.HazelcastHeaders;
import org.springframework.integration.hazelcast.HazelcastIntegrationTestUser;
import org.springframework.integration.hazelcast.HazelcastTestRequestHandlerAdvice;
import org.springframework.integration.support.DefaultMessageBuilderFactory;
import org.springframework.integration.support.MessageBuilderFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandlingException;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.hazelcast.core.ITopic;
import com.hazelcast.core.MessageListener;
import com.hazelcast.core.MultiMap;
import com.hazelcast.core.ReplicatedMap;

/**
 * Hazelcast Outbound Channel Adapter Test Class
 *
 * @author Eren Avsarogullari
 * @author Artem Bilan
 * @since 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@DirtiesContext
@SuppressWarnings({ "rawtypes", "unchecked" })
public class HazelcastOutboundChannelAdapterTests {

	private static final int DATA_COUNT = 100;

	private static final int DEFAULT_AGE = 5;

	private static final String TEST_NAME = "Test_Name";

	private static final String TEST_SURNAME = "Test_Surname";

	private static final String DISTRIBUTED_MAP = "distributedMap";

	private static final String CACHE_HEADER = "CACHE_HEADER";

	private final MessageBuilderFactory messageBuilderFactory = new DefaultMessageBuilderFactory();

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
	public void testWriteToDistributedMap() throws InterruptedException {
		sendMessageToChannel(this.firstMapChannel);
		assertTrue(this.testFirstMapRequestHandlerAdvice.executeLatch.await(10,
				TimeUnit.SECONDS));
		verifyMapForPayload(new TreeMap(this.distributedMap));
	}

	@Test
	public void testBulkWriteToDistributedMap() throws InterruptedException {
		Map<Integer, HazelcastIntegrationTestUser> userMap = new HashMap<>(DATA_COUNT);
		for (int index = 1; index <= DATA_COUNT; index++) {
			userMap.put(index, getTestUser(index));
		}

		this.bulkMapChannel.send(new GenericMessage<>(userMap));

		assertTrue(this.testBulkMapRequestHandlerAdvice.executeLatch.await(10,
				TimeUnit.SECONDS));
		verifyMapForPayload(new TreeMap(this.distributedBulkMap));
	}

	@Test
	public void testWriteToDistributedMapWhenCacheExpressionIsSet()
			throws InterruptedException {
		sendMessageWithCacheHeaderToChannel(this.secondMapChannel, CACHE_HEADER,
				DISTRIBUTED_MAP);
		assertTrue(this.testSecondMapRequestHandlerAdvice.executeLatch.await(10,
				TimeUnit.SECONDS));
		verifyMapForPayload(new TreeMap(this.distributedMap));
	}

	@Test
	public void testWriteToDistributedMapWhenHazelcastHeaderIsSet()
			throws InterruptedException {
		sendMessageWithCacheHeaderToChannel(this.thirdMapChannel,
				HazelcastHeaders.CACHE_NAME, DISTRIBUTED_MAP);
		assertTrue(this.testThirdMapRequestHandlerAdvice.executeLatch.await(10,
				TimeUnit.SECONDS));
		verifyMapForPayload(new TreeMap(this.distributedMap));
	}

	@Test
	public void testWriteToDistributedMapWhenExtractPayloadIsFalse()
			throws InterruptedException {
		sendMessageWithCacheHeaderToChannel(this.fourthMapChannel,
				HazelcastHeaders.CACHE_NAME, DISTRIBUTED_MAP);
		assertTrue(this.testFourthMapRequestHandlerAdvice.executeLatch.await(10,
				TimeUnit.SECONDS));
		verifyMapForMessage(new TreeMap(this.distributedMap));
	}

	@Test
	public void testWriteToMultiMap() throws InterruptedException {
		sendMessageToChannel(this.multiMapChannel);
		assertTrue(this.testMultiMapRequestHandlerAdvice.executeLatch.await(10,
				TimeUnit.SECONDS));
		verifyMultiMapForPayload(this.multiMap);
	}

	@Test
	public void testWriteToReplicatedMap() throws InterruptedException {
		sendMessageToChannel(this.replicatedMapChannel);
		assertTrue(this.testReplicatedMapRequestHandlerAdvice.executeLatch.await(10,
				TimeUnit.SECONDS));
		verifyMapForPayload(new TreeMap(this.replicatedMap));
	}

	@Test
	public void testBulkWriteToReplicatedMap() throws InterruptedException {
		Map<Integer, HazelcastIntegrationTestUser> userMap = new HashMap<>(DATA_COUNT);
		for (int index = 1; index <= DATA_COUNT; index++) {
			userMap.put(index, getTestUser(index));
		}

		this.bulkReplicatedMapChannel.send(new GenericMessage<>(userMap));

		assertTrue(this.testBulkReplicatedMapRequestHandlerAdvice.executeLatch.await(10,
				TimeUnit.SECONDS));
		verifyMapForPayload(new TreeMap(this.bulkReplicatedMap));
	}


	@Test
	public void testWriteToDistributedList() throws InterruptedException {
		sendMessageToChannel(this.listChannel);
		assertTrue(this.testListRequestHandlerAdvice.executeLatch.await(10,
				TimeUnit.SECONDS));
		verifyCollection(this.distributedList, DATA_COUNT);
	}

	@Test
	public void testBulkWriteToDistributedList() throws InterruptedException {
		List<HazelcastIntegrationTestUser> userList = new ArrayList<>(DATA_COUNT);
		for (int index = 1; index <= DATA_COUNT; index++) {
			userList.add(getTestUser(index));
		}

		this.bulkListChannel.send(new GenericMessage<>(userList));

		assertTrue(this.testBulkListRequestHandlerAdvice.executeLatch.await(10,
				TimeUnit.SECONDS));
		verifyCollection(this.distributedBulkList, DATA_COUNT);
	}

	@Test
	public void testWriteToDistributedSet() throws InterruptedException {
		sendMessageToChannel(this.setChannel);
		assertTrue(this.testSetRequestHandlerAdvice.executeLatch.await(10,
				TimeUnit.SECONDS));
		final List<HazelcastIntegrationTestUser> list = new ArrayList(this.distributedSet);
		Collections.sort(list);
		verifyCollection(list, DATA_COUNT);
	}

	@Test
	public void testBulkWriteToDistributedSet() throws InterruptedException {
		Set<HazelcastIntegrationTestUser> userSet = new HashSet<>(DATA_COUNT);
		for (int index = 1; index <= DATA_COUNT; index++) {
			userSet.add(getTestUser(index));
		}

		this.bulkSetChannel.send(new GenericMessage<>(userSet));

		assertTrue(this.testBulkSetRequestHandlerAdvice.executeLatch.await(10,
				TimeUnit.SECONDS));
		final List<HazelcastIntegrationTestUser> list = new ArrayList(this.distributedBulkSet);
		Collections.sort(list);
		verifyCollection(list, DATA_COUNT);
	}

	@Test
	public void testWriteToDistributedQueue() throws InterruptedException {
		sendMessageToChannel(this.queueChannel);
		assertTrue(this.testQueueRequestHandlerAdvice.executeLatch.await(10,
				TimeUnit.SECONDS));
		verifyCollection(this.distributedQueue, DATA_COUNT);
	}

	@Test
	public void testBulkWriteToDistributedQueue() throws InterruptedException {
		Queue<HazelcastIntegrationTestUser> userQueue = new ArrayBlockingQueue(DATA_COUNT);
		for (int index = 1; index <= DATA_COUNT; index++) {
			userQueue.add(getTestUser(index));
		}

		this.bulkQueueChannel.send(new GenericMessage<>(userQueue));

		assertTrue(this.testBulkQueueRequestHandlerAdvice.executeLatch.await(10,
				TimeUnit.SECONDS));
		verifyCollection(this.distributedBulkQueue, DATA_COUNT);
	}

	@Test
	public void testWriteToTopic() throws InterruptedException {
		this.topic.addMessageListener(new TestTopicMessageListener());
		sendMessageToChannel(this.topicChannel);
		assertTrue(this.testTopicRequestHandlerAdvice.executeLatch.await(10,
				TimeUnit.SECONDS));
	}

	@Test(expected = MessageHandlingException.class)
	public void testWriteToDistributedMapWhenCacheIsNotSet() {
		this.fifthMapChannel.send(new GenericMessage<>(getTestUser(1)));
	}

	@Test(expected = MessageHandlingException.class)
	public void testWriteToDistributedMapWhenKeyExpressionIsNotSet() {
		Message<HazelcastIntegrationTestUser> message = this.messageBuilderFactory
				.withPayload(getTestUser(1))
				.setHeader(HazelcastHeaders.CACHE_NAME, DISTRIBUTED_MAP).build();
		this.sixthMapChannel.send(message);
	}

	@Test(expected = MessageHandlingException.class)
	public void testWriteToLock() {
		this.lockChannel.send(new GenericMessage<>("foo"));
	}

	private void sendMessageToChannel(final MessageChannel channel) {
		for (int index = 1; index <= DATA_COUNT; index++) {
			channel.send(new GenericMessage<>(getTestUser(index)));
		}
	}

	private void sendMessageWithCacheHeaderToChannel(final MessageChannel channel,
			final String headerName, final String distributedObjectName) {
		for (int index = 1; index <= DATA_COUNT; index++) {
			Message<HazelcastIntegrationTestUser> message = this.messageBuilderFactory
					.withPayload(getTestUser(index))
					.setHeader(headerName, distributedObjectName).build();
			channel.send(message);
		}
	}

	private void verifyMapForPayload(final Map<Integer, HazelcastIntegrationTestUser> map) {
		int index = 1;
		assertNotNull(map);
		assertEquals(true, map.size() == DATA_COUNT);
		for (Entry<Integer, HazelcastIntegrationTestUser> entry : map.entrySet()) {
			assertNotNull(entry);
			assertEquals(index, entry.getKey().intValue());
			verifyHazelcastIntegrationTestUser(entry.getValue(), index);
			index++;
		}
	}

	private void verifyMultiMapForPayload(
			final MultiMap<Integer, HazelcastIntegrationTestUser> multiMap) {
		int index = 1;
		assertNotNull(multiMap);
		assertEquals(true, multiMap.size() == DATA_COUNT);
		SortedSet<Integer> keys = new TreeSet<>(multiMap.keySet());
		for (Integer key : keys) {
			assertNotNull(key);
			assertEquals(index, key.intValue());
			HazelcastIntegrationTestUser user = multiMap.get(key).iterator().next();
			verifyHazelcastIntegrationTestUser(user, index);
			index++;
		}
	}

	private void verifyMapForMessage(
			final Map<Integer, Message<HazelcastIntegrationTestUser>> map) {
		int index = 1;
		assertNotNull(map);
		assertEquals(true, map.size() == DATA_COUNT);
		for (Entry<Integer, Message<HazelcastIntegrationTestUser>> entry : map.entrySet()) {
			assertNotNull(entry);
			assertEquals(index, entry.getKey().intValue());
			assertTrue(entry.getValue().getHeaders().size() > 0);
			verifyHazelcastIntegrationTestUser(entry.getValue().getPayload(), index);
			index++;
		}
	}

	private void verifyCollection(final Collection<HazelcastIntegrationTestUser> coll,
			final int dataCount) {
		int index = 1;
		assertNotNull(coll);
		assertEquals(true, coll.size() == dataCount);
		for (HazelcastIntegrationTestUser user : coll) {
			verifyHazelcastIntegrationTestUser(user, index);
			index++;
		}
	}

	private void verifyHazelcastIntegrationTestUser(HazelcastIntegrationTestUser user,
			int index) {
		assertNotNull(user);
		assertEquals(index, user.getId());
		assertEquals(TEST_NAME, user.getName());
		assertEquals(TEST_SURNAME, user.getSurname());
		assertEquals(index + DEFAULT_AGE, user.getAge());
	}

	private HazelcastIntegrationTestUser getTestUser(int index) {
		return new HazelcastIntegrationTestUser(index, TEST_NAME, TEST_SURNAME, index
				+ DEFAULT_AGE);
	}

	private class TestTopicMessageListener implements MessageListener {

		private int index = 1;

		@Override
		public void onMessage(com.hazelcast.core.Message message) {
			HazelcastIntegrationTestUser user = (HazelcastIntegrationTestUser) message
					.getMessageObject();
			verifyHazelcastIntegrationTestUser(user, index);
			index++;
		}

	}

}
