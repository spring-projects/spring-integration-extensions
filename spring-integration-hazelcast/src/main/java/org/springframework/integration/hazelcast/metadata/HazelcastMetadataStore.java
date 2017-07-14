/*
 * Copyright 2017 the original author or authors.
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


package org.springframework.integration.hazelcast.metadata;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import org.springframework.integration.metadata.ConcurrentMetadataStore;
import org.springframework.util.Assert;

/**
 * @author Vinicius Carvalho
 */
public class HazelcastMetadataStore implements ConcurrentMetadataStore{

	private static final String METADATA_STORE_MAP_NAME = "SPRING_INTEGRATION_METADATA_STORE";

	private final IMap<String, String> map;

	public HazelcastMetadataStore(HazelcastInstance hazelcastInstance){
		Assert.notNull(hazelcastInstance, "Hazelcast instance can't be null");
		this.map = hazelcastInstance.getMap(METADATA_STORE_MAP_NAME);
	}

	public HazelcastMetadataStore(IMap<String, String> map) {
		Assert.notNull(map, "IMap reference can not be null");
		this.map = map;
	}

	@Override
	public String putIfAbsent(String key, String value) {
		Assert.notNull(key, "'key' must not be null.");
		Assert.notNull(value, "'value' must not be null.");
		return this.map.putIfAbsent(key,value);
	}

	@Override
	public boolean replace(String key, String oldValue, String newValue) {
		Assert.notNull(key, "'key' must not be null.");
		Assert.notNull(oldValue, "'oldValue' must not be null.");
		Assert.notNull(newValue, "'newValue' must not be null.");
		return this.map.replace(key,oldValue,newValue);
	}

	@Override
	public void put(String key, String value) {
		Assert.notNull(key, "'key' must not be null.");
		Assert.notNull(value, "'value' must not be null.");
		this.map.put(key,value);
	}

	@Override
	public String get(String key) {
		Assert.notNull(key, "'key' must not be null.");
		return this.map.get(key);
	}

	@Override
	public String remove(String key) {
		Assert.notNull(key, "'key' must not be null.");
		return this.map.remove(key);
	}

}
