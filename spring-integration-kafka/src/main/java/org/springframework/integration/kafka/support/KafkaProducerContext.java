package org.springframework.integration.kafka.support;

/**
 * Created with IntelliJ IDEA.
 * User: chackos
 * Date: 3/26/13
 * Time: 12:30 AM
 * To change this template use File | Settings | File Templates.
 */
public class KafkaProducerContext {

    private String zkConnect = "127.0.0.1:2181";
    private String brokerList = "localhost:9092";

    public String getZkConnect() {
        return zkConnect;
    }

    public void setZkConnect(String zkConnect) {
        this.zkConnect = zkConnect;
    }

    public String getBrokerList() {
        return brokerList;
    }

    public void setBrokerList(String brokerList) {
        this.brokerList = brokerList;
    }
}
