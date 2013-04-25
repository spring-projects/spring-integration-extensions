package org.springframework.integration.kafka.config.xml;

import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.springframework.integration.kafka.support.ConsumerConfiguration;
import org.springframework.integration.kafka.support.KafkaConsumerContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Soby Chacko
 */
public class KafkaConsumerContextParser extends AbstractSimpleBeanDefinitionParser {

    @Override
    protected Class<?> getBeanClass(Element element) {
        return KafkaConsumerContext.class;
    }

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);

        Element topics = DomUtils.getChildElementByTagName(element, "consumer-configurations");
        parseConsumerConfigurations(topics, parserContext);
    }

    private void parseConsumerConfigurations(Element topics, ParserContext parserContext) {

        for (Element consumerConfiguration : DomUtils.getChildElementsByTagName(topics, "consumer-configuration")) {
            BeanDefinitionBuilder consumerConfigurationBuilder = BeanDefinitionBuilder.genericBeanDefinition(ConsumerConfiguration.class);

            IntegrationNamespaceUtils.setReferenceIfAttributeDefined(consumerConfigurationBuilder, consumerConfiguration, "group-id");

            IntegrationNamespaceUtils.setReferenceIfAttributeDefined(consumerConfigurationBuilder, consumerConfiguration, "topic");
            IntegrationNamespaceUtils.setReferenceIfAttributeDefined(consumerConfigurationBuilder, consumerConfiguration, "streams");
            IntegrationNamespaceUtils.setReferenceIfAttributeDefined(consumerConfigurationBuilder, consumerConfiguration, "value-encoder");
            IntegrationNamespaceUtils.setReferenceIfAttributeDefined(consumerConfigurationBuilder, consumerConfiguration, "key-encoder");
            IntegrationNamespaceUtils.setValueIfAttributeDefined(consumerConfigurationBuilder, consumerConfiguration, "key-class-type");
            IntegrationNamespaceUtils.setValueIfAttributeDefined(consumerConfigurationBuilder, consumerConfiguration, "value-class-type");

            final Map<String, Integer> topicStreamsMap = new HashMap<String, Integer>();
            for (Element topicConfiguration : DomUtils.getChildElementsByTagName(consumerConfiguration, "topic")) {
                final String topic = topicConfiguration.getAttribute("id");
                final String streams = topicConfiguration.getAttribute("streams");
                final Integer streamsInt = Integer.valueOf(streams);
                topicStreamsMap.put(topic, streamsInt);
            }

            consumerConfigurationBuilder.addPropertyValue("topicStreamMap", topicStreamsMap);

            String kafkaServerBeanName = consumerConfiguration.getAttribute("broker-ref");
            if (StringUtils.hasText(kafkaServerBeanName)) {
                consumerConfigurationBuilder.addConstructorArgReference(kafkaServerBeanName);
            }

            AbstractBeanDefinition consumerConfigurationBeanDefinition = consumerConfigurationBuilder.getBeanDefinition();
            final String consumerConfigurationBeanName = "consumerConfiguration_" + consumerConfiguration.getAttribute("topic");
            registerBeanDefinition(new BeanDefinitionHolder(consumerConfigurationBeanDefinition, consumerConfigurationBeanName),
                    parserContext.getRegistry());
        }
    }
}
