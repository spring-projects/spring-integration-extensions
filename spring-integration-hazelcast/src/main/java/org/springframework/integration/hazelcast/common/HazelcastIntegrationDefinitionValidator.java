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

import java.util.Set;

import reactor.util.StringUtils;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.IList;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;
import com.hazelcast.core.ISet;
import com.hazelcast.core.ITopic;

/**
 * HazelcastIntegrationDefinitionValidator
 * 
 * @author Eren Avsarogullari
 * @since 1.0.0
 *
 */
public class HazelcastIntegrationDefinitionValidator {

	public static <E extends Enum<E>> boolean validateEnumType(Class<E> enumType, String cacheEventTypes) {
		Set<String> eventTypeSet = StringUtils.commaDelimitedListToSet(cacheEventTypes);
		for(String eventType : eventTypeSet) {
			Enum.valueOf(enumType, eventType);
		}
		
		return true;
	}
	
	public static boolean validateCacheType(DistributedObject distributedObject) {
		if(distributedObject instanceof IMap
				|| distributedObject instanceof IList
				|| distributedObject instanceof ISet
				|| distributedObject instanceof IQueue) {
			return true;
		}
		
		return false;
	}
	
	public static boolean validateCacheEventByDistributedObject(DistributedObject distributedObject, Set<String> cacheEventTypeSet) {
		if((distributedObject instanceof IList) 
				|| (distributedObject instanceof ISet) 
				|| (distributedObject instanceof IQueue)) {
			
			for(String cacheEventType : cacheEventTypeSet) {
				if(!(CacheEventType.ADDED.toString().equals(cacheEventType) || CacheEventType.REMOVED.toString().equals(cacheEventType))) {
					throw new IllegalStateException("'cache-events' attribute of IList, ISet or IQueue can be set as only " + CacheEventType.ADDED.toString() + " and / or " + CacheEventType.REMOVED.toString());
				}
			}
		
		} else if((distributedObject instanceof ITopic) && !cacheEventTypeSet.isEmpty()) {
			throw new IllegalStateException("'cache-events' attribute is not required for ITopic.");
		} 
		
		return true;
	}

}
