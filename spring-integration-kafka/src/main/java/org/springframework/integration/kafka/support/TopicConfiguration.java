package org.springframework.integration.kafka.support;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.serializer.DefaultEncoder;
import kafka.serializer.Encoder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.integration.Message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * @author Soby Chacko
 */
public class TopicConfiguration<K,V> implements InitializingBean {

    private Encoder<K> kafkaKeyEncoder;
    private Encoder<V> kafkaEncoder;
    private String topic;
    private Class<K> keyClass;
    private Class<V> valueClass;

    private Producer<K,V> producer;

    public void setProducer(Producer<K, V> producer) {
        this.producer = producer;
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

    public void send(final Message<?> message) throws Exception {
        final V v = getPayload(message);

        if (message.getHeaders().containsKey("messageKey")) {
            final K k = getKey(message);
            producer.send(new KeyedMessage<K, V>(getTopic(), k, v));
        } else {
            producer.send(new KeyedMessage<K, V>(getTopic(), v));
        }
    }

    @SuppressWarnings("unchecked")
    private V getPayload(final Message<?> message) throws Exception {
        if (kafkaEncoder.getClass().isAssignableFrom(DefaultEncoder.class)) {
            return (V) getByteStream(message.getPayload());
        } else if (message.getPayload().getClass().isAssignableFrom(valueClass)) {
            return valueClass.cast(message.getPayload());
        }
        throw new Exception("Message payload type is not matching with what is configured");
    }

    @SuppressWarnings("unchecked")
    private K getKey(final Message<?> message) throws Exception {
        final Object key = message.getHeaders().get("messageKey");
        if (kafkaKeyEncoder.getClass().isAssignableFrom(DefaultEncoder.class)) {
            return (K) getByteStream(key);
        } else {
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
    public void afterPropertiesSet() throws Exception {
        if (kafkaEncoder == null) {
            setKafkaEncoder((Encoder<V>) new DefaultEncoder(null));
        }
        if (kafkaKeyEncoder == null) {
            setKafkaKeyEncoder((Encoder<K>) getKafkaEncoder());
        }
    }
}
