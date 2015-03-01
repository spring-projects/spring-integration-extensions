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

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.springframework.integration.hazelcast.common.CacheEventType;
import org.springframework.integration.hazelcast.common.HazelcastIntegrationDefinitionValidator;
import org.springframework.integration.hazelcast.inbound.HazelcastEventDrivenMessageProducer;
import org.w3c.dom.Element;

import reactor.util.Assert;
import reactor.util.StringUtils;

/**
 * HazelcastEventDrivenInboundChannelAdapterParser
 * 
 * @author Eren Avsarogullari
 * @since 1.0.0
 *
 */
public class HazelcastEventDrivenInboundChannelAdapterParser extends AbstractSingleBeanDefinitionParser {
	
	private static final String CHANNEL = "channel";
	private static final String CACHE = "cache";
	private static final String CACHE_EVENTS = "cache-events";
	private static final String OUTPUT_CHANNEL = "outputChannel";
	private static final String DISTRIBUTED_OBJECT = "distributedObject";
	private static final String CACHE_EVENT_TYPES = "cacheEventTypes";
	
	@Override
	protected String getBeanClassName(Element element) {
		return HazelcastEventDrivenMessageProducer.class.getName();
	}

	@Override
	protected boolean shouldGenerateIdAsFallback() {
		return false;
	}

	@Override
	protected boolean shouldGenerateId() {
		return true;
	}

	@Override
	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
		if (!StringUtils.hasText(element.getAttribute(CHANNEL))) {
			parserContext.getReaderContext().error("'" + CHANNEL + "' attribute is required.", element);
		} else if (!StringUtils.hasText(element.getAttribute(CACHE))) {
			parserContext.getReaderContext().error("'" + CACHE + "' attribute is required.", element);
		} else if (!StringUtils.hasText(element.getAttribute(CACHE_EVENTS))) {
			parserContext.getReaderContext().error("'" + CACHE_EVENTS + "' attribute is required.", element);
		}
		
		Assert.isTrue(HazelcastIntegrationDefinitionValidator.validateEnumType(CacheEventType.class, element.getAttribute(CACHE_EVENTS)));
		
		IntegrationNamespaceUtils.setReferenceIfAttributeDefined(builder, element, CHANNEL, OUTPUT_CHANNEL);
		IntegrationNamespaceUtils.setReferenceIfAttributeDefined(builder, element, CACHE, DISTRIBUTED_OBJECT);
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, CACHE_EVENTS, CACHE_EVENT_TYPES);
	}
	
}
