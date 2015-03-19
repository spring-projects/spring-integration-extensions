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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.DefaultMessageBuilderFactory;
import org.springframework.integration.support.MessageBuilderFactory;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandlingException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Hazelcast Outbound Channel Adapter Test Class
 *
 * @author Eren Avsarogullari
 * @since 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@DirtiesContext
public class HazelcastOutboundChannelAdapterTests {

	private static final int DATA_COUNT = 100;

	private volatile MessageBuilderFactory messageBuilderFactory;

	@Autowired
	private MessageChannel mapChannel;

	@Autowired
	private MessageChannel listChannel;

	@Autowired
	private MessageChannel setChannel;

	@Autowired
	private MessageChannel queueChannel;

	@Resource
	private Map<?, ?> distributedMap;

	@Resource
	private List<?> distributedList;

	@Resource
	private Set<?> distributedSet;

	@Resource
	private Queue<?> distributedQueue;

	@Before
	public void setUp() {
		distributedMap.clear();
		distributedList.clear();
		distributedSet.clear();
		distributedQueue.clear();

		messageBuilderFactory = new DefaultMessageBuilderFactory();
	}

	@Test
	public void testWriteDistributedMap() {
		Map<Integer, String> map = createMapByEntryCount();
		mapChannel.send(this.messageBuilderFactory.withPayload(map).build());
		verifyDistributedMap();
	}

	@Test
	public void testWriteDistributedList() {
		List<Integer> list = (List<Integer>) fillCollectionByEntryCount(new ArrayList<Integer>());
		listChannel.send(this.messageBuilderFactory.withPayload(list).build());
		verifyDistributedList();
	}

	@Test
	public void testWriteDistributedSet() {
		Set<Integer> set = (Set<Integer>) fillCollectionByEntryCount(new HashSet<Integer>());
		setChannel.send(this.messageBuilderFactory.withPayload(set).build());
		verifyDistributedSet();
	}

	@Test
	public void testWriteDistributedQueue() {
		Queue<Integer> queue = (Queue<Integer>) fillCollectionByEntryCount(
				new LinkedBlockingQueue<Integer>(DATA_COUNT));
		queueChannel.send(this.messageBuilderFactory.withPayload(queue).build());
		verifyDistributedQueue();
	}

	@Test(expected = MessageHandlingException.class)
	public void testMapChannelWithIncorrectDataType() {
		Set<Integer> set = new HashSet<>();
		set.add(1);
		mapChannel.send(this.messageBuilderFactory.withPayload(set).build());
	}

	@Test(expected = MessageHandlingException.class)
	public void testListChannelWithIncorrectDataType() {
		Set<Integer> set = new HashSet<>();
		set.add(1);
		listChannel.send(this.messageBuilderFactory.withPayload(set).build());
	}

	@Test(expected = MessageHandlingException.class)
	public void testSetChannelWithIncorrectDataType() {
		List<Integer> list = new ArrayList<>();
		list.add(1);
		setChannel.send(this.messageBuilderFactory.withPayload(list).build());
	}

	@Test(expected = MessageHandlingException.class)
	public void testQueueChannelWithIncorrectDataType() {
		Set<Integer> set = new HashSet<>();
		set.add(1);
		queueChannel.send(this.messageBuilderFactory.withPayload(set).build());
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void verifyDistributedSet() {
		Assert.assertEquals(true, distributedSet.size() == DATA_COUNT);
		List list = new ArrayList(distributedSet);
		Collections.sort(list);
		for (int index = 0; index < DATA_COUNT; index++) {
			Assert.assertEquals(index, list.get(index));
		}
	}

	private void verifyDistributedQueue() {
		Assert.assertEquals(true, distributedQueue.size() == DATA_COUNT);
		Iterator<?> it = distributedQueue.iterator();
		int index = 0;
		while (it.hasNext()) {
			Assert.assertEquals(index, it.next());
			index++;
		}
	}
}
