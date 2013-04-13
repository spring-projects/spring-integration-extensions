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
import kafka.serializer.DefaultEncoder;
import kafka.serializer.Encoder;
import org.springframework.integration.Message;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.integration.kafka.support.KafkaProducerContext;
import scala.collection.mutable.HashMap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Properties;

/**
 * @author Soby Chacko
 */
public class KafkaProducerMessageHandler<K, V> extends AbstractMessageHandler {

    private final KafkaProducerContext kafkaProducerContext;
    private Encoder<K> kafkaKeyEncoder;
    private Encoder<V> kafkaEncoder;
    private String topic;
    private Class<K> keyClass;
    private Class<V> valueClass;
    
    public KafkaProducerMessageHandler(final KafkaProducerContext kafkaProducerContext) {
        this.kafkaProducerContext = kafkaProducerContext;
    }

    public Encoder<V> getKafkaEncoder() {
        return kafkaEncoder;
    }

    public void setKafkaEncoder(final Encoder<V> kafkaEncoder) {
        this.kafkaEncoder = kafkaEncoder;
    }
    
    public Encoder<K> getKafkaKeyEncoder() {
		return kafkaKeyEncoder;
	}

	public void setKafkaKeyEncoder(final Encoder<K> kafkaKeyEncoder) {
		this.kafkaKeyEncoder = kafkaKeyEncoder;
	}

	public String getTopic() {
        return topic;
    }

    public void setTopic(final String topic) {
        this.topic = topic;
    }

    public Class<K> getKeyClass() {
		return keyClass;
	}
   
	public void setKeyClass(final Class<K> keyClass) {
		this.keyClass = keyClass;
	}

	public Class<V> getValueClass() {
		return valueClass;
	}

	public void setValueClass(final Class<V> valueClass) {
		this.valueClass = valueClass;
	}

	public KafkaProducerContext getKafkaProducerContext() {
		return kafkaProducerContext;
	}

	@Override
    protected void handleMessageInternal(final Message<?> message) throws Exception {
        final Properties props = new Properties();
        props.put("broker.list", kafkaProducerContext.getBrokerList());
        props.put("compression.codec", getCompressionCodec());

        final ProducerConfig config = new ProducerConfig(props);
        final EventHandler<K, V> eventHandler  = new DefaultEventHandler<K, V>(config, new NaivePartitioner<K>(),
                kafkaEncoder, kafkaKeyEncoder,
                new ProducerPool(config), new HashMap<String, TopicMetadata>());

        final kafka.producer.Producer<K,V> prod = new kafka.producer.Producer<K,V>(config, eventHandler);

        final kafka.javaapi.producer.Producer<K,V> producer =
                       new kafka.javaapi.producer.Producer<K,V>(prod);

        final V v = getPayload(message);

        if (message.getHeaders().containsKey("messageKey")) {
            final K k = getKey(message);
            producer.send(new KeyedMessage<K, V>(getTopic(), k, v));
        }
        else{
            producer.send(new KeyedMessage<K, V>(getTopic(), v));
        }
        producer.close();
    }

    @SuppressWarnings("unchecked")
    private V getPayload(final Message<?> message) throws Exception {
        if (kafkaEncoder.getClass().isAssignableFrom(DefaultEncoder.class)) {
            return (V)getByteStream(message.getPayload());
        }
        else if (message.getPayload().getClass().isAssignableFrom(valueClass)){
            return valueClass.cast(message.getPayload());
        }
        throw new Exception("Message payload type is not matching with what is configured");
    }

    //TODO: Refactor to use an enum
    private String getCompressionCodec() {
        if (kafkaProducerContext.getCompressionCodec().equalsIgnoreCase("gzip")){
            return "1";
        } else if (kafkaProducerContext.getCompressionCodec().equalsIgnoreCase("snappy")) {
            return "2";
        }
        return "0";
    }

    @SuppressWarnings("unchecked")
    private K getKey(final Message<?> message) throws Exception {
        final Object key = message.getHeaders().get("messageKey");
        if (kafkaKeyEncoder.getClass().isAssignableFrom(DefaultEncoder.class)){
            return (K)getByteStream(key);
        }
        else {
            return message.getHeaders().get("messageKey", keyClass);
        }
    }

	private byte[] getByteStream(final Object obj) throws IOException {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		final ObjectOutputStream os = new ObjectOutputStream(out);
		os.writeObject(obj);
		return out.toByteArray();
	}

	@SuppressWarnings("unchecked")
	protected void onInit() throws Exception {
		if (kafkaEncoder == null) {
			setKafkaEncoder((Encoder<V>)new DefaultEncoder(null));
		}
		if (kafkaKeyEncoder == null) {
			setKafkaKeyEncoder((Encoder<K>)getKafkaEncoder());
		}
	}
}
