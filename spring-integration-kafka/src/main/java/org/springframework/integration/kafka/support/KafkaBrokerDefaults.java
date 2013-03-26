package org.springframework.integration.kafka.support;

/**
 * Created with IntelliJ IDEA.
 * User: chackos
 * Date: 3/23/13
 * Time: 11:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class KafkaBrokerDefaults {

    public static String ZK_CONNECT = "localhost:2181";
    public static String ZK_CONNECTION_TIMEOUT = "6000";
    public static String ZK_SESSION_TIMEOUT = "6000";
    public static String ZK_SYNC_TIME = "2000";

    /** Noninstantiable utility class */
    private KafkaBrokerDefaults() {
        throw new AssertionError();
    }
}
