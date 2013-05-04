package org.springframework.integration.kafka.config.xml;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.springframework.integration.kafka.support.ConsumerConfigFactoryBean;
import org.springframework.integration.kafka.support.ConsumerConfiguration;
import org.springframework.integration.kafka.support.ConsumerConnectionProvider;
import org.springframework.integration.kafka.support.ConsumerMetadata;
import org.springframework.integration.kafka.support.KafkaConsumerContext;
import org.springframework.integration.kafka.support.MessageLeftOverTracker;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Soby Chacko
 */
public class KafkaConsumerContextParser extends AbstractSingleBeanDefinitionParser {

    @Override
    protected Class<?> getBeanClass(Element element) {
        return KafkaConsumerContext.class;
    }

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);

        Element consumerConfigurations = DomUtils.getChildElementByTagName(element, "consumer-configurations");
        parseConsumerConfigurations(consumerConfigurations, parserContext, builder, element);
    }

    private void parseConsumerConfigurations(Element consumerConfigurations, ParserContext parserContext, BeanDefinitionBuilder builder, Element parentElem) {

        for (Element consumerConfiguration : DomUtils.getChildElementsByTagName(consumerConfigurations, "consumer-configuration")) {
            BeanDefinitionBuilder consumerConfigurationBuilder = BeanDefinitionBuilder.genericBeanDefinition(ConsumerConfiguration.class);
            BeanDefinitionBuilder consumerMetadataBuilder = BeanDefinitionBuilder.genericBeanDefinition(ConsumerMetadata.class);

            IntegrationNamespaceUtils.setValueIfAttributeDefined(consumerMetadataBuilder, consumerConfiguration, "group-id");

            IntegrationNamespaceUtils.setReferenceIfAttributeDefined(consumerMetadataBuilder, consumerConfiguration, "value-decoder");
            IntegrationNamespaceUtils.setReferenceIfAttributeDefined(consumerMetadataBuilder, consumerConfiguration, "key-decoder");
            IntegrationNamespaceUtils.setValueIfAttributeDefined(consumerMetadataBuilder, consumerConfiguration, "key-class-type");
            IntegrationNamespaceUtils.setValueIfAttributeDefined(consumerMetadataBuilder, consumerConfiguration, "value-class-type");
            IntegrationNamespaceUtils.setValueIfAttributeDefined(consumerConfigurationBuilder, consumerConfiguration, "max-messages");
            IntegrationNamespaceUtils.setValueIfAttributeDefined(consumerMetadataBuilder, parentElem, "consumer-timeout");

            final Map<String, Integer> topicStreamsMap = new HashMap<String, Integer>();
            for (Element topicConfiguration : DomUtils.getChildElementsByTagName(consumerConfiguration, "topic")) {
                final String topic = topicConfiguration.getAttribute("id");
                final String streams = topicConfiguration.getAttribute("streams");
                final Integer streamsInt = Integer.valueOf(streams);
                topicStreamsMap.put(topic, streamsInt);
            }

            consumerMetadataBuilder.addPropertyValue("topicStreamMap", topicStreamsMap);
            BeanDefinition consumerMetadataBeanDef = consumerMetadataBuilder.getBeanDefinition();
            registerBeanDefinition(new BeanDefinitionHolder(consumerMetadataBeanDef, "consumerMetadata_" + consumerConfiguration.getAttribute("group-id")),
                    parserContext.getRegistry());

            String zookeeperConnectBean = parentElem.getAttribute("zookeeper-connect");
            IntegrationNamespaceUtils.setReferenceIfAttributeDefined(builder, parentElem, zookeeperConnectBean);

            BeanDefinitionBuilder consumerConfigFactoryBuilder = BeanDefinitionBuilder.genericBeanDefinition(ConsumerConfigFactoryBean.class);
            consumerConfigFactoryBuilder.addConstructorArgReference("consumerMetadata_" + consumerConfiguration.getAttribute("group-id"));
            if (StringUtils.hasText(zookeeperConnectBean)) {
                consumerConfigFactoryBuilder.addConstructorArgReference(zookeeperConnectBean);
            }

            BeanDefinition consumerConfigFactoryBuilderBeanDefinition = consumerConfigFactoryBuilder.getBeanDefinition();
            registerBeanDefinition(new BeanDefinitionHolder(consumerConfigFactoryBuilderBeanDefinition, "consumerConfigFactory_" + consumerConfiguration.getAttribute("group-id")), parserContext.getRegistry());

            BeanDefinitionBuilder consumerConnectionProviderBuilder = BeanDefinitionBuilder.genericBeanDefinition(ConsumerConnectionProvider.class);
            consumerConnectionProviderBuilder.addConstructorArgReference("consumerConfigFactory_" + consumerConfiguration.getAttribute("group-id"));

            BeanDefinition consumerConnectionProviderBuilderBeanDefinition = consumerConnectionProviderBuilder.getBeanDefinition();
            registerBeanDefinition(new BeanDefinitionHolder(consumerConnectionProviderBuilderBeanDefinition, "consumerConnectionProvider_" + consumerConfiguration.getAttribute("group-id")), parserContext.getRegistry());


            BeanDefinitionBuilder messageLeftOverBeanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(MessageLeftOverTracker.class);
            BeanDefinition messageLeftOverBeanDefinition = messageLeftOverBeanDefinitionBuilder.getBeanDefinition();
            registerBeanDefinition(new BeanDefinitionHolder(messageLeftOverBeanDefinition, "messageLeftOver_" + consumerConfiguration.getAttribute("group-id")),
                    parserContext.getRegistry());

            consumerConfigurationBuilder.addConstructorArgReference("consumerMetadata_" + consumerConfiguration.getAttribute("group-id"));
            consumerConfigurationBuilder.addConstructorArgReference("consumerConnectionProvider_" + consumerConfiguration.getAttribute("group-id"));
            consumerConfigurationBuilder.addConstructorArgReference("messageLeftOver_" + consumerConfiguration.getAttribute("group-id"));
            AbstractBeanDefinition consumerConfigurationBeanDefinition = consumerConfigurationBuilder.getBeanDefinition();
            final String consumerConfigurationBeanName = "consumerConfiguration_" + consumerConfiguration.getAttribute("group-id");
            registerBeanDefinition(new BeanDefinitionHolder(consumerConfigurationBeanDefinition, consumerConfigurationBeanName),
                    parserContext.getRegistry());
        }
    }
}
