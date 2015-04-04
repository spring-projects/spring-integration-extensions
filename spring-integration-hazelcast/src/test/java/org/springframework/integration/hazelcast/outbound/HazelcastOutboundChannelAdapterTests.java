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

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.handler.advice.AbstractRequestHandlerAdvice;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandlingException;
import org.springframework.messaging.PollableChannel;
import org.springframework.messaging.support.ErrorMessage;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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
public class HazelcastOutboundChannelAdapterTests {

	private static final int DATA_COUNT = 100;

	@Autowired
	private MessageChannel mapChannel;

	@Autowired
	private MessageChannel listChannel;

	@Autowired
	private MessageChannel setChannel;

	@Autowired
	private MessageChannel queueChannel;

	@Autowired
	private PollableChannel errorChannel;

	@Resource
	private Map<?, ?> distributedMap;

	@Resource
	private List<?> distributedList;

	@Resource
	private Set<?> distributedSet;

	@Resource
	private Queue<?> distributedQueue;

	@Autowired
	private TestRequestHandlerAdvice testRequestHandlerAdvice;

	@Before
	public void setUp() {
		distributedMap.clear();
		distributedList.clear();
		distributedSet.clear();
		distributedQueue.clear();
	}

	@Test
	public void testWriteDistributedMap() {
		Map<Integer, String> map = createMapByEntryCount();
		mapChannel.send(new GenericMessage<>(map));
		verifyDistributedMap();
	}

	@Test
	public void testWriteDistributedList() {
		List<Integer> list = (List<Integer>) fillCollectionByEntryCount(new ArrayList<Integer>());
		listChannel.send(new GenericMessage<>(list));
		verifyDistributedList();
	}

	@Test
	public void testWriteDistributedSet() {
		Set<Integer> set = (Set<Integer>) fillCollectionByEntryCount(new HashSet<Integer>());
		setChannel.send(new GenericMessage<>(set));
		verifyDistributedSet();
	}

	@Test
	public void testWriteDistributedQueue() throws InterruptedException {
		Collection<Integer> queue = fillCollectionByEntryCount(new LinkedBlockingQueue<Integer>(DATA_COUNT));
		this.queueChannel.send(new GenericMessage<>(queue));

		assertTrue(this.testRequestHandlerAdvice.executeLatch.await(10, TimeUnit.SECONDS));

		Assert.assertEquals(true, this.distributedQueue.size() == DATA_COUNT);
		int index = 0;
		for (Object o : this.distributedQueue) {
			Assert.assertEquals(index++, o);
		}
	}

	@Test(expected = MessageHandlingException.class)
	public void testMapChannelWithIncorrectDataType() {
		Set<Integer> set = new HashSet<>();
		set.add(1);
		mapChannel.send(new GenericMessage<>(set));
	}

	@Test(expected = MessageHandlingException.class)
	public void testListChannelWithIncorrectDataType() {
		Set<Integer> set = new HashSet<>();
		set.add(1);
		listChannel.send(new GenericMessage<>(set));
	}

	@Test(expected = MessageHandlingException.class)
	public void testSetChannelWithIncorrectDataType() {
		List<Integer> list = new ArrayList<>();
		list.add(1);
		setChannel.send(new GenericMessage<>(list));
	}

	public void testQueueChannelWithIncorrectDataType() {
		Set<Integer> set = new HashSet<>();
		set.add(1);
		this.queueChannel.send(new GenericMessage<>(set));
		Message<?> receive = this.errorChannel.receive(10000);
		assertNotNull(receive);
		assertThat(receive, instanceOf(ErrorMessage.class));
		assertThat(receive.getPayload(), instanceOf(MessageHandlingException.class));

	}

	private Map<Integer, String> createMapByEntryCount() {
		Map<Integer, String> map = new HashMap<>();
		StringBuilder strBuilder = new StringBuilder();
		for (int index = 0; index < DATA_COUNT; index++) {
			String value = strBuilder.append("Value_").append(index).toString();
			map.put(index, value);
			strBuilder.delete(0, strBuilder.length());
		}

		return map;
	}

	private void verifyDistributedMap() {
		Assert.assertEquals(true, distributedMap.size() == DATA_COUNT);

		StringBuilder strBuilder = new StringBuilder();
		for (int index = 0; index < DATA_COUNT; index++) {
			String value = strBuilder.append("Value_").append(index).toString();
			Assert.assertEquals(value, distributedMap.get(index));
			strBuilder.delete(0, strBuilder.length());
		}
	}

	private Collection<Integer> fillCollectionByEntryCount(Collection<Integer> coll) {
		for (int index = 0; index < DATA_COUNT; index++) {
			coll.add(index);
		}

		return coll;
	}

	private void verifyDistributedList() {
		Assert.assertEquals(true, distributedList.size() == DATA_COUNT);
		for (int index = 0; index < DATA_COUNT; index++) {
			Assert.assertEquals(index, distributedList.get(index));
		}
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private void verifyDistributedSet() {
		Assert.assertEquals(true, distributedSet.size() == DATA_COUNT);
		List list = new ArrayList(distributedSet);
		Collections.sort(list);
		for (int index = 0; index < DATA_COUNT; index++) {
			Assert.assertEquals(index, list.get(index));
		}
	}

	public static class TestRequestHandlerAdvice extends AbstractRequestHandlerAdvice {

		public final CountDownLatch executeLatch = new CountDownLatch(1);

		@Override
		protected Object doInvoke(ExecutionCallback callback, Object target, Message<?> message) throws Exception {
			try {
				return callback.execute();
			}
			finally {
				this.executeLatch.countDown();
			}
		}

	}

}
