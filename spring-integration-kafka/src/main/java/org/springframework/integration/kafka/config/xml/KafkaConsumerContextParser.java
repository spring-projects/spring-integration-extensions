package org.springframework.integration.kafka.config.xml;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.springframework.integration.kafka.support.ConsumerConfiguration;
import org.springframework.integration.kafka.support.ConsumerConnectorFactoryBean;
import org.springframework.integration.kafka.support.ConsumerMetadata;
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
            BeanDefinitionBuilder consumerMetadataBuilder = BeanDefinitionBuilder.genericBeanDefinition(ConsumerMetadata.class);

            IntegrationNamespaceUtils.setReferenceIfAttributeDefined(consumerMetadataBuilder, consumerConfiguration, "group-id");

            IntegrationNamespaceUtils.setReferenceIfAttributeDefined(consumerMetadataBuilder, consumerConfiguration, "value-encoder");
            IntegrationNamespaceUtils.setReferenceIfAttributeDefined(consumerMetadataBuilder, consumerConfiguration, "key-encoder");
            IntegrationNamespaceUtils.setValueIfAttributeDefined(consumerMetadataBuilder, consumerConfiguration, "key-class-type");
            IntegrationNamespaceUtils.setValueIfAttributeDefined(consumerMetadataBuilder, consumerConfiguration, "value-class-type");
            IntegrationNamespaceUtils.setValueIfAttributeDefined(consumerConfigurationBuilder, consumerConfiguration, "max-messages");

            final Map<String, Integer> topicStreamsMap = new HashMap<String, Integer>();
            for (Element topicConfiguration : DomUtils.getChildElementsByTagName(consumerConfiguration, "topic")) {
                final String topic = topicConfiguration.getAttribute("id");
                final String streams = topicConfiguration.getAttribute("streams");
                final Integer streamsInt = Integer.valueOf(streams);
                topicStreamsMap.put(topic, streamsInt);
            }

            consumerMetadataBuilder.addPropertyValue("topicStreamMap", topicStreamsMap);

            String kafkaServerBeanName = consumerConfiguration.getAttribute("broker-ref");
            if (StringUtils.hasText(kafkaServerBeanName)) {
                consumerMetadataBuilder.addConstructorArgReference(kafkaServerBeanName);
            }

            BeanDefinition consumerMetadataBeanDef = consumerMetadataBuilder.getBeanDefinition();
            registerBeanDefinition(new BeanDefinitionHolder(consumerMetadataBeanDef, "consumerMetadata_" + consumerConfiguration.getAttribute("group_id")),
                    parserContext.getRegistry());

            BeanDefinitionBuilder consumerConnectorFactoryBuilder = BeanDefinitionBuilder.genericBeanDefinition(ConsumerConnectorFactoryBean.class);
            consumerConnectorFactoryBuilder.addConstructorArgReference("consumerMetadata_" + consumerConfiguration.getAttribute("group_id"));
            if (StringUtils.hasText(kafkaServerBeanName)) {
                consumerConnectorFactoryBuilder.addConstructorArgReference(kafkaServerBeanName);
            }

            BeanDefinition consumerConnectorfactoryBeanDefinition = consumerConnectorFactoryBuilder.getBeanDefinition();
            registerBeanDefinition(new BeanDefinitionHolder(consumerConnectorfactoryBeanDefinition, "consumerConnectorFactory_" + consumerConfiguration.getAttribute("group_id")), parserContext.getRegistry());

            consumerConfigurationBuilder.addConstructorArgReference("consumerMetadata_" + consumerConfiguration.getAttribute("group-id"));
            consumerConfigurationBuilder.addConstructorArgReference("consumerConnectorFactory_" + consumerConfiguration.getAttribute("group-id"));
            AbstractBeanDefinition consumerConfigurationBeanDefinition = consumerConfigurationBuilder.getBeanDefinition();
            final String consumerConfigurationBeanName = "consumerConfiguration_" + consumerConfiguration.getAttribute("group-id");
            registerBeanDefinition(new BeanDefinitionHolder(consumerConfigurationBeanDefinition, consumerConfigurationBeanName),
                    parserContext.getRegistry());
        }
    }
}
