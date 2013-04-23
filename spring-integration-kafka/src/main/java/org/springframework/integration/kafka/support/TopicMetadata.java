package org.springframework.integration.kafka.support;

import kafka.producer.Partitioner;
import kafka.serializer.DefaultEncoder;
import kafka.serializer.Encoder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author Soby Chacko
 */
public class TopicMetadata<K,V> implements InitializingBean {

    private Encoder<K> keyEncoder;
    private Encoder<V> valueEncoder;
    private Class<K> keyClassType;
    private Class<V> valueClassType;
    private final String topic;
    private String compressionCodec = "default";
    private Partitioner<K> partitioner;

    public TopicMetadata(final String topic) {
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }

    public Encoder<K> getKeyEncoder() {
        return keyEncoder;
    }

    public void setKeyEncoder(Encoder<K> keyEncoder) {
        this.keyEncoder = keyEncoder;
    }

    public Encoder<V> getValueEncoder() {
        return valueEncoder;
    }

    public void setValueEncoder(Encoder<V> valueEncoder) {
        this.valueEncoder = valueEncoder;
    }

    public Class<K> getKeyClassType() {
        return keyClassType;
    }

    public void setKeyClassType(Class<K> keyClassType) {
        this.keyClassType = keyClassType;
    }

    public Class<V> getValueClassType() {
        return valueClassType;
    }

    public void setValueClassType(Class<V> valueClassType) {
        this.valueClassType = valueClassType;
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
        if (valueEncoder == null) {
            setValueEncoder((Encoder<V>) new DefaultEncoder(null));
        }
        if (keyEncoder == null) {
            setKeyEncoder((Encoder<K>) getValueEncoder());
        }
    }

    @Override
    public boolean equals(Object obj){
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
