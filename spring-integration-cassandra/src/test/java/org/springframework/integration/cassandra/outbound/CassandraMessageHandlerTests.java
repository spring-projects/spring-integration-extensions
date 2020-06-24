/*
 * Copyright 2015-2020 the original author or authors.
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

package org.springframework.integration.cassandra.outbound;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.InsertOptions;
import org.springframework.data.cassandra.core.ReactiveCassandraOperations;
import org.springframework.data.cassandra.core.WriteResult;
import org.springframework.data.cassandra.core.cql.WriteOptions;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.integration.cassandra.config.IntegrationTestConfig;
import org.springframework.integration.cassandra.test.domain.Book;
import org.springframework.integration.cassandra.test.domain.BookSampler;
import org.springframework.integration.channel.FluxMessageChannel;
import org.springframework.integration.channel.NullChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.PollableChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.select.Select;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * @author Soby Chacko
 * @author Artem Bilan
 */
@DisabledOnOs(OS.WINDOWS)
@SpringJUnitConfig
@DirtiesContext
public class CassandraMessageHandlerTests {

	private static final SpelExpressionParser PARSER = new SpelExpressionParser();

	private static final String CASSANDRA_CONFIG = "spring-cassandra.yaml";

	@Autowired
	public MessageHandler cassandraMessageHandler1;

	@Autowired
	public MessageHandler cassandraMessageHandler2;

	@Autowired
	public MessageHandler cassandraMessageHandler3;

	@Autowired
	public MessageHandler cassandraMessageHandler4;

	@Autowired
	public CassandraOperations template;

	@Autowired
	public FluxMessageChannel resultChannel;

	@BeforeAll
	static void startCassandra() throws Exception {
		EmbeddedCassandraServerHelper.startEmbeddedCassandra(CASSANDRA_CONFIG, "build/embeddedCassandra");
		EmbeddedCassandraServerHelper.getSession();
	}

	@AfterAll
	static void cleanup() {
		EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();
	}

	@Test
	void testBasicCassandraInsert() {
		Book b1 = new Book();
		b1.setIsbn("123456-1");
		b1.setTitle("Spring Integration Cassandra");
		b1.setAuthor("Cassandra Guru");
		b1.setPages(521);
		b1.setSaleDate(LocalDate.now());
		b1.setInStock(true);

		Message<Book> message = MessageBuilder.withPayload(b1).build();
		this.cassandraMessageHandler1.handleMessage(message);

		Select select = QueryBuilder.selectFrom("book").all();
		List<Book> books = this.template.select(select.build(), Book.class);
		assertThat(books).hasSize(1);

		this.template.delete(b1);
	}

	@Test
	void testCassandraBatchInsertAndSelectStatement() {
		List<Book> books = BookSampler.getBookList(5);

		this.cassandraMessageHandler2.handleMessage(new GenericMessage<>(books));

		Message<?> message = MessageBuilder.withPayload("Cassandra Guru").setHeader("limit", 2).build();
		this.cassandraMessageHandler4.handleMessage(message);

		Mono<Integer> testMono =
				Mono.from(this.resultChannel)
						.map(Message::getPayload)
						.cast(WriteResult.class)
						.map(r -> r.getRows().size());

		StepVerifier.create(testMono)
				.expectNext(1)
				.expectComplete()
				.verify();

		this.cassandraMessageHandler1.handleMessage(new GenericMessage<>(QueryBuilder.truncate("book").build()));
	}

	@Test
	void testCassandraBatchIngest() {
		List<Book> books = BookSampler.getBookList(5);
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
		this.cassandraMessageHandler3.handleMessage(message);

		Select select = QueryBuilder.selectFrom("book").all();
		books = this.template.select(select.build(), Book.class);
		assertThat(books).hasSize(5);

		this.template.batchOps().delete(books);
	}

	@Configuration
	@EnableIntegration
	public static class Config extends IntegrationTestConfig {

		@Autowired
		public ReactiveCassandraOperations template;

		@Override
		public String[] getEntityBasePackages() {
			return new String[] { Book.class.getPackage().getName() };
		}

		@Bean
		public MessageHandler cassandraMessageHandler1() {
			CassandraMessageHandler cassandraMessageHandler = new CassandraMessageHandler(this.template);
			cassandraMessageHandler.setAsync(false);
			return cassandraMessageHandler;
		}

		@Bean
		public PollableChannel messageChannel() {
			return new NullChannel();
		}

		@Bean
		public MessageHandler cassandraMessageHandler2() {
			CassandraMessageHandler cassandraMessageHandler = new CassandraMessageHandler(this.template);

			WriteOptions options =
					InsertOptions.builder()
							.ttl(60)
							.consistencyLevel(ConsistencyLevel.ONE)
							.build();

			cassandraMessageHandler.setWriteOptions(options);
			cassandraMessageHandler.setOutputChannel(messageChannel());
			cassandraMessageHandler.setAsync(false);
			return cassandraMessageHandler;
		}

		@Bean
		public MessageHandler cassandraMessageHandler3() {
			CassandraMessageHandler cassandraMessageHandler = new CassandraMessageHandler(this.template);
			String cqlIngest =
					"insert into book (isbn, title, author, pages, saleDate, isInStock) values (?, ?, ?, ?, ?, ?)";
			cassandraMessageHandler.setIngestQuery(cqlIngest);
			cassandraMessageHandler.setAsync(false);
			return cassandraMessageHandler;
		}

		@Bean
		public FluxMessageChannel resultChannel() {
			return new FluxMessageChannel();
		}

		@Bean
		public MessageHandler cassandraMessageHandler4() {
			CassandraMessageHandler cassandraMessageHandler = new CassandraMessageHandler(this.template);
			cassandraMessageHandler.setQuery("SELECT * FROM book WHERE author = :author limit :size");

			Map<String, Expression> params = new HashMap<>();
			params.put("author", PARSER.parseExpression("payload"));
			params.put("size", PARSER.parseExpression("headers.limit"));

			cassandraMessageHandler.setParameterExpressions(params);

			cassandraMessageHandler.setOutputChannel(resultChannel());
			cassandraMessageHandler.setProducesReply(true);
			return cassandraMessageHandler;
		}

	}

}
