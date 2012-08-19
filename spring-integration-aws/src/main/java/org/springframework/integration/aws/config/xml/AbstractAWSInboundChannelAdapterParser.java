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

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.aws.core.AmazonWSCredentials;
import org.springframework.integration.aws.core.BasicAWSCredentials;
import org.springframework.integration.aws.core.PropertiesAWSCredentials;
import org.springframework.integration.config.xml.AbstractPollingInboundChannelAdapterParser;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * The Abstract AWS inbound channel adapter parser
 *
 * @author Amol Nayak
 *
 * @since 1.0
 *
 */
public abstract class AbstractAWSInboundChannelAdapterParser extends
		AbstractPollingInboundChannelAdapterParser {

	/**
	 * Registers the {@link AmazonWSCredentials} bean with the current ApplicationContext
	 * @param element
	 * @param parserContext
	 * @return
	 */
	protected String registerAmazonWSCredentials(Element element,ParserContext parserContext) {
		String accessKey = element.getAttribute(ACCESS_KEY);
		String secretKey = element.getAttribute(SECRET_KEY);
		String propertiesFile = element.getAttribute(PROPERTIES_FILE);
		String awsCredentialsGeneratedName;
		if(StringUtils.hasText(propertiesFile)) {
			Assert.isTrue(!StringUtils.hasText(accessKey) && !StringUtils.hasText(secretKey),
			"When " + ACCESS_KEY + " and " + SECRET_KEY + " are specified, do not specify the " + PROPERTIES_FILE + " attribute");
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


}
