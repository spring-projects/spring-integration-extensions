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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.handler.advice.AbstractRequestHandlerAdvice;
import org.springframework.integration.hazelcast.HazelcastHeaders;
import org.springframework.integration.hazelcast.HazelcastIntegrationTestUser;
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

	private static final String DISTRIBUTED_LIST = "distributedList";

	private static final String DISTRIBUTED_QUEUE = "distributedQueue";

	private static final String CACHE_HEADER = "CACHE_HEADER";

	private final MessageBuilderFactory messageBuilderFactory = new DefaultMessageBuilderFactory();

	@Autowired
	private MessageChannel firstMapChannel;

	@Autowired
	private MessageChannel secondMapChannel;

	@Autowired
	private MessageChannel thirdMapChannel;

	@Autowired
	private MessageChannel fourthMapChannel;

	@Autowired
	private MessageChannel fifthMapChannel;

	@Autowired
	private MessageChannel sixthMapChannel;

	@Autowired
	private MessageChannel multiMapChannel;

	@Autowired
	private MessageChannel replicatedMapChannel;

	@Autowired
	private MessageChannel listChannel;

	@Autowired
	private MessageChannel setChannel;

	@Autowired
	private MessageChannel queueChannel;

	@Autowired
	private MessageChannel topicChannel;

	@Autowired
	private MessageChannel differentDistributedObjectsChannel;

	@Resource
	private Map<?, ?> distributedMap;

	@Resource
	private MultiMap<Integer, HazelcastIntegrationTestUser> multiMap;

	@Resource
	private ReplicatedMap<Integer, HazelcastIntegrationTestUser> replicatedMap;

	@Resource
	private List<HazelcastIntegrationTestUser> distributedList;

	@Resource
	private Set<HazelcastIntegrationTestUser> distributedSet;

	@Resource
	private Queue<HazelcastIntegrationTestUser> distributedQueue;

	@Resource
	private ITopic<HazelcastIntegrationTestUser> topic;

	@Autowired
	@Qualifier("testFirstMapRequestHandlerAdvice")
	private TestRequestHandlerAdvice testFirstMapRequestHandlerAdvice;

	@Autowired
	@Qualifier("testSecondMapRequestHandlerAdvice")
	private TestRequestHandlerAdvice testSecondMapRequestHandlerAdvice;

	@Autowired
	@Qualifier("testThirdMapRequestHandlerAdvice")
	private TestRequestHandlerAdvice testThirdMapRequestHandlerAdvice;

	@Autowired
	@Qualifier("testFourthMapRequestHandlerAdvice")
	private TestRequestHandlerAdvice testFourthMapRequestHandlerAdvice;

	@Autowired
	@Qualifier("testMultiMapRequestHandlerAdvice")
	private TestRequestHandlerAdvice testMultiMapRequestHandlerAdvice;

	@Autowired
	@Qualifier("testReplicatedMapRequestHandlerAdvice")
	private TestRequestHandlerAdvice testReplicatedMapRequestHandlerAdvice;

	@Autowired
	@Qualifier("testListRequestHandlerAdvice")
	private TestRequestHandlerAdvice testListRequestHandlerAdvice;

	@Autowired
	@Qualifier("testSetRequestHandlerAdvice")
	private TestRequestHandlerAdvice testSetRequestHandlerAdvice;

	@Autowired
	@Qualifier("testQueueRequestHandlerAdvice")
	private TestRequestHandlerAdvice testQueueRequestHandlerAdvice;

	@Autowired
	@Qualifier("testTopicRequestHandlerAdvice")
	private TestRequestHandlerAdvice testTopicRequestHandlerAdvice;

	@Before
	public void setUp() {
		this.distributedMap.clear();
		this.distributedList.clear();
		this.distributedSet.clear();
		this.distributedQueue.clear();
		this.multiMap.clear();
		this.replicatedMap.clear();
	}

	@Test
	public void testWriteToDistributedMap() throws InterruptedException {
		sendMessageToChannel(this.firstMapChannel);
		assertTrue(this.testFirstMapRequestHandlerAdvice.executeLatch.await(10,
				TimeUnit.SECONDS));
		verifyMapForPayload(new TreeMap(this.distributedMap));
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
	public void testWriteToDistributedList() throws InterruptedException {
		sendMessageToChannel(this.listChannel);
		assertTrue(this.testListRequestHandlerAdvice.executeLatch.await(10,
				TimeUnit.SECONDS));
		verifyCollection(this.distributedList, DATA_COUNT);
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
	public void testWriteToDistributedQueue() throws InterruptedException {
		sendMessageToChannel(this.queueChannel);
		assertTrue(this.testQueueRequestHandlerAdvice.executeLatch.await(10,
				TimeUnit.SECONDS));
		verifyCollection(this.distributedQueue, DATA_COUNT);
	}

	@Test
	public void testWriteToTopic() throws InterruptedException {
		this.topic.addMessageListener(new TestTopicMessageListener());
		sendMessageToChannel(this.topicChannel);
		assertTrue(this.testTopicRequestHandlerAdvice.executeLatch.await(10,
				TimeUnit.SECONDS));
	}

	@Test
	public void testWriteToDifferentDistributedObjects() throws InterruptedException {
		int id = 1;
		HazelcastIntegrationTestUser user = getTestUser(id);
		Message<HazelcastIntegrationTestUser> message = this.messageBuilderFactory
				.withPayload(user).setHeader(CACHE_HEADER, DISTRIBUTED_MAP).build();
		this.differentDistributedObjectsChannel.send(message);
		verifyHazelcastIntegrationTestUser(
				(HazelcastIntegrationTestUser) this.distributedMap.get(id), id);

		message = this.messageBuilderFactory.withPayload(user)
				.setHeader(CACHE_HEADER, DISTRIBUTED_LIST).build();
		this.differentDistributedObjectsChannel.send(message);
		verifyCollection(this.distributedList, 1);

		message = this.messageBuilderFactory.withPayload(user)
				.setHeader(CACHE_HEADER, DISTRIBUTED_QUEUE).build();
		this.differentDistributedObjectsChannel.send(message);
		verifyCollection(this.distributedQueue, 1);
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

	private static class TestRequestHandlerAdvice extends AbstractRequestHandlerAdvice {

		private final CountDownLatch executeLatch = new CountDownLatch(DATA_COUNT);

		@Override
		protected Object doInvoke(ExecutionCallback callback, Object target,
				Message<?> message) throws Exception {
			try {
				return callback.execute();
			}
			finally {
				this.executeLatch.countDown();
			}
		}

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
