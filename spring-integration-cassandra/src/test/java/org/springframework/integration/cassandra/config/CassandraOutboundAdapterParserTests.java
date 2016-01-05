/*
 * Copyright 2015 the original author or authors.
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

package org.springframework.integration.cassandra.config;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.cassandra.outbound.CassandraMessageHandler;
import org.springframework.integration.test.util.TestUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Filippo Balicchia
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class CassandraOutboundAdapterParserTests {

	@Autowired
	private ApplicationContext context;

	@Test
	public void minimalConfig() {

		CassandraMessageHandler<?> handler = TestUtils.getPropertyValue(context.getBean("outbound1.adapter"), "handler",
				CassandraMessageHandler.class);

		assertEquals("outbound1.adapter", TestUtils.getPropertyValue(handler, "componentName"));
		assertEquals(CassandraMessageHandler.Type.INSERT, TestUtils.getPropertyValue(handler, "mode"));
		assertEquals(context.getBean("cassandraTemplate"), TestUtils.getPropertyValue(handler, "cassandraTemplate"));
		assertEquals(context.getBean("writeOptions"), TestUtils.getPropertyValue(handler, "writeOptions"));
	}

	@Test
	public void ingestConfig() {
		CassandraMessageHandler<?> handler = TestUtils.getPropertyValue(context.getBean("outbound2"), "handler",
				CassandraMessageHandler.class);

		assertEquals("insert into book (isbn, title, author, pages, saleDate, isInStock) values (?, ?, ?, ?, ?, ?)",
				TestUtils.getPropertyValue(handler, "ingestQuery"));
		assertEquals(Boolean.FALSE, TestUtils.getPropertyValue(handler, "producesReply"));
	}

	@Test
	public void fullConfig() {
		CassandraMessageHandler<?> handler = TestUtils.getPropertyValue(context.getBean("outgateway"), "handler",
				CassandraMessageHandler.class);

		assertEquals(Boolean.TRUE, TestUtils.getPropertyValue(handler, "producesReply"));
		assertEquals(CassandraMessageHandler.Type.STATEMENT, TestUtils.getPropertyValue(handler, "mode"));
		assertEquals(context.getBean("writeOptions"), TestUtils.getPropertyValue(handler, "writeOptions"));
	}

	@Test
	public void statementConfig() {

		CassandraMessageHandler<?> handler = TestUtils.getPropertyValue(context.getBean("outbound4.adapter"), "handler",
				CassandraMessageHandler.class);
		assertEquals("outbound4.adapter", TestUtils.getPropertyValue(handler, "componentName"));
		assertEquals(CassandraMessageHandler.Type.STATEMENT, TestUtils.getPropertyValue(handler, "mode"));
		assertEquals(context.getBean("cassandraTemplate"), TestUtils.getPropertyValue(handler, "cassandraTemplate"));
		assertEquals(context.getBean("writeOptions"), TestUtils.getPropertyValue(handler, "writeOptions"));

	}

}
