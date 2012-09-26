/*
 * Copyright 2011-2012 the original author or authors.
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

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.BeanDefinitionParserDelegate;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.splunk.entity.SplunkServer;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Splunk server element parser.
 *
 * The XML element is like this:
 * <pre>
 * {@code
 * <splunk:server id="splunkServer" host="host" port="8089" userName="admin" password="password" />
 * }
 *
 * @author Jarred Li
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

		String scope = element.getAttribute(BeanDefinitionParserDelegate.SCOPE_ATTRIBUTE);
		if (StringUtils.hasText(scope)) {
			builder.setScope(scope);
		}

		String host = element.getAttribute("host");
		if (StringUtils.hasText(host)) {
			builder.addPropertyValue("host", host);
		}

		String port = element.getAttribute("port");
		if (StringUtils.hasText(port)) {
			builder.addPropertyValue("port", port);
		}

		String scheme = element.getAttribute("scheme");
		if (StringUtils.hasText(scheme)) {
			builder.addPropertyValue("scheme", scheme);
		}

		String app = element.getAttribute("app");
		if (StringUtils.hasText(app)) {
			builder.addPropertyValue("app", app);
		}

		String owner = element.getAttribute("owner");
		if (StringUtils.hasText(owner)) {
			builder.addPropertyValue("owner", owner);
		}

		String userName = element.getAttribute("userName");
		if (StringUtils.hasText(userName)) {
			builder.addPropertyValue("userName", userName);
		}

		String password = element.getAttribute("password");
		if (StringUtils.hasText(password)) {
			builder.addPropertyValue("password", password);
		}
	}


}
