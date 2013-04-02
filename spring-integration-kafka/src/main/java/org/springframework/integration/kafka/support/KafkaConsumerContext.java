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
package org.springframework.integration.kafka.support;

import kafka.serializer.Decoder;
import org.springframework.integration.kafka.core.KafkaConsumerDefaults;

/**
 * @author Soby Chacko
 */
public class KafkaConsumerContext {

    //High level consumer defaults
    private String groupId = KafkaConsumerDefaults.GROUP_ID;
    private String socketTimeout = KafkaConsumerDefaults.SOCKET_TIMEOUT;
    private String socketBufferSize = KafkaConsumerDefaults.SOCKET_BUFFER_SIZE;
    private String fetchSize = KafkaConsumerDefaults.FETCH_SIZE;
    private String backoffIncrement = KafkaConsumerDefaults.BACKOFF_INCREMENT;
    private String queuedChunksMax = KafkaConsumerDefaults.QUEUED_CHUNKS_MAX;
    private String autoCommitEnable = KafkaConsumerDefaults.AUTO_COMMIT_ENABLE;
    private String autoCommitInterval = KafkaConsumerDefaults.AUTO_COMMIT_INTERVAL;
    private String autoOffsetReset = KafkaConsumerDefaults.AUTO_OFFSET_RESET;
    private String rebalanceRetriesMax = KafkaConsumerDefaults.REBALANCE_RETRIES_MAX;
    private String receiveTimeout = KafkaConsumerDefaults.CONSUMER_TIMEOUT;

    private String topic;
    private int streams;
    private int maxMessagesPerPoll = 1;
    private Decoder kafkaDecoder;

    private final KafkaBroker kafkaBroker;

    public KafkaConsumerContext(final KafkaBroker kafkaBroker) {
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

    public String getReceiveTimeout() {
        return receiveTimeout;
    }

    public void setReceiveTimeout(String receiveTimeout) {
        this.receiveTimeout = receiveTimeout;
    }

    public int getMaxMessagesPerPoll() {
        return maxMessagesPerPoll;
    }

    public void setMaxMessagesPerPoll(int maxMessagesPerPoll) {
        this.maxMessagesPerPoll = maxMessagesPerPoll;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getStreams() {
        return streams;
    }

    public void setStreams(int streams) {
        this.streams = streams;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(String socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public String getSocketBufferSize() {
        return socketBufferSize;
    }

    public void setSocketBufferSize(String socketBufferSize) {
        this.socketBufferSize = socketBufferSize;
    }

    public String getFetchSize() {
        return fetchSize;
    }

    public void setFetchSize(String fetchSize) {
        this.fetchSize = fetchSize;
    }

    public String getBackoffIncrement() {
        return backoffIncrement;
    }

    public void setBackoffIncrement(String backoffIncrement) {
        this.backoffIncrement = backoffIncrement;
    }

    public String getQueuedChunksMax() {
        return queuedChunksMax;
    }

    public void setQueuedChunksMax(String queuedChunksMax) {
        this.queuedChunksMax = queuedChunksMax;
    }

    public String getAutoCommitEnable() {
        return autoCommitEnable;
    }

    public void setAutoCommitEnable(String autoCommitEnable) {
        this.autoCommitEnable = autoCommitEnable;
    }

    public String getAutoCommitInterval() {
        return autoCommitInterval;
    }

    public void setAutoCommitInterval(String autoCommitInterval) {
        this.autoCommitInterval = autoCommitInterval;
    }

    public String getAutoOffsetReset() {
        return autoOffsetReset;
    }

    public void setAutoOffsetReset(String autoOffsetReset) {
        this.autoOffsetReset = autoOffsetReset;
    }

    public String getRebalanceRetriesMax() {
        return rebalanceRetriesMax;
    }

    public void setRebalanceRetriesMax(String rebalanceRetriesMax) {
        this.rebalanceRetriesMax = rebalanceRetriesMax;
    }
}
