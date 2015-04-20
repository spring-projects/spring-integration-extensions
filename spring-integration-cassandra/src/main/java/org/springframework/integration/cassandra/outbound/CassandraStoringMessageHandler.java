package org.springframework.integration.cassandra.outbound;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
            cassandraTemplate.insert(entities);
        } else {
            cassandraTemplate.insert(message.getPayload());
        }
    }

    private void handleAsyncInsert(Object payload) {
        WriteListener<T> writeListener = gettWriteListener();
        if (payload instanceof List) {
            @SuppressWarnings("unchecked")
            List<T> entities = (List<T>) payload;
            cassandraTemplate.insertAsynchronously(entities, writeListener);
        } else {
            @SuppressWarnings("unchecked")
            T typedPayload = (T) payload;
            cassandraTemplate.insertAsynchronously(typedPayload, writeListener);
        }
    }

    private void handleHighThroughputIngest(Object payload) {
        if (payload instanceof List) {
            @SuppressWarnings("unchecked")
            List<List<?>> data = (List<List<?>>) payload;
            assert cqlIngest != null;
            cassandraTemplate.ingest(cqlIngest, data);
        }
    }

    private WriteListener<T> gettWriteListener() {
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

    @Override
    public String getComponentType() {
        return "cassandra:outbound-channel-adapter";
    }

}
