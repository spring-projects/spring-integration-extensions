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

import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;
import voldemort.versioning.Versioned;

/**
 * Converter allowing to transform Spring Integration message to Voldemort record with desired key and value
 * representation.
 *
 * @param <K> Voldemort key type.
 * @param <V> Voldemort value type.
 * @param <P> Spring Integration payload type.
 *
 * @author Lukasz Antoniak
 * @since 1.0
 */
public interface VoldemortConverter<K, V, P> {
	/**
	 * Transform Spring Integration message to key-value pair.
	 * <p />
	 * Implementation note: Typically use {@link KeyValue#make(Object, Object)} method to create key-value pair.
	 *
	 * @param message Spring Integration message.
	 * @return Key-value pair to persist in Voldemort store.
	 */
	public KeyValue<K, V> toKeyValue(Message<P> message);

	/**
	 * Transform key-value pair to Spring Integration message (payload and headers).
	 * <p />
	 * Implementation note: Typically use {@link MessageBuilder} pattern for creating messages.
	 *
	 * @param key Object key.
	 * @param versioned Voldemort version object (includes value reference).
	 * @return Spring Integration message.
	 */
	public Message<P> toMessage(K key, Versioned<V> versioned);
}
