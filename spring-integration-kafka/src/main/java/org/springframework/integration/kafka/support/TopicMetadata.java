package org.springframework.integration.kafka.support;

import kafka.producer.Partitioner;
import kafka.serializer.DefaultEncoder;
import kafka.serializer.Encoder;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author Soby Chacko
 */
public class TopicMetadata<K,V> implements InitializingBean {

    private Encoder<K> kafkaKeyEncoder;
    private Encoder<V> kafkaEncoder;
    private Class<K> keyClass;
    private Class<V> valueClass;
    private final String topic;
    private String compressionCodec = "default";
    private Partitioner<K> partitioner;

    public TopicMetadata(final String topic) {
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }

    public Encoder<K> getKafkaKeyEncoder() {
        return kafkaKeyEncoder;
    }

    public void setKafkaKeyEncoder(Encoder<K> kafkaKeyEncoder) {
        this.kafkaKeyEncoder = kafkaKeyEncoder;
    }

    public Encoder<V> getKafkaEncoder() {
        return kafkaEncoder;
    }

    public void setKafkaEncoder(Encoder<V> kafkaEncoder) {
        this.kafkaEncoder = kafkaEncoder;
    }

    public Class<K> getKeyClass() {
        return keyClass;
    }

    public void setKeyClass(Class<K> keyClass) {
        this.keyClass = keyClass;
    }

    public Class<V> getValueClass() {
        return valueClass;
    }

    public void setValueClass(Class<V> valueClass) {
        this.valueClass = valueClass;
    }

    //TODO: Use an enum
    public String getCompressionCodec() {
        if (compressionCodec.equalsIgnoreCase("gzip")) {
            return "1";
        } else if (compressionCodec.equalsIgnoreCase("snappy")) {
            return "2";
        }
        return "0";
    }

    public void setCompressionCodec(String compressionCodec) {
        this.compressionCodec = compressionCodec;
    }

    public Partitioner<K> getPartitioner() {
        return partitioner;
    }

    public void setPartitioner(Partitioner<K> partitioner) {
        this.partitioner = partitioner;
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
