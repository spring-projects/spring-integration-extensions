/*
 * Copyright 2015-2019 the original author or authors.
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.DirectFieldAccessor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.cassandra.ReactiveResultSet;
import org.springframework.data.cassandra.ReactiveSession;
import org.springframework.data.cassandra.core.InsertOptions;
import org.springframework.data.cassandra.core.ReactiveCassandraOperations;
import org.springframework.data.cassandra.core.UpdateOptions;
import org.springframework.data.cassandra.core.WriteResult;
import org.springframework.data.cassandra.core.cql.QueryOptionsUtil;
import org.springframework.data.cassandra.core.cql.ReactiveSessionCallback;
import org.springframework.data.cassandra.core.cql.WriteOptions;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.TypeLocator;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.expression.spel.support.StandardTypeLocator;
import org.springframework.integration.expression.ExpressionEvalMap;
import org.springframework.integration.expression.ExpressionUtils;
import org.springframework.integration.handler.AbstractReplyProducingMessageHandler;
import org.springframework.integration.handler.ExpressionEvaluatingMessageProcessor;
import org.springframework.integration.handler.MessageProcessor;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.exceptions.DriverException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * An {@link AbstractReplyProducingMessageHandler} implementation for Cassandra outbound operations.
 *
 * @author Soby Chacko
 * @author Artem Bilan
 * @author Filippo Balicchia
 */
public class CassandraMessageHandler extends AbstractReplyProducingMessageHandler {

	private final Map<String, Expression> parameterExpressions = new HashMap<>();

	private Type mode;

	private final ReactiveCassandraOperations cassandraOperations;

	private boolean producesReply;

	/**
	 * Prepared statement to use in association with high throughput ingestion.
	 */
	private String ingestQuery;

	/**
	 * Various options that can be used for Cassandra writes.
	 */
	private WriteOptions writeOptions = WriteOptions.empty();

	private ReactiveSessionMessageCallback sessionMessageCallback;

	private EvaluationContext evaluationContext;

	public CassandraMessageHandler(ReactiveCassandraOperations cassandraOperations) {
		this(cassandraOperations, Type.INSERT);
	}

	public CassandraMessageHandler(ReactiveCassandraOperations cassandraOperations,
			CassandraMessageHandler.Type queryType) {

		Assert.notNull(cassandraOperations, "'cassandraOperations' must not be null.");
		Assert.notNull(queryType, "'queryType' must not be null.");
		this.cassandraOperations = cassandraOperations;
		this.mode = queryType;
		setAsync(true);
		switch (this.mode) {

			case INSERT:
				this.writeOptions = InsertOptions.empty();
				break;
			case UPDATE:
				this.writeOptions = UpdateOptions.empty();
				break;
		}
	}

	public void setIngestQuery(String ingestQuery) {
		Assert.hasText(ingestQuery, "'ingestQuery' must not be empty");
		this.ingestQuery = ingestQuery;
		this.mode = Type.INSERT;
	}

	public void setWriteOptions(WriteOptions writeOptions) {
		Assert.notNull(writeOptions, "'writeOptions' must not be null");
		this.writeOptions = writeOptions;
	}

	public void setProducesReply(boolean producesReply) {
		this.producesReply = producesReply;
	}

	public void setStatementExpressionString(String statementExpression) {
		setStatementExpression(EXPRESSION_PARSER.parseExpression(statementExpression));
	}

	public void setStatementExpression(Expression statementExpression) {
		setStatementProcessor(
				new ExpressionEvaluatingMessageProcessor<Statement>(statementExpression, Statement.class) {

					@Override
					protected StandardEvaluationContext getEvaluationContext() {
						return (StandardEvaluationContext) CassandraMessageHandler.this.evaluationContext;
					}

				});
	}

	public void setQuery(String query) {
		Assert.hasText(query, "'query' must not be empty");
		this.sessionMessageCallback =
				(session, requestMessage) ->
						session.execute(query,
								ExpressionEvalMap.from(this.parameterExpressions)
										.usingEvaluationContext(this.evaluationContext)
										.withRoot(requestMessage)
										.build());
		this.mode = Type.STATEMENT;
	}

	public void setParameterExpressions(Map<String, Expression> parameterExpressions) {
		Assert.notEmpty(parameterExpressions, "'parameterExpressions' must not be empty.");
		this.parameterExpressions.clear();
		this.parameterExpressions.putAll(parameterExpressions);
	}

	public void setStatementProcessor(MessageProcessor<Statement> statementProcessor) {
		Assert.notNull(statementProcessor, "'statementProcessor' must not be null.");
		this.sessionMessageCallback =
				(session, requestMessage) ->
						session.execute(
								QueryOptionsUtil.addQueryOptions(statementProcessor.processMessage(requestMessage),
										this.writeOptions));
		this.mode = Type.STATEMENT;
	}

	@Override
	public String getComponentType() {
		return "cassandra:outbound-" + (this.producesReply ? "gateway" : "channel-adapter");
	}

