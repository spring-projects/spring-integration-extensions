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
package org.springframework.integration.kafka.core;

import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.ConsumerTimeoutException;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.serializer.Decoder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.integration.MessagingException;
import org.springframework.integration.kafka.support.KafkaBroker;
import org.springframework.integration.kafka.support.KafkaConsumerContext;
import org.springframework.validation.BindingResultUtils;

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
 * Bundles common core logic for the Kafka components.
 *
 * @author Soby Chacko
 * @since 1.0
 *
 */
public class KafkaExecutor {

	private static final Log logger = LogFactory.getLog(KafkaExecutor.class);

    private final KafkaBroker kafkaBroker;
    private ConsumerConnector consumerConnector;
    private Decoder kafkaDecoder;

    public KafkaExecutor(final KafkaBroker kafkaBroker){
        this.kafkaBroker = kafkaBroker;
    }

    public KafkaBroker getKafkaBroker() {
        return kafkaBroker;
    }

    public Decoder getKafkaDecoder() {
        return kafkaDecoder;
    }

    public void setKafkaDecoder(Decoder kafkaDecoder) {
        this.kafkaDecoder = kafkaDecoder;
    }

    private void clearAllLatches(CountDownLatch latch) {
        while (latch.getCount() > 0){
            latch.countDown();
        }
    }

    private Map<String, List<KafkaStream<byte[], byte[]>>> getConsumerMapWithMessageStreams(Map<String, Integer> topicCountMap,
                                                                                            final KafkaConsumerContext kafkaConsumerContext) {
        if (kafkaDecoder != null) {
            return getConsumerConnector(kafkaConsumerContext).createMessageStreams(topicCountMap, kafkaDecoder, kafkaDecoder);
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
        properties.put("zk.connect", kafkaBroker.getZkConnect());
        //properties.put("zk.connectiontimeout.ms", kafkaBroker.getZkConnectionTimeout());
        properties.put("zk.session.timeout.ms", kafkaBroker.getZkSessionTimeout());
        properties.put("zk.sync.time.ms", kafkaBroker.getZkSyncTime());
        properties.put("auto.commit.interval.ms", kafkaConsumerContext.getAutoCommitInterval());
        properties.put("consumer.timeout.ms", kafkaConsumerContext.getReceiveTimeout());
        properties.put("group.id", kafkaConsumerContext.getGroupId());

        final ConsumerConfig consumerConfig = new ConsumerConfig(properties);
        consumerConnector = kafka.consumer.Consumer.createJavaConsumerConnector(consumerConfig);

        return consumerConnector;
    }

    public List<Object> poll(final KafkaConsumerContext kafkaConsumerContext) {
        final List<KafkaStream<byte[], byte[]>> streams = getKafkaConsumerStreams(kafkaConsumerContext);
        final ExecutorService executorService = Executors.newFixedThreadPool(4);
        final CountDownLatch latch = new CountDownLatch(kafkaConsumerContext.getMaxMessagesPerPoll());

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
                //latch.await();
                return messageAggregate;
        }
        catch(Exception e) {
            String errorMsg = "Consuming from Kafka failed";
            logger.warn(errorMsg, e);
            throw new MessagingException(errorMsg, e);
        } finally {
            executorService.shutdown();
        }
    }

        private List<KafkaStream<byte[], byte[]>> getKafkaConsumerStreams(final KafkaConsumerContext kafkaConsumerContext) {
            Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = getConsumerMapWithMessageStreams(
                    getTopicPartitionsMap(kafkaConsumerContext), kafkaConsumerContext);
            List<KafkaStream<byte[], byte[]>> streams =  consumerMap.get(kafkaConsumerContext.getTopic());
            return streams;
        }
}
