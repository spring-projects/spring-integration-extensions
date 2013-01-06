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

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.aws.core.BasicAWSCredentials;
import org.springframework.integration.aws.core.PropertiesAWSCredentials;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * The utility class for the namespace parsers
 *
 * @author Amol Nayak
 *
 * @since 1.0
 *
 */
public final class AmazonWSParserUtils {

	public static final String ACCESS_KEY = "accessKey";
	public static final String SECRET_KEY = "secretKey";
	public static final String PROPERTIES_FILE = "propertiesFile";
	public static final String CREDENTIALS_REF = "credentials-ref";

	private AmazonWSParserUtils() {
		throw new AssertionError("Cannot instantiate the utility class");
	}


	/**
	 * Registers the {@link AmazonWSCredentials} bean with the current ApplicationContext if
	 * accessKey and secretKey is given, if the credentials-ref is given, the given value
	 * is returned.
	 *
	 * @param element
	 * @param parserContext
	 * @return
	 */
	public static String getAmazonWSCredentials(Element element,ParserContext parserContext) {
		//TODO: Some mechanism to use the same instance with same ACCESS_KEY to be implemented
		String accessKey = element.getAttribute(ACCESS_KEY);
		String secretKey = element.getAttribute(SECRET_KEY);
		String propertiesFile = element.getAttribute(PROPERTIES_FILE);
		String credentialsRef = element.getAttribute(CREDENTIALS_REF);
		String awsCredentialsGeneratedName;

		if(StringUtils.hasText(credentialsRef)) {
			if(StringUtils.hasText(propertiesFile)
					|| StringUtils.hasText(accessKey)
					|| StringUtils.hasText(secretKey)) {
				parserContext.getReaderContext().error("When " + CREDENTIALS_REF + " is specified, " +
						"do not specify the " + PROPERTIES_FILE + " attribute or the "
						+ SECRET_KEY  + " and " + ACCESS_KEY + " attributes", element);
			}
			awsCredentialsGeneratedName = credentialsRef;
		}
		else {
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
		}

		return awsCredentialsGeneratedName;
	}
}
