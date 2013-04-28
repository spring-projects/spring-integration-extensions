package org.springframework.integration.kafka.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Soby Chacko
 */
public class KafkaConsumerContext implements BeanFactoryAware {

    protected final Log logger = LogFactory.getLog(getClass());
    private Map<String, ConsumerConfiguration> consumerConfigurations;

    private ExecutorService executorService = Executors.newCachedThreadPool();

    private String receiveTimeout = KafkaConsumerDefaults.CONSUMER_TIMEOUT;

    public Collection<ConsumerConfiguration> getConsumerConfigurations() {
        return consumerConfigurations.values();
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        consumerConfigurations = ((ListableBeanFactory) beanFactory).getBeansOfType(ConsumerConfiguration.class);
    }

    public Message<Map<String, List<Object>>> receive() {
        Map<String, List<Object>> consumedData = new HashMap<String, List<Object>>();
        for (final ConsumerConfiguration consumerConfiguration : getConsumerConfigurations()) {
            consumedData.putAll(consumerConfiguration.receive());
        }
        return MessageBuilder.withPayload(consumedData).build();
    }

    public String getReceiveTimeout() {
        return receiveTimeout;
    }

    public void setReceiveTimeout(String receiveTimeout) {
        this.receiveTimeout = receiveTimeout;
    }
}
