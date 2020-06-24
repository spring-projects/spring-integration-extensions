/*
 * Copyright 2017-2019 the original author or authors.
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

package org.springframework.integration.hazelcast.lock;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;

import org.junit.AfterClass;
import org.junit.Test;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.lock.FencedLock;
import com.hazelcast.instance.HazelcastInstanceFactory;

/**
 * @author Artem Bilan
 */
public class HazelcastLockRegistryTests {

	private static Config config = new Config();

	static {
		config.getCPSubsystemConfig().setCPMemberCount(3);
	}

	private static final HazelcastInstance instance = Hazelcast.newHazelcastInstance(config);

	private static final HazelcastInstance instance2 = Hazelcast.newHazelcastInstance(config);

	private static final HazelcastInstance instance3 = Hazelcast.newHazelcastInstance(config);

	@AfterClass
	public static void destroy() {
		HazelcastInstanceFactory.terminateAll();
	}

	@Test
	public void testLock() {
		HazelcastLockRegistry registry = new HazelcastLockRegistry(instance);
		for (int i = 0; i < 10; i++) {
			Lock lock = registry.obtain("foo");
			lock.lock();
			try {
				assertTrue(((FencedLock) lock).isLocked());
				assertTrue(((FencedLock) lock).isLockedByCurrentThread());
			}
			finally {
				lock.unlock();
			}
		}
	}

	@Test
	public void testLockInterruptibly() throws Exception {
		HazelcastLockRegistry registry = new HazelcastLockRegistry(instance);
		for (int i = 0; i < 10; i++) {
			Lock lock = registry.obtain("foo");
			lock.lockInterruptibly();
			try {
				assertTrue(((FencedLock) lock).isLocked());
				assertTrue(((FencedLock) lock).isLockedByCurrentThread());
			}
			finally {
				lock.unlock();
			}
		}
	}

	@Test
	public void testReentrantLock() {
		HazelcastLockRegistry registry = new HazelcastLockRegistry(instance);
		for (int i = 0; i < 10; i++) {
			Lock lock1 = registry.obtain("foo");
			lock1.lock();
			try {
				Lock lock2 = registry.obtain("foo");
				assertSame(lock1, lock2);
				lock2.lock();
				lock2.unlock();
			}
			finally {
				lock1.unlock();
			}
		}
	}

	@Test
	public void testReentrantLockInterruptibly() throws Exception {
		HazelcastLockRegistry registry = new HazelcastLockRegistry(instance);
		for (int i = 0; i < 10; i++) {
			Lock lock1 = registry.obtain("foo");
			lock1.lockInterruptibly();
			try {
				Lock lock2 = registry.obtain("foo");
				assertSame(lock1, lock2);
				lock2.lockInterruptibly();
				lock2.unlock();
			}
			finally {
				lock1.unlock();
			}
		}
	}

	@Test
	public void testTwoLocks() throws Exception {
		HazelcastLockRegistry registry = new HazelcastLockRegistry(instance);
		for (int i = 0; i < 10; i++) {
			Lock lock1 = registry.obtain("foo");
			lock1.lockInterruptibly();
			try {
				Lock lock2 = registry.obtain("bar");
				assertNotSame(lock1, lock2);
				lock2.lockInterruptibly();
				lock2.unlock();
			}
			finally {
				lock1.unlock();
			}
		}
	}

	@Test
	public void testTwoThreadsSecondFailsToGetLock() throws Exception {
		HazelcastLockRegistry registry = new HazelcastLockRegistry(instance);
		Lock lock1 = registry.obtain("foo");
		lock1.lockInterruptibly();
		AtomicBoolean locked = new AtomicBoolean();
		CountDownLatch latch = new CountDownLatch(1);
		Future<Object> result = Executors.newSingleThreadExecutor().submit(() -> {
			Lock lock2 = registry.obtain("foo");
			locked.set(lock2.tryLock(200, TimeUnit.MILLISECONDS));
			latch.countDown();
			try {
				lock2.unlock();
			}
			catch (Exception e) {
				return e;
			}
			return null;
		});
		assertTrue(latch.await(10, TimeUnit.SECONDS));
		assertFalse(locked.get());
		lock1.unlock();
		Object ise = result.get(10, TimeUnit.SECONDS);
		assertThat(ise, instanceOf(IllegalMonitorStateException.class));
		assertThat(((Exception) ise).getMessage(), containsString("Current thread is not owner of the lock!"));
	}

