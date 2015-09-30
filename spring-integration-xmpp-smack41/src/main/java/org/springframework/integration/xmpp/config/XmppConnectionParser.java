/*
 * Copyright 2002-2015 the original author or authors.
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

package org.springframework.integration.xmpp.config;

import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.w3c.dom.Element;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.springframework.util.StringUtils;

/**
 * Parser for 'xmpp:xmpp-connection' element
 * @author Oleg Zhurakousky
 * @author Artem Bilan
 * @since 2.0
 */
public class XmppConnectionParser extends AbstractSingleBeanDefinitionParser {

	private static String[] connectionFactoryAttributes =
			new String[]{"user", "password", "resource", "subscription-mode"};

	@Override
	protected Class<?> getBeanClass(Element element) {
		return XmppConnectionFactoryBean.class;
	}

	@Override
	protected boolean shouldGenerateIdAsFallback() {
		return false;
	}

	@Override
	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
		String serviceName = element.getAttribute("service-name");
		String host = element.getAttribute("host");
		String port = element.getAttribute("port");

		if (!StringUtils.hasText(serviceName)) {
			serviceName = host;
		}

		BeanDefinitionBuilder connectionConfigurationBuilder =
				BeanDefinitionBuilder.genericBeanDefinition(XMPPTCPConnectionConfiguration.class)
						.setFactoryMethod("builder")
						.addPropertyValue("host", host)
						.addPropertyValue("port", port)
						.addPropertyValue("serviceName", serviceName);

		builder.addConstructorArgValue(connectionConfigurationBuilder.getBeanDefinition());

		for (String attribute : connectionFactoryAttributes) {
			IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, attribute);
		}
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, IntegrationNamespaceUtils.AUTO_STARTUP);
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, IntegrationNamespaceUtils.PHASE);
	}

}
