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

import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.IMap;
import com.hazelcast.query.SqlPredicate;

import org.springframework.integration.hazelcast.common.HazelcastIntegrationDefinitionValidator;
import org.springframework.util.Assert;

/**
 * Hazelcast Continuous Query Message Producer is a message producer which enables
 * {@link HazelcastEntryListener} with a {@link SqlPredicate} in order to listen related
 * distributed map events in the light of defined predicate and sends events to related
 * channel.
 *
 * @author Eren Avsarogullari
 * @since 1.0.0
 *
 */
public class HazelcastContinuousQueryMessageProducer extends AbstractHazelcastMessageProducer {

	private final String predicate;

	public HazelcastContinuousQueryMessageProducer(DistributedObject distributedObject, String predicate) {
		super(distributedObject);
		Assert.notNull(predicate, "predicate must not be null");
		this.predicate = predicate;
	}

	@Override
	protected void onInit() {
		super.onInit();
		HazelcastIntegrationDefinitionValidator.validateCacheTypeForContinuousQueryMessageProducer(getDistributedObject());
	}

	@Override
	protected void doStart() {
		IMap<?, ?> distributedMap = (IMap<?, ?>) getDistributedObject();
		String hazelcastRegisteredEventListenerId = distributedMap.addEntryListener(new HazelcastEntryListener(),
																					new SqlPredicate(this.predicate),
																					true);
		setHazelcastRegisteredEventListenerId(hazelcastRegisteredEventListenerId);
	}

	@Override
	protected void doStop() {
		((IMap<?, ?>) getDistributedObject()).removeEntryListener(getHazelcastRegisteredEventListenerId());
	}

}
