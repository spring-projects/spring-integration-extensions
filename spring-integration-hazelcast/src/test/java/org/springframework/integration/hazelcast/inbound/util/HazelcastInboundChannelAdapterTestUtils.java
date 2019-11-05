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

package org.springframework.integration.hazelcast.inbound.util;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.Map;

import org.junit.Assert;

import org.springframework.integration.hazelcast.HazelcastHeaders;
import org.springframework.integration.hazelcast.HazelcastIntegrationTestUser;
import org.springframework.integration.hazelcast.message.EntryEventMessagePayload;
import org.springframework.messaging.Message;
import org.springframework.messaging.PollableChannel;

import com.hazelcast.core.DistributedObjectEvent;
import com.hazelcast.core.EntryEventType;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ICollection;
import com.hazelcast.core.IMap;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Member;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MultiMap;
import com.hazelcast.core.ReplicatedMap;
import com.hazelcast.spi.exception.DistributedObjectDestroyedException;

/**
 * Util Class for Hazelcast Inbound Channel Adapters Test Support.
 *
 * @author Eren Avsarogullari
 * @since 1.0.0
 */
@SuppressWarnings("unchecked")
public final class HazelcastInboundChannelAdapterTestUtils {

	public static final int TIMEOUT = 30_000;

	public static void verifyEntryEvent(Message<?> msg, String cacheName,
			EntryEventType event) {
		Assert.assertNotNull(msg);
		Assert.assertNotNull(msg.getPayload());
		if (event == EntryEventType.CLEAR_ALL || event == EntryEventType.EVICT_ALL) {
			Assert.assertTrue(msg.getPayload() instanceof Integer);
		}
		else {
			Assert.assertTrue(msg.getPayload() instanceof EntryEventMessagePayload);
		}

		Assert.assertEquals(cacheName, msg.getHeaders().get(HazelcastHeaders.CACHE_NAME));
		Assert.assertEquals(event.name(),
				msg.getHeaders().get(HazelcastHeaders.EVENT_TYPE));
		Assert.assertNotNull(msg.getHeaders().get(HazelcastHeaders.MEMBER));
	}

	public static void verifyItemEvent(Message<?> msg, EntryEventType event) {
		Assert.assertNotNull(msg);
		Assert.assertNotNull(msg.getPayload());
		Assert.assertNotNull(msg.getHeaders().get(HazelcastHeaders.MEMBER));
		Assert.assertEquals(event.toString(),
				msg.getHeaders().get(HazelcastHeaders.EVENT_TYPE).toString());
	}

	public static void testEventDrivenForADDEDDistributedMapEntryEvent(
			final IMap<Integer, HazelcastIntegrationTestUser> distributedMap,
			final PollableChannel channel, final String cacheName) {
		HazelcastIntegrationTestUser hazelcastIntegrationTestUser =
				new HazelcastIntegrationTestUser(1, "TestName1", "TestSurname1");
		distributedMap.put(1, hazelcastIntegrationTestUser);
		Message<?> msg = channel.receive(TIMEOUT);
		Assert.assertNotNull(msg);
		Assert.assertNotNull(msg.getPayload());
		Assert.assertTrue(msg.getPayload() instanceof EntryEventMessagePayload);
		Assert.assertNotNull(msg.getHeaders().get(HazelcastHeaders.MEMBER));
		Assert.assertEquals(EntryEventType.ADDED.name(),
				msg.getHeaders().get(HazelcastHeaders.EVENT_TYPE));
		Assert.assertEquals(cacheName, msg.getHeaders().get(HazelcastHeaders.CACHE_NAME));

		Assert.assertEquals(Integer.valueOf(1),
				((EntryEventMessagePayload<Integer, HazelcastIntegrationTestUser>) msg
						.getPayload()).key);
		Assert.assertEquals(1,
				(((EntryEventMessagePayload<Integer, HazelcastIntegrationTestUser>) msg
						.getPayload()).value).getId());
		Assert.assertEquals("TestName1",
				(((EntryEventMessagePayload<Integer, HazelcastIntegrationTestUser>) msg
						.getPayload()).value).getName());
		Assert.assertEquals("TestSurname1",
				(((EntryEventMessagePayload<Integer, HazelcastIntegrationTestUser>) msg
						.getPayload()).value).getSurname());
	}

