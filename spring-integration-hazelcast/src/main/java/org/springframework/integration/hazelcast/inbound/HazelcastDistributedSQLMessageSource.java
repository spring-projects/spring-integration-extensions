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

import com.hazelcast.core.IMap;
import com.hazelcast.query.SqlPredicate;

import org.springframework.integration.core.MessageSource;
import org.springframework.integration.hazelcast.common.DistributedSQLIterationType;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

/**
 * Hazelcast Distributed SQL Message Source is a message source which runs defined
 * distributed query in the cluster and returns results in the light of iteration type.
 * 
 * @author Eren Avsarogullari
 * @since 1.0.0
 *
 */
public class HazelcastDistributedSQLMessageSource<E> implements MessageSource<Collection<E>> {

	private IMap distributedMap;

	private String distributedSQL;

	private DistributedSQLIterationType iterationType;

	@Override
	public Message<Collection<E>> receive() {
		if (DistributedSQLIterationType.ENTRY == this.iterationType) {
			Set<E> entrySet = this.distributedMap.entrySet(new SqlPredicate(this.distributedSQL));
			return MessageBuilder.withPayload(Collections.unmodifiableCollection(entrySet)).build();
		} 
		else if (DistributedSQLIterationType.KEY == this.iterationType) {
			Set<E> keySet = this.distributedMap.keySet(new SqlPredicate(this.distributedSQL));
			return MessageBuilder.withPayload(Collections.unmodifiableCollection(keySet)).build();
		} 
		else if (DistributedSQLIterationType.LOCAL_KEY == this.iterationType) {
			Set<E> localKeySet = this.distributedMap.localKeySet(new SqlPredicate(this.distributedSQL));
			return MessageBuilder.withPayload(Collections.unmodifiableCollection(localKeySet)).build();
		} 
		else {
			Collection<E> values = this.distributedMap.values(new SqlPredicate(this.distributedSQL));
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
