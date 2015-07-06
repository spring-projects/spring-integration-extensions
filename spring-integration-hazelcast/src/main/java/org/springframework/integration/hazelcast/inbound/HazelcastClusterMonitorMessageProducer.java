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

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.integration.hazelcast.ClusterMonitorType;
import org.springframework.integration.hazelcast.HazelcastIntegrationDefinitionValidator;
import org.springframework.util.Assert;

import com.hazelcast.core.Client;
import com.hazelcast.core.ClientListener;
import com.hazelcast.core.DistributedObjectEvent;
import com.hazelcast.core.DistributedObjectListener;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.LifecycleEvent;
import com.hazelcast.core.LifecycleListener;
import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;
import com.hazelcast.core.MigrationEvent;
import com.hazelcast.core.MigrationListener;

/**
 * Hazelcast Cluster Monitor Event Driven Message Producer is a message producer which
 * enables {@link HazelcastClusterMonitorMessageProducer.HazelcastClusterMonitorListener}
 * listener in order to listen cluster related events and sends events to related channel.
 *
 * @author Eren Avsarogullari
 * @since 1.0.0
 */
public class HazelcastClusterMonitorMessageProducer extends MessageProducerSupport {

	private static final int CLUSTER_MONITOR_TYPE_COUNT = 5;

	private final HazelcastInstance hazelcastInstance;

	private Set<String> monitorTypes = Collections
			.singleton(ClusterMonitorType.MEMBERSHIP.name());

	private final Map<ClusterMonitorType, String> hazelcastRegisteredListenerIdMap =
			new ConcurrentHashMap<>(CLUSTER_MONITOR_TYPE_COUNT);

	public HazelcastClusterMonitorMessageProducer(
			final HazelcastInstance hazelcastInstance) {
		Assert.notNull(hazelcastInstance, "'hazelcastInstance' must not be null");
		this.hazelcastInstance = hazelcastInstance;
	}

	public void setMonitorEventTypes(String monitorEventTypes) {
		final Set<String> monitorTypes = HazelcastIntegrationDefinitionValidator.validateEnumType(
				ClusterMonitorType.class, monitorEventTypes);
		Assert.notEmpty(monitorTypes, "'monitorTypes' must have elements");
		this.monitorTypes = monitorTypes;
	}

	@Override
	protected void doStart() {
		final HazelcastClusterMonitorListener clusterMonitorListener = new HazelcastClusterMonitorListener();

		if (this.monitorTypes.contains(ClusterMonitorType.MEMBERSHIP.name())) {
			final String registrationId = this.hazelcastInstance.getCluster()
					.addMembershipListener(clusterMonitorListener);
			this.hazelcastRegisteredListenerIdMap.put(ClusterMonitorType.MEMBERSHIP,
					registrationId);
		}

		if (this.monitorTypes.contains(ClusterMonitorType.DISTRIBUTED_OBJECT.name())) {
			final String registrationId = this.hazelcastInstance
					.addDistributedObjectListener(clusterMonitorListener);
			this.hazelcastRegisteredListenerIdMap.put(
					ClusterMonitorType.DISTRIBUTED_OBJECT, registrationId);
		}

		if (this.monitorTypes.contains(ClusterMonitorType.MIGRATION.name())) {
			final String registrationId = this.hazelcastInstance.getPartitionService()
					.addMigrationListener(clusterMonitorListener);
			this.hazelcastRegisteredListenerIdMap.put(ClusterMonitorType.MIGRATION,
					registrationId);
		}

		if (this.monitorTypes.contains(ClusterMonitorType.LIFECYCLE.name())) {
			final String registrationId = this.hazelcastInstance.getLifecycleService()
					.addLifecycleListener(clusterMonitorListener);
			this.hazelcastRegisteredListenerIdMap.put(ClusterMonitorType.LIFECYCLE,
					registrationId);
		}

		if (this.monitorTypes.contains(ClusterMonitorType.CLIENT.name())) {
			final String registrationId = this.hazelcastInstance.getClientService()
					.addClientListener(clusterMonitorListener);
			this.hazelcastRegisteredListenerIdMap.put(ClusterMonitorType.CLIENT,
					registrationId);
		}
	}

	@Override
	protected void doStop() {
		String id = this.hazelcastRegisteredListenerIdMap
				.remove(ClusterMonitorType.MEMBERSHIP);
		if (id != null) {
			this.hazelcastInstance.getCluster().removeMembershipListener(id);
		}

		id = this.hazelcastRegisteredListenerIdMap
				.remove(ClusterMonitorType.DISTRIBUTED_OBJECT);
		if (id != null) {
			this.hazelcastInstance.removeDistributedObjectListener(id);
		}

		id = this.hazelcastRegisteredListenerIdMap.remove(ClusterMonitorType.MIGRATION);
		if (id != null) {
			this.hazelcastInstance.getPartitionService().removeMigrationListener(id);
		}

		id = this.hazelcastRegisteredListenerIdMap.remove(ClusterMonitorType.LIFECYCLE);
		if (id != null) {
			this.hazelcastInstance.getLifecycleService().removeLifecycleListener(id);
		}

		id = this.hazelcastRegisteredListenerIdMap.remove(ClusterMonitorType.CLIENT);
		if (id != null) {
			this.hazelcastInstance.getClientService().removeClientListener(id);
		}
	}

	@Override
	public String getComponentType() {
		return "hazelcast:cm-inbound-channel-adapter";
	}

	private void processEvent(Object event) {
		Assert.notNull(event, "'hazelcast event' must not be null");
		this.sendMessage(getMessageBuilderFactory().withPayload(event).build());

		if (logger.isDebugEnabled()) {
			logger.debug("Received Cluster Monitor Event : " + event);
		}
	}

	private final class HazelcastClusterMonitorListener implements MembershipListener,
			DistributedObjectListener, MigrationListener, LifecycleListener,
			ClientListener {

		@Override
		public void memberAdded(MembershipEvent membershipEvent) {
			processEvent(membershipEvent);
		}

		@Override
		public void memberRemoved(MembershipEvent membershipEvent) {
			processEvent(membershipEvent);
		}

		@Override
		public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {
			processEvent(memberAttributeEvent);
		}

		@Override
		public void distributedObjectCreated(DistributedObjectEvent event) {
			processEvent(event);
		}

		@Override
		public void distributedObjectDestroyed(DistributedObjectEvent event) {
			processEvent(event);
		}

		@Override
		public void migrationStarted(MigrationEvent migrationEvent) {
			processEvent(migrationEvent);
		}

		@Override
		public void migrationCompleted(MigrationEvent migrationEvent) {
			processEvent(migrationEvent);
		}

		@Override
		public void migrationFailed(MigrationEvent migrationEvent) {
			processEvent(migrationEvent);
		}

		@Override
		public void stateChanged(LifecycleEvent event) {
			processEvent(event);
		}

		@Override
		public void clientConnected(Client client) {
			processEvent(client);
		}

		@Override
		public void clientDisconnected(Client client) {
			processEvent(client);
		}
	}

}