	public static void testEventDrivenForDistributedMapEntryEvents(
			final IMap<Integer, HazelcastIntegrationTestUser> distributedMap,
			final PollableChannel channel, final String cacheName) {
		distributedMap
				.put(1, new HazelcastIntegrationTestUser(1, "TestName1", "TestSurname1"));
		Message<?> msg = channel.receive(TIMEOUT);
		verifyEntryEvent(msg, cacheName, EntryEventType.ADDED);

		distributedMap.put(1,
				new HazelcastIntegrationTestUser(1, "TestName1", "TestSurnameUpdated"));
		msg = channel.receive(TIMEOUT);
		verifyEntryEvent(msg, cacheName, EntryEventType.UPDATED);

		distributedMap.remove(1);
		msg = channel.receive(TIMEOUT);
		verifyEntryEvent(msg, cacheName, EntryEventType.REMOVED);

		distributedMap
				.put(2, new HazelcastIntegrationTestUser(2, "TestName2", "TestSurname2"));
		msg = channel.receive(TIMEOUT);
		verifyEntryEvent(msg, cacheName, EntryEventType.ADDED);

		distributedMap.clear();
		msg = channel.receive(TIMEOUT);
		verifyEntryEvent(msg, cacheName, EntryEventType.CLEAR_ALL);
	}

	public static void testEventDrivenForDistributedCollectionItemEvents(
			final ICollection<HazelcastIntegrationTestUser> distributedObject,
			final PollableChannel channel) {
		HazelcastIntegrationTestUser user =
				new HazelcastIntegrationTestUser(1, "TestName1", "TestSurname1");
		distributedObject.add(user);
		Message<?> msg = channel.receive(TIMEOUT);
		verifyItemEvent(msg, EntryEventType.ADDED);

		distributedObject.remove(user);
		msg = channel.receive(TIMEOUT);
		verifyItemEvent(msg, EntryEventType.REMOVED);

		user = new HazelcastIntegrationTestUser(2, "TestName2", "TestSurname2");
		distributedObject.add(user);
		msg = channel.receive(TIMEOUT);
		verifyItemEvent(msg, EntryEventType.ADDED);
	}

	public static void testEventDrivenForReplicatedMapEntryEvents(
			final ReplicatedMap<Integer, HazelcastIntegrationTestUser> replicatedMap,
			final PollableChannel channel, final String cacheName) {
		replicatedMap
				.put(1, new HazelcastIntegrationTestUser(1, "TestName1", "TestSurname1"));
		Message<?> msg = channel.receive(TIMEOUT);
		verifyEntryEvent(msg, cacheName, EntryEventType.ADDED);

		replicatedMap.put(1,
				new HazelcastIntegrationTestUser(1, "TestName1", "TestSurnameUpdated"));
		msg = channel.receive(TIMEOUT);
		verifyEntryEvent(msg, cacheName, EntryEventType.UPDATED);

		replicatedMap.remove(1);
		msg = channel.receive(TIMEOUT);
		verifyEntryEvent(msg, cacheName, EntryEventType.REMOVED);

		replicatedMap
				.put(2, new HazelcastIntegrationTestUser(2, "TestName2", "TestSurname2"));
		msg = channel.receive(TIMEOUT);
		verifyEntryEvent(msg, cacheName, EntryEventType.ADDED);
	}

	public static void testEventDrivenForTopicMessageEvent(
			final ITopic<HazelcastIntegrationTestUser> topic, final PollableChannel channel) {
		topic.publish(new HazelcastIntegrationTestUser(1, "TestName1", "TestSurname1"));
		Message<?> msg = channel.receive(TIMEOUT);
		Assert.assertNotNull(msg);
		Assert.assertNotNull(msg.getPayload());
		Assert.assertNotNull(msg.getHeaders().get(HazelcastHeaders.MEMBER));
		Assert.assertNotNull(msg.getHeaders().get(HazelcastHeaders.PUBLISHING_TIME));
		Assert.assertEquals(topic.getName(),
				msg.getHeaders().get(HazelcastHeaders.CACHE_NAME));
		Assert.assertEquals(1, ((HazelcastIntegrationTestUser) msg.getPayload()).getId());
		Assert.assertEquals("TestName1",
				((HazelcastIntegrationTestUser) msg.getPayload()).getName());
		Assert.assertEquals("TestSurname1",
				((HazelcastIntegrationTestUser) msg.getPayload()).getSurname());
	}

	public static void testEventDrivenForMultiMapEntryEvents(
			final MultiMap<Integer, HazelcastIntegrationTestUser> multiMap,
			final PollableChannel channel, final String cacheName) {
		multiMap.put(1, new HazelcastIntegrationTestUser(1, "TestName1", "TestSurname1"));
		Message<?> msg = channel.receive(TIMEOUT);
		verifyEntryEvent(msg, cacheName, EntryEventType.ADDED);

		multiMap.put(1,
				new HazelcastIntegrationTestUser(1, "TestName1", "TestSurnameUpdated"));
		msg = channel.receive(TIMEOUT);
		verifyEntryEvent(msg, cacheName, EntryEventType.ADDED);

		multiMap.remove(1);
		msg = channel.receive(TIMEOUT);
		verifyEntryEvent(msg, cacheName, EntryEventType.REMOVED);
		msg = channel.receive(TIMEOUT);
		verifyEntryEvent(msg, cacheName, EntryEventType.REMOVED);

		multiMap.put(2, new HazelcastIntegrationTestUser(2, "TestName2", "TestSurname2"));
		msg = channel.receive(TIMEOUT);
		verifyEntryEvent(msg, cacheName, EntryEventType.ADDED);

		multiMap.clear();
		msg = channel.receive(TIMEOUT);
		verifyEntryEvent(msg, cacheName, EntryEventType.CLEAR_ALL);
	}

