/*
 * Copyright 2002-2014 the original author or authors.
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
package org.springframework.integration.aws.config.xml;

import static org.springframework.integration.aws.config.xml.AmazonWSParserUtils.getAmazonWSCredentials;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.config.xml.AbstractOutboundChannelAdapterParser;
import org.springframework.messaging.MessageHandler;
import org.w3c.dom.Element;

/**
 * The common adapter parser for all AWS Outbound channel adapters
 *
 * @author Amol Nayak
 * @author Rob Harrop
 *
 * @since 0.5
 *
 */
public abstract class AbstractAWSOutboundChannelAdapterParser extends
		AbstractOutboundChannelAdapterParser {

	/* (non-Javadoc)
	 * @see org.springframework.integration.config.xml.AbstractOutboundChannelAdapterParser#parseConsumer(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
	 */
	@Override
	protected final AbstractBeanDefinition parseConsumer(Element element,
			ParserContext parserContext) {
		String awsCredentialsGeneratedName = getAmazonWSCredentials(element,parserContext);

		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(getMessageHandlerImplementation());
		builder.addConstructorArgReference(awsCredentialsGeneratedName);
		processBeanDefinition(builder,awsCredentialsGeneratedName,element,parserContext);
		return builder.getBeanDefinition();
	}

	protected abstract Class<? extends MessageHandler> getMessageHandlerImplementation();

	/**
	 * The subclasses can override this method to set additional attributes and perform some
	 * additional operations on the {@link BeanDefinitionBuilder}
	 *
	 * @param builder
	 * @param awsCredentialsGeneratedName
	 * @param element
	 * @param context
	 */
	protected void processBeanDefinition(BeanDefinitionBuilder builder,String awsCredentialsGeneratedName,
				Element element,ParserContext context) {
		//Default implementation does nothing
	}

}
