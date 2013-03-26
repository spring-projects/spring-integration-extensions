package org.springframework.integration.kafka.config.xml;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.BeanDefinitionParserDelegate;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.springframework.integration.kafka.support.KafkaBroker;
import org.w3c.dom.Element;

/**
 * @author Soby Chacko
 */
public class KafkaBrokerParser extends AbstractSimpleBeanDefinitionParser {

    @Override
    protected Class<?> getBeanClass(Element element) {
        return KafkaBroker.class;
    }

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);
        IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element,
                BeanDefinitionParserDelegate.SCOPE_ATTRIBUTE);
        IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "zk-connect");
        IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "zk-connection-timeout");
        IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "zk-session-timeout");
        IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "zk-sync-time");
    }
}
