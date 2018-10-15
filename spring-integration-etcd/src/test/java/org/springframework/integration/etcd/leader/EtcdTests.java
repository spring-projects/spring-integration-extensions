/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.integration.etcd.leader;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Ignore;
import org.junit.Test;

import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.leader.AbstractCandidate;
import org.springframework.integration.leader.Context;
import org.springframework.integration.leader.DefaultCandidate;
import org.springframework.integration.leader.event.AbstractLeaderEvent;
import org.springframework.integration.leader.event.DefaultLeaderEventPublisher;
import org.springframework.integration.leader.event.LeaderEventPublisher;

import com.ibm.etcd.client.EtcdClient;

/**
 * Tests for etcd leader election.
 *
 * @author Venil Noronha
 * @author Lewis Headden
 */
@Ignore("Need a docker image for ETCD node")
public class EtcdTests {

	@Test
	public void testSimpleLeader() throws InterruptedException {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(SimpleTestConfig.class);
		TestCandidate candidate = ctx.getBean(TestCandidate.class);
		TestEventListener listener = ctx.getBean(TestEventListener.class);
		assertThat(candidate.onGrantedLatch.await(5, TimeUnit.SECONDS), is(true));
		assertThat(listener.onEventLatch.await(5, TimeUnit.SECONDS), is(true));
		assertThat(listener.events.size(), is(1));
		ctx.close();
	}

	@Test
	public void testLeaderYield() throws InterruptedException {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(YieldTestConfig.class);
		YieldTestCandidate candidate = ctx.getBean(YieldTestCandidate.class);
		YieldTestEventListener listener = ctx.getBean(YieldTestEventListener.class);
		assertThat(candidate.onGrantedLatch.await(5, TimeUnit.SECONDS), is(true));
		assertThat(candidate.onRevokedLatch.await(10, TimeUnit.SECONDS), is(true));
		assertThat(listener.onEventsLatch.await(1, TimeUnit.MILLISECONDS), is(true));
		assertThat(listener.events.size(), is(2));
		ctx.close();
	}

	@Test
	public void testBlockingThreadLeader() throws InterruptedException {
		AnnotationConfigApplicationContext ctx =
				new AnnotationConfigApplicationContext(BlockingThreadTestConfig.class);
		BlockingThreadTestCandidate candidate = ctx.getBean(BlockingThreadTestCandidate.class);
		YieldTestEventListener listener = ctx.getBean(YieldTestEventListener.class);
		assertThat(candidate.onGrantedLatch.await(5, TimeUnit.SECONDS), is(true));
		Thread.sleep(2000); // Let the grant-notification thread run for a while
		candidate.ctx.yield(); // Internally interrupts the grant notification thread
		assertThat(candidate.onRevokedLatch.await(10, TimeUnit.SECONDS), is(true));
		assertThat(listener.onEventsLatch.await(1, TimeUnit.MILLISECONDS), is(true));
		assertThat(listener.events.size(), is(2));
		ctx.close();
	}

	@Test
	public void testFailingCandidateGrantCallback() throws InterruptedException {
		AnnotationConfigApplicationContext ctx =
				new AnnotationConfigApplicationContext(FailingCandidateTestConfig.class);
		FailingTestCandidate candidate = ctx.getBean(FailingTestCandidate.class);
		YieldTestEventListener listener = ctx.getBean(YieldTestEventListener.class);
		assertThat(candidate.onGrantedLatch.await(5, TimeUnit.SECONDS), is(true));
		assertThat(candidate.onRevokedLatch.await(10, TimeUnit.SECONDS), is(true));
		assertThat(listener.onEventsLatch.await(10, TimeUnit.SECONDS), is(true));
		assertThat(listener.events.size(), is(2));
		ctx.close();
	}

	@Configuration
	static class SimpleTestConfig {

		@Bean
		public TestCandidate candidate() {
			return new TestCandidate();
		}

		@Bean
		public EtcdClient etcdInstance() {
			return EtcdClient.forEndpoint("localhost", 2379).withPlainText().build();
		}

