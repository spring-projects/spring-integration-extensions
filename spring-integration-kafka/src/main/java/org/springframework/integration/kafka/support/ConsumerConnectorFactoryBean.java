package org.springframework.integration.kafka.support;

import kafka.consumer.ConsumerConfig;
import kafka.javaapi.consumer.ConsumerConnector;
import org.springframework.beans.factory.FactoryBean;

import java.util.Properties;

/**
 * @author Soby Chacko
 */
public class ConsumerConnectorFactoryBean implements FactoryBean<ConsumerConnector> {

    private final ConsumerMetadata consumerMetadata;
    private final KafkaBroker kafkaBroker;

    public ConsumerConnectorFactoryBean(final ConsumerMetadata consumerMetadata,
                                        final KafkaBroker kafkaBroker){
        this.consumerMetadata = consumerMetadata;
        this.kafkaBroker = kafkaBroker;
    }

    @Override
    public ConsumerConnector getObject() throws Exception {
        final Properties properties = new Properties();
        properties.put("zk.connect", kafkaBroker.getZkConnect());
        properties.put("zk.session.timeout.ms", kafkaBroker.getZkSessionTimeout());
        properties.put("zk.sync.time.ms", kafkaBroker.getZkSyncTime());
        properties.put("auto.commit.interval.ms", consumerMetadata.getAutoCommitInterval());
        properties.put("consumer.timeout.ms", consumerMetadata.getConsumerTimeout());
        properties.put("group.id", consumerMetadata.getGroupId());

        final ConsumerConfig consumerConfig = new ConsumerConfig(properties);
        final ConsumerConnector consumerConnector = kafka.consumer.Consumer.createJavaConsumerConnector(consumerConfig);

        return consumerConnector;
    }

    @Override
    public Class<?> getObjectType() {
        return ConsumerConnector.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
