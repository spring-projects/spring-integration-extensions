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
 * Hazelcast Outbound Channel Adapter Parser parses int-hazelcast:outbound-channel-adapter
 * xml definition. It also validates and returns the created BeanDefinition object.
 * 
 * @author Eren Avsarogullari
 * @since 1.0.0
 *
 */
public class HazelcastOutboundChannelAdapterParser extends AbstractOutboundChannelAdapterParser {

	private static final String CACHE_ATTRIBUTE = "cache";

	private static final String DISTRIBUTED_OBJECT = "distributedObject";

	@Override
	protected AbstractBeanDefinition parseConsumer(Element element, ParserContext parserContext) {
		BeanDefinitionBuilder hazelcastOutboundChannelAdapter = BeanDefinitionBuilder
				.genericBeanDefinition(HazelcastCacheWritingMessageHandler.class);

		if (!StringUtils.hasText(element.getAttribute(CACHE_ATTRIBUTE))) {
			parserContext.getReaderContext().error(
					"'" + CACHE_ATTRIBUTE + "' attribute is required.", element);
		}

		hazelcastOutboundChannelAdapter.addPropertyReference(DISTRIBUTED_OBJECT,
				element.getAttribute(CACHE_ATTRIBUTE));

		return hazelcastOutboundChannelAdapter.getBeanDefinition();
	}

}
