package org.springframework.integration.kafka.support;

import kafka.javaapi.producer.Producer;
import kafka.producer.ProducerConfig;
import kafka.producer.ProducerPool;
import kafka.producer.async.DefaultEventHandler;
import kafka.producer.async.EventHandler;
import org.springframework.beans.factory.FactoryBean;
import scala.collection.mutable.HashMap;

import java.util.Properties;

/**
 * @author Soby Chacko
 */
public class ProducerFactoryBean<K,V> implements FactoryBean<Producer<K,V>> {

    private final String brokerList;
    private final ProducerMetadata<K,V> producerMetadata;

    public ProducerFactoryBean(final ProducerMetadata<K,V> producerMetadata, final String brokerList){
        this.producerMetadata = producerMetadata;
        this.brokerList = brokerList;
    }

    @Override
    public Producer<K, V> getObject() throws Exception {
        final Properties props = new Properties();
        props.put("broker.list", brokerList);
        props.put("compression.codec", producerMetadata.getCompressionCodec());

        if (producerMetadata.isAsync()){
            props.put("producer.type", "async");
            if (producerMetadata.getBatchNumMessages() != null){
                props.put("batch.num.messages", producerMetadata.getBatchNumMessages());
            }
        }

        final ProducerConfig config = new ProducerConfig(props);
        final EventHandler<K, V> eventHandler = new DefaultEventHandler<K, V>(config,
                producerMetadata.getPartitioner() == null ? new DefaultPartitioner<K>() : producerMetadata.getPartitioner(),
                producerMetadata.getValueEncoder(), producerMetadata.getKeyEncoder(),
                new ProducerPool(config), new HashMap<String, kafka.api.TopicMetadata>());

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
        return true;
    }
}
