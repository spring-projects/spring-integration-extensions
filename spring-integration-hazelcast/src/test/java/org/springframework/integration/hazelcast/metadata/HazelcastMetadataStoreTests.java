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

package org.springframework.integration.hazelcast.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.springframework.integration.metadata.MetadataStoreListener;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

/**
 * @author Vinicius Carvalho
 */
public class HazelcastMetadataStoreTests {

	private static HazelcastInstance instance;

	private static IMap<String, String> map;

	HazelcastMetadataStore metadataStore;

	@BeforeClass
	public static void init() {
		instance = Hazelcast.newHazelcastInstance();
		map = instance.getMap("customTestsMetadataStore");
	}

	@AfterClass
	public static void destroy() {
		instance.getLifecycleService().terminate();
	}

	@Before
	public void setup() throws Exception {
		this.metadataStore = new HazelcastMetadataStore(map);
		this.metadataStore.afterPropertiesSet();
	}

	@After
	public void clean() {
		map.clear();
	}

	@Test
	public void testGetNonExistingKeyValue() {
		String retrievedValue = this.metadataStore.get("does-not-exist");
		assertNull(retrievedValue);
	}

	@Test
	public void testPersistKeyValue() {
		this.metadataStore.put("HazelcastMetadataStoreTests-Spring", "Integration");
		assertEquals("Integration", map.get("HazelcastMetadataStoreTests-Spring"));
	}

	@Test
	public void testGetValueFromMetadataStore() {
		this.metadataStore.put("HazelcastMetadataStoreTests-GetValue", "Hello Hazelcast");
		String retrievedValue = this.metadataStore
				.get("HazelcastMetadataStoreTests-GetValue");
		assertEquals("Hello Hazelcast", retrievedValue);
	}

	@Test
	public void testPersistEmptyStringToMetadataStore() {
		this.metadataStore.put("HazelcastMetadataStoreTests-PersistEmpty", "");

		String retrievedValue = this.metadataStore
				.get("HazelcastMetadataStoreTests-PersistEmpty");
		assertEquals("", retrievedValue);
	}

	@Test
	public void testPersistNullStringToMetadataStore() {
		try {
			this.metadataStore.put("HazelcastMetadataStoreTests-PersistEmpty", null);
			fail("Expected an IllegalArgumentException to be thrown.");
		}
		catch (IllegalArgumentException e) {
			assertEquals("'value' must not be null.", e.getMessage());
		}
	}

	@Test
	public void testPersistWithEmptyKeyToMetadataStore() {
		this.metadataStore.put("", "PersistWithEmptyKey");

		String retrievedValue = this.metadataStore.get("");
		assertEquals("PersistWithEmptyKey", retrievedValue);
	}

	@Test
	public void testPersistWithNullKeyToMetadataStore() {
		try {
			this.metadataStore.put(null, "something");
			fail("Expected an IllegalArgumentException to be thrown.");

		}
		catch (IllegalArgumentException e) {
			assertEquals("'key' must not be null.", e.getMessage());
		}
	}

	@Test
	public void testGetValueWithNullKeyFromMetadataStore() {
		try {
			this.metadataStore.get(null);
		}
		catch (IllegalArgumentException e) {
			assertEquals("'key' must not be null.", e.getMessage());
			return;
		}

		fail("Expected an IllegalArgumentException to be thrown.");
	}

	@Test
	public void testRemoveFromMetadataStore() {
		String testKey = "HazelcastMetadataStoreTests-Remove";
		String testValue = "Integration";

		this.metadataStore.put(testKey, testValue);

		assertEquals(testValue, this.metadataStore.remove(testKey));
		assertNull(this.metadataStore.remove(testKey));
	}

	@Test
	public void testPersistKeyValueIfAbsent() {
		this.metadataStore.putIfAbsent("HazelcastMetadataStoreTests-Spring",
				"Integration");
		assertEquals("Integration", map.get("HazelcastMetadataStoreTests-Spring"));
	}

	@Test
	public void testReplaceValue() {
		this.metadataStore.put("key", "old");
		assertEquals("old", map.get("key"));
		this.metadataStore.replace("key", "old", "new");
		assertEquals("new", map.get("key"));
	}

	@Test
	public void testListener() {
		MetadataStoreListener listener = mock(MetadataStoreListener.class);
		this.metadataStore.addListener(listener);

		this.metadataStore.put("foo", "bar");
		this.metadataStore.replace("foo", "bar", "baz");
		this.metadataStore.remove("foo");
		verify(listener).onAdd("foo", "bar");
		verify(listener).onUpdate("foo", "baz");
		verify(listener).onRemove("foo", "baz");
	}

}
