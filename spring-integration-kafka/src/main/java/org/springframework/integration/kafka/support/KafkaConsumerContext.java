package org.springframework.integration.kafka.support;

/**
 * Created with IntelliJ IDEA.
 * User: chackos
 * Date: 3/25/13
 * Time: 5:16 PM
 * To change this template use File | Settings | File Templates.
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
