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
package org.springframework.integration.aws.config.xml;

import static org.springframework.integration.aws.core.CommonConstants.ACCESS_KEY;
import static org.springframework.integration.aws.core.CommonConstants.PROPERTIES_FILE;
import static org.springframework.integration.aws.core.CommonConstants.SECRET_KEY;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.aws.core.AmazonWSCredentials;
import org.springframework.integration.aws.core.BasicAWSCredentials;
import org.springframework.integration.aws.core.PropertiesAWSCredentials;
import org.springframework.integration.config.xml.AbstractOutboundChannelAdapterParser;
import org.springframework.integration.core.MessageHandler;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * The common adapter parser for all AWS Outbound channel adapters
 *
 * @author Amol Nayak
 *
 * @since 1.0
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
		String awsCredentialsGeneratedName = registerAmazonWSCredentials(element,parserContext);

		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(getMessageHandlerImplementation());
		builder.addConstructorArgReference(awsCredentialsGeneratedName);
		processBeanDefinition(builder,awsCredentialsGeneratedName,element,parserContext);
		return builder.getBeanDefinition();
	}

	/**
	 * Registers the {@link AmazonWSCredentials} bean with the current ApplicationContext
	 *
	 * @param element
	 * @param parserContext
	 * @return
	 */
	protected String registerAmazonWSCredentials(Element element,ParserContext parserContext) {
		//TODO: Some mechanism to use the same instance with same ACCESS_KEY to be implemented
		String accessKey = element.getAttribute(ACCESS_KEY);
		String secretKey = element.getAttribute(SECRET_KEY);
		String propertiesFile = element.getAttribute(PROPERTIES_FILE);
		String awsCredentialsGeneratedName;
		if(StringUtils.hasText(propertiesFile)) {
			if(StringUtils.hasText(accessKey) && StringUtils.hasText(secretKey)) {
				parserContext.getReaderContext().error("When " + ACCESS_KEY + " and " + SECRET_KEY +
						" are specified, do not specify the " + PROPERTIES_FILE + " attribute", element);
			}

			BeanDefinitionBuilder builder =
				BeanDefinitionBuilder.genericBeanDefinition(PropertiesAWSCredentials.class);
			builder.addConstructorArgValue(propertiesFile);
			awsCredentialsGeneratedName = BeanDefinitionReaderUtils.registerWithGeneratedName(
					builder.getBeanDefinition(), parserContext.getRegistry());
		} else {
			BeanDefinitionBuilder builder
			= BeanDefinitionBuilder.genericBeanDefinition(BasicAWSCredentials.class);
			builder.addConstructorArgValue(accessKey);
			builder.addConstructorArgValue(secretKey);
			awsCredentialsGeneratedName = BeanDefinitionReaderUtils.registerWithGeneratedName(
					builder.getBeanDefinition(), parserContext.getRegistry());

		}
		return awsCredentialsGeneratedName;
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
