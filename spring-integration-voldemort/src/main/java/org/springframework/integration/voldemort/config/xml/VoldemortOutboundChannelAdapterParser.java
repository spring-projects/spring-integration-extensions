/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.integration.voldemort.config.xml;

import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.config.ExpressionFactoryBean;
import org.springframework.integration.config.xml.AbstractOutboundChannelAdapterParser;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.springframework.integration.voldemort.outbound.VoldemortStoringMessageHandler;
import org.w3c.dom.Element;

/**
 * Parses Voldemort outbound adapter XML definition.
 *
 * @author Lukasz Antoniak
 * @since 1.0
 */
public class VoldemortOutboundChannelAdapterParser extends AbstractOutboundChannelAdapterParser {
	/**
	 * Produces "int-voldemort:outbound-channel-adapter" bean definition.
	 * <p />
	 * {@inheritDoc}
	 */
	@Override
	protected AbstractBeanDefinition parseConsumer(Element element, ParserContext parserContext) {
		final BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition( VoldemortStoringMessageHandler.class );
		VoldemortParserUtils.processCommonAttributes( element, builder );
		final boolean hasKey = element.hasAttribute( VoldemortParserUtils.STORE_KEY );
		final boolean hasKeyExpression = element.hasAttribute( VoldemortParserUtils.STORE_KEY_EXPRESSION );
		if ( hasKey && hasKeyExpression ) {
			parserContext.getReaderContext().error( "At most one of '" + VoldemortParserUtils.STORE_KEY
					+ "' or '" + VoldemortParserUtils.STORE_KEY_EXPRESSION + "' is allowed.", element );
		}
		if ( hasKey ) {
			builder.addPropertyValue(
					VoldemortParserUtils.KEY_PROPERTY,
					new TypedStringValue( element.getAttribute( VoldemortParserUtils.STORE_KEY ) )
			);
		}
		if ( hasKeyExpression ) {
			RootBeanDefinition expressionDef = new RootBeanDefinition( ExpressionFactoryBean.class );
			expressionDef.getConstructorArgumentValues().addGenericArgumentValue( element.getAttribute( VoldemortParserUtils.STORE_KEY_EXPRESSION ) );
			builder.addPropertyValue( VoldemortParserUtils.KEY_EXPRESSION_PROPERTY, expressionDef );
		}
		IntegrationNamespaceUtils.setValueIfAttributeDefined( builder, element, VoldemortParserUtils.PERSIST_MODE );
		return builder.getBeanDefinition();
	}
}
