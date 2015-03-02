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

import org.springframework.integration.hazelcast.listener.HazelcastEntryListener;

import com.hazelcast.core.IMap;
import com.hazelcast.core.ReplicatedMap;
import com.hazelcast.query.SqlPredicate;

/**
 * HazelcastContinuousQueryMessageProducer
 * 
 * @author Eren Avsarogullari
 * @since 1.0.0
 *
 */
public class HazelcastContinuousQueryMessageProducer extends HazelcastMessageProducer {

	private String predicate;
	
	@Override
	protected void doStart() {
		if(getDistributedObject() instanceof IMap) {
			((IMap<?, ?>)getDistributedObject()).addEntryListener(new HazelcastEntryListener(this), new SqlPredicate(predicate), true);
		} else if(getDistributedObject() instanceof ReplicatedMap) {
			((ReplicatedMap<?, ?>)getDistributedObject()).addEntryListener(new HazelcastEntryListener(this), new SqlPredicate(predicate));
		}
	}

	public void setPredicate(String predicate) {
		this.predicate = predicate;
	}

}
