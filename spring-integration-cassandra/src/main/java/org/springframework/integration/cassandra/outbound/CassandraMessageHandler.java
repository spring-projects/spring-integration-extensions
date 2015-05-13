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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.cassandra.core.CachedPreparedStatementCreator;
import org.springframework.cassandra.core.PreparedStatementCreator;
import org.springframework.cassandra.core.WriteOptions;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.TypeLocator;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.expression.spel.support.StandardTypeLocator;
import org.springframework.integration.expression.IntegrationEvaluationContextAware;
import org.springframework.integration.handler.AbstractReplyProducingMessageHandler;
import org.springframework.integration.handler.ExpressionEvaluatingMessageProcessor;
import org.springframework.integration.handler.MessageProcessor;
import org.springframework.integration.util.AbstractExpressionEvaluator;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;

import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Statement;

/**
 * @author Soby Chacko
 * @author Artem Bilan
 */
@SuppressWarnings("unchecked")
public class CassandraMessageHandler<T> extends AbstractReplyProducingMessageHandler
		implements IntegrationEvaluationContextAware {

	private final Map<String, Expression> parameterExpressions = new HashMap<>();

	private final CassandraOperations cassandraTemplate;

	private Type queryType;

	private boolean producesReply;

	/**
	 * Prepared statement to use in association with high throughput ingestion.
	 */
	private String ingestQuery;

	/**
	 * Various options that can be used for Cassandra writes.
	 */
	private WriteOptions writeOptions;

	private MessageProcessor<Statement> statementProcessor;

	private EvaluationContext evaluationContext;

	public CassandraMessageHandler(CassandraOperations cassandraTemplate) {
		this(cassandraTemplate, Type.INSERT);
	}

	public CassandraMessageHandler(CassandraOperations cassandraTemplate, CassandraMessageHandler.Type queryType) {
		Assert.notNull(cassandraTemplate, "'cassandraTemplate' must not be null.");
		Assert.notNull(queryType, "'queryType' must not be null.");
		this.cassandraTemplate = cassandraTemplate;
		this.queryType = queryType;
	}

	public void setIngestQuery(String ingestQuery) {
		Assert.hasText(ingestQuery, "'ingestQuery' must not be empty");
		this.ingestQuery = ingestQuery;
	}

	public void setWriteOptions(WriteOptions writeOptions) {
		this.writeOptions = writeOptions;
	}

	public void setProducesReply(boolean producesReply) {
		this.producesReply = producesReply;
	}

	public void setStatementExpression(Expression statementExpression) {
		setStatementProcessor(new ExpressionEvaluatingMessageProcessor<Statement>(statementExpression,
				Statement.class) {

			@Override
			protected StandardEvaluationContext getEvaluationContext() {
				return (StandardEvaluationContext) CassandraMessageHandler.this.evaluationContext;
			}

		});
	}

	public void setQuery(String query) {
		Assert.hasText(query, "'query' must not be empty");

		final PreparedStatementCreator statementCreator = new CachedPreparedStatementCreator(query);

		setStatementProcessor(new MessageProcessor<Statement>() {

			@Override
			public Statement processMessage(Message<?> message) {
				PreparedStatement preparedStatement =
						statementCreator.createPreparedStatement(cassandraTemplate.getSession());
				ColumnDefinitions variables = preparedStatement.getVariables();
				List<Object> values = new ArrayList<>(variables.size());
				Map<String, Object> valueMap = new HashMap<>(variables.size());
				for (ColumnDefinitions.Definition definition : variables) {
					String name = definition.getName();
					Object value = valueMap.get(name);
					if (value == null) {
						Expression expression = parameterExpressions.get(name);
						Assert.state(expression != null, "No expression for parameter: " + name);
						value = expression.getValue(evaluationContext, message);
						valueMap.put(name, value);
					}
					values.add(value);
				}
				return preparedStatement.bind(values.toArray());
			}

		});
	}

	public void setParameterExpressions(Map<String, Expression> parameterExpressions) {
		Assert.notEmpty(parameterExpressions, "'parameterExpressions' must not be empty.");
		this.parameterExpressions.clear();
		this.parameterExpressions.putAll(parameterExpressions);
	}

	public void setStatementProcessor(MessageProcessor<Statement> statementProcessor) {
		Assert.notNull(statementProcessor, "'statementProcessor' must not be null.");
		this.statementProcessor = statementProcessor;
		this.queryType = Type.STATEMENT;
	}

	@Override
	public void setIntegrationEvaluationContext(EvaluationContext evaluationContext) {
		TypeLocator typeLocator = evaluationContext.getTypeLocator();
		if (typeLocator instanceof StandardTypeLocator) {
			/*
			 * Register the Cassandra Query DSL package so they don't need a FQCN for QueryBuilder, for example.
			 */
			((StandardTypeLocator) typeLocator).registerImport("com.datastax.driver.core.querybuilder");
		}
		this.evaluationContext = evaluationContext;
	}


	@Override
	public String getComponentType() {
		return "cassandra:outbound-" + (this.producesReply ? "gateway" : "channel-adapter");
	}

	@Override
	protected Object handleRequestMessage(Message<?> requestMessage) {
		Object payload = requestMessage.getPayload();

		Object result = payload;

		Type queryType = this.queryType;

		Statement statement = null;

		if (payload instanceof Statement) {
			statement = (Statement) payload;
			queryType = Type.STATEMENT;
		}

		switch (queryType) {
			case INSERT:
				if (this.ingestQuery != null) {
					Assert.isInstanceOf(List.class, payload,
							"to perform 'ingest' the 'payload' must be of 'List<List<?>>' type.");
					List<?> list = (List<?>) payload;
					for (Object o : list) {
						Assert.isInstanceOf(List.class, o,
								"to perform 'ingest' the 'payload' must be of 'List<List<?>>' type.");
					}
					List<List<?>> rows = (List<List<?>>) payload;
					this.cassandraTemplate.ingest(this.ingestQuery, rows, this.writeOptions);
				}
				else {
					if (payload instanceof List) {
						this.cassandraTemplate.insert((List<T>) payload, this.writeOptions);
					}
					else {
						this.cassandraTemplate.insert(payload, this.writeOptions);
					}
				}
				break;
			case UPDATE:
				if (payload instanceof List) {
					this.cassandraTemplate.update((List<T>) payload, this.writeOptions);
				}
				else {
					this.cassandraTemplate.update(payload, this.writeOptions);
				}
				break;
			case DELETE:
				if (payload instanceof List) {
					this.cassandraTemplate.delete((List<T>) payload, this.writeOptions);
				}
				else {
					this.cassandraTemplate.delete(payload, this.writeOptions);
				}
				break;
			case STATEMENT:
				if (statement == null) {
					statement = this.statementProcessor.processMessage(requestMessage);
				}

				result = this.cassandraTemplate.executeAsynchronously(statement).getUninterruptibly();
				break;
		}

		return this.producesReply ? result : null;
	}

	/**
	 * Always return {@code false} to prevent a {@link com.datastax.driver.core.ResultSet}
	 * draining on iteration.
	 *
	 * @param reply ignored.
	 * @return {@code false}.
	 */
	@Override
	protected boolean shouldSplitOutput(Iterable<?> reply) {
		return false;
	}


	public enum Type {

		INSERT, UPDATE, DELETE, STATEMENT;

	}

}
