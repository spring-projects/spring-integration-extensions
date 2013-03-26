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
package org.springframework.integration.kafka.inbound;

import org.springframework.integration.Message;
import org.springframework.integration.context.IntegrationObjectSupport;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.kafka.core.KafkaExecutor;
import org.springframework.integration.kafka.support.KafkaConsumerContext;
import org.springframework.integration.support.MessageBuilder;

import java.util.List;

/**
 * @author Soby Chacko
 * @since 1.0
 *
 */
public class HighLevelConsumerMessageSource extends IntegrationObjectSupport implements MessageSource<List<Object>> {

    private final KafkaExecutor kafkaExecutor;
    private final KafkaConsumerContext kafkaConsumerContext;

    public HighLevelConsumerMessageSource(final KafkaExecutor kafkaExecutor, final KafkaConsumerContext kafkaConsumerContext) {
        this.kafkaExecutor = kafkaExecutor;
        this.kafkaConsumerContext = kafkaConsumerContext;
    }

    @Override
    public Message<List<Object>> receive() {
        List<Object> payload = kafkaExecutor.poll(kafkaConsumerContext);
        if (payload == null) {
            return null;
        }
        return MessageBuilder.withPayload(payload).build();
    }

    @Override
    public String getComponentType() {
        return "kafka:inbound-channel-adapter";
    }
}
