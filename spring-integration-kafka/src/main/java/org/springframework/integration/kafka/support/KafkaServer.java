package org.springframework.integration.kafka.support;

/**
 * @author Soby Chacko
 */
public class KafkaServer {

    private String zkConnect = KafkaZooKeeperServerDefaults.ZK_CONNECT;
    private String zkConnectionTimeout = KafkaZooKeeperServerDefaults.ZK_CONNECTION_TIMEOUT;
    private String zkSessionTimeout = KafkaZooKeeperServerDefaults.ZK_SESSION_TIMEOUT;
    private String zkSyncTime = KafkaZooKeeperServerDefaults.ZK_SYNC_TIME;
    private String autoCommitInterval = KafkaZooKeeperServerDefaults.AUTO_COMMIT_INTERVAL;
    private String groupId;

    public String getZkConnect() {
        return zkConnect;
    }

    public void setZkConnect(String zkConnect) {
        this.zkConnect = zkConnect;
    }

    public String getZkConnectionTimeout() {
        return zkConnectionTimeout;
    }

    public void setZkConnectionTimeout(String zkConnectionTimeout) {
        this.zkConnectionTimeout = zkConnectionTimeout;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getZkSessionTimeout() {
        return zkSessionTimeout;
    }

    public void setZkSessionTimeout(String zkSessionTimeout) {
        this.zkSessionTimeout = zkSessionTimeout;
    }

    public String getZkSyncTime() {
        return zkSyncTime;
    }

    public void setZkSyncTime(String zkSyncTime) {
        this.zkSyncTime = zkSyncTime;
    }

    public String getAutoCommitInterval() {
        return autoCommitInterval;
    }

    public void setAutoCommitInterval(String autoCommitInterval) {
        this.autoCommitInterval = autoCommitInterval;
    }
}
