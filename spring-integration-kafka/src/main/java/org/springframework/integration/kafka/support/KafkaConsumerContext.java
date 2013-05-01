package org.springframework.integration.kafka.support;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.integration.Message;
import org.springframework.integration.kafka.core.KafkaConsumerDefaults;
import org.springframework.integration.support.MessageBuilder;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Soby Chacko
 */
public class KafkaConsumerContext implements BeanFactoryAware {

    private Map<String, ConsumerConfiguration> consumerConfigurations;

    private String consumerTimeout = KafkaConsumerDefaults.CONSUMER_TIMEOUT;
    private ZookeeperConnect zookeeperConnect;

    public Collection<ConsumerConfiguration> getConsumerConfigurations() {
        return consumerConfigurations.values();
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        consumerConfigurations = ((ListableBeanFactory) beanFactory).getBeansOfType(ConsumerConfiguration.class);
    }

    public Message<Map<String, Map<Integer, List<Object>>>> receive() {
        Map<String, Map<Integer, List<Object>>> consumedData = new HashMap<String, Map<Integer, List<Object>>>();
        for (final ConsumerConfiguration consumerConfiguration : getConsumerConfigurations()) {
            Map<String, Map<Integer, List<Object>>> messages = consumerConfiguration.receive();
            if (messages != null){
                consumedData.putAll(messages);
            }
        }
        return MessageBuilder.withPayload(consumedData).build();
    }

    public String getConsumerTimeout() {
        return consumerTimeout;
    }

    public void setConsumerTimeout(String consumerTimeout) {
        this.consumerTimeout = consumerTimeout;
    }

    public ZookeeperConnect getZookeeperConnect() {
        return zookeeperConnect;
    }

    public void setZookeeperConnect(ZookeeperConnect zookeeperConnect) {
        this.zookeeperConnect = zookeeperConnect;
    }
}
