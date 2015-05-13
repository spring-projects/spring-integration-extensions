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

import java.util.Collection;
import java.util.List;

import org.springframework.cassandra.core.WriteOptions;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.DeletionListener;
import org.springframework.data.cassandra.core.WriteListener;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.TypeLocator;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.expression.spel.support.StandardTypeLocator;
import org.springframework.integration.expression.IntegrationEvaluationContextAware;
import org.springframework.integration.handler.AbstractReplyProducingMessageHandler;
import org.springframework.integration.handler.ExpressionEvaluatingMessageProcessor;
import org.springframework.integration.handler.MessageProcessor;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;

import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.Select;

/**
 * @author Soby Chacko
 * @author Artem Bilan
 */
@SuppressWarnings("unchecked")
public class CassandraMessageHandler<T> extends AbstractReplyProducingMessageHandler
		implements IntegrationEvaluationContextAware {

	private final CassandraOperations cassandraTemplate;

	private Type queryType;

	private boolean producesReply;

	private QueryListenerAdapter<T> queryListener;

	/**
	 * Prepared statement to use in association with high throughput ingestion.
	 */
	private String cqlIngest;

	/**
	 * Indicates whether the outbound operations need to be async.
	 */
	private boolean async = true;

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

	public void setCqlIngest(String cqlIngest) {
		this.cqlIngest = cqlIngest;
	}

	public void setQueryListener(QueryListener<T> queryListener) {
		this.queryListener = new QueryListenerAdapter<T>(queryListener);
	}

	public void setWriteOptions(WriteOptions writeOptions) {
		this.writeOptions = writeOptions;
	}

	public void setAsync(boolean async) {
		this.async = async;
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

	/* TODO: implement QueryParameterSource to allocate named parameters from query, e.g.
	               SELECT * FROM book WHERE author = :payload.author

	public void setQuery(final String query) {
		Assert.hasText(query, "'query' must not be empty");
		setStatementProcessor(new MessageProcessor<Statement>() {

			@Override
			public Statement processMessage(Message<?> message) {
				return new SimpleStatement(query);
			}

		});
	}*/

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
		return "cassandra:outbound-" + (this.async ? "gateway" : "channel-adapter");
	}

	@Override
	protected Object handleRequestMessage(Message<?> requestMessage) {
		Object payload = requestMessage.getPayload();

		Object result = null;

		Type queryType = this.queryType;

		Statement statement = null;

		if (payload instanceof Statement) {
			statement = (Statement) payload;
			queryType = Type.STATEMENT;
		}

		switch (queryType) {
			case INSERT:
				if (this.cqlIngest != null) {
					Assert.isInstanceOf(List.class, payload,
							"to perform 'ingest' the 'payload' must be of 'List<List<?>>' type.");
					List<?> list = (List<?>) payload;
					for (Object o : list) {
						Assert.isInstanceOf(List.class, o,
								"to perform 'ingest' the 'payload' must be of 'List<List<?>>' type.");
					}
					List<List<?>> rows = (List<List<?>>) payload;
					this.cassandraTemplate.ingest(this.cqlIngest, rows, this.writeOptions);
				}
				else if (this.async) {
					if (payload instanceof List) {
						result = this.cassandraTemplate.insertAsynchronously((List<T>) payload, this.queryListener,
								this.writeOptions);
					}
					else {
						result = this.cassandraTemplate.insertAsynchronously((T) payload, this.queryListener,
								this.writeOptions);
					}
				}
				else {
					if (payload instanceof List) {
						result = this.cassandraTemplate.insert((List<T>) payload, this.writeOptions);
					}
					else {
						result = this.cassandraTemplate.insert(payload, this.writeOptions);
					}
				}
				break;
			case UPDATE:
				if (this.async) {
					if (payload instanceof List) {
						result = this.cassandraTemplate.updateAsynchronously((List<T>) payload, this.queryListener,
								this.writeOptions);
					}
					else {
						result = this.cassandraTemplate.updateAsynchronously((T) payload, this.queryListener,
								this.writeOptions);
					}
				}
				else {
					if (payload instanceof List) {
						result = this.cassandraTemplate.update((List<T>) payload, this.writeOptions);
					}
					else {
						result = this.cassandraTemplate.update(payload, this.writeOptions);
					}
				}
				break;
			case DELETE:
				if (this.async) {
					if (payload instanceof List) {
						result = this.cassandraTemplate.deleteAsynchronously((List<T>) payload, this.queryListener,
								this.writeOptions);
					}
					else {
						result = this.cassandraTemplate.deleteAsynchronously((T) payload, this.queryListener,
								this.writeOptions);
					}
				}
				else {
					if (payload instanceof List) {
						this.cassandraTemplate.delete((List<T>) payload, this.writeOptions);
					}
					else {
						this.cassandraTemplate.delete(payload, this.writeOptions);
					}
				}
				break;
			case STATEMENT:
				if (statement == null) {
					statement = this.statementProcessor.processMessage(requestMessage);
				}
				if (this.async) {
					if (statement instanceof Select) {
						result = this.cassandraTemplate.queryAsynchronously((Select) statement);
					}
					else {
						result = this.cassandraTemplate.executeAsynchronously(statement);
					}
				}
				else {
					if (statement instanceof Select) {
						result = this.cassandraTemplate.query((Select) statement);
					}
					else {
						this.cassandraTemplate.execute(statement);
					}
				}
				break;
		}

		if (result == null || !this.producesReply) {
			return null;
		}

		return result;
	}

	private static class QueryListenerAdapter<T> implements WriteListener<T>, DeletionListener<T> {

		private final QueryListener<T> delegate;

		private QueryListenerAdapter(QueryListener<T> delegate) {
			Assert.notNull(delegate, "'delegate' must not be null.");
			this.delegate = delegate;
		}

		@Override
		public void onDeletionComplete(Collection<T> entities) {
			this.delegate.onComplete(entities);
		}

		@Override
		public void onWriteComplete(Collection<T> entities) {
			this.delegate.onComplete(entities);
		}

		@Override
		public void onException(Exception x) {
			this.delegate.onException(x);
		}

	}

	public enum Type {

		INSERT, UPDATE, DELETE, STATEMENT;

	}

}
