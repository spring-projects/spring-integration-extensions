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

import org.springframework.cassandra.core.WriteOptions;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.WriteListener;
import org.springframework.integration.cassandra.support.CassandraOutboundGatewayType;
import org.springframework.integration.handler.AbstractReplyProducingMessageHandler;
import org.springframework.messaging.Message;

import java.util.List;

/**
 * @author sobychacko
 */
public class CassandraOutboundGateway<T> extends AbstractReplyProducingMessageHandler {

    private final CassandraOperations cassandraTemplate;

    private CassandraOutboundGatewayType gatewayType = CassandraOutboundGatewayType.INSERTING;

    private WriteListener<T> writeListener;

    private boolean producesReply = true;

    private boolean async = false;

    /**
     * Various options that can be used for Cassandra writes.
     */
    private WriteOptions writeOptions;

    public CassandraOutboundGateway(CassandraOperations cassandraTemplate) {
        this.cassandraTemplate = cassandraTemplate;
    }

    @Override
    protected Object handleRequestMessage(Message<?> requestMessage) {
        Object payload = requestMessage.getPayload();
       final Object result;

        switch (gatewayType){
            case INSERTING:
                if (async) {
                    if (payload instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<T> entities = (List<T>) payload;
                        result = cassandraTemplate.insertAsynchronously(entities, writeListener, writeOptions);
                    } else {
                        @SuppressWarnings("unchecked")
                        T typedPayload = (T) payload;
                        result = cassandraTemplate.insertAsynchronously(typedPayload, writeListener, writeOptions);
                    }
                }
                else {
                    if (payload instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<T> entities = (List<T>) payload;
                        result = cassandraTemplate.insert(entities, writeOptions);
                    } else {
                        result = cassandraTemplate.insert(payload, writeOptions);
                    }
                }
                break;
            case UPDATING:
                if (async) {
                    if (payload instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<T> entities = (List<T>) payload;
                        result = cassandraTemplate.updateAsynchronously(entities, writeListener, writeOptions);
                    } else {
                        @SuppressWarnings("unchecked")
                        T typedPayload = (T) payload;
                        result = cassandraTemplate.updateAsynchronously(typedPayload, writeListener, writeOptions);
                    }
                }
                else {
                    if (payload instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<T> entities = (List<T>) payload;
                        result = cassandraTemplate.update(entities, writeOptions);
                    } else {
                        result = cassandraTemplate.update(payload, writeOptions);
                    }
                }
                break;
            case DELETING:
                result = null;
                break;
            default:
                result = null;

        }

        if (result == null || !producesReply) {
            return null;
        }

        return this.getMessageBuilderFactory().withPayload(result).copyHeaders(requestMessage.getHeaders()).build();
    }

    public void setGatewayType(CassandraOutboundGatewayType gatewayType) {
        this.gatewayType = gatewayType;
    }

    public void setWriteListener(WriteListener<T> writeListener) {
        this.writeListener = writeListener;
    }

    public void setWriteOptions(WriteOptions writeOptions) {
        this.writeOptions = writeOptions;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

    @Override
    public String getComponentType() {
        return "cassandra:outbound-gateway";
    }
}
