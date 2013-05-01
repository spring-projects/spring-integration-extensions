package org.springframework.integration.kafka.support;

import kafka.consumer.ConsumerConfig;
import kafka.javaapi.consumer.ConsumerConnector;

/**
 * @author Soby Chacko
 */
public class ConsumerConnectionProvider {

    private final ConsumerConfig consumerConfig;

    public ConsumerConnectionProvider(final ConsumerConfig consumerConfig) {
        this.consumerConfig = consumerConfig;
    }

    public ConsumerConnector getConsumerConnector() {
        return kafka.consumer.Consumer.createJavaConsumerConnector(consumerConfig);
    }
}