	public static void testContinuousQueryForUPDATEDEntryEventWhenIncludeValueIsFalse(
			final IMap<Integer, HazelcastIntegrationTestUser> cqDistributedMap,
			final PollableChannel channel, final String cacheName) {
		cqDistributedMap
				.put(1, new HazelcastIntegrationTestUser(1, "TestName1", "TestSurname1"));
		cqDistributedMap
				.put(1, new HazelcastIntegrationTestUser(2, "TestName2", "TestSurname2"));
		Message<?> msg = channel.receive(TIMEOUT);
		assertNotNull(msg);
		assertNotNull(msg.getPayload());
		assertTrue(msg.getPayload() instanceof EntryEventMessagePayload);
		assertNotNull(msg.getHeaders().get(HazelcastHeaders.MEMBER));
		assertEquals(EntryEventType.UPDATED.name(),
				msg.getHeaders().get(HazelcastHeaders.EVENT_TYPE));
		assertEquals(cacheName, msg.getHeaders().get(HazelcastHeaders.CACHE_NAME));

		assertEquals(Integer.valueOf(1),
				((EntryEventMessagePayload<Integer, HazelcastIntegrationTestUser>) msg
						.getPayload()).key);
		assertNull(((EntryEventMessagePayload<Integer, HazelcastIntegrationTestUser>) msg
				.getPayload()).oldValue);
		assertNull(((EntryEventMessagePayload<Integer, HazelcastIntegrationTestUser>) msg
				.getPayload()).value);
	}

	public static void testDistributedSQLForENTRYIterationType(
			final IMap<Integer, HazelcastIntegrationTestUser> dsDistributedMap,
			final PollableChannel channel) {
		dsDistributedMap
				.put(1, new HazelcastIntegrationTestUser(1, "TestName1", "TestSurname1", 10));
		dsDistributedMap
				.put(2, new HazelcastIntegrationTestUser(2, "TestName2", "TestSurname2", 20));
		dsDistributedMap
				.put(3, new HazelcastIntegrationTestUser(3, "TestName3", "TestSurname3", 30));
		dsDistributedMap
				.put(4, new HazelcastIntegrationTestUser(4, "TestName4", "TestSurname4", 40));
		dsDistributedMap
				.put(5, new HazelcastIntegrationTestUser(5, "TestName5", "TestSurname5", 50));

		Message<?> msg = channel.receive(TIMEOUT);

		Assert.assertNotNull(msg);
		Assert.assertNotNull(msg.getPayload());
		Assert.assertTrue(msg.getPayload() instanceof Collection);
		Assert.assertEquals(4,
				(((Map.Entry<?, ?>) ((Collection<?>) msg.getPayload()).iterator().next())
						.getKey()));
		Assert.assertEquals(4,
				((HazelcastIntegrationTestUser) ((Map.Entry<?, ?>) ((Collection<?>) msg
						.getPayload()).iterator().next()).getValue()).getId());
		Assert.assertEquals("TestName4",
				((HazelcastIntegrationTestUser) ((Map.Entry<?, ?>) ((Collection<?>) msg
						.getPayload()).iterator().next()).getValue()).getName());
		Assert.assertEquals("TestSurname4",
				((HazelcastIntegrationTestUser) ((Map.Entry<?, ?>) ((Collection<?>) msg
						.getPayload()).iterator().next()).getValue()).getSurname());
	}

	public static void testDistributedSQLForKEYIterationType(
			final IMap<Integer, HazelcastIntegrationTestUser> dsDistributedMap,
			final PollableChannel channel) {
		dsDistributedMap
				.put(1, new HazelcastIntegrationTestUser(1, "TestName1", "TestSurname1", 10));
		dsDistributedMap
				.put(2, new HazelcastIntegrationTestUser(2, "TestName2", "TestSurname2", 20));
		dsDistributedMap
				.put(3, new HazelcastIntegrationTestUser(3, "TestName3", "TestSurname3", 30));

		Message<?> msg = channel.receive(TIMEOUT);

		Assert.assertNotNull(msg);
		Assert.assertNotNull(msg.getPayload());
		Assert.assertTrue(msg.getPayload() instanceof Collection);
		Assert.assertEquals(1, ((Collection<?>) msg.getPayload()).iterator().next());
	}

