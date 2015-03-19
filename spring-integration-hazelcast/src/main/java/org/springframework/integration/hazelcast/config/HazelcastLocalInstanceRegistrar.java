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

package org.springframework.integration.hazelcast.config;

import java.net.SocketAddress;
import java.util.concurrent.locks.Lock;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MultiMap;

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.integration.hazelcast.listener.HazelcastMembershipListener;

/**
 * This class creates an internal configuration {@link MultiMap} to cache Hazelcast instances' socket
 * address information which used Hazelcast event-driven inbound channel adapter(s). It
 * also enables a Hazelcast {@link com.hazelcast.core.MembershipListener} to listen for
 * membership updates.
 *
 * @author Eren Avsarogullari
 * @since 1.0.0
 */
public class HazelcastLocalInstanceRegistrar implements SmartInitializingSingleton {

	public static final String HZ_CLUSTER_WIDE_CONFIG_MULTI_MAP = "HZ_CLUSTER_WIDE_CONFIG_MULTI_MAP";

	public static final String HZ_CLUSTER_WIDE_CONFIG_MULTI_MAP_LOCK = "HZ_CLUSTER_WIDE_CONFIG_MULTI_MAP_LOCK";

	@Override
	public void afterSingletonsInstantiated() {
		if (!Hazelcast.getAllHazelcastInstances().isEmpty()) {
			HazelcastInstance hazelcastInstance = Hazelcast.getAllHazelcastInstances().iterator().next();
			hazelcastInstance.getCluster().addMembershipListener(new HazelcastMembershipListener());
			syncConfigurationMultiMap(hazelcastInstance);
		}
		else {
			throw new IllegalStateException("No Active Local Hazelcast Instance found.");
		}
	}

	private void syncConfigurationMultiMap(HazelcastInstance hazelcastInstance) {
		Lock lock = hazelcastInstance.getLock(HZ_CLUSTER_WIDE_CONFIG_MULTI_MAP_LOCK);
		lock.lock();
		try {
			MultiMap<SocketAddress, SocketAddress> multiMap = hazelcastInstance
					.getMultiMap(HZ_CLUSTER_WIDE_CONFIG_MULTI_MAP);
			for (HazelcastInstance localInstance : Hazelcast.getAllHazelcastInstances()) {
				SocketAddress localInstanceSocketAddress = localInstance.getLocalEndpoint().getSocketAddress();
				if (multiMap.size() == 0) {
					multiMap.put(localInstanceSocketAddress, localInstanceSocketAddress);
				}
				else {
					multiMap.put(multiMap.keySet().iterator().next(), localInstanceSocketAddress);
				}
			}
		}
		finally {
			lock.unlock();
		}
	}

}
