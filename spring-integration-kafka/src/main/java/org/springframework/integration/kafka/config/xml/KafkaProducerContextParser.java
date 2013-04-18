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

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.springframework.integration.kafka.support.KafkaProducerContext;
import org.springframework.integration.kafka.support.ProducerFactoryBean;
import org.springframework.integration.kafka.support.TopicMetadata;
import org.springframework.integration.kafka.support.TopicConfiguration;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * @author Soby Chacko
 */
public class KafkaProducerContextParser extends AbstractSimpleBeanDefinitionParser {

    @Override
    protected Class<?> getBeanClass(Element element) {
        return KafkaProducerContext.class;
    }

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);

        Element topics = DomUtils.getChildElementByTagName(element, "topic-configurations");
        parseTopicConfigurations(topics, parserContext);
    }

    private void parseTopicConfigurations(Element topics, ParserContext parserContext) {

        for (Element topic : DomUtils.getChildElementsByTagName(topics, "topic-configuration")){
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(TopicConfiguration.class);

            BeanDefinitionBuilder topicBuilder = BeanDefinitionBuilder.genericBeanDefinition(TopicMetadata.class);
            topicBuilder.addConstructorArgValue(topic.getAttribute("topic"));
            IntegrationNamespaceUtils.setReferenceIfAttributeDefined(topicBuilder, topic, "kafka-encoder");
            IntegrationNamespaceUtils.setReferenceIfAttributeDefined(topicBuilder, topic, "kafka-key-encoder");
            IntegrationNamespaceUtils.setValueIfAttributeDefined(topicBuilder, topic, "key-class");
            IntegrationNamespaceUtils.setValueIfAttributeDefined(topicBuilder, topic, "value-class");
            IntegrationNamespaceUtils.setValueIfAttributeDefined(topicBuilder, topic, "partitioner");
            IntegrationNamespaceUtils.setValueIfAttributeDefined(topicBuilder, topic, "compression-codec");

            BeanDefinition topicBeanDef = topicBuilder.getBeanDefinition();
            registerBeanDefinition(new BeanDefinitionHolder(topicBeanDef, "topic_" + topic.getAttribute("topic")),
                                        parserContext.getRegistry());

            BeanDefinitionBuilder producerFactoryBuilder = BeanDefinitionBuilder.genericBeanDefinition(ProducerFactoryBean.class);
            IntegrationNamespaceUtils.setValueIfAttributeDefined(producerFactoryBuilder, topic, "broker-list");
            producerFactoryBuilder.addConstructorArgReference("topic_" + topic.getAttribute("topic"));

            BeanDefinition bd = producerFactoryBuilder.getBeanDefinition();
            registerBeanDefinition(new BeanDefinitionHolder(bd, "prodFactory_"+topic.getAttribute("topic")), parserContext.getRegistry());

            builder.addConstructorArgReference("topic_" + topic.getAttribute("topic"));

            builder.addPropertyReference("producer", "prodFactory_" + topic.getAttribute("topic"));

            AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
            registerBeanDefinition(new BeanDefinitionHolder(beanDefinition, parserContext.getReaderContext()
                    .generateBeanName(beanDefinition)), parserContext.getRegistry());
        }
    }
}