	public static void testDistributedSQLForLOCAL_KEYIterationType(
			final IMap<Integer, HazelcastIntegrationTestUser> dsDistributedMap,
			final PollableChannel channel) {
		dsDistributedMap
				.put(1, new HazelcastIntegrationTestUser(1, "TestName1", "TestSurname1", 10));
		dsDistributedMap
				.put(2, new HazelcastIntegrationTestUser(2, "TestName2", "TestSurname2", 20));
		dsDistributedMap
				.put(3, new HazelcastIntegrationTestUser(3, "TestName3", "TestSurname3", 30));

		Message<?> msg = channel.receive(TIMEOUT);

		Assert.assertNotNull(msg);
		Assert.assertNotNull(msg.getPayload());
		Assert.assertTrue(msg.getPayload() instanceof Collection);
	}

	public static void testDistributedSQLForVALUEIterationType(
			final IMap<Integer, HazelcastIntegrationTestUser> dsDistributedMap,
			final PollableChannel channel) {
		dsDistributedMap
				.put(1, new HazelcastIntegrationTestUser(1, "TestName1", "TestSurname1", 10));
		dsDistributedMap
				.put(2, new HazelcastIntegrationTestUser(2, "TestName2", "TestSurname2", 20));
		dsDistributedMap
				.put(3, new HazelcastIntegrationTestUser(3, "TestName3", "TestSurname3", 30));
		dsDistributedMap
				.put(4, new HazelcastIntegrationTestUser(4, "TestName4", "TestSurname4", 40));
		dsDistributedMap
				.put(5, new HazelcastIntegrationTestUser(5, "TestName5", "TestSurname5", 50));

		Message<?> msg = channel.receive(TIMEOUT);

		Assert.assertNotNull(msg);
		Assert.assertNotNull(msg.getPayload());
		Assert.assertTrue(msg.getPayload() instanceof Collection);
		Assert.assertEquals(3,
				((HazelcastIntegrationTestUser) (((Collection<?>) msg.getPayload()).iterator()
						.next())).getId());
		Assert.assertEquals("TestName3",
				((HazelcastIntegrationTestUser) (((Collection<?>) msg.getPayload()).iterator()
						.next())).getName());
		Assert.assertEquals("TestSurname3",
				((HazelcastIntegrationTestUser) (((Collection<?>) msg.getPayload()).iterator()
						.next())).getSurname());
	}

	public static void testMembershipEvent(final HazelcastInstance instance,
			final PollableChannel channel, final String key, final String value) {
		Member member = instance.getCluster().getLocalMember();
		member.setStringAttribute(key, value);

		Message<?> msg = channel.receive(TIMEOUT);
		verifyMembershipEvent(msg, MembershipEvent.MEMBER_ATTRIBUTE_CHANGED);
	}

	public static void testDistributedObjectEventByChannelAndHazelcastInstance(
			final PollableChannel channel, final HazelcastInstance hazelcastInstance,
			final String distributedObjectName) {
		final IMap<Integer, String> distributedMap =
				hazelcastInstance.getMap(distributedObjectName);

		Message<?> msg = channel.receive(TIMEOUT);
		verifyDistributedObjectEvent(msg, DistributedObjectEvent.EventType.CREATED,
				distributedObjectName);

		distributedMap.destroy();

		msg = channel.receive(TIMEOUT);
		try {
			// Since Hazelcast 3.6 we can use DistributedObjectEvent.getDistributedObject() for DESTROYED objects.
			verifyDistributedObjectEvent(msg, DistributedObjectEvent.EventType.DESTROYED, distributedObjectName);
			fail("DistributedObjectDestroyedException expected");
		}
		catch (Exception e) {
			assertThat(e, instanceOf(DistributedObjectDestroyedException.class));
		}
	}

	private static void verifyMembershipEvent(final Message<?> msg,
			final int membershipEvent) {
		assertNotNull(msg);
		assertNotNull(msg.getPayload());
		assertTrue(msg.getPayload() instanceof MembershipEvent);
		assertEquals(membershipEvent,
				((MembershipEvent) msg.getPayload()).getEventType());
		assertNotNull(((MembershipEvent) msg.getPayload()).getMember());
	}

	private static void verifyDistributedObjectEvent(final Message<?> msg,
			final DistributedObjectEvent.EventType eventType,
			final String distributedObjectName) {
		assertNotNull(msg);
		assertNotNull(msg.getPayload());
		assertTrue(msg.getPayload() instanceof DistributedObjectEvent);
		assertEquals(eventType,
				((DistributedObjectEvent) msg.getPayload()).getEventType());
		assertNotNull((((DistributedObjectEvent) msg.getPayload()).getDistributedObject())
				.getName(), distributedObjectName);
	}

	private HazelcastInboundChannelAdapterTestUtils() {
	}

}
