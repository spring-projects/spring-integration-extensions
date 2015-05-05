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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.cassandra.core.WriteOptions;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.WriteListener;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;

/**
 * @author Soby Chacko
 */
public class CassandraStoringMessageHandler<T> extends AbstractMessageHandler {

	private static final Log log = LogFactory.getLog(CassandraStoringMessageHandler.class);

	private final CassandraOperations cassandraOperations;

	/**
	 * Indicates whether the outbound operations need to use the underlying high throughput ingest
	 * capabilities.
	 */
	private boolean highThroughputIngest;

	/**
	 * Prepared statement to use in association with high throughput ingestion.
	 */
	private String cqlIngest;

	/**
	 * Indicates whether the outbound operations need to be async.
	 */
	private boolean async;

	/**
	 * Various options that can be used for Cassandra writes.
	 */
	private WriteOptions writeOptions;

	public CassandraStoringMessageHandler(CassandraOperations cassandraOperations) {
		Assert.notNull(cassandraOperations, "'cassandraOperations' must not be null.");
		this.cassandraOperations = cassandraOperations;
	}

	public void setHighThroughputIngest(boolean highThroughputIngest) {
		this.highThroughputIngest = highThroughputIngest;
	}

	public void setAsync(boolean async) {
		this.async = async;
	}

	public void setCqlIngest(String cqlIngest) {
		this.cqlIngest = cqlIngest;
	}

	public void setWriteOptions(WriteOptions writeOptions) {
		this.writeOptions = writeOptions;
	}

	@Override
	public String getComponentType() {
		return "cassandra:outbound-channel-adapter";
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void handleMessageInternal(Message<?> message) throws Exception {

		Object payload = message.getPayload();
		if (this.highThroughputIngest) {
			handleHighThroughputIngest(payload);
		}
		else if (this.async) {
			if (payload instanceof List) {
				this.cassandraOperations.insertAsynchronously((List<T>) payload, getWriteListener(), this.writeOptions);
			}
			else {
				this.cassandraOperations.insertAsynchronously((T) payload, getWriteListener(), this.writeOptions);
			}
		}
		else {
			if (payload instanceof List) {
				this.cassandraOperations.insert((List<T>) payload, this.writeOptions);
			}
			else {
				this.cassandraOperations.insert(payload, this.writeOptions);
			}

		}
	}

	@SuppressWarnings("unchecked")
	private void handleHighThroughputIngest(Object payload) {
		Assert.isInstanceOf(List.class, payload, "to perform 'ingest' the 'payload' must be of 'List<List<?>>' type.");
		List<?> list = (List<?>) payload;
		for (Object o : list) {
			Assert.isInstanceOf(List.class, o, "to perform 'ingest' the 'payload' must be of 'List<List<?>>' type.");
		}
		List<List<?>> rows = (List<List<?>>) payload;
		this.cassandraOperations.ingest(this.cqlIngest, rows, this.writeOptions);
	}

	private WriteListener<T> getWriteListener() {
		return new WriteListener<T>() {

			@Override
			public void onWriteComplete(Collection<T> entities) {

			}

			@Override
			public void onException(Exception x) {
				log.debug("Exception thrown", x);
			}

		};
	}

}
