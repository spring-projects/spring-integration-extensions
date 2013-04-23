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
    private final TopicMetadata<K,V> topicMetadata;

    public ProducerFactoryBean(final TopicMetadata<K,V> topicMetadata, final String brokerList){
        this.topicMetadata = topicMetadata;
        this.brokerList = brokerList;
    }

    @Override
    public Producer<K, V> getObject() throws Exception {
        final Properties props = new Properties();
        props.put("broker.list", brokerList);
        props.put("compression.codec", topicMetadata.getCompressionCodec());

        final ProducerConfig config = new ProducerConfig(props);
        final EventHandler<K, V> eventHandler = new DefaultEventHandler<K, V>(config,
                topicMetadata.getPartitioner() == null ? new DefaultPartitioner<K>() : topicMetadata.getPartitioner(),
                topicMetadata.getValueEncoder(), topicMetadata.getKeyEncoder(),
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
