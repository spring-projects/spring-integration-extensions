/*
 * Copyright 2002-2012 the original author or authors.
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.integration.MessagingException;
import org.springframework.integration.kafka.support.KafkaServer;

import java.util.ArrayList;
import java.util.HashMap;
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

    private final KafkaServer kafkaServer;
    private String receiveTimeout = "5000";
    private int maxMessagesPerPoll = 1;
    private ConsumerConnector consumerConnector;

    public KafkaExecutor(final KafkaServer kafkaServer){
        this.kafkaServer = kafkaServer;
    }

    public KafkaServer getKafkaServer() {
        return kafkaServer;
    }

    public String getReceiveTimeout() {
        return receiveTimeout;
    }

    public int getMaxMessagesPerPoll() {
        return maxMessagesPerPoll;
    }

    public void setReceiveTimeout(String receiveTimeout) {
        this.receiveTimeout = receiveTimeout;
    }

    public void setMaxMessagesPerPoll(int maxMessagesPerPoll) {
        this.maxMessagesPerPoll = maxMessagesPerPoll;
    }

    public List<Object> poll(final String topic, final int partitions) {
        final ConsumerIterator<byte[], byte[]> it = getKafkaConsumerIterator(topic, partitions);
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        final CountDownLatch latch = new CountDownLatch(maxMessagesPerPoll);

        final Future<List<Object>> future = executorService.submit(new Callable<List<Object>>() {
            @Override
            public List<Object> call() throws Exception {
                final List<Object> messages = new ArrayList<Object>();
                try {
                    while(latch.getCount() > 0) {
                        messages.add(new String(it.next().message()));
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

        try{
            latch.await();
            return future.get();
        }
        catch(Exception e) {
            String errorMsg = "Consuming from Kafka failed";
            logger.warn(errorMsg, e);
            throw new MessagingException(errorMsg, e);
        } finally {
            executorService.shutdown();
        }
	}

    private ConsumerIterator<byte[], byte[]> getKafkaConsumerIterator(String topic, int partitions) {
        Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = getConsumerMapWithMessageStreams(
                getTopicPartitionsMap(topic, partitions));
        KafkaStream<byte[], byte[]> stream =  consumerMap.get(topic).get(0);
        return stream.iterator();
    }

    private void clearAllLatches(CountDownLatch latch) {
        while (latch.getCount() > 0){
            latch.countDown();
        }
    }

    private Map<String, List<KafkaStream<byte[], byte[]>>> getConsumerMapWithMessageStreams(Map<String, Integer> topicCountMap) {
        return getConsumerConnector().createMessageStreams(topicCountMap);
    }

    private Map<String, Integer> getTopicPartitionsMap(String topic, int partitions) {
        final Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
        topicCountMap.put(topic, partitions);
        return topicCountMap;
    }

    public synchronized ConsumerConnector getConsumerConnector() {
        if (consumerConnector != null) {
            return consumerConnector;
        }
        final Properties properties = new Properties();
        properties.put("zk.connect", kafkaServer.getZkConnect());
        properties.put("zk.connectiontimeout.ms", kafkaServer.getZkConnectionTimeout());
        properties.put("group.id", kafkaServer.getGroupId());
        properties.put("zk.session.timeout.ms", kafkaServer.getZkSessionTimeout());
        properties.put("zk.sync.time.ms", kafkaServer.getZkSyncTime());
        properties.put("auto.commit.interval.ms", kafkaServer.getAutoCommitInterval());
        properties.put("consumer.timeout.ms", receiveTimeout);

        final ConsumerConfig consumerConfig = new ConsumerConfig(properties);
        consumerConnector = kafka.consumer.Consumer.createJavaConsumerConnector(consumerConfig);

        return consumerConnector;
    }
}
