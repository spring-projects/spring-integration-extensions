package org.springframework.integration.kafka.support;

import kafka.serializer.Decoder;
import org.springframework.integration.kafka.core.KafkaConsumerDefaults;

import java.util.Map;

/**
 * @author Soby Chacko
 */
public class ConsumerMetadata<K,V> {

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
    private String consumerTimeout = KafkaConsumerDefaults.CONSUMER_TIMEOUT;

    private String topic;
    private int streams;
    private Decoder<V> valueDecoder;
    private Decoder<K> keyDecoder;
    private Map<String, Integer> topicStreamMap;

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

    public String getConsumerTimeout() {
        return consumerTimeout;
    }

    public void setConsumerTimeout(String consumerTimeout) {
        this.consumerTimeout = consumerTimeout;
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

    public Decoder<V> getValueDecoder() {
        return valueDecoder;
    }

    public void setValueDecoder(Decoder<V> valueDecoder) {
        this.valueDecoder = valueDecoder;
    }

    public Decoder<K> getKeyDecoder() {
        return keyDecoder;
    }

    public void setKeyDecoder(Decoder<K> keyDecoder) {
        this.keyDecoder = keyDecoder;
    }

    public Map<String, Integer> getTopicStreamMap() {
        return topicStreamMap;
    }

    public void setTopicStreamMap(Map<String, Integer> topicStreamMap) {
        this.topicStreamMap = topicStreamMap;
    }
}
