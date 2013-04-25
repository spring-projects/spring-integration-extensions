package org.springframework.integration.kafka.support;

import kafka.javaapi.consumer.ConsumerConnector;

import java.util.Collection;

/**
 * @author Soby Chacko
 */
public class ConsumerConfiguration {

    private final ConsumerMetadata consumerMetadata;
    private final ConsumerConnector consumerConnector;

    public ConsumerConfiguration(final ConsumerMetadata consumerMetadata,
                                 final ConsumerConnector consumerConnector) {
        this.consumerMetadata = consumerMetadata;
        this.consumerConnector = consumerConnector;
    }

    public ConsumerMetadata getConsumerMetadata() {
        return consumerMetadata;
    }

    public ConsumerConnector getConsumerConnector() {
        return consumerConnector;
    }

    public int getTotalStreams(){
        Collection<Integer> streams = consumerMetadata.getTopicStreamMap().values();
        int sum = 0;
        for (int s : streams){
            sum += s;
        }
        return sum;
    }
}
