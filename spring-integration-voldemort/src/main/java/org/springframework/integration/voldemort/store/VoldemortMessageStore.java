/*
 * Copyright 2002-2013 the original author or authors.
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
package org.springframework.integration.voldemort.store;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.integration.store.AbstractKeyValueMessageStore;
import org.springframework.integration.store.MessageGroupStore;
import org.springframework.integration.store.MessageStore;
import org.springframework.integration.util.DefaultLockRegistry;
import org.springframework.integration.util.LockRegistry;
import org.springframework.integration.util.UUIDConverter;
import voldemort.client.StoreClient;
import voldemort.client.UpdateAction;
import voldemort.serialization.SerializationException;
import voldemort.versioning.Versioned;

/**
 * Voldemort implementation of the key-value style {@link MessageStore} and {@link MessageGroupStore}.
 * Implementation note: message identifiers are persisted as {@link String}s.
 *
 * @author Lukasz Antoniak
 * @since 1.0
 */
@SuppressWarnings("unchecked")
public class VoldemortMessageStore extends AbstractKeyValueMessageStore implements InitializingBean {
	/**
	 * Key under which message store tracks all currently saved message identifiers.
	 */
	protected static final String MESSAGE_KEY_LIST = "MESSAGE_KEY_LIST";

	/**
	 * Key under which message store tracks all currently saved message group identifiers.
	 */
	protected static final String MESSAGE_GROUP_KEY_LIST = "MESSAGE_GROUP_KEY_LIST";

	private static final LockRegistry LOCK_REGISTRY = new DefaultLockRegistry();

	private final StoreClient client;

	public VoldemortMessageStore(StoreClient client) {
		this.client = client;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// Initialize empty set of message and message group identifiers if needed.
		synchronized ( getClass() ) {
			if ( client.get( MESSAGE_KEY_LIST ) == null ) {
				client.put( MESSAGE_KEY_LIST, new HashSet<Object>() );
			}
			if ( client.get( MESSAGE_GROUP_KEY_LIST ) == null ) {
				client.put( MESSAGE_GROUP_KEY_LIST, new HashSet<Object>() );
			}
		}
	}

	@Override
	protected Object doRetrieve(Object id) {
		final Versioned version = client.get( id );
		if ( version != null ) {
			return version.getValue();
		}
		return null;
	}

	@Override
	protected void doStore(final Object id, final Object objectToStore) {
		final Lock messageLock = doLock( id );
		try {
			client.put( id, objectToStore );
			// Keeping track of stored message identifiers for efficient implementation of #doListKeys(String) method.
			// Voldemort does not provide API to list all keys matching specified pattern.
			final String keyListKey = computeKeyListKey( id );
			final Lock keyListLock = doLock( keyListKey );
			try {
				client.applyUpdate( new AddKeyUpdateAction( id, keyListKey ) );
			}
			finally {
				keyListLock.unlock();
			}
		}
		catch ( SerializationException e ) {
			throw new IllegalArgumentException( "Voldemort failed to serialize message with id: " + id + ".", e );
		}
		finally {
			messageLock.unlock();
		}
	}

	@Override
	protected Object doRemove(final Object id) {
		final Object message = doRetrieve( id );
		final Lock messageLock = doLock( id );
		try {
			client.delete( id );
			// Keeping track of stored message identifiers for efficient implementation of #doListKeys(String) method.
			// Voldemort does not provide API to list all keys matching specified pattern.
			final String keyListKey = computeKeyListKey( id );
			final Lock keyListLock = doLock( keyListKey );
			try {
				client.applyUpdate( new RemoveKeyUpdateAction( id, keyListKey ) );
			}
			finally {
				keyListLock.unlock();
			}
			return message;
		}
		finally {
			messageLock.unlock();
		}
	}

	@Override
	protected Collection<?> doListKeys(String keyPattern) {
		return Collections.unmodifiableSet( (Set<Object>) client.get( computeKeyListKey( keyPattern ) ).getValue() );
	}

	/**
	 * @param id Message or message group identifier. {@link String} type required.
	 * @return Identifier under which set of all currently persisted message or message group keys is saved.
	 */
	private String computeKeyListKey(Object id) {
		final String key = (String) id;
		if ( isMessageGroupKey( key ) ) {
			return MESSAGE_GROUP_KEY_LIST;
		}
		else if ( isMessageKey( key ) ) {
			return MESSAGE_KEY_LIST;
		}
		else {
			throw new IllegalArgumentException("Unsupported identifier: " + key + ".");
		}
	}

	/**
	 * @param key Message or message group identifier.
	 * @return {@code true} in case of message identifier, {@code false} otherwise.
	 */
	private boolean isMessageKey(String key) {
		return key.startsWith( AbstractKeyValueMessageStore.MESSAGE_KEY_PREFIX );
	}

	/**
	 * @param key Message or message group identifier.
	 * @return {@code true} in case of message group identifier, {@code false} otherwise.
	 */
	private boolean isMessageGroupKey(String key) {
		return key.startsWith( AbstractKeyValueMessageStore.MESSAGE_GROUP_KEY_PREFIX );
	}

	/**
	 * Acquire JVM wide lock on the given object.
	 * @param obj Object.
	 * @return Lock.
	 */
	private Lock doLock(Object obj) {
		final Lock lock = LOCK_REGISTRY.obtain( UUIDConverter.getUUID( obj ).toString() );
		lock.lock();
		return lock;
	}

	/**
	 * Voldemort update action that adds given key to the list of currently saved identifiers.
	 */
	private static final class AddKeyUpdateAction extends UpdateAction {
		private final Object id;
		private final String keyListKey;

		/**
		 * The only constructor.
		 *
		 * @param id Message or message group identifier.
		 * @param keyListKey Key under which Voldemort stores set of all currently persisted identifiers.
		 */
		private AddKeyUpdateAction(Object id, String keyListKey) {
			this.id = id;
			this.keyListKey = keyListKey;
		}

		@Override
		public void update(StoreClient storeClient) {
			final Set<Object> keys = (Set<Object>) storeClient.get( keyListKey ).getValue();
			if ( keys.add( id ) ) {
				storeClient.put( keyListKey, keys );
			}
		}
	}

	/**
	 * Voldemort update action that removes given key from the list of currently saved identifiers.
	 */
	private static final class RemoveKeyUpdateAction extends UpdateAction {
		private final Object id;
		private final String keyListKey;

		/**
		 * The only constructor.
		 *
		 * @param id Message or message group identifier.
		 * @param keyListKey Key under which Voldemort stores set of all currently persisted identifiers.
		 */
		private RemoveKeyUpdateAction(Object id, String keyListKey) {
			this.id = id;
			this.keyListKey = keyListKey;
		}

		@Override
		public void update(StoreClient storeClient) {
			final Set<Object> keys = (Set<Object>) storeClient.get( keyListKey ).getValue();
			if ( keys.remove( id ) ) {
				storeClient.put( keyListKey, keys );
			}
		}
	}
}
