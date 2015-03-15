/*
 * Copyright 2015 the original author or authors.
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

package org.springframework.integration.hazelcast.config.xml;

import org.w3c.dom.Element;

import reactor.util.StringUtils;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.config.xml.AbstractOutboundChannelAdapterParser;
import org.springframework.integration.hazelcast.outbound.HazelcastCacheWritingMessageHandler;

/**
 * Hazelcast Outbound Channel Adapter Parser parses {@code <int-hazelcast:inbound-channel-adapter />}
 * configuration. It also validates and returns the created
 * {@link org.springframework.beans.factory.config.BeanDefinition} object.
 *
 * @author Eren Avsarogullari
 * @since 1.0.0
 *
 */
public class HazelcastOutboundChannelAdapterParser extends AbstractOutboundChannelAdapterParser {

	private static final String CACHE_ATTRIBUTE = "cache";

	@Override
	protected AbstractBeanDefinition parseConsumer(Element element, ParserContext parserContext) {
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(HazelcastCacheWritingMessageHandler.class);

		if (!StringUtils.hasText(element.getAttribute(CACHE_ATTRIBUTE))) {
			parserContext.getReaderContext().error("'" + CACHE_ATTRIBUTE + "' attribute is required.", element);
		}

		builder.addConstructorArgReference(element.getAttribute(CACHE_ATTRIBUTE));

		return builder.getBeanDefinition();
	}

}
