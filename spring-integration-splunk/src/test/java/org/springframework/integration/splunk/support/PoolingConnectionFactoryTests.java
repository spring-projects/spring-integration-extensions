/*
 * Copyright 2011-2012 the original author or authors.
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
package org.springframework.integration.splunk.support;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.springframework.integration.splunk.core.Connection;
import org.springframework.integration.splunk.core.ConnectionFactory;

/**
 * @author Jarred Li
 * @since 1.0
 *
 */
public class PoolingConnectionFactoryTests {

	private ConnectionFactory<TestEntity> conFactory;

	private PoolingConnectionFactory<TestEntity> poolConFactory;

	@SuppressWarnings("unchecked")
	@Before
	public void before() {
		conFactory = mock(ConnectionFactory.class);
		poolConFactory = new PoolingConnectionFactory<TestEntity>(conFactory);
	}


	/**
	 * Test method for {@link org.springframework.integration.splunk.support.PoolingConnectionFactory#getConnection()}.
	 * @throws Exception
	 */
	@Test
	public void testGetConnection() throws Exception {
		@SuppressWarnings("unchecked")
		Connection<TestEntity> con = mock(Connection.class);

		when(con.getTarget()).thenReturn(new TestEntity("entity1"));
		when(conFactory.getConnection()).thenReturn(con);
		Connection<TestEntity> returnCon = poolConFactory.getConnection();
		TestEntity obj1 = returnCon.getTarget();
		Assert.assertNotNull(obj1);
		Assert.assertEquals("entity1", obj1.getName());

		when(con.getTarget()).thenReturn(new TestEntity("entity2"));
		when(conFactory.getConnection()).thenReturn(con);
		returnCon = poolConFactory.getConnection();
		TestEntity obj2 = returnCon.getTarget();
		Assert.assertNotNull(obj2);
		Assert.assertEquals("entity2", obj2.getName());

		when(con.getTarget()).thenReturn(new TestEntity("entity3"));
		when(conFactory.getConnection()).thenReturn(con);
		returnCon = poolConFactory.getConnection();
		TestEntity obj3 = returnCon.getTarget();
		Assert.assertNotNull(obj3);
		Assert.assertEquals("entity3", obj3.getName());


	}

	/**
	 * Test method for {@link org.springframework.integration.splunk.support.PoolingConnectionFactory#destroy()}.
	 * @throws Exception
	 */
	@Test(expected = IllegalStateException.class)
	public void testDestroy() throws Exception {
		try {
			poolConFactory.destroy();
		} catch (Exception e) {
			fail("exception when destoying pool connection factory");
		}
		poolConFactory.getConnection();
	}

	public static class TestEntity {
		private String name;

		public TestEntity(String n) {
			this.name = n;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}


}
