/*
 * Copyright 2011-2014 the original author or authors.
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

package org.springframework.integration.splunk.config.xml;

import org.w3c.dom.Element;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.BeanDefinitionParserDelegate;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.springframework.integration.splunk.support.SplunkServer;

/**
 * Splunk server element parser.
 *
 * The XML element is like this:
 * <pre class="code">
 * {@code
 * <splunk:server id="splunkServer" host="host" port="8089" username="admin" password="password"
 *                scheme="https" owner="admin" app="search"/>
 * }
 * </pre>
 *
 * @author Jarred Li
 * @author Olivier Lamy
 * @since 1.0
 *
 */
public class SplunkServerParser extends AbstractSimpleBeanDefinitionParser {

	@Override
	public Class<?> getBeanClass(Element element) {
		return SplunkServer.class;
	}

	@Override
	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
		super.doParse(element, parserContext, builder);
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element,
				BeanDefinitionParserDelegate.SCOPE_ATTRIBUTE);
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "host");
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "port");
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "scheme");
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "app");
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "owner");
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "username");
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "password");
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "timeout");
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "checkServiceOnBorrow");

	}

}