	@Override
	protected void doInit() {
		super.doInit();

		this.evaluationContext = ExpressionUtils.createStandardEvaluationContext(getBeanFactory());
		TypeLocator typeLocator = this.evaluationContext.getTypeLocator();
		if (typeLocator instanceof StandardTypeLocator) {
			/*
			 * Register the Cassandra Query DSL package so they don't need a FQCN for QueryBuilder, for example.
			 */
			((StandardTypeLocator) typeLocator).registerImport("com.datastax.driver.core.querybuilder");
		}
	}

	@Override
	protected Object handleRequestMessage(Message<?> requestMessage) {
		Object payload = requestMessage.getPayload();

		Mono<? extends WriteResult> result = null;

		Type mode = this.mode;

		if (payload instanceof Statement) {
			mode = Type.STATEMENT;
		}

		switch (mode) {
			case INSERT:
				result = handleInsert(payload);
				break;
			case UPDATE:
				result = handleUpdate(payload);
				break;
			case DELETE:
				result = handleDelete(payload);
				break;
			case STATEMENT:
				result = handleStatement(requestMessage);
				break;
		}

		if (this.producesReply) {
			return isAsync() ? result : result.block();
		}
		else {
			if (isAsync()) {
				result.subscribe();
			}
			else {
				result.block();
			}
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private Mono<? extends WriteResult> handleInsert(Object payload) {
		if (this.ingestQuery != null) {
			Assert.isInstanceOf(List.class, payload,
					"to perform 'ingest' the 'payload' must be of 'List<List<?>>' type.");
			List<?> list = (List<?>) payload;
			for (Object o : list) {
				Assert.isInstanceOf(List.class, o,
						"to perform 'ingest' the 'payload' must be of 'List<List<?>>' type.");
			}
			List<List<?>> rows = (List<List<?>>) payload;
			return this.cassandraOperations.getReactiveCqlOperations()
					.execute((ReactiveSessionCallback<WriteResult>) session ->
							session.prepare(this.ingestQuery)
									.map(s -> QueryOptionsUtil.addPreparedStatementOptions(s, this.writeOptions))
									.flatMapMany(s ->
											Flux.fromIterable(rows)
													.map(row -> s.bind(row.toArray())))
									.collect(BatchStatement::new, BatchStatement::add)
									.flatMap(session::execute)
									.transform(this::transformToWriteResult))
					.next();
		}
		else {
			if (payload instanceof List) {
				return this.cassandraOperations.batchOps()
						.insert((List<?>) payload, this.writeOptions)
						.execute();
			}
			else {
				return this.cassandraOperations.insert(payload, (InsertOptions) this.writeOptions);
			}
		}
	}

	private Mono<? extends WriteResult> handleUpdate(Object payload) {
		if (payload instanceof List) {
			return this.cassandraOperations.batchOps()
					.update((List<?>) payload, this.writeOptions)
					.execute();
		}
		else {
			return this.cassandraOperations.update(payload, (UpdateOptions) this.writeOptions);
		}
	}

	private Mono<WriteResult> handleDelete(Object payload) {
		if (payload instanceof List) {
			return this.cassandraOperations.batchOps()
					.delete((List<?>) payload)
					.execute();
		}
		else {
			return this.cassandraOperations.delete(payload, this.writeOptions);
		}
	}

	private Mono<WriteResult> handleStatement(Message<?> requestMessage) {
		Object payload = requestMessage.getPayload();
		Mono<ReactiveResultSet> resultSetMono;
		if (payload instanceof Statement) {
			resultSetMono = this.cassandraOperations.getReactiveCqlOperations().queryForResultSet((Statement) payload);
		}
		else {
			resultSetMono = this.cassandraOperations.getReactiveCqlOperations()
					.execute((ReactiveSessionCallback<ReactiveResultSet>) session ->
							this.sessionMessageCallback.doInSession(session, requestMessage))
					.next();
		}

		return resultSetMono.transform(this::transformToWriteResult);
	}

	private Mono<WriteResult> transformToWriteResult(Mono<ReactiveResultSet> resultSetMono) {
		return resultSetMono
				.map(DirectFieldAccessor::new)
				.map(accessor -> accessor.getPropertyValue("resultSet"))
				.cast(ResultSet.class)
				.map(WriteResult::of);
	}

	/**
	 * The mode for the {@link CassandraMessageHandler}.
	 */
	public enum Type {

		/**
		 * Set a {@link CassandraMessageHandler} into an {@code insert} mode.
		 */
		INSERT,

		/**
		 * Set a {@link CassandraMessageHandler} into an {@code update} mode.
		 */
		UPDATE,

		/**
		 * Set a {@link CassandraMessageHandler} into a {@code delete} mode.
		 */
		DELETE,

		/**
		 * Set a {@link CassandraMessageHandler} into a {@code statement} mode.
		 */
		STATEMENT;

	}

	@FunctionalInterface
	private interface ReactiveSessionMessageCallback {

		Mono<ReactiveResultSet> doInSession(ReactiveSession session, Message<?> requestMessage)
				throws DriverException, DataAccessException;

	}

}
