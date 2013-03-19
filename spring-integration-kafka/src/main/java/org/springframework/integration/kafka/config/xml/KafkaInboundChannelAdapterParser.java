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
package org.springframework.integration.kafka.config.xml;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
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

        BeanDefinitionBuilder kafkaExecutorBuilder = KafkaParserUtils.getKafkaExecutorBuilder(element, parserContext);

        String kafkaServerBeanName = element.getAttribute("kafka-server-ref");
        if (StringUtils.hasText(kafkaServerBeanName)) {
            kafkaExecutorBuilder.addConstructorArgReference(kafkaServerBeanName);
        }
        Element pollerElement = DomUtils.getChildElementByTagName(element, "poller");
        IntegrationNamespaceUtils.setValueIfAttributeDefined(kafkaExecutorBuilder, pollerElement, "receive-timeout");
        IntegrationNamespaceUtils.setValueIfAttributeDefined(kafkaExecutorBuilder, pollerElement, "max-messages-per-poll");

        String channelAdapterId = this.resolveId(element, highLevelConsumerMessageSourceBuilder.getRawBeanDefinition(),
        				parserContext);
        String kafkaExecutorBeanName = channelAdapterId + ".kafkaExecutor";

        BeanDefinition kafkaExecutorBuilderBeanDefinition = kafkaExecutorBuilder.getBeanDefinition();
        parserContext.registerBeanComponent(new BeanComponentDefinition(kafkaExecutorBuilderBeanDefinition,
                kafkaExecutorBeanName));

        highLevelConsumerMessageSourceBuilder.addConstructorArgReference(kafkaExecutorBeanName);

        IntegrationNamespaceUtils.setValueIfAttributeDefined(highLevelConsumerMessageSourceBuilder, element, "topic");
        IntegrationNamespaceUtils.setValueIfAttributeDefined(highLevelConsumerMessageSourceBuilder, element, "partitionCount");

        return highLevelConsumerMessageSourceBuilder.getBeanDefinition();
	}
}
