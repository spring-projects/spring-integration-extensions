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

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cassandra.core.ConsistencyLevel;
import org.springframework.cassandra.core.RetryPolicy;
import org.springframework.cassandra.core.WriteOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.integration.cassandra.config.IntegrationTestConfig;
import org.springframework.integration.cassandra.test.domain.Book;
import org.springframework.integration.channel.NullChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.PollableChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.thrift.transport.TTransportException;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;

/**
 * @author Soby Chacko
 * @author Artem Bilan
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
public class CassandraMessageHandlerTests {

	private static final SpelExpressionParser PARSER = new SpelExpressionParser();

	@Configuration
	@EnableIntegration
	public static class Config extends IntegrationTestConfig {

		@Autowired
		public CassandraOperations template;

		@Override
		public String[] getEntityBasePackages() {
			return new String[]{Book.class.getPackage().getName()};
		}

		@Bean(name = "sync")
		public MessageHandler cassandraMessageHandlerSync() {
			CassandraMessageHandler<Book> cassandraMessageHandler = new CassandraMessageHandler<Book>(template);
			cassandraMessageHandler.setProducesReply(false);
			cassandraMessageHandler.setAsync(false);
			return cassandraMessageHandler;
		}

		@Bean
		public PollableChannel messageChannel() {
			return new NullChannel();
		}

		@Bean(name = "async")
		public MessageHandler cassandraMessageHandlerAsync() {
			CassandraMessageHandler<Book> cassandraMessageHandler = new CassandraMessageHandler<Book>(template);

			WriteOptions options = new WriteOptions();
			options.setTtl(60);
			options.setConsistencyLevel(ConsistencyLevel.ONE);
			options.setRetryPolicy(RetryPolicy.DOWNGRADING_CONSISTENCY);

			cassandraMessageHandler.setWriteOptions(options);

			cassandraMessageHandler.setOutputChannel(messageChannel());

			return cassandraMessageHandler;
		}

		@Bean(name = "ingest")
		public MessageHandler cassandraMessageHandlerIngest() {
			CassandraMessageHandler<Book> cassandraMessageHandler = new CassandraMessageHandler<Book>(template);
			String cqlIngest = "insert into book (isbn, title, author, pages, saleDate, isInStock) values (?, ?, ?, ?, ?, ?)";
			cassandraMessageHandler.setCqlIngest(cqlIngest);
			return cassandraMessageHandler;
		}

		@Bean
		public PollableChannel resultChannel() {
			return new QueueChannel();
		}


		@Bean(name = "select")
		public MessageHandler cassandraMessageHandlerSelect() {
			CassandraMessageHandler<Book> cassandraMessageHandler = new CassandraMessageHandler<Book>(template);
			Expression query = PARSER.parseExpression("T(QueryBuilder).select().all().from('book').limit(payload)");
			cassandraMessageHandler.setStatementExpression(query);
			cassandraMessageHandler.setOutputChannel(resultChannel());
			cassandraMessageHandler.setProducesReply(true);
			return cassandraMessageHandler;
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
	@Qualifier("select")
	public MessageHandler messageHandlerSelect;

	@Autowired
	public CassandraOperations template;

	@Autowired
	public PollableChannel resultChannel;

	protected static final String CASSANDRA_CONFIG = "spring-cassandra.yaml";

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
		EmbeddedCassandraServerHelper.startEmbeddedCassandra(CASSANDRA_CONFIG, "build/embeddedCassandra");
		cluster = Cluster.builder()
				.addContactPoint(IntegrationTestConfig.HOST)
				.withPort(IntegrationTestConfig.PORT)
				.build();
		system = cluster.connect();
	}

	@AfterClass
	public static void cleanup() {
		cluster.close();
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
		messageHandlerSync.handleMessage(message);

		int n = 0;
		Select select = QueryBuilder.select().all().from("book");
		List<Book> books;
		while ((books = template.select(select, Book.class)).isEmpty() && n++ < 10) {
			Thread.sleep(100);
		}
		assertTrue(n < 10);
		assertEquals(1, books.size());

		template.delete(b1);
	}

	@Test
	public void testCassandraBatchInsertAndSelectStatement() throws Exception {
		List<Book> books = getBookList(5);
		Message<List<Book>> message = MessageBuilder.withPayload(books).build();
		this.messageHandlerAsync.handleMessage(message);

		int n = 0;
		Select select = QueryBuilder.select().all().from("book");
		while ((books = this.template.select(select, Book.class)).isEmpty() && n++ < 10) {
			Thread.sleep(100);
		}
		assertTrue(n < 10);
		assertEquals(5, books.size());

		this.messageHandlerSelect.handleMessage(new GenericMessage<>(2));

		Message<?> receive = this.resultChannel.receive(10000);
		assertNotNull(receive);
		assertThat(receive.getPayload(), instanceOf(ResultSetFuture.class));
		ResultSetFuture result = (ResultSetFuture) receive.getPayload();
		ResultSet resultSet = result.get(10, TimeUnit.SECONDS);
		assertNotNull(resultSet);
		List<Row> rows = resultSet.all();
		assertEquals(2, rows.size());

		this.messageHandlerSync.handleMessage(new GenericMessage<>(QueryBuilder.truncate("book")));
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

		int n = 0;
		Select select = QueryBuilder.select().all().from("book");
		while ((books = template.select(select, Book.class)).isEmpty() && n++ < 10) {
			Thread.sleep(100);
		}
		assertTrue(n < 10);
		assertEquals(5, books.size());

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
