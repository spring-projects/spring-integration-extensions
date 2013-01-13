/*
 * Copyright 2002-2012 the original author or authors.
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
package org.springframework.integration.voldemort.config.xml;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.config.xml.AbstractPollingInboundChannelAdapterParser;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.springframework.integration.voldemort.inbound.VoldemortMessageSource;
import org.w3c.dom.Element;

/**
 * Parses Voldemort inbound adapter XML definition.
 *
 * @author Lukasz Antoniak
 * @since 1.0
 */
public class VoldemortInboundChannelAdapterParser extends AbstractPollingInboundChannelAdapterParser {
	/**
	 * Produces "int-voldemort:inbound-channel-adapter" bean definition.
	 * <p />
	 * {@inheritDoc}
	 */
	@Override
	protected BeanMetadataElement parseSource(Element element, ParserContext parserContext) {
		final BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition( VoldemortMessageSource.class );
		VoldemortParserUtils.processCommonAttributes( element, builder );
		final RootBeanDefinition queryExpressionDef =
				IntegrationNamespaceUtils.createExpressionDefinitionFromValueOrExpression(
						VoldemortParserUtils.SEARCH_KEY, VoldemortParserUtils.SEARCH_KEY_EXPRESSION,
						parserContext, element, true
				);
		builder.addPropertyValue( VoldemortParserUtils.KEY_EXPRESSION_PROPERTY, queryExpressionDef );
		IntegrationNamespaceUtils.setValueIfAttributeDefined( builder, element, VoldemortParserUtils.DELETE_AFTER_POLL );
		return builder.getBeanDefinition();
	}
}
