/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.integration.kafka.inbound;

import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerTimeoutException;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.serializer.Decoder;
import org.springframework.integration.Message;
import org.springframework.integration.MessagingException;
import org.springframework.integration.context.IntegrationObjectSupport;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.kafka.core.KafkaConsumerDefaults;
import org.springframework.integration.kafka.support.KafkaConsumerContext;
import org.springframework.integration.support.MessageBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author Soby Chacko
 *
 */
public class HighLevelConsumerMessageSource extends IntegrationObjectSupport implements MessageSource<List<Object>> {

    private Decoder kafkaDecoder;
    private ConsumerConnector consumerConnector;
    private final KafkaConsumerContext kafkaConsumerContext;

    private int maxMessagesPerPoll = 1;
    private String receiveTimeout = KafkaConsumerDefaults.CONSUMER_TIMEOUT;

    public HighLevelConsumerMessageSource(final KafkaConsumerContext kafkaConsumerContext) {
        this.kafkaConsumerContext = kafkaConsumerContext;
    }

    @Override
    public Message<List<Object>> receive() {
        final List<KafkaStream<byte[], byte[]>> streams = getKafkaConsumerStreams(kafkaConsumerContext);
        final ExecutorService executorService = Executors.newFixedThreadPool(kafkaConsumerContext.getStreams());
        final CountDownLatch latch = new CountDownLatch(maxMessagesPerPoll);

        final List<Callable<List<Object>>> tasks = new LinkedList<Callable<List<Object>>>();

        final List<Object> messageAggregate = new ArrayList<Object>();

        for (final KafkaStream<byte[], byte[]> stream : streams)   {

             tasks.add(new Callable<List<Object>>() {
                 @Override
                 public List<Object> call() throws Exception {
                     final List<Object> messages = new ArrayList<Object>();
                     try {
                         while(latch.getCount() > 0) {
                             messages.add(stream.iterator().next().message());
                             latch.countDown();
                         }
                     }
                     catch (ConsumerTimeoutException cte) {
                         logger.info("Consumer timed out");
                     }
                     finally {
                         clearAllLatches(latch);
                     }
                     return messages;
                 }
             });
        }

        try{

            for(Future<List<Object>> result : executorService.invokeAll(tasks)) {
                messageAggregate.add(result.get());
            }

        }
        catch(Exception e) {
            String errorMsg = "Consuming from Kafka failed";
            logger.warn(errorMsg, e);
            throw new MessagingException(errorMsg, e);
        } finally {
            executorService.shutdown();
        }

        if (messageAggregate.isEmpty()){
            return null;
        }

        return MessageBuilder.withPayload(messageAggregate).build();
    }

    @Override
    public String getComponentType() {
        return "kafka:inbound-channel-adapter";
    }

    public Decoder getKafkaDecoder() {
        return kafkaDecoder;
    }

    public void setKafkaDecoder(Decoder kafkaDecoder) {
        this.kafkaDecoder = kafkaDecoder;
    }

    private List<KafkaStream<byte[], byte[]>> getKafkaConsumerStreams(final KafkaConsumerContext kafkaConsumerContext) {
               Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = getConsumerMapWithMessageStreams(
                       getTopicPartitionsMap(kafkaConsumerContext), kafkaConsumerContext);
               List<KafkaStream<byte[], byte[]>> streams =  consumerMap.get(kafkaConsumerContext.getTopic());
               return streams;
           }

    private void clearAllLatches(CountDownLatch latch) {
            while (latch.getCount() > 0){
                latch.countDown();
            }
        }

        private Map<String, List<KafkaStream<byte[], byte[]>>> getConsumerMapWithMessageStreams(Map<String, Integer> topicCountMap,
                                                                                                final KafkaConsumerContext kafkaConsumerContext) {
            if (kafkaConsumerContext.getKafkaDecoder() != null) {
                return getConsumerConnector(kafkaConsumerContext).createMessageStreams(topicCountMap,
                        kafkaConsumerContext.getKafkaDecoder(),
                        kafkaConsumerContext.getKafkaDecoder());
            }
            return  getConsumerConnector(kafkaConsumerContext).createMessageStreams(topicCountMap);
        }

        private Map<String, Integer> getTopicPartitionsMap(final KafkaConsumerContext kafkaConsumerContext) {
            final Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
            topicCountMap.put(kafkaConsumerContext.getTopic(), kafkaConsumerContext.getStreams());
            return topicCountMap;
        }

        public synchronized ConsumerConnector getConsumerConnector(final KafkaConsumerContext kafkaConsumerContext) {
            if (consumerConnector != null) {
                return consumerConnector;
            }
            final Properties properties = new Properties();
            properties.put("zk.connect", kafkaConsumerContext.getKafkaBroker().getZkConnect());
            //properties.put("zk.connectiontimeout.ms", kafkaBroker.getZkConnectionTimeout());
            properties.put("zk.session.timeout.ms", kafkaConsumerContext.getKafkaBroker().getZkSessionTimeout());
            properties.put("zk.sync.time.ms", kafkaConsumerContext.getKafkaBroker().getZkSyncTime());
            properties.put("auto.commit.interval.ms", kafkaConsumerContext.getAutoCommitInterval());
            properties.put("consumer.timeout.ms", receiveTimeout);
            properties.put("group.id", kafkaConsumerContext.getGroupId());

            final ConsumerConfig consumerConfig = new ConsumerConfig(properties);
            consumerConnector = kafka.consumer.Consumer.createJavaConsumerConnector(consumerConfig);

            return consumerConnector;
        }

    public int getMaxMessagesPerPoll() {
        return maxMessagesPerPoll;
    }

    public void setMaxMessagesPerPoll(int maxMessagesPerPoll) {
        this.maxMessagesPerPoll = maxMessagesPerPoll;
    }

    public String getReceiveTimeout() {
        return receiveTimeout;
    }

    public void setReceiveTimeout(String receiveTimeout) {
        this.receiveTimeout = receiveTimeout;
    }
}
