package org.springframework.integration.hazelcast.metadata;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.mockito.Mockito.*;


import org.springframework.integration.metadata.MetadataStoreListener;

import static org.junit.Assert.*;

/**
 * @author Vinicius Carvalho
 */
public class HazelcastMetadataStoreTests {

	HazelcastMetadataStore metadataStore;

	private static HazelcastInstance instance;

	private static IMap<String, String> map;

	@BeforeClass
	public static void init(){
		instance = Hazelcast.newHazelcastInstance();
		map = instance.getMap("customTestsMetadataStore");
	}

	@AfterClass
	public static void destroy() throws Exception {
		instance.shutdown();
	}

	@Before
	public void setup() throws Exception{
		this.metadataStore = new HazelcastMetadataStore(map);
		metadataStore.afterPropertiesSet();
	}

	@After
	public void clean() {
		map.clear();
	}

	@Test
	public void testGetNonExistingKeyValue() {
		String retrievedValue = metadataStore.get("does-not-exist");
		assertNull(retrievedValue);
	}

	@Test
	public void testPersistKeyValue() {
		metadataStore.put("HazelcastMetadataStoreTests-Spring", "Integration");
		assertEquals("Integration", map.get("HazelcastMetadataStoreTests-Spring"));
	}

	@Test
	public void testGetValueFromMetadataStore() {
		metadataStore.put("HazelcastMetadataStoreTests-GetValue", "Hello Hazelcast");
		String retrievedValue = metadataStore.get("HazelcastMetadataStoreTests-GetValue");
		assertEquals("Hello Hazelcast", retrievedValue);
	}

	@Test
	public void testPersistEmptyStringToMetadataStore() {
		metadataStore.put("HazelcastMetadataStoreTests-PersistEmpty", "");

		String retrievedValue = metadataStore.get("HazelcastMetadataStoreTests-PersistEmpty");
		assertEquals("", retrievedValue);
	}

	@Test
	public void testPersistNullStringToMetadataStore() {
		try {
			metadataStore.put("HazelcastMetadataStoreTests-PersistEmpty", null);
			fail("Expected an IllegalArgumentException to be thrown.");
		}
		catch (IllegalArgumentException e) {
			assertEquals("'value' must not be null.", e.getMessage());
		}
	}

	@Test
	public void testPersistWithEmptyKeyToMetadataStore() {
		metadataStore.put("", "PersistWithEmptyKey");

		String retrievedValue = metadataStore.get("");
		assertEquals("PersistWithEmptyKey", retrievedValue);
	}

	@Test
	public void testPersistWithNullKeyToMetadataStore() {
		try {
			metadataStore.put(null, "something");
			fail("Expected an IllegalArgumentException to be thrown.");

		}
		catch (IllegalArgumentException e) {
			assertEquals("'key' must not be null.", e.getMessage());
		}
	}

	@Test
	public void testGetValueWithNullKeyFromMetadataStore() {
		try {
			metadataStore.get(null);
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

		metadataStore.put(testKey, testValue);

		assertEquals(testValue, metadataStore.remove(testKey));
		assertNull(metadataStore.remove(testKey));
	}

	@Test
	public void testPersistKeyValueIfAbsent() {
		metadataStore.putIfAbsent("HazelcastMetadataStoreTests-Spring", "Integration");
		assertEquals("Integration", map.get("HazelcastMetadataStoreTests-Spring"));
	}

	@Test
	public void testReplaceValue() {
		metadataStore.put("key","old");
		assertEquals("old",map.get("key"));
		metadataStore.replace("key","old","new");
		assertEquals("new",map.get("key"));
	}

	@Test
	public void testListener() {
		MetadataStoreListener listener = mock(MetadataStoreListener.class);
		metadataStore.addListener(listener);

		metadataStore.put("foo","bar");
		metadataStore.replace("foo","bar","baz");
		metadataStore.remove("foo");
		verify(listener).onAdd("foo","bar");
		verify(listener).onUpdate("foo","baz");
		verify(listener).onRemove("foo","baz");
	}

}
