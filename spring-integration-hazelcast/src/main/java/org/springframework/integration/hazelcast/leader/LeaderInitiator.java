/*
 * Copyright 2015-2018 the original author or authors.
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

package org.springframework.integration.hazelcast.leader;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.SmartLifecycle;
import org.springframework.integration.leader.Candidate;
import org.springframework.integration.leader.Context;
import org.springframework.integration.leader.DefaultCandidate;
import org.springframework.integration.leader.event.DefaultLeaderEventPublisher;
import org.springframework.integration.leader.event.LeaderEventPublisher;
import org.springframework.integration.support.leader.LockRegistryLeaderInitiator;
import org.springframework.util.Assert;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;

/**
 * Bootstrap leadership {@link org.springframework.integration.leader.Candidate candidates}
 * with Hazelcast. Upon construction, {@link #start} must be invoked to
 * register the candidate for leadership election.
 *
 * @author Patrick Peralta
 * @author Gary Russell
 * @author Dave Syer
 * @author Artem Bilan
 */
public class LeaderInitiator implements SmartLifecycle, DisposableBean, ApplicationEventPublisherAware {

	private static final Log logger = LogFactory.getLog(LeaderInitiator.class);

	private static int threadNameCount = 0;

	private static final Context NULL_CONTEXT = new NullContext();

	/*** Hazelcast client.
	 */
	private final HazelcastInstance client;

	/**
	 * Candidate for leader election.
	 */
	private final Candidate candidate;

	/**
	 * Executor service for running leadership daemon.
	 */
	private final ExecutorService executorService = Executors.newSingleThreadExecutor(new ThreadFactory() {

		@Override
		public Thread newThread(Runnable r) {
			Thread thread = new Thread(r, "Hazelcast-leadership-" + (threadNameCount++));
			thread.setDaemon(true);
			return thread;
		}

	});

	private long heartBeatMillis = LockRegistryLeaderInitiator.DEFAULT_HEART_BEAT_TIME;

	private long busyWaitMillis = LockRegistryLeaderInitiator.DEFAULT_BUSY_WAIT_TIME;

	private LeaderSelector leaderSelector;

	/**
	 * Leader event publisher.
	 */
	private LeaderEventPublisher leaderEventPublisher = new DefaultLeaderEventPublisher();

	private boolean autoStartup = true;

	private int phase;

	/**
	 * Future returned by submitting an {@link LeaderSelector} to {@link #executorService}.
	 * This is used to cancel leadership.
	 */
	private volatile Future<Void> future;

	/**
	 * Hazelcast distributed lock.
	 */
	private volatile ILock lock;

	private boolean customPublisher = false;

	private volatile boolean running;

	/**
	 * Construct a {@link LeaderInitiator} with a default candidate.
	 * @param client Hazelcast client
	 */
	public LeaderInitiator(HazelcastInstance client) {
		this(client, new DefaultCandidate());
	}

	/**
	 * Construct a {@link LeaderInitiator}.
	 * @param client Hazelcast client
	 * @param candidate leadership election candidate
	 */
	public LeaderInitiator(HazelcastInstance client, Candidate candidate) {
		Assert.notNull(client, "'client' must not be null");
		Assert.notNull(candidate, "'candidate' must not be null");
		this.client = client;
		this.candidate = candidate;
	}

	/**
	 * Sets the {@link LeaderEventPublisher}.
	 * @param leaderEventPublisher the event publisher
	 */
	public void setLeaderEventPublisher(LeaderEventPublisher leaderEventPublisher) {
		Assert.notNull(leaderEventPublisher, "'leaderEventPublisher' must not be null");
		this.leaderEventPublisher = leaderEventPublisher;
		this.customPublisher = true;
	}

	/**
	 * Time in milliseconds to wait in between attempts to re-acquire the lock, once it is
	 * held. The heartbeat time has to be less than the remote lock expiry period, if
	 * there is one, otherwise other nodes can steal the lock while we are sleeping here.
	 * @param heartBeatMillis the heart-beat timeout in milliseconds.
	 * Defaults to {@link LockRegistryLeaderInitiator#DEFAULT_HEART_BEAT_TIME}
	 * @since 1.0.1
	 */
	public void setHeartBeatMillis(long heartBeatMillis) {
		this.heartBeatMillis = heartBeatMillis;
	}

