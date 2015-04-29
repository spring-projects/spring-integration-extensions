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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cassandra.core.WriteOptions;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.WriteListener;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.messaging.Message;

import java.util.Collection;
import java.util.List;

/**
 * @author Soby Chacko
 */
public class CassandraStoringMessageHandler<T> extends AbstractMessageHandler {

    private static final Log log = LogFactory.getLog(CassandraStoringMessageHandler.class);

    private final CassandraOperations cassandraTemplate;

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

    public CassandraStoringMessageHandler(CassandraOperations cassandraTemplate) {
        this.cassandraTemplate = cassandraTemplate;
    }

    @Override
    protected void handleMessageInternal(Message<?> message) throws Exception {

        Object payload = message.getPayload();
        if (highThroughputIngest) {
            handleHighThroughputIngest(payload);
        }
        else if (async) {
            handleAsyncInsert(payload);
        }
        else {
            handleSynchronousInsert(message, payload);
        }
    }

    private void handleSynchronousInsert(Message<?> message, Object payload) {
        if (payload instanceof List) {
            @SuppressWarnings("unchecked")
            List<T> entities = (List<T>) payload;
            cassandraTemplate.insert(entities, writeOptions);
        } else {
            cassandraTemplate.insert(message.getPayload(), writeOptions);
        }
    }

    private void handleAsyncInsert(Object payload) {
        WriteListener<T> writeListener = getWriteListener();
        if (payload instanceof List) {
            @SuppressWarnings("unchecked")
            List<T> entities = (List<T>) payload;
            cassandraTemplate.insertAsynchronously(entities, writeListener, writeOptions);
        } else {
            @SuppressWarnings("unchecked")
            T typedPayload = (T) payload;
            cassandraTemplate.insertAsynchronously(typedPayload, writeListener, writeOptions);
        }
    }

    private void handleHighThroughputIngest(Object payload) {
        if (payload instanceof List) {
            @SuppressWarnings("unchecked")
            List<List<?>> data = (List<List<?>>) payload;
            assert cqlIngest != null;
            cassandraTemplate.ingest(cqlIngest, data, writeOptions);
        }
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

}