	@Test
	public void testTwoThreads() throws Exception {
		HazelcastLockRegistry registry = new HazelcastLockRegistry(instance);
		Lock lock1 = registry.obtain("foo");
		AtomicBoolean locked = new AtomicBoolean();
		CountDownLatch latch1 = new CountDownLatch(1);
		CountDownLatch latch2 = new CountDownLatch(1);
		CountDownLatch latch3 = new CountDownLatch(1);
		lock1.lockInterruptibly();
		Executors.newSingleThreadExecutor().execute(() -> {
			Lock lock2 = registry.obtain("foo");
			try {
				latch1.countDown();
				lock2.lockInterruptibly();
				latch2.await(10, TimeUnit.SECONDS);
				locked.set(true);
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			finally {
				lock2.unlock();
				latch3.countDown();
			}
		});
		assertTrue(latch1.await(10, TimeUnit.SECONDS));
		assertFalse(locked.get());
		lock1.unlock();
		latch2.countDown();
		assertTrue(latch3.await(10, TimeUnit.SECONDS));
		assertTrue(locked.get());
	}

	@Test
	public void testTwoThreadsDifferentRegistries() throws Exception {
		HazelcastLockRegistry registry1 = new HazelcastLockRegistry(instance);
		HazelcastLockRegistry registry2 = new HazelcastLockRegistry(instance);
		Lock lock1 = registry1.obtain("foo");
		AtomicBoolean locked = new AtomicBoolean();
		CountDownLatch latch1 = new CountDownLatch(1);
		CountDownLatch latch2 = new CountDownLatch(1);
		CountDownLatch latch3 = new CountDownLatch(1);
		lock1.lockInterruptibly();
		Executors.newSingleThreadExecutor().execute(() -> {
			Lock lock2 = registry2.obtain("foo");
			try {
				latch1.countDown();
				lock2.lockInterruptibly();
				latch2.await(10, TimeUnit.SECONDS);
				locked.set(true);
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			finally {
				lock2.unlock();
				latch3.countDown();
			}
		});
		assertTrue(latch1.await(10, TimeUnit.SECONDS));
		assertFalse(locked.get());
		lock1.unlock();
		latch2.countDown();
		assertTrue(latch3.await(10, TimeUnit.SECONDS));
		assertTrue(locked.get());
	}

	@Test
	public void testTwoThreadsWrongOneUnlocks() throws Exception {
		HazelcastLockRegistry registry = new HazelcastLockRegistry(instance);
		final Lock lock = registry.obtain("foo");
		lock.lockInterruptibly();
		final AtomicBoolean locked = new AtomicBoolean();
		final CountDownLatch latch = new CountDownLatch(1);
		Future<Object> result = Executors.newSingleThreadExecutor().submit(() -> {
			try {
				lock.unlock();
			}
			catch (Exception e) {
				latch.countDown();
				return e;
			}
			return null;
		});
		assertTrue(latch.await(10, TimeUnit.SECONDS));
		assertFalse(locked.get());
		lock.unlock();
		Object imse = result.get(10, TimeUnit.SECONDS);
		assertThat(imse, instanceOf(IllegalMonitorStateException.class));
		assertThat(((Exception) imse).getMessage(), containsString("Current thread is not owner of the lock!"));
	}

	@Test
	public void testTryLock() throws Exception {
		HazelcastLockRegistry registry = new HazelcastLockRegistry(instance);
		for (int i = 0; i < 10; i++) {
			Lock lock = registry.obtain("foo");

			int n = 0;
			while (!lock.tryLock() && n++ < 100) {
				Thread.sleep(100);
			}
			assertThat(n, lessThan(100));

			lock.unlock();
		}
	}

}
