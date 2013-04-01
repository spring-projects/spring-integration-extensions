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

/**
 * Kafka adapter specific message headers.
 *
 * @author Soby Chacko
 * @since 1.0
 */
public class KafkaConsumerDefaults {

    //High level consumer
    public static String GROUP_ID = "groupid";
    public static String SOCKET_TIMEOUT = "30000";
    public static String SOCKET_BUFFER_SIZE = "64*1024";
    public static String FETCH_SIZE = "300 * 1024";
    public static String BACKOFF_INCREMENT = "1000";
    public static String QUEUED_CHUNKS_MAX = "100";
    public static String AUTO_COMMIT_ENABLE = "true";
    public static String AUTO_COMMIT_INTERVAL = "10000";
    public static String AUTO_OFFSET_RESET = "smallest";
    //Overriding the default value of -1, which will make the consumer to wait indefinitely
    public static String CONSUMER_TIMEOUT = "5000";
    public static String REBALANCE_RETRIES_MAX = "4";

	/** Noninstantiable utility class */
	private KafkaConsumerDefaults() {
		throw new AssertionError();
	}
}