	/**
	 * Time in milliseconds to wait in between attempts to acquire the lock, if it is not
	 * held. The longer this is, the longer the system can be leaderless, if the leader
	 * dies. If a leader dies without releasing its lock, the system might still have to
	 * wait for the old lock to expire, but after that it should not have to wait longer
	 * than the busy wait time to get a new leader.
	 * @param busyWaitMillis the busy-wait timeout in milliseconds
	 * Defaults to {@link LockRegistryLeaderInitiator#DEFAULT_BUSY_WAIT_TIME}
	 * @since 1.0.1
	 */
	public void setBusyWaitMillis(long busyWaitMillis) {
		this.busyWaitMillis = busyWaitMillis;
	}

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		if (!this.customPublisher) {
			this.leaderEventPublisher = new DefaultLeaderEventPublisher(applicationEventPublisher);
		}
	}

	public void setAutoStartup(boolean autoStartup) {
		this.autoStartup = autoStartup;
	}

	@Override
	public boolean isAutoStartup() {
		return this.autoStartup;
	}

	public void setPhase(int phase) {
		this.phase = phase;
	}

	@Override
	public int getPhase() {
		return this.phase;
	}

	/**
	 * The context of the initiator or null if not running.
	 * @return the context (or null if not running)
	 */
	public Context getContext() {
		if (this.leaderSelector == null) {
			return NULL_CONTEXT;
		}
		return this.leaderSelector.context;
	}

	/**
	 * Start the registration of the {@link #candidate} for leader election.
	 */
	@Override
	public synchronized void start() {
		if (!this.running) {
			this.lock = this.client.getLock(this.candidate.getRole());
			this.leaderSelector = new LeaderSelector();
			this.running = true;
			this.future = this.executorService.submit(this.leaderSelector);
		}
	}

	@Override
	public void stop(Runnable callback) {
		stop();
		if (callback != null) {
			callback.run();
		}
	}

	/**
	 * Stop the registration of the {@link #candidate} for leader election.
	 * If the candidate is currently leader, its leadership will be revoked.
	 */
	@Override
	public synchronized void stop() {
		if (this.running) {
			this.running = false;
			if (this.future != null) {
				this.future.cancel(true);
			}
			this.future = null;
		}
	}

	/**
	 * {@code true} if leadership election for this {@link #candidate} is running.
	 * @return true if leadership election for this {@link #candidate} is running
	 */
	@Override
	public boolean isRunning() {
		return this.running;
	}

	@Override
	public void destroy() throws Exception {
		stop();
		this.executorService.shutdown();
	}

	/**
	 * Callable that manages the acquisition of Hazelcast locks
	 * for leadership election.
	 */
	protected class LeaderSelector implements Callable<Void> {

		protected final HazelcastContext context = new HazelcastContext();

		protected final String role = LeaderInitiator.this.candidate.getRole();

		private volatile boolean locked = false;

		@Override
		public Void call() throws Exception {
			try {
				while (isRunning()) {
					try {
						// We always try to acquire the lock, in case it expired
						boolean acquired =
								LeaderInitiator.this.lock.tryLock(LeaderInitiator.this.heartBeatMillis,
										TimeUnit.MILLISECONDS);
						if (!this.locked) {
							if (acquired) {
								// Success: we are now leader
								this.locked = true;
								handleGranted();
							}
						}
						else if (acquired) {
							// If we were able to acquire it but we were already locked we
							// should release it
							LeaderInitiator.this.lock.unlock();
							// Give it a chance to expire.
							Thread.sleep(LeaderInitiator.this.heartBeatMillis);
						}
						else {
							this.locked = false;
							// We were not able to acquire it, therefore not leading any more
							handleRevoked();
							// Try again quickly in case the lock holder dropped it
							Thread.sleep(LeaderInitiator.this.busyWaitMillis);
						}
					}
					catch (Exception e) {
						if (this.locked) {
							LeaderInitiator.this.lock.unlock();
							this.locked = false;
							// The lock was broken and we are no longer leader
							handleRevoked();
							// Give it a chance to elect some other leader.
							Thread.sleep(LeaderInitiator.this.busyWaitMillis);
							if (isRunning()) {
								logger.warn("Restarting LeaderSelector because of error.", e);
								LeaderInitiator.this.future = LeaderInitiator.this.executorService.submit(this);
							}
							if (e instanceof InterruptedException) {
								Thread.currentThread().interrupt();
							}
							return null;
						}
					}
				}
			}
			finally {
				if (this.locked) {
					LeaderInitiator.this.lock.unlock();
					// We are stopping, therefore not leading any more
					handleRevoked();
				}
				this.locked = false;
			}
			return null;
		}


		private void handleGranted() throws InterruptedException {
			LeaderInitiator.this.candidate.onGranted(this.context);
			if (LeaderInitiator.this.leaderEventPublisher != null) {
				try {
					LeaderInitiator.this.leaderEventPublisher.publishOnGranted(
							LeaderInitiator.this, this.context, this.role);
				}
				catch (Exception e) {
					logger.warn("Error publishing OnGranted event.", e);
				}
			}
		}

		private void handleRevoked() {
			LeaderInitiator.this.candidate.onRevoked(this.context);
			if (LeaderInitiator.this.leaderEventPublisher != null) {
				try {
					LeaderInitiator.this.leaderEventPublisher.publishOnRevoked(
							LeaderInitiator.this, this.context, role);
				}
				catch (Exception e) {
					logger.warn("Error publishing OnRevoked event.", e);
				}
			}
		}

	}

	/**
	 * Implementation of leadership context backed by Hazelcast.
	 */
	protected class HazelcastContext implements Context {

		@Override
		public boolean isLeader() {
			return LeaderInitiator.this.leaderSelector.locked;
		}

		@Override
		public void yield() {
			if (LeaderInitiator.this.future != null) {
				LeaderInitiator.this.future.cancel(true);
			}
		}

		@Override
		public String toString() {
			return "HazelcastContext{role=" + LeaderInitiator.this.candidate.getRole() +
					", id=" + LeaderInitiator.this.candidate.getId() +
					", isLeader=" + isLeader() + "}";
		}

	}

	private static final class NullContext implements Context {

		@Override
		public boolean isLeader() {
			return false;
		}

		@Override
		public void yield() {
			// No-op
		}

	}

}
