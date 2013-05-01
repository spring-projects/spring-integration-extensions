package org.springframework.integration.kafka.support;

import kafka.consumer.ConsumerConfig;
import org.springframework.beans.factory.FactoryBean;

import java.util.Properties;

/**
 * @author Soby Chacko
 */
public class ConsumerConfigFactoryBean implements FactoryBean<ConsumerConfig> {

    private final ConsumerMetadata consumerMetadata;
    private final ZookeeperConnect zookeeperConnect;

    public ConsumerConfigFactoryBean(final ConsumerMetadata consumerMetadata,
                                     final ZookeeperConnect zookeeperConnect){
        this.consumerMetadata = consumerMetadata;
        this.zookeeperConnect = zookeeperConnect;
    }

    @Override
    public ConsumerConfig getObject() throws Exception {
        final Properties properties = new Properties();
        properties.put("zk.connect", zookeeperConnect.getZkConnect());
        properties.put("zk.session.timeout.ms", zookeeperConnect.getZkSessionTimeout());
        properties.put("zk.sync.time.ms", zookeeperConnect.getZkSyncTime());
        properties.put("auto.commit.interval.ms", consumerMetadata.getAutoCommitInterval());
        properties.put("consumer.timeout.ms", consumerMetadata.getConsumerTimeout());
        properties.put("group.id", consumerMetadata.getGroupId());

        return new ConsumerConfig(properties);
        //return kafka.consumer.Consumer.createJavaConsumerConnector(consumerConfig);
    }

    @Override
    public Class<?> getObjectType() {
        return ConsumerConfig.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
