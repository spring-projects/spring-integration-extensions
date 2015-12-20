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

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.thrift.transport.TTransportException;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.integration.cassandra.test.domain.Book;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;

/**
 * @author Filippo Balicchia
 */

public class CassandraOutboundChannelAdapterIntegrationTests {

	protected static final String CASSANDRA_CONFIG = "spring-cassandra.yaml";
	

	@BeforeClass
	public static void startCassandra() throws TTransportException, IOException,
			InterruptedException, ConfigurationException {

		EmbeddedCassandraServerHelper.startEmbeddedCassandra(CASSANDRA_CONFIG,
				"build/embeddedCassandra");
		
	}

	@AfterClass
	public static void cleanup() {
		EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();
	}

	@Test
	public void testBasicCassandraInsert() throws Exception {
		Book b1 = new Book();
		b1.setIsbn("123456-1");
		b1.setTitle("Spring Integration Cassandra");
		b1.setAuthor("Cassandra Guru");
		b1.setPages(521);
		b1.setSaleDate(new Date());
		b1.setInStock(true);

		Message<Book> message = MessageBuilder.withPayload(b1).build();

		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				"outbound-adapter-parser-config.xml", this.getClass());
		DirectChannel channel = context.getBean("outbound1",DirectChannel.class);
		CassandraTemplate template = context.getBean("cassandraTemplate",CassandraTemplate.class);
		channel.send(message);
		Select select = QueryBuilder.select().all().from("book");
		List<Book> books = template.select(select, Book.class);
		assertEquals(1, books.size());
		template.delete(b1);
	}

}
