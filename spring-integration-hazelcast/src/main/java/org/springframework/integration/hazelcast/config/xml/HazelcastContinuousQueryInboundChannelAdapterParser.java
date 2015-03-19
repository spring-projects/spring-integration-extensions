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

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.springframework.integration.hazelcast.inbound.HazelcastContinuousQueryMessageProducer;

/**
 * Hazelcast Continuous Query Inbound Channel Adapter Parser is a
 * {@link org.springframework.beans.factory.xml.BeanDefinitionParser} implementation. It parses
 * {@code <int-hazelcast:cq-inbound-channel-adapter/>} configuration and defines just a single
 * {@link org.springframework.beans.factory.config.BeanDefinition}.
 *
 * @author Eren Avsarogullari
 * @since 1.0.0
 */
public class HazelcastContinuousQueryInboundChannelAdapterParser extends AbstractSingleBeanDefinitionParser {

	private static final String CHANNEL_ATTRIBUTE = "channel";

	private static final String CACHE_ATTRIBUTE = "cache";

	private static final String CACHE_EVENTS_ATTRIBUTE = "cache-events";

	private static final String PREDICATE_ATTRIBUTE = "predicate";

	private static final String OUTPUT_CHANNEL = "outputChannel";

	private static final String CACHE_EVENT_TYPES = "cacheEventTypes";

	@Override
	protected Class<?> getBeanClass(Element element) {
		return HazelcastContinuousQueryMessageProducer.class;
	}

	@Override
	protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext)
			throws BeanDefinitionStoreException {
		String id = super.resolveId(element, definition, parserContext);

		if (!element.hasAttribute(CHANNEL_ATTRIBUTE)) {
			id = id + ".adapter";
		}

		if (!StringUtils.hasText(id)) {
			id = BeanDefinitionReaderUtils.generateBeanName(definition, parserContext.getRegistry());
		}

		return id;
	}

	@Override
	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
		String channelName = element.getAttribute(CHANNEL_ATTRIBUTE);
		if (!StringUtils.hasText(channelName)) {
			channelName = IntegrationNamespaceUtils.createDirectChannel(element, parserContext);
		}

		if (!StringUtils.hasText(element.getAttribute(CACHE_ATTRIBUTE))) {
			parserContext.getReaderContext().error("'" + CACHE_ATTRIBUTE + "' attribute is required.", element);
		}
		else if (!StringUtils.hasText(element.getAttribute(CACHE_EVENTS_ATTRIBUTE))) {
			parserContext.getReaderContext().error("'" + CACHE_EVENTS_ATTRIBUTE + "' attribute is required.", element);
		}
		else if (!StringUtils.hasText(element.getAttribute(PREDICATE_ATTRIBUTE))) {
			parserContext.getReaderContext().error("'" + PREDICATE_ATTRIBUTE + "' attribute is required.", element);
		}

		builder.addPropertyReference(OUTPUT_CHANNEL, channelName);
		builder.addConstructorArgReference(element.getAttribute(CACHE_ATTRIBUTE));
		builder.addConstructorArgValue(element.getAttribute(PREDICATE_ATTRIBUTE));
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, CACHE_EVENTS_ATTRIBUTE, CACHE_EVENT_TYPES);
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, IntegrationNamespaceUtils.AUTO_STARTUP);
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, IntegrationNamespaceUtils.PHASE);
	}

}
