package org.springframework.integration.kafka.config.xml;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.config.xml.AbstractOutboundChannelAdapterParser;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.springframework.integration.kafka.outbound.KafkaProducerMessageHandler;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Created with IntelliJ IDEA.
 * User: chackos
 * Date: 3/26/13
 * Time: 12:51 AM
 * To change this template use File | Settings | File Templates.
 */
public class KafkaOutboundChannelAdapterParser extends AbstractOutboundChannelAdapterParser {


    @Override
    protected AbstractBeanDefinition parseConsumer(Element element, ParserContext parserContext) {

        final BeanDefinitionBuilder kafkaProducerMessageHandlerBuilder =
                                BeanDefinitionBuilder.genericBeanDefinition(KafkaProducerMessageHandler.class);


        String kafkaServerBeanName = element.getAttribute("kafka-producer-context-ref");
        if (StringUtils.hasText(kafkaServerBeanName)) {
            kafkaProducerMessageHandlerBuilder.addConstructorArgReference(kafkaServerBeanName);
        }

        IntegrationNamespaceUtils.setReferenceIfAttributeDefined(kafkaProducerMessageHandlerBuilder, element, "kafka-encoder");
        IntegrationNamespaceUtils.setValueIfAttributeDefined(kafkaProducerMessageHandlerBuilder, element, "topic");

        return kafkaProducerMessageHandlerBuilder.getBeanDefinition();
    }
}
