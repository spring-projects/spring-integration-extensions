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
package org.springframework.integration.kafka.config.xml;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.config.xml.AbstractPollingInboundChannelAdapterParser;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.springframework.integration.kafka.inbound.HighLevelConsumerMessageSource;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * The Kafka Inbound Channel adapter parser
 *
 * @author Soby Chacko
 * @since 1.0
 *
 */
public class KafkaInboundChannelAdapterParser extends AbstractPollingInboundChannelAdapterParser{

    protected BeanMetadataElement parseSource(Element element, ParserContext parserContext) {

        final BeanDefinitionBuilder highLevelConsumerMessageSourceBuilder =
                        BeanDefinitionBuilder.genericBeanDefinition(HighLevelConsumerMessageSource.class);

        Element pollerElement = DomUtils.getChildElementByTagName(element, "poller");
        IntegrationNamespaceUtils.setValueIfAttributeDefined(highLevelConsumerMessageSourceBuilder, pollerElement, "receive-timeout");
        IntegrationNamespaceUtils.setValueIfAttributeDefined(highLevelConsumerMessageSourceBuilder, pollerElement, "max-messages-per-poll");

        IntegrationNamespaceUtils.setReferenceIfAttributeDefined(highLevelConsumerMessageSourceBuilder, element, "kafka-decoder");

        String kafkaConsumerContext = element.getAttribute("kafka-consumer-context-ref");
        if (StringUtils.hasText(kafkaConsumerContext)) {
            highLevelConsumerMessageSourceBuilder.addConstructorArgReference(kafkaConsumerContext);
        }

        return highLevelConsumerMessageSourceBuilder.getBeanDefinition();
	}
}
