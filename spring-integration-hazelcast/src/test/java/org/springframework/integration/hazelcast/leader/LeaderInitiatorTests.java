/*
 * Copyright 2015-2019 the original author or authors.
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

package org.springframework.integration.hazelcast.leader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.leader.Context;
import org.springframework.integration.leader.DefaultCandidate;
import org.springframework.integration.leader.event.AbstractLeaderEvent;
import org.springframework.integration.leader.event.DefaultLeaderEventPublisher;
import org.springframework.integration.leader.event.LeaderEventPublisher;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.impl.HazelcastInstanceFactory;

/**
 * Tests for hazelcast leader election.
 *
 * @author Janne Valkealahti
 * @author Patrick Peralta
 * @author Dave Syer
 * @author Artem Bilan
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@DirtiesContext
public class LeaderInitiatorTests {

	@Autowired
	private HazelcastInstance hazelcastInstance;

	@Autowired
	private TestCandidate candidate;

	@Autowired
	private TestEventListener listener;

	@Autowired
	private LeaderInitiator initiator;

	@AfterClass
	public static void shutdown() {
		HazelcastInstanceFactory.terminateAll();
	}

	@Test
	public void testLeaderElections() throws Exception {
		assertTrue(this.candidate.onGrantedLatch.await(5, TimeUnit.SECONDS));
		assertTrue(this.listener.onEventLatch.await(5, TimeUnit.SECONDS));
		assertEquals(1, this.listener.events.size());

		this.initiator.destroy();

		CountDownLatch granted = new CountDownLatch(1);
		CountingPublisher countingPublisher = new CountingPublisher(granted);
		List<LeaderInitiator> initiators = new ArrayList<>();
		for (int i = 0; i < 2; i++) {
			LeaderInitiator initiator = new LeaderInitiator(this.hazelcastInstance, new DefaultCandidate());
			initiator.setLeaderEventPublisher(countingPublisher);
			initiators.add(initiator);
		}

		for (LeaderInitiator initiator : initiators) {
			initiator.start();
		}

		assertTrue(granted.await(10, TimeUnit.SECONDS));

		LeaderInitiator initiator1 = countingPublisher.initiator;

		LeaderInitiator initiator2 = null;

		for (LeaderInitiator initiator : initiators) {
			if (initiator != initiator1) {
				initiator2 = initiator;
				break;
			}
		}

		assertNotNull(initiator2);

		assertTrue(initiator1.getContext().isLeader());
		assertFalse(initiator2.getContext().isLeader());

		final CountDownLatch granted1 = new CountDownLatch(1);
		final CountDownLatch granted2 = new CountDownLatch(1);
		CountDownLatch revoked1 = new CountDownLatch(1);
		CountDownLatch revoked2 = new CountDownLatch(1);
		initiator1.setLeaderEventPublisher(new CountingPublisher(granted1, revoked1) {

			@Override
			public void publishOnRevoked(Object source, Context context, String role) {
				try {
					// It's difficult to see round-robin election, so block one initiator until the second is elected.
					assertTrue(granted2.await(10, TimeUnit.SECONDS));
				}
				catch (InterruptedException e) {
					// No op
				}
				super.publishOnRevoked(source, context, role);
			}

		});

		initiator2.setLeaderEventPublisher(new CountingPublisher(granted2, revoked2) {

			@Override
			public void publishOnRevoked(Object source, Context context, String role) {
				try {
					// It's difficult to see round-robin election, so block one initiator until the second is elected.
					assertTrue(granted1.await(10, TimeUnit.SECONDS));
				}
				catch (InterruptedException e) {
					// No op
				}
				super.publishOnRevoked(source, context, role);
			}

		});

		initiator1.getContext().yield();

		assertTrue(revoked1.await(10, TimeUnit.SECONDS));

		assertTrue(initiator2.getContext().isLeader());
		assertFalse(initiator1.getContext().isLeader());

		initiator2.getContext().yield();

		assertTrue(revoked2.await(10, TimeUnit.SECONDS));

		assertTrue(initiator1.getContext().isLeader());
		assertFalse(initiator2.getContext().isLeader());

		initiator2.destroy();

		CountDownLatch revoked11 = new CountDownLatch(1);
		initiator1.setLeaderEventPublisher(new CountingPublisher(new CountDownLatch(1), revoked11));

		initiator1.getContext().yield();

		assertTrue(revoked11.await(10, TimeUnit.SECONDS));

		initiator1.destroy();


		CountDownLatch onGranted = new CountDownLatch(1);

		DefaultCandidate candidate = spy(new DefaultCandidate());
		willAnswer(invocation -> {
			try {
				return invocation.callRealMethod();
			}
			finally {
				onGranted.countDown();
			}
		})
				.given(candidate).onGranted(any(Context.class));

		LeaderInitiator initiator = new LeaderInitiator(this.hazelcastInstance, candidate);

		initiator.setLeaderEventPublisher(new DefaultLeaderEventPublisher() {

			@Override
			public void publishOnGranted(Object source, Context context, String role) {
				throw new RuntimeException("intentional");
			}

		});

		initiator.start();

		assertTrue(onGranted.await(5, TimeUnit.SECONDS));

		assertTrue(initiator.getContext().isLeader());

		initiator.destroy();
	}


	@Configuration
	public static class TestConfig {

		@Bean
		public TestCandidate candidate() {
			return new TestCandidate();
		}

		@Bean
		public Config hazelcastConfig() {
			Config config = new Config();
			config.getCPSubsystemConfig().setCPMemberCount(3)
					.setSessionHeartbeatIntervalSeconds(1);
			return config;
		}

		@Bean(destroyMethod = "")
		public HazelcastInstance hazelcastInstance() {
			return Hazelcast.newHazelcastInstance(hazelcastConfig());
		}

		@Bean(destroyMethod = "")
		public HazelcastInstance hazelcastInstance2() {
			return Hazelcast.newHazelcastInstance(hazelcastConfig());
		}

		@Bean(destroyMethod = "")
		public HazelcastInstance hazelcastInstance3() {
			return Hazelcast.newHazelcastInstance(hazelcastConfig());
		}

		@Bean
		public LeaderInitiator initiator() {
			return new LeaderInitiator(hazelcastInstance(), candidate());
		}

		@Bean
		public TestEventListener testEventListener() {
			return new TestEventListener();
		}

	}

	static class TestCandidate extends DefaultCandidate {

		CountDownLatch onGrantedLatch = new CountDownLatch(1);

		@Override
		public void onGranted(Context ctx) {
			this.onGrantedLatch.countDown();
			super.onGranted(ctx);
		}

	}

	static class TestEventListener implements ApplicationListener<AbstractLeaderEvent> {

		CountDownLatch onEventLatch = new CountDownLatch(1);

		ArrayList<AbstractLeaderEvent> events = new ArrayList<>();

		@Override
		public void onApplicationEvent(AbstractLeaderEvent event) {
			this.events.add(event);
			this.onEventLatch.countDown();
		}

	}

	private static class CountingPublisher implements LeaderEventPublisher {

		private CountDownLatch granted;

		private CountDownLatch revoked;

		private volatile LeaderInitiator initiator;

		CountingPublisher(CountDownLatch granted, CountDownLatch revoked) {
			this.granted = granted;
			this.revoked = revoked;
		}

		CountingPublisher(CountDownLatch granted) {
			this(granted, new CountDownLatch(1));
		}

		@Override
		public void publishOnRevoked(Object source, Context context, String role) {
			this.revoked.countDown();
		}

		@Override
		public void publishOnFailedToAcquire(Object source, Context context, String role) {

		}

		@Override
		public void publishOnGranted(Object source, Context context, String role) {
			this.initiator = (LeaderInitiator) source;
			this.granted.countDown();
		}

	}

}
