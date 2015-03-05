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

package org.springframework.integration.hazelcast.common;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.IList;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;
import com.hazelcast.core.ISet;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.MultiMap;
import com.hazelcast.core.ReplicatedMap;

import reactor.util.CollectionUtils;
import reactor.util.StringUtils;

/**
 * Common Validator for Hazelcast Integration. It validates cache types and events.
 *
 * @author Eren Avsarogullari
 * @since 1.0.0
 */
public class HazelcastIntegrationDefinitionValidator {

	public static <E extends Enum<E>> void validateEnumType(final Class<E> enumType, final String cacheEventTypes) {
		Set<String> eventTypeSet = StringUtils.commaDelimitedListToSet(cacheEventTypes);
		for (String eventType : eventTypeSet) {
			Enum.valueOf(enumType, eventType);
		}
	}

	public static void validateCacheTypeForEventDrivenMessageProducer(final DistributedObject distributedObject) {
		if (!(distributedObject instanceof IMap
				|| distributedObject instanceof MultiMap
				|| distributedObject instanceof ReplicatedMap
				|| distributedObject instanceof IList
				|| distributedObject instanceof ISet
				|| distributedObject instanceof IQueue
				|| distributedObject instanceof ITopic)) {
			throw new IllegalArgumentException(
					"Invalid 'cache' type is set. IMap, MultiMap, ReplicatedMap, IList, ISet, IQueue and ITopic" +
							" cache object types are acceptable for Hazelcast Inbound Channel Adapter.");
		}
	}

	public static void validateCacheTypeForCacheWritingMessageHandler(final DistributedObject distributedObject) {
		if (!(distributedObject instanceof IMap
				|| distributedObject instanceof IList
				|| distributedObject instanceof ISet
				|| distributedObject instanceof IQueue)) {
			throw new IllegalArgumentException(
					"Invalid 'cache' type is set. IMap, IList, ISet and IQueue cache object types are acceptable "
							+ "for Hazelcast Outbound Channel Adapter.");
		}
	}

	public static void validateCacheEventsByDistributedObject(
			final DistributedObject distributedObject, final Set<String> cacheEventTypeSet) {
		List<String> supportedCacheEventTypes = getSupportedCacheEventTypes(distributedObject);
		if (!CollectionUtils.isEmpty(supportedCacheEventTypes)) {
			validateCacheEventsByDistributedObject(distributedObject, cacheEventTypeSet, supportedCacheEventTypes);
		}
	}

	private static List<String> getSupportedCacheEventTypes(final DistributedObject distributedObject) {
		if ((distributedObject instanceof IList)
				|| (distributedObject instanceof ISet)
				|| (distributedObject instanceof IQueue)) {
			return Arrays.asList(CacheEventType.ADDED.toString(), CacheEventType.REMOVED.toString());
		}
		else if (distributedObject instanceof MultiMap) {
			return Arrays.asList(CacheEventType.ADDED.toString(),
					CacheEventType.REMOVED.toString(),
					CacheEventType.CLEAR_ALL.toString());
		}
		else if (distributedObject instanceof ReplicatedMap) {
			return Arrays.asList(CacheEventType.ADDED.toString(),
					CacheEventType.REMOVED.toString(),
					CacheEventType.UPDATED.toString(),
					CacheEventType.EVICTED.toString());
		}

		return null;
	}

	private static void validateCacheEventsByDistributedObject(DistributedObject distributedObject,
			Set<String> cacheEventTypeSet, List<String> supportedCacheEventTypes) {
		if (!supportedCacheEventTypes.containsAll(cacheEventTypeSet)) {
			throw new IllegalArgumentException("'cache-events' attribute of "
					+ distributedObject.getName() + " can be set as " + supportedCacheEventTypes);
		}
	}

}