		@Bean
		public LeaderInitiator initiator() {
			LeaderInitiator initiator = new LeaderInitiator(etcdInstance(), candidate(), "etcd-test");
			initiator.start();
			return initiator;
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

	@Configuration
	static class YieldTestConfig {

		@Bean
		public YieldTestCandidate candidate() {
			return new YieldTestCandidate();
		}

		@Bean
		public EtcdClient etcdInstance() {
			return EtcdClient.forEndpoint("localhost", 2379).withPlainText().build();
		}

		@Bean
		public LeaderInitiator initiator() {
			LeaderInitiator initiator = new LeaderInitiator(etcdInstance(), candidate(), "etcd-yield-test");
			initiator.setLeaderEventPublisher(leaderEventPublisher());
			initiator.start();
			return initiator;
		}

		@Bean
		public LeaderEventPublisher leaderEventPublisher() {
			return new DefaultLeaderEventPublisher();
		}

		@Bean
		public YieldTestEventListener testEventListener() {
			return new YieldTestEventListener();
		}

	}

	static class YieldTestCandidate extends DefaultCandidate {

		CountDownLatch onGrantedLatch = new CountDownLatch(1);

		CountDownLatch onRevokedLatch = new CountDownLatch(1);

		@Override
		public void onGranted(Context ctx) {
			super.onGranted(ctx);
			this.onGrantedLatch.countDown();
			ctx.yield();
		}

		@Override
		public void onRevoked(Context ctx) {
			super.onRevoked(ctx);
			this.onRevokedLatch.countDown();
		}

	}

	static class YieldTestEventListener implements ApplicationListener<AbstractLeaderEvent> {

		CountDownLatch onEventsLatch = new CountDownLatch(2);

		ArrayList<AbstractLeaderEvent> events = new ArrayList<>();

		@Override
		public void onApplicationEvent(AbstractLeaderEvent event) {
			this.events.add(event);
			this.onEventsLatch.countDown();
		}

	}

	@Configuration
	static class BlockingThreadTestConfig {

		@Bean
		public BlockingThreadTestCandidate candidate() {
			return new BlockingThreadTestCandidate();
		}

		@Bean
		public EtcdClient etcdInstance() {
			return EtcdClient.forEndpoint("localhost", 2379).withPlainText().build();
		}

		@Bean
		public LeaderInitiator initiator() {
			LeaderInitiator initiator = new LeaderInitiator(etcdInstance(), candidate(), "etcd-blocking-thread-test");
			initiator.setLeaderEventPublisher(leaderEventPublisher());
			initiator.start();
			return initiator;
		}

		@Bean
		public LeaderEventPublisher leaderEventPublisher() {
			return new DefaultLeaderEventPublisher();
		}

		@Bean
		public YieldTestEventListener testEventListener() {
			return new YieldTestEventListener();
		}

	}

	static class BlockingThreadTestCandidate extends AbstractCandidate {

		private final Log logger = LogFactory.getLog(getClass());

		CountDownLatch onGrantedLatch = new CountDownLatch(1);

		CountDownLatch onRevokedLatch = new CountDownLatch(1);

		Context ctx = null;

		@Override
		public void onGranted(Context ctx) throws InterruptedException {
			this.ctx = ctx;
			logger.info(this + " has been granted leadership; context: " + ctx);
			this.onGrantedLatch.countDown();
			while (true) {
				logger.info(this + " is doing some heavy lifting");
				try {
					Thread.sleep(1000); // Mock heavy lifting
				}
				catch (InterruptedException e) {
					logger.info(this + " was interrupted, rethrowing the exception");
					throw e;
				}
			}
		}

		@Override
		public void onRevoked(Context ctx) {
			logger.info(this + " leadership has been revoked");
			this.onRevokedLatch.countDown();
		}

	}

	@Configuration
	static class FailingCandidateTestConfig {

		@Bean
		public FailingTestCandidate candidate() {
			return new FailingTestCandidate();
		}

		@Bean
		public EtcdClient etcdInstance() {
			return EtcdClient.forEndpoint("localhost", 2379).withPlainText().build();
		}

		@Bean
		public LeaderInitiator initiator() {
			LeaderInitiator initiator =
					new LeaderInitiator(etcdInstance(), candidate(), "etcd-failing-candidate-test");
			initiator.setLeaderEventPublisher(leaderEventPublisher());
			initiator.start();
			return initiator;
		}

		@Bean
		public LeaderEventPublisher leaderEventPublisher() {
			return new DefaultLeaderEventPublisher();
		}

		@Bean
		public YieldTestEventListener testEventListener() {
			return new YieldTestEventListener();
		}

	}

	static class FailingTestCandidate extends DefaultCandidate {

		CountDownLatch onGrantedLatch = new CountDownLatch(1);

		CountDownLatch onRevokedLatch = new CountDownLatch(1);

		@Override
		public void onGranted(Context ctx) {
			super.onGranted(ctx);
			this.onGrantedLatch.countDown();
			throw new RuntimeException("Candidate grant callback failure");
		}

		@Override
		public void onRevoked(Context ctx) {
			super.onRevoked(ctx);
			this.onRevokedLatch.countDown();
		}

	}

}
