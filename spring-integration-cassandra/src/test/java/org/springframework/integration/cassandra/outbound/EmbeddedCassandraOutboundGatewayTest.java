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
package org.springframework.integration.cassandra.outbound;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.thrift.transport.TTransportException;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cassandra.core.ConsistencyLevel;
import org.springframework.cassandra.core.RetryPolicy;
import org.springframework.cassandra.core.WriteOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.integration.cassandra.config.IntegrationTestConfig;
import org.springframework.integration.cassandra.test.domain.Book;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

/**
 * @author Soby Chacko
 */

@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
public class EmbeddedCassandraOutboundGatewayTest {

	@Configuration
	public static class Config extends IntegrationTestConfig {

		@Autowired
		public CassandraOperations template;

		@Override
		public String[] getEntityBasePackages() {
			return new String[]{Book.class.getPackage().getName()};
		}

		@Bean(name = "sync")
		public MessageHandler cassandraOutboundGatewaySync() {
			CassandraOutboundGateway<Book> cassandraOutboundGateway = new CassandraOutboundGateway<Book>(template);
			cassandraOutboundGateway.setProducesReply(false);
			return cassandraOutboundGateway;
		}

		@Bean
		SubscribableChannel messageChannel() {
			return new DirectChannel();
		}

		@Bean(name = "async")
		public MessageHandler cassandraOutboundGatewayAsync() {
			CassandraOutboundGateway<Book> cassandraOutboundGateway = new CassandraOutboundGateway<Book>(template);
			cassandraOutboundGateway.setAsync(true);

			WriteOptions options = new WriteOptions();
			options.setTtl(60);
			options.setConsistencyLevel(ConsistencyLevel.ONE);
			options.setRetryPolicy(RetryPolicy.DOWNGRADING_CONSISTENCY);

			cassandraOutboundGateway.setWriteOptions(options);

			cassandraOutboundGateway.setOutputChannel(messageChannel());

			return cassandraOutboundGateway;
		}

		@Bean(name = "ingest")
		public MessageHandler cassandraOutboundGatewayIngest() {
			CassandraOutboundGateway<Book> cassandraOutboundGateway = new CassandraOutboundGateway<Book>(template);
			String cqlIngest = "insert into book (isbn, title, author, pages, saleDate, isInStock) values (?, ?, ?, ?, ?, ?)";
			cassandraOutboundGateway.setCqlIngest(cqlIngest);
			return cassandraOutboundGateway;
		}
	}

	@Autowired
	@Qualifier("sync")
	public MessageHandler messageHandlerSync;

	@Autowired
	@Qualifier("async")
	public MessageHandler messageHandlerAsync;

	@Autowired
	@Qualifier("ingest")
	public MessageHandler messageHandlerIngest;

	@Autowired
	public CassandraOperations template;

	@Autowired
	public SubscribableChannel channel;

	protected static String CASSANDRA_CONFIG = "spring-cassandra.yaml";
	protected static String CASSANDRA_HOST = "localhost";

	/**
	 * The {@link Cluster} that's connected to Cassandra.
	 */
	protected static Cluster cluster;

	/**
	 * The session connected to the system keyspace.
	 */
	protected static Session system;

	@BeforeClass
	public static void startCassandra() throws TTransportException, IOException, InterruptedException,
			ConfigurationException {

		EmbeddedCassandraServerHelper.startEmbeddedCassandra(CASSANDRA_CONFIG);
		ensureClusterConnection();
	}

	@AfterClass
	public static void cleanup() {
		cluster.close();
		EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();
	}

	@Before
	public void setup() {
		channel.subscribe(new MessageHandler() {
			@Override
			public void handleMessage(Message<?> message) throws MessagingException {
				System.out.println("Foo:" + message);
			}
		});
	}

	public static Cluster cluster() {
		return Cluster.builder().addContactPoint(CASSANDRA_HOST).withPort(9043).build();
	}


	public static void ensureClusterConnection() {
		// check cluster
		if (cluster == null) {
			cluster = cluster();
		}

		if (system == null) {
			system = cluster.connect();
		}
	}

	@Test
	public void testBasicCassandraInsert() throws Exception {
		assertEquals(1, 1);

		Book b1 = new Book();
		b1.setIsbn("123456-1");
		b1.setTitle("Spring Integration Cassandra");
		b1.setAuthor("Cassandra Guru");
		b1.setPages(521);
		b1.setSaleDate(new Date());
		b1.setInStock(true);

		Message<Book> message = MessageBuilder.withPayload(b1).build();
		messageHandlerSync.handleMessage(message);
		Thread.sleep(1000); //TODO: convert to a latch

		ResultSet query = template.query("select * from book;");
		assertEquals(query.all().size(), 1);
		for (Row r : query) {
			System.out.println("Foo: " + r.toString());
		}
		template.delete(b1);
	}

	@Test
	public void testCassandraBatchInsert() throws Exception {
		List<Book> books = getBookList(5);
		Message<List<Book>> message = MessageBuilder.withPayload(books).build();
		messageHandlerAsync.handleMessage(message);
		Thread.sleep(1000); //TODO: convert to a latch
		ResultSet query = template.query("select * from book;");
		assertEquals(query.all().size(), 5);
		for (Row r : query) {
			System.out.println("Foo: " + r.toString());
		}


		template.delete(books);
	}

	@Test
	public void testCassandraBatchIngest() throws Exception {
		List<Book> books = getBookList(5);
		List<List<?>> ingestBooks = new ArrayList<List<?>>();
		for (Book b : books) {

			List<Object> l = new ArrayList<Object>();
			l.add(b.getIsbn());
			l.add(b.getTitle());
			l.add(b.getAuthor());
			l.add(b.getPages());
			l.add(b.getSaleDate());
			l.add(b.isInStock());
			ingestBooks.add(l);
		}

		Message<List<List<?>>> message = MessageBuilder.withPayload(ingestBooks).build();
		messageHandlerIngest.handleMessage(message);
		Thread.sleep(1000); //TODO: convert to a latch
		ResultSet query = template.query("select * from book;");
		assertEquals(query.all().size(), 5);
		for (Row r : query) {
			System.out.println("Foo: " + r.toString());
		}
		template.delete(books);
	}

	private List<Book> getBookList(int numBooks) {

		List<Book> books = new ArrayList<Book>();

		Book b;
		for (int i = 0; i < numBooks; i++) {
			b = new Book();
			b.setIsbn(UUID.randomUUID().toString());
			b.setTitle("Spring Data Cassandra Guide");
			b.setAuthor("Cassandra Guru");
			b.setPages(i * 10 + 5);
			b.setInStock(true);
			b.setSaleDate(new Date());
			books.add(b);
		}

		return books;
	}
}