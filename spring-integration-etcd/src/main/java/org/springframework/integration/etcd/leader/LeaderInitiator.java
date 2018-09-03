/*
 * Copyright 2014-2015 the original author or authors.
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

import com.ibm.etcd.api.DeleteRangeRequest;
import com.ibm.etcd.api.PutRequest;
import com.ibm.etcd.api.TxnResponse;
import com.ibm.etcd.client.EtcdClient;
import io.grpc.StatusRuntimeException;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.Lifecycle;
import org.springframework.integration.leader.Candidate;
import org.springframework.integration.leader.Context;
import org.springframework.integration.leader.DefaultCandidate;
import org.springframework.integration.leader.event.DefaultLeaderEventPublisher;
import org.springframework.integration.leader.event.LeaderEventPublisher;
import org.springframework.util.Assert;

import static com.ibm.etcd.client.KeyUtils.bs;

/**
 * Bootstrap leadership {@link Candidate candidates} with etcd. Upon construction, {@link #start}
 * must be invoked to register the candidate for leadership election.
 *
 * @author Venil Noronha
 * @author Patrick Peralta
 */
public class LeaderInitiator
    implements Lifecycle, InitializingBean, DisposableBean, ApplicationEventPublisherAware {

  private static final Logger logger = LoggerFactory.getLogger(LeaderInitiator.class);

  /** TTL for etcd entry in seconds. */
  private static final int TTL = 10;

  /** Number of seconds to sleep between issuing heartbeats. */
  private static final int HEART_BEAT_SLEEP = TTL / 2;

  /** Default namespace for etcd entry. */
  private static final String DEFAULT_NAMESPACE = "spring-cloud";

  /** {@link EtcdClient} instance. */
  private final EtcdClient client;

  /** Candidate for leader election. */
  private final Candidate candidate;

  /** Executor service for running leadership daemon. */
  private final ExecutorService leaderExecutorService =
      Executors.newSingleThreadExecutor(
          new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
              Thread thread = new Thread(r, "Etcd-Leadership");
              thread.setDaemon(true);
              return thread;
            }
          });

  /** Executor service for running leadership worker daemon. */
  private final ExecutorService workerExecutorService =
      Executors.newSingleThreadExecutor(
          new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
              Thread thread = new Thread(r, "Etcd-Leadership-Worker");
              thread.setDaemon(true);
              return thread;
            }
          });

  /** Flag that indicates whether the current candidate is the leader. */
  private volatile boolean isLeader = false;

  /** Flag that indicates whether the current candidate's leadership should be relinquished. */
  private volatile boolean relinquishLeadership = false;

  /**
   * Future returned by submitting a {@link Initiator} to {@link #leaderExecutorService}. This is
   * used to cancel leadership.
   */
  private volatile Future<Void> initiatorFuture;

  /**
   * Future returned by submitting a {@link Worker} to {@link #workerExecutorService}. This is used
   * to notify leadership revocation.
   */
  private volatile Future<Void> workerFuture;

  /** Flag that indicates whether the leadership election for this {@link #candidate} is running. */
  private volatile boolean running;

  /** Leader event publisher. */
  private volatile LeaderEventPublisher leaderEventPublisher = new DefaultLeaderEventPublisher();

  private boolean customPublisher = false;

  /** The {@link EtcdContext} instance. */
  private final EtcdContext context;

  /** The base etcd path where candidate id is to be stored. */
  private final String baseEtcdPath;

  /**
   * Construct a {@link LeaderInitiator}.
   *
   * @param client {@link EtcdClient} instance
   * @param namespace Etcd namespace
   */
  public LeaderInitiator(EtcdClient client, String namespace) {
    this(client, new DefaultCandidate(), namespace);
  }

  /**
   * Construct a {@link LeaderInitiator}.
   *
   * @param client {@link EtcdClient} instance
   * @param candidate leadership election candidate
   * @param namespace Etcd namespace
   */
  public LeaderInitiator(EtcdClient client, Candidate candidate, String namespace) {
    this.client = client;
    this.candidate = candidate;
    this.context = new EtcdContext();
    this.baseEtcdPath =
        (namespace == null ? DEFAULT_NAMESPACE : namespace) + "/" + candidate.getRole();
  }

  @Override
  public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
    if (!this.customPublisher) {
      this.leaderEventPublisher = new DefaultLeaderEventPublisher(applicationEventPublisher);
    }
  }

  /** Start the registration of the {@link #candidate} for leader election. */
  @Override
  public synchronized void start() {
    if (!this.running) {
      this.running = true;
      this.initiatorFuture = this.leaderExecutorService.submit(new Initiator());
    }
  }

  /**
   * Stop the registration of the {@link #candidate} for leader election. If the candidate is
   * currently leader, its leadership will be revoked.
   */
  @Override
  public synchronized void stop() {
    if (this.running) {
      this.running = false;
      this.initiatorFuture.cancel(true);
    }
  }

  /** @return true if leadership election for this {@link #candidate} is running */
  @Override
  public boolean isRunning() {
    return this.running;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    start();
  }

  @Override
  public void destroy() throws Exception {
    stop();
    this.workerExecutorService.shutdown();
    this.leaderExecutorService.shutdown();
    this.workerExecutorService.awaitTermination(30, TimeUnit.SECONDS);
    this.leaderExecutorService.awaitTermination(30, TimeUnit.SECONDS);
  }

  /**
   * Sets the {@link LeaderEventPublisher}.
   *
   * @param leaderEventPublisher the event publisher
   */
  public void setLeaderEventPublisher(LeaderEventPublisher leaderEventPublisher) {
    Assert.notNull(leaderEventPublisher);
    this.leaderEventPublisher = leaderEventPublisher;
  }

  /** Notifies that the candidate has acquired leadership. */
  private void notifyGranted() {
    this.isLeader = true;
    this.leaderEventPublisher.publishOnGranted(
        LeaderInitiator.this, this.context, this.candidate.getRole());
    this.workerFuture = this.workerExecutorService.submit(new Worker());
  }

  /**
   * Notifies that the candidate's leadership was revoked.
   *
   * @throws InterruptedException if the current thread was interrupted while waiting for the worker
   *     thread to finish.
   */
  private void notifyRevoked() throws InterruptedException {
    this.isLeader = false;
    this.leaderEventPublisher.publishOnRevoked(
        LeaderInitiator.this, this.context, this.candidate.getRole());
    this.workerFuture.cancel(true);
    try {
      this.workerFuture.get();
    } catch (InterruptedException e) {
      throw e;
    } catch (CancellationException e) {
      // Consume
    } catch (ExecutionException e) {
      logger.error("Exception thrown by candidate", e.getCause());
    }
  }

  /** Tries to delete the candidate's entry from etcd. */
  private void tryDeleteCandidateEntry() {
    try {
      final TxnResponse response =
          this.client
              .getKvClient()
              .txnIf()
              .cmpEqual(bs(this.baseEtcdPath))
              .value(bs(this.candidate.getId()))
              .then()
              .delete(DeleteRangeRequest.newBuilder().setKey(bs(this.baseEtcdPath)).build())
              .sync();

      if (!response.getSucceeded()) {
        logger.warn("Couldn't delete candidate's entry from etcd because candidate was not leader");
      }
    } catch (StatusRuntimeException e) {
      logger.warn("Failed deleting candidate entry from etcd", e);
    }
  }

  /**
   * Callable that invokes {@link Candidate#onGranted(Context)} when the candidate is granted
   * leadership.
   */
  class Worker implements Callable<Void> {

    @Override
    public Void call() throws InterruptedException {
      try {
        LeaderInitiator.this.candidate.onGranted(LeaderInitiator.this.context);
        Thread.sleep(Long.MAX_VALUE);
      } finally {
        LeaderInitiator.this.relinquishLeadership = true;
        LeaderInitiator.this.candidate.onRevoked(LeaderInitiator.this.context);
      }
      return null;
    }
  }

  /** Callable that manages the etcd heart beats for leadership election. */
  class Initiator implements Callable<Void> {

    @Override
    public Void call() throws InterruptedException {
      try {
        while (LeaderInitiator.this.running) {
          if (LeaderInitiator.this.relinquishLeadership) {
            relinquishLeadership();
            LeaderInitiator.this.relinquishLeadership = false;
          } else if (LeaderInitiator.this.isLeader) {
            sendHeartBeat();
          } else {
            tryAcquire();
          }
          TimeUnit.SECONDS.sleep(HEART_BEAT_SLEEP);
        }
      } finally {
        if (LeaderInitiator.this.isLeader) {
          relinquishLeadership();
        }
      }
      return null;
    }

    /**
     * Relinquishes leadership of current candidate by deleting candidate's entry from etcd and then
     * notifies that the current candidate is no longer leader.
     *
     * @throws InterruptedException if the current thread was interrupted while notifying
     *     revocation.
     */
    private void relinquishLeadership() throws InterruptedException {
      tryDeleteCandidateEntry();
      notifyRevoked();
    }

    /**
     * Sends a heart beat to maintain leadership by refreshing the ttl of the etcd key. If the key
     * has a different value during the call, it is assumed that the current candidate's leadership
     * is revoked. If access to etcd fails, then the the current candidate's leadership is
     * relinquished.
     *
     * @throws InterruptedException if the current thread was interrupted while notifying
     *     revocation.
     */
    private void sendHeartBeat() throws InterruptedException {
      try {
        final TxnResponse response =
            LeaderInitiator.this
                .client
                .getKvClient()
                .txnIf()
                .cmpEqual(bs(LeaderInitiator.this.baseEtcdPath))
                .value(bs(LeaderInitiator.this.candidate.getId()))
                .then()
                .put(
                    PutRequest.newBuilder()
                        .setKey(bs(LeaderInitiator.this.baseEtcdPath))
                        .setValue(bs(LeaderInitiator.this.candidate.getId()))
                        .build())
                .sync();

        if (!response.getSucceeded()) {
          notifyRevoked();
        }
      } catch (StatusRuntimeException e) {
        logger.warn("Failed to send leadership heartbeat", e);
      }
    }

    /**
     * Tries to acquire leadership by posting the candidate's id to etcd. If the etcd call is
     * successful, it is assumed that the current candidate is now leader.
     */
    private void tryAcquire() {
      try {
        final long leaseId = LeaderInitiator.this.client.getLeaseClient().create(TTL).get().getID();
        final TxnResponse response =
            LeaderInitiator.this
                .client
                .getKvClient()
                .txnIf()
                .notExists(bs(LeaderInitiator.this.baseEtcdPath))
                .then()
                .put(
                    PutRequest.newBuilder()
                        .setKey(bs(LeaderInitiator.this.baseEtcdPath))
                        .setValue(bs(LeaderInitiator.this.candidate.getId()))
                        .setLease(leaseId)
                        .build())
                .sync();
        if (response.getSucceeded()) {
          notifyGranted();
        } else {
          logger.info("Tried to acquire leadership but another candidate is leader");
        }
      } catch (InterruptedException | ExecutionException e) {
        logger.warn("Failed trying to acquire leadership", e.getCause());
      }
    }
  }

  /** Implementation of leadership context backed by Etcd. */
  class EtcdContext implements Context {

    @Override
    public boolean isLeader() {
      return LeaderInitiator.this.isLeader;
    }

    @Override
    public void yield() {
      if (LeaderInitiator.this.isLeader) {
        LeaderInitiator.this.relinquishLeadership = true;
      }
    }

    @Override
    public String toString() {
      return String.format(
          "EtcdContext{role=%s, id=%s, isLeader=%s}",
          LeaderInitiator.this.candidate.getRole(),
          LeaderInitiator.this.candidate.getId(),
          isLeader());
    }
  }
}
