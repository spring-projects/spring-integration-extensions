/*
 * Copyright 2015 the original author or authors.
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

package org.springframework.integration.hazelcast.inbound;

import java.util.Collection;
import java.util.Collections;

import org.springframework.integration.endpoint.AbstractMessageSource;
import org.springframework.integration.hazelcast.DistributedSQLIterationType;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.hazelcast.map.IMap;
import com.hazelcast.query.impl.predicates.SqlPredicate;

/**
 * Hazelcast Distributed SQL Message Source is a message source which runs defined
 * distributed query in the cluster and returns results in the light of iteration type.
 *
 * @author Eren Avsarogullari
 * @since 1.0.0
 */
@SuppressWarnings("rawtypes")
public class HazelcastDistributedSQLMessageSource extends AbstractMessageSource {

	private final IMap<?, ?> distributedMap;

	private final String distributedSql;

	private DistributedSQLIterationType iterationType = DistributedSQLIterationType.VALUE;

	public HazelcastDistributedSQLMessageSource(IMap distributedMap, String distributedSql) {
		Assert.notNull(distributedMap, "'distributedMap' must not be null");
		Assert.hasText(distributedSql, "'distributedSql' must not be empty");
		this.distributedMap = distributedMap;
		this.distributedSql = distributedSql;
	}

	public void setIterationType(DistributedSQLIterationType iterationType) {
		Assert.notNull(this.iterationType, "'iterationType' must not be null");
		this.iterationType = iterationType;
	}

	@Override
	public String getComponentType() {
		return "hazelcast:ds-inbound-channel-adapter";
	}

	@Override
	@SuppressWarnings("unchecked")
	protected Collection<?> doReceive() {
		switch (this.iterationType) {
			case ENTRY:
				return getDistributedSQLResultSet(Collections
						.unmodifiableCollection(this.distributedMap.entrySet(new SqlPredicate(this.distributedSql))));

			case KEY:
				return getDistributedSQLResultSet(Collections
						.unmodifiableCollection(this.distributedMap.keySet(new SqlPredicate(this.distributedSql))));

			case LOCAL_KEY:
				return getDistributedSQLResultSet(Collections
						.unmodifiableCollection(this.distributedMap.localKeySet(new SqlPredicate(this.distributedSql))));

			default:
				return getDistributedSQLResultSet(this.distributedMap.values(new SqlPredicate(this.distributedSql)));
		}
	}

	private Collection<?> getDistributedSQLResultSet(Collection<?> collection) {
		if (CollectionUtils.isEmpty(collection)) {
			return null;
		}

		return collection;
	}

}
