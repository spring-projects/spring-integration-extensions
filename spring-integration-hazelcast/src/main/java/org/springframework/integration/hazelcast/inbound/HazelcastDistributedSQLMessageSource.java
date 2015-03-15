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

import org.springframework.integration.endpoint.AbstractMessageSource;
import org.springframework.integration.hazelcast.common.DistributedSQLIterationType;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.Assert;

/**
 * Hazelcast Distributed SQL Message Source is a message source which runs defined
 * distributed query in the cluster and returns results in the light of iteration type.
 *
 * @author Eren Avsarogullari
 * @since 1.0.0
 *
 */
public class HazelcastDistributedSQLMessageSource<T> extends AbstractMessageSource<T> {

	private final IMap distributedMap;

	private final String distributedSQL;

	private DistributedSQLIterationType iterationType;

	public HazelcastDistributedSQLMessageSource(IMap distributedMap, String distributedSQL) {
		Assert.notNull(distributedMap, "cache must not be null");
		Assert.notNull(distributedSQL, "distributed-sql must not be null");
		this.distributedMap = distributedMap;
		this.distributedSQL = distributedSQL;
	}

	@Override
	public String getComponentType() {
		return "ds-inbound-channel-adapter";
	}

	@Override
	protected Message<Collection<T>> doReceive() {
		if (DistributedSQLIterationType.ENTRY == this.iterationType) {
			Set<T> entrySet = this.distributedMap.entrySet(new SqlPredicate(this.distributedSQL));
			return MessageBuilder.withPayload(Collections.unmodifiableCollection(entrySet)).build();
		}
		else if (DistributedSQLIterationType.KEY == this.iterationType) {
			Set<T> keySet = this.distributedMap.keySet(new SqlPredicate(this.distributedSQL));
			return MessageBuilder.withPayload(Collections.unmodifiableCollection(keySet)).build();
		}
		else if (DistributedSQLIterationType.LOCAL_KEY == this.iterationType) {
			Set<T> localKeySet = this.distributedMap.localKeySet(new SqlPredicate(this.distributedSQL));
			return MessageBuilder.withPayload(Collections.unmodifiableCollection(localKeySet)).build();
		}
		else {
			Collection<T> values = this.distributedMap.values(new SqlPredicate(this.distributedSQL));
			return MessageBuilder.withPayload(values).build();
		}
	}

	public void setIterationType(DistributedSQLIterationType iterationType) {
		this.iterationType = iterationType;
	}

}
