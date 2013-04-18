package org.springframework.integration.kafka.support;

import kafka.api.TopicMetadata;
import kafka.javaapi.producer.Producer;
import kafka.producer.Partitioner;
import kafka.producer.ProducerConfig;
import kafka.producer.ProducerPool;
import kafka.producer.async.DefaultEventHandler;
import kafka.producer.async.EventHandler;
import kafka.serializer.DefaultEncoder;
import kafka.serializer.Encoder;
import org.springframework.beans.factory.FactoryBean;
import scala.collection.mutable.HashMap;

import java.util.Properties;

/**
 * @author Soby Chacko
 */
public class ProducerFactoryBean<K,V> implements FactoryBean<Producer<K,V>> {

    private String brokerList = "localhost:9092";
    private Encoder<K> kafkaKeyEncoder;
    private Encoder<V> kafkaEncoder;
    private String compressionCodec;
    private Partitioner<K> partitioner;

    public void setBrokerList(String brokerList) {
        this.brokerList = brokerList;
    }

    public void setKafkaKeyEncoder(Encoder<K> kafkaKeyEncoder) {
        this.kafkaKeyEncoder = kafkaKeyEncoder;
    }

    public void setKafkaEncoder(Encoder<V> kafkaEncoder) {
        this.kafkaEncoder = kafkaEncoder;
    }

    public void setCompressionCodec(String compressionCodec) {
        this.compressionCodec = compressionCodec;
    }

    public void setPartitioner(Partitioner<K> partitioner) {
        this.partitioner = partitioner;
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

    @Override
    public Producer<K, V> getObject() throws Exception {
        final Properties props = new Properties();
        props.put("broker.list", brokerList);
        props.put("compression.codec", getCompressionCodec());

        final ProducerConfig config = new ProducerConfig(props);
        if (kafkaEncoder == null) {
            setKafkaEncoder((Encoder<V>) new DefaultEncoder(null));
        }
        if (kafkaKeyEncoder == null) {
            setKafkaKeyEncoder((Encoder<K>) kafkaEncoder);
        }
        final EventHandler<K, V> eventHandler = new DefaultEventHandler<K, V>(config,
                partitioner == null ? new DefaultPartitioner<K>() : partitioner,
                kafkaEncoder, kafkaKeyEncoder,
                new ProducerPool(config), new HashMap<String, TopicMetadata>());

        final kafka.producer.Producer<K, V> prod = new kafka.producer.Producer<K, V>(config,
                eventHandler);
        return new Producer<K, V>(prod);
    }

    @Override
    public Class<?> getObjectType() {
        return Producer.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
}
