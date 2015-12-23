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

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;


import java.io.IOException;
import java.util.ArrayList;
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
import org.springframework.integration.cassandra.test.utils.SICassandraTestUtils;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.PollableChannel;
import org.springframework.messaging.support.GenericMessage;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;



/**
 * @author Filippo Balicchia
 */

public class CassandraOutboundChannelAdapterIntegrationTests {

	protected static final String CASSANDRA_CONFIG = "spring-cassandra.yaml";
	
	private static ClassPathXmlApplicationContext context;

	private static CassandraTemplate template;
	
	private static DirectChannel cassandraMessageHandler1;
	
	private static DirectChannel cassandraMessageHandler2;
	
	private static DirectChannel cassandraMessageHandler3;
	
	private static DirectChannel cassandraMessageHandler4;
	
	
	private static PollableChannel resultChannel;
	
	
	@BeforeClass
	public static void init()throws TTransportException, IOException,
	InterruptedException, ConfigurationException 
	{
		startCassandra();
		context = new ClassPathXmlApplicationContext("outbound-adapter-parser-config.xml", CassandraOutboundChannelAdapterIntegrationTests.class);
		template = context.getBean("cassandraTemplate",CassandraTemplate.class);
		resultChannel = context.getBean("resultChannel",PollableChannel.class);
		cassandraMessageHandler1 = context.getBean("cassandraMessageHandler1",DirectChannel.class);
		cassandraMessageHandler2 = context.getBean("cassandraMessageHandler2",DirectChannel.class);
		cassandraMessageHandler3 = context.getBean("cassandraMessageHandler3",DirectChannel.class);
		cassandraMessageHandler4 = context.getBean("cassandraMessageHandler4",DirectChannel.class);
	}
	
	private static void startCassandra() throws TTransportException, IOException,
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
		cassandraMessageHandler1.send(message);
		Select select = QueryBuilder.select().all().from("book");
		List<Book> books = template.select(select, Book.class);
		assertEquals(1, books.size());
		template.delete(b1);
	}

	@Test
	public void testCassandraBatchInsertAndSelectStatement() throws Exception {
		List<Book> books = SICassandraTestUtils.getBookList(5);
		cassandraMessageHandler2.send(new GenericMessage<>(books));

		Message<?> message = MessageBuilder.withPayload("Cassandra Puppy Guru").setHeader("limit", 2).build();
	    cassandraMessageHandler4.send(message);
	    Message<?> receive =  resultChannel.receive(10000);
	    assertNotNull(receive);
	    assertThat(receive.getPayload(), instanceOf(ResultSet.class));
	    ResultSet resultSet = (ResultSet) receive.getPayload();
	    assertNotNull(resultSet);
	    List<Row> rows = resultSet.all();
	    assertEquals(2, rows.size());
	    cassandraMessageHandler1.send(new GenericMessage<>(QueryBuilder.truncate("book")));

	}
	
	   @Test
	    public void testCassandraBatchIngest() throws Exception {
	        List<Book> books = SICassandraTestUtils.getBookList(5);
	        List<List<?>> ingestBooks = new ArrayList<>();
	        for (Book b : books) {

	            List<Object> l = new ArrayList<>();
	            l.add(b.getIsbn());
	            l.add(b.getTitle());
	            l.add(b.getAuthor());
	            l.add(b.getPages());
	            l.add(b.getSaleDate());
	            l.add(b.isInStock());
	            ingestBooks.add(l);
	        }

	        Message<List<List<?>>> message = MessageBuilder.withPayload(ingestBooks).build();
	        cassandraMessageHandler3.send(message);
	        Select select = QueryBuilder.select().all().from("book");
	        books = template.select(select, Book.class);
	        assertEquals(5, books.size());
	        template.delete(books);
	    }
	
	
	
	

}
