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

import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.config.xml.AbstractPollingInboundChannelAdapterParser;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.springframework.integration.hazelcast.common.DistributedSQLIterationType;
import org.springframework.integration.hazelcast.common.HazelcastIntegrationDefinitionValidator;
import org.springframework.integration.hazelcast.inbound.HazelcastDistributedSQLMessageSource;
import org.w3c.dom.Element;

import reactor.util.Assert;
import reactor.util.StringUtils;

/**
 * Hazelcast Distributed SQL Inbound Channel Adapter Parser parses
 * int-hazelcast:ds-inbound-channel-adapter xml definition.
 * 
 * @author Eren Avsarogullari
 * @since 1.0.0
 *
 */
public class HazelcastDistributedSQLInboundChannelAdapterParser extends AbstractPollingInboundChannelAdapterParser {
	
	private static final String CACHE_ATTRIBUTE = "cache";
	private static final String DISTRIBUTED_SQL_ATTRIBUTE = "distributed-sql";
	private static final String ITERATION_TYPE_ATTRIBUTE = "iteration-type";
	private static final String DISTRIBUTED_MAP = "distributedMap";
	private static final String DISTRIBUTED_SQL_PROPERTY = "distributedSQL";
	private static final String ITERATION_TYPE_PROPERTY = "iterationType";
	
	@Override
	protected BeanMetadataElement parseSource(Element element, ParserContext parserContext) {
		if (!StringUtils.hasText(element.getAttribute(CACHE_ATTRIBUTE))) {
			parserContext.getReaderContext().error("'" + CACHE_ATTRIBUTE + "' attribute is required.", element);
		} else if (!StringUtils.hasText(element.getAttribute(DISTRIBUTED_SQL_ATTRIBUTE))) {
			parserContext.getReaderContext().error("'" + DISTRIBUTED_SQL_ATTRIBUTE + "' attribute is required.", element);
		} else if (!StringUtils.hasText(element.getAttribute(ITERATION_TYPE_ATTRIBUTE))) {
			parserContext.getReaderContext().error("'" + ITERATION_TYPE_ATTRIBUTE + "' attribute is required.", element);
		}
		
		Assert.isTrue(HazelcastIntegrationDefinitionValidator.validateEnumType(DistributedSQLIterationType.class, element.getAttribute(ITERATION_TYPE_ATTRIBUTE)));
		
		BeanDefinitionBuilder sourceBuilder = BeanDefinitionBuilder.genericBeanDefinition(HazelcastDistributedSQLMessageSource.class.getName());
		IntegrationNamespaceUtils.setReferenceIfAttributeDefined(sourceBuilder, element, CACHE_ATTRIBUTE, DISTRIBUTED_MAP);
		IntegrationNamespaceUtils.setValueIfAttributeDefined(sourceBuilder, element, DISTRIBUTED_SQL_ATTRIBUTE, DISTRIBUTED_SQL_PROPERTY);
		IntegrationNamespaceUtils.setValueIfAttributeDefined(sourceBuilder, element, ITERATION_TYPE_ATTRIBUTE, ITERATION_TYPE_PROPERTY);
		
		return sourceBuilder.getBeanDefinition();
	}
}
