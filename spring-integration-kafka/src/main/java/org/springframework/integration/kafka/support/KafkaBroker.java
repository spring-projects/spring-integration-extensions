package org.springframework.integration.kafka.support;

/**
 * @author Soby Chacko
 */
public class KafkaBroker {

    private String zkConnect = KafkaBrokerDefaults.ZK_CONNECT;
    private String zkConnectionTimeout = KafkaBrokerDefaults.ZK_CONNECTION_TIMEOUT;
    private String zkSessionTimeout = KafkaBrokerDefaults.ZK_SESSION_TIMEOUT;
    private String zkSyncTime = KafkaBrokerDefaults.ZK_SYNC_TIME;

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
}
