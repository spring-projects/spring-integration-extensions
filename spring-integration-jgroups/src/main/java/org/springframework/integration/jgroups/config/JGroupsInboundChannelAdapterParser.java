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

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.config.xml.AbstractChannelAdapterParser;
import org.springframework.integration.jgroups.JGroupsInboundEndpoint;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 *
 * @author Jaroslaw Palka <jaroslaw.palka@symentis.pl>
 * @since 1.0.0
 *
 */
public class JGroupsInboundChannelAdapterParser extends AbstractChannelAdapterParser {


	@Override
	protected AbstractBeanDefinition doParse(Element element, ParserContext parserContext, String channelName) {

		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(JGroupsInboundEndpoint.class);

		builder.addConstructorArgReference(element.getAttribute("cluster"));

		String headerMapperBeanName = element.getAttribute("header-mapper");
		if(StringUtils.hasText(headerMapperBeanName)){
			builder.addConstructorArgReference(headerMapperBeanName);
		}

		builder.addPropertyReference("outputChannel", channelName);

		return builder.getBeanDefinition();
	}
}

