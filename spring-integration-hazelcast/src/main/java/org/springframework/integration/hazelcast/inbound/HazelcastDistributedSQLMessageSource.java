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
package org.springframework.integration.hazelcast.inbound;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.springframework.integration.core.MessageSource;
import org.springframework.integration.hazelcast.common.DistributedSQLIterationType;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import com.hazelcast.core.IMap;
import com.hazelcast.query.SqlPredicate;

/**
 * HazelcastDistributedSQLMessageSource
 * 
 * @author Eren Avsarogullari
 * @param <E>
 * @since 1.0.0
 *
 */
public class HazelcastDistributedSQLMessageSource<E> implements MessageSource<Collection<E>> {
	
	private IMap distributedMap;
	private String distributedSQL;
	private DistributedSQLIterationType iterationType;

	@Override
	public Message<Collection<E>> receive() {
		if(DistributedSQLIterationType.ENTRY == iterationType) {
			Set<E> entrySet = distributedMap.entrySet(new SqlPredicate(distributedSQL));
			return MessageBuilder.withPayload(Collections.unmodifiableCollection(entrySet)).build();
		} else if(DistributedSQLIterationType.KEY == iterationType) {
			Set<E> keySet = distributedMap.keySet(new SqlPredicate(distributedSQL));
			return MessageBuilder.withPayload(Collections.unmodifiableCollection(keySet)).build();
		} else if(DistributedSQLIterationType.LOCAL_KEY == iterationType) {
			Set<E> localKeySet = distributedMap.localKeySet(new SqlPredicate(distributedSQL));
			return MessageBuilder.withPayload(Collections.unmodifiableCollection(localKeySet)).build();
		} else {
			Collection<E> values = distributedMap.values(new SqlPredicate(distributedSQL));
			return MessageBuilder.withPayload(values).build();
		}
	}

	public void setDistributedMap(IMap distributedMap) {
		this.distributedMap = distributedMap;
	}

	public void setDistributedSQL(String distributedSQL) {
		this.distributedSQL = distributedSQL;
	}

	public void setIterationType(DistributedSQLIterationType iterationType) {
		this.iterationType = iterationType;
	}

}
