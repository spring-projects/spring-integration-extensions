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
package org.springframework.integration.kafka.inbound;

import org.springframework.integration.Message;
import org.springframework.integration.context.IntegrationObjectSupport;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.kafka.core.KafkaConsumerDefaults;
import org.springframework.integration.kafka.support.KafkaConsumerContext;

import java.util.List;

/**
 * @author Soby Chacko
 *
 */
public class KafkaHighLevelConsumerMessageSource extends IntegrationObjectSupport implements MessageSource<List<Object>> {

    private final KafkaConsumerContext kafkaConsumerContext;

    private int maxMessagesPerPoll = 1;
    private String receiveTimeout = KafkaConsumerDefaults.CONSUMER_TIMEOUT;

    public KafkaHighLevelConsumerMessageSource(final KafkaConsumerContext kafkaConsumerContext) {
        this.kafkaConsumerContext = kafkaConsumerContext;
    }

    @Override
    public Message<List<Object>> receive() {
        return kafkaConsumerContext.receive(maxMessagesPerPoll);
    }

    @Override
    public String getComponentType() {
        return "kafka:inbound-channel-adapter";
    }

    public int getMaxMessagesPerPoll() {
        return maxMessagesPerPoll;
    }

    public void setMaxMessagesPerPoll(int maxMessagesPerPoll) {
        this.maxMessagesPerPoll = maxMessagesPerPoll;
    }

    public String getReceiveTimeout() {
        return receiveTimeout;
    }

    public void setReceiveTimeout(String receiveTimeout) {
        this.receiveTimeout = receiveTimeout;
    }
}
