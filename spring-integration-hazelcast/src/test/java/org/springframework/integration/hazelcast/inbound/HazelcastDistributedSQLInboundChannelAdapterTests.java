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

import java.util.Collection;
import java.util.Map;

import javax.annotation.Resource;

import com.hazelcast.core.IMap;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.PollableChannel;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Hazelcast Distributed SQL Inbound Channel Adapter Test
 * 
 * @author Eren Avsarogullari
 * @since 1.0.0
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/HazelcastDistributedSQLInboundChannelAdapterTests-context.xml" })
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class HazelcastDistributedSQLInboundChannelAdapterTests {

	@Autowired
	private PollableChannel dsMapChannel1;

	@Autowired
	private PollableChannel dsMapChannel2;

	@Autowired
	private PollableChannel dsMapChannel3;

	@Autowired
	private PollableChannel dsMapChannel4;

	@Resource
	private IMap<Integer, User> dsDistributedMap1;

	@Resource
	private IMap<Integer, User> dsDistributedMap2;

	@Resource
	private IMap<Integer, User> dsDistributedMap3;

	@Resource
	private IMap<Integer, User> dsDistributedMap4;

	@Test
	public void testDistributedSQLForOnlyENTRYIterationType() throws InterruptedException {
		dsDistributedMap1.put(1, new User(1, "TestName1", "TestSurname1", 10));
		dsDistributedMap1.put(2, new User(2, "TestName2", "TestSurname2", 20));
		dsDistributedMap1.put(3, new User(3, "TestName3", "TestSurname3", 30));
		dsDistributedMap1.put(4, new User(4, "TestName4", "TestSurname4", 40));
		dsDistributedMap1.put(5, new User(5, "TestName5", "TestSurname5", 50));

		Message<?> msg = null;
		for (int i = 0; i < 10; i++) {
			msg = dsMapChannel1.receive();
			if (((Collection) msg.getPayload()).iterator().hasNext()) {
				break;
			}
			Thread.sleep(1_000);
		}

		Assert.assertNotNull(msg);
		Assert.assertNotNull(msg.getPayload());
		Assert.assertTrue(msg.getPayload() instanceof Collection);
		Assert.assertEquals(4, (((Map.Entry) ((Collection) msg.getPayload()).iterator()
				.next()).getKey()));
		Assert.assertEquals(4, ((User) ((Map.Entry) ((Collection) msg.getPayload())
				.iterator().next()).getValue()).getId());
		Assert.assertEquals("TestName4", ((User) ((Map.Entry) ((Collection) msg
				.getPayload()).iterator().next()).getValue()).getName());
		Assert.assertEquals("TestSurname4", ((User) ((Map.Entry) ((Collection) msg
				.getPayload()).iterator().next()).getValue()).getSurname());
	}

	@Test
	public void testDistributedSQLForOnlyKEYIterationType() throws InterruptedException {
		dsDistributedMap2.put(1, new User(1, "TestName1", "TestSurname1", 10));
		dsDistributedMap2.put(2, new User(2, "TestName2", "TestSurname2", 20));
		dsDistributedMap2.put(3, new User(3, "TestName3", "TestSurname3", 30));

		Message<?> msg = null;
		for (int i = 0; i < 10; i++) {
			msg = dsMapChannel2.receive();
			if (((Collection) msg.getPayload()).iterator().hasNext()) {
				break;
			}
			Thread.sleep(1_000);
		}

		Assert.assertNotNull(msg);
		Assert.assertNotNull(msg.getPayload());
		Assert.assertTrue(msg.getPayload() instanceof Collection);
		Assert.assertEquals(1, ((Collection) msg.getPayload()).iterator().next());
	}

	@Test
	public void testDistributedSQLForOnlyLOCAL_KEYIterationType()
			throws InterruptedException {
		dsDistributedMap3.put(1, new User(1, "TestName1", "TestSurname1", 10));
		dsDistributedMap3.put(2, new User(2, "TestName2", "TestSurname2", 20));
		dsDistributedMap3.put(3, new User(3, "TestName3", "TestSurname3", 30));

		Message<?> msg = null;
		for (int i = 0; i < 10; i++) {
			msg = dsMapChannel3.receive();
			if (((Collection) msg.getPayload()).iterator().hasNext()) {
				break;
			}
			Thread.sleep(1_000);
		}

		Assert.assertNotNull(msg);
		Assert.assertNotNull(msg.getPayload());
		Assert.assertTrue(msg.getPayload() instanceof Collection);
		Assert.assertEquals(2, ((Collection) msg.getPayload()).iterator().next());
	}

	@Test
	public void testDistributedSQLForOnlyVALUEIterationType() throws InterruptedException {
		dsDistributedMap4.put(1, new User(1, "TestName1", "TestSurname1", 10));
		dsDistributedMap4.put(2, new User(2, "TestName2", "TestSurname2", 20));
		dsDistributedMap4.put(3, new User(3, "TestName3", "TestSurname3", 30));
		dsDistributedMap4.put(4, new User(4, "TestName4", "TestSurname4", 40));
		dsDistributedMap4.put(5, new User(5, "TestName5", "TestSurname5", 50));

		Message<?> msg = null;
		for (int i = 0; i < 10; i++) {
			msg = dsMapChannel4.receive();
			if (((Collection) msg.getPayload()).iterator().hasNext()) {
				break;
			}
			Thread.sleep(1_000);
		}

		Assert.assertNotNull(msg);
		Assert.assertNotNull(msg.getPayload());
		Assert.assertTrue(msg.getPayload() instanceof Collection);
		Assert.assertEquals(3,
				((User) (((Collection) msg.getPayload()).iterator().next())).getId());
		Assert.assertEquals("TestName3",
				((User) (((Collection) msg.getPayload()).iterator().next())).getName());
		Assert.assertEquals("TestSurname3",
				((User) (((Collection) msg.getPayload()).iterator().next())).getSurname());
	}
}
