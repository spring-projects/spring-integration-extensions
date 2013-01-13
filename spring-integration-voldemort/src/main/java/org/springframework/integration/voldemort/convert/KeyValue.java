/*
 * Copyright 2002-2012 the original author or authors.
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
package org.springframework.integration.voldemort.convert;

import java.io.Serializable;

/**
 * Intermediate representation of Voldemort key-value pair.
 *
 * @param <K> Voldemort key type.
 * @param <V> Voldemort value type.
 *
 * @author Lukasz Antoniak
 * @since 1.0
 */
public class KeyValue<K, V> implements Serializable {
	private static final long serialVersionUID = 2378842276985778338L;

	private final K key;
	private final V value;

	protected KeyValue(K key, V value) {
		this.key = key;
		this.value = value;
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) return true;
		if ( ! ( o instanceof KeyValue ) ) return false;

		KeyValue keyValue = (KeyValue) o;

		if ( key != null ? !key.equals( keyValue.key ) : keyValue.key != null ) return false;
		if ( value != null ? !value.equals( keyValue.value ) : keyValue.value != null ) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = key != null ? key.hashCode() : 0;
		result = 31 * result + ( value != null ? value.hashCode() : 0 );
		return result;
	}

	@Override
	public String toString() {
		return "KeyValue(key = " + key + ", value = " + value + ")";
	}

	/**
	 * Factory method used to instantiate key-value pair representation.
	 *
	 * @param key Key object.
	 * @param value Value object.
	 * @param <K> Key type.
	 * @param <V> Value type.
	 * @return Intermediate representation of Voldemort key-value pair.
	 */
	public static <K, V> KeyValue<K, V> make(K key, V value) {
		return new KeyValue<K, V>( key, value );
	}

	public K getKey() {
		return key;
	}

	public V getValue() {
		return value;
	}
}
