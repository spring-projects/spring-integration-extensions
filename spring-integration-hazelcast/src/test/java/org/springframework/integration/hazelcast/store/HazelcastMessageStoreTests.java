/*
 * Copyright 2017 the original author or authors.
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

package org.springframework.integration.hazelcast.store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.history.MessageHistory;
import org.springframework.integration.store.MessageGroup;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.test.util.TestUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

/**
 * @author Vinicius Carvalho
 * @author Artem Bilan
 */
public class HazelcastMessageStoreTests {

	private static HazelcastMessageStore store;

	private static HazelcastInstance instance;

	private static IMap<Object, Object> map;

	@BeforeClass
	public static void init() throws Exception {
		instance = Hazelcast.newHazelcastInstance();
		map = instance.getMap("customTestsMessageStore");
		store = new HazelcastMessageStore(map);
	}

	@AfterClass
	public static void destroy() throws Exception {
		instance.shutdown();
	}

	@Before
	public void clean() throws Exception {
		map.clear();
	}

	@Test
	public void testWithMessageHistory() throws Exception {

		Message<?> message = new GenericMessage<>("Hello");
		DirectChannel fooChannel = new DirectChannel();
		fooChannel.setBeanName("fooChannel");
		DirectChannel barChannel = new DirectChannel();
		barChannel.setBeanName("barChannel");

		message = MessageHistory.write(message, fooChannel);
		message = MessageHistory.write(message, barChannel);
		store.addMessage(message);
		message = store.getMessage(message.getHeaders().getId());
		MessageHistory messageHistory = MessageHistory.read(message);
		assertNotNull(messageHistory);
		assertEquals(2, messageHistory.size());
		Properties fooChannelHistory = messageHistory.get(0);
		assertEquals("fooChannel", fooChannelHistory.get("name"));
		assertEquals("channel", fooChannelHistory.get("type"));

	}

	@Test
	public void testAddAndRemoveMessagesFromMessageGroup() throws Exception {
		String groupId = "X";
		List<Message<?>> messages = new ArrayList<>();
		for (int i = 0; i < 25; i++) {
			Message<String> message = MessageBuilder.withPayload("foo").setCorrelationId(groupId).build();
			store.addMessagesToGroup(groupId, message);
			messages.add(message);
		}
		MessageGroup group = store.getMessageGroup(groupId);
		assertEquals(25, group.size());
		store.removeMessagesFromGroup(groupId, messages);
		group = store.getMessageGroup(groupId);
		assertEquals(0, group.size());
	}

	@Test
	public void addAndGetMessage() throws Exception {

		Message<?> message = MessageBuilder.withPayload("test").build();
		store.addMessage(message);
		Message<?> retrieved = store.getMessage(message.getHeaders().getId());
		assertEquals(message, retrieved);
	}

	@Test
	public void customMap() throws Exception {
		assertSame(map, TestUtils.getPropertyValue(store, "map"));
		HazelcastMessageStore store2 = new HazelcastMessageStore(instance);
		assertNotSame(map, TestUtils.getPropertyValue(store2, "map"));
	}

	@Test
	public void messageStoreSize() throws Exception {
		Message<?> message1 = MessageBuilder.withPayload("test").build();
		Message<?> message2 = MessageBuilder.withPayload("test").build();
		store.addMessage(message1);
		store.addMessage(message2);
		long size = store.getMessageCount();
		assertEquals(2, size);
	}

}
