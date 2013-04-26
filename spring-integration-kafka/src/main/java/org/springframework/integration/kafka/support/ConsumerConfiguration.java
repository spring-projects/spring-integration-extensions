package org.springframework.integration.kafka.support;

import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;

import java.util.List;
import java.util.Map;

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

    public Map<String, List<KafkaStream<byte[], byte[]>>> getConsumerMapWithMessageStreams() {
       if (consumerMetadata.getKafkaDecoder() != null) {
           return consumerConnector.createMessageStreams(
                   consumerMetadata.getTopicStreamMap(),
                   consumerMetadata.getKafkaDecoder(),
                   consumerMetadata.getKafkaDecoder());
       }
       return consumerConnector.createMessageStreams(consumerMetadata.getTopicStreamMap());
   }
}
