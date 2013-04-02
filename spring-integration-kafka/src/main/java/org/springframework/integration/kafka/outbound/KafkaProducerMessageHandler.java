/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.integration.kafka.outbound;

import kafka.api.TopicMetadata;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import kafka.producer.ProducerPool;
import kafka.producer.async.DefaultEventHandler;
import kafka.producer.async.EventHandler;
import kafka.serializer.Encoder;
import org.springframework.integration.Message;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.integration.kafka.support.KafkaProducerContext;
import scala.collection.mutable.HashMap;

import java.util.Collection;
import java.util.Properties;

/**
 * @author Soby Chacko
 *
 */
public class KafkaProducerMessageHandler extends AbstractMessageHandler {

    private final KafkaProducerContext kafkaProducerContext;
    private Encoder kafkaEncoder;
    private String topic;

    public KafkaProducerMessageHandler(final KafkaProducerContext kafkaProducerContext) {
        this.kafkaProducerContext = kafkaProducerContext;
    }

    public Encoder getKafkaEncoder() {
        return kafkaEncoder;
    }

    public void setKafkaEncoder(Encoder kafkaEncoder) {
        this.kafkaEncoder = kafkaEncoder;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    @Override
    protected void handleMessageInternal(Message<?> message) throws Exception {
        final Properties props = new Properties();
        props.put("broker.list", kafkaProducerContext.getBrokerList());
        final ProducerConfig config = new ProducerConfig(props);

        final EventHandler eventHandler  = new DefaultEventHandler(config, new NaivePartitioner(), kafkaEncoder,
                        kafkaEncoder, new ProducerPool(config), new HashMap<String, TopicMetadata>());

        final kafka.producer.Producer prod = new kafka.producer.Producer(config, eventHandler);

        final kafka.javaapi.producer.Producer producer =
                       new kafka.javaapi.producer.Producer<String, String>(prod);
        if (Collection.class.isInstance(message.getPayload())){
            final Collection<Object> payloads = (Collection<Object>) message.getPayload();

            for (final Object payload : payloads) {
                producer.send(new KeyedMessage<String, Object>(getTopic(), String.valueOf(payload.hashCode()), payload));
            }
        }
        else {
            producer.send(new KeyedMessage<String, Object>(getTopic(), String.valueOf(message.hashCode()), message.getPayload()));
        }
        producer.close();
    }
}
