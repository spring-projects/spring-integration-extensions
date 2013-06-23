/**
 * Copyright 2013 Jaroslaw Palka<jaroslaw.palka@symentis.pl>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.integration.jgroups.config;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.jgroups.JGroupsChannelFactoryBean;
import org.springframework.integration.jgroups.XmlConfiguratorFactoryBean;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * 
 * @author Jaroslaw Palka <jaroslaw.palka@symentis.pl>
 * @since 1.0.0
 *
 */
public class JGroupsClusterAdapterParser extends AbstractBeanDefinitionParser {

	@Override
	protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {

		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(JGroupsChannelFactoryBean.class).addPropertyValue(
				"clusterName", element.getAttribute("name"));

		// TODO this is quick and dirty as I don't still get how to work with
		// root and child beans parsing
		NodeList nodeList = element.getElementsByTagName("jgroups:xml-configurator");
		if (nodeList.getLength() == 1) {
			Element configuratorElement = (Element) nodeList.item(0);

			AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(XmlConfiguratorFactoryBean.class)
					.addPropertyValue("resource", configuratorElement.getAttribute("resource")).getBeanDefinition();
			builder.addPropertyValue("protocolStackConfigurator", beanDefinition);// }
		}

		return builder.getBeanDefinition();
	}

	/**
	 * Resolves bean ID to cluster name
	 */
	@Override
	protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext)
			throws BeanDefinitionStoreException {
		return element.getAttribute("name");
	}

}
