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
package org.springframework.integration.hazelcast.message;

import org.springframework.util.Assert;

/**
 * Hazelcast Message Payload for Entry Events
 *
 * @author Eren Avsarogullari
 * @since 1.0.0
 */
public class EntryEventMessagePayload<K, V> {

	public final K key;

	public final V value;

	public final V oldValue;

	public EntryEventMessagePayload(final K key, final V value, final V oldValue) {
		Assert.notNull(key, "key must not be null");
		this.key = key;
		this.value = value;
		this.oldValue = oldValue;
	}

	@Override
	public String toString() {
		return "EntryEventMessagePayload [key=" + key + ", value=" + value
				+ ", oldValue=" + oldValue + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((oldValue == null) ? 0 : oldValue.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EntryEventMessagePayload other = (EntryEventMessagePayload) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		}
		else if (!key.equals(other.key))
			return false;
		if (oldValue == null) {
			if (other.oldValue != null)
				return false;
		}
		else if (!oldValue.equals(other.oldValue))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		}
		else if (!value.equals(other.value))
			return false;
		return true;
	}

}
