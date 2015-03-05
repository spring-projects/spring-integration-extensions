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

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

/**
 * This is common utility class for SI-Hazelcast Support feature.
 * 
 * @author Eren Avsarogullari
 * @since 1.0.0
 *
 */
public abstract class HazelcastIntegrationUtils {
	
	/**
	 * This constructor is added to prevent instantiation.
	 * 
	 */
	private HazelcastIntegrationUtils() {

    }

	public static void shutdownAllHazelcastInstances() {
		if (!Hazelcast.getAllHazelcastInstances().isEmpty()) {
			for (HazelcastInstance hazelcastInstance : Hazelcast.getAllHazelcastInstances()) {
				hazelcastInstance.shutdown();
			}
		}
	}
}
