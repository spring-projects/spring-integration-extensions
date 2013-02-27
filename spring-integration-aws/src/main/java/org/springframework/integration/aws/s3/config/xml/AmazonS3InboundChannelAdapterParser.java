/*
 * Copyright 2002-2013 the original author or authors.
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
package org.springframework.integration.aws.s3.config.xml;
import static org.springframework.integration.aws.config.xml.AmazonWSParserUtils.getAmazonWSCredentials;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.expression.Expression;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.integration.aws.s3.AmazonS3InboundSynchronizationMessageSource;
import org.springframework.integration.config.xml.AbstractPollingInboundChannelAdapterParser;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;


/**
 * The channel adapter parser for the S3 inbound parser
 *
 * @author Amol Nayak
 *
 * @since 0.5
 *
 */
public class AmazonS3InboundChannelAdapterParser extends
	AbstractPollingInboundChannelAdapterParser  {

	private static final String S3_BUCKET 						= 	"bucket";
	private static final String TEMPORARY_SUFFIX				=	"temporary-suffix";
	private static final String S3_OPERATIONS					=	"s3-operations";
	private static final String AWS_ENDPOINT					=	"aws-endpoint";
	private static final String REMOTE_DIRECTORY				=	"remote-directory";
	private static final String LOCAL_DIRECTORY					=	"local-directory";
	private static final String LOCAL_DIRECTORY_EXPRESSION		=	"local-directory-expression";
	private static final String AWS_CREDENTIAL					=	"credentials";
	private static final String MAX_OBJECTS_PER_BATCH			=	"max-objects-per-batch";
	private static final String FILE_NAME_WILDCARD				=	"file-name-wildcard";
	private static final String FILE_NAME_REGEX					=	"file-name-regex";
	private static final String ACCEPT_SUB_FOLDERS				=	"accept-sub-folders";

	/* (non-Javadoc)
	 * @see org.springframework.integration.config.xml.AbstractPollingInboundChannelAdapterParser#parseSource(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
	 */
	@Override
	protected BeanMetadataElement parseSource(Element element,
			ParserContext parserContext) {
		String awsCredentials = getAmazonWSCredentials(element, parserContext);
		BeanDefinitionBuilder builder = BeanDefinitionBuilder
			.genericBeanDefinition(AmazonS3InboundSynchronizationMessageSource.class);
		builder.addPropertyReference(AWS_CREDENTIAL, awsCredentials);
		IntegrationNamespaceUtils.setReferenceIfAttributeDefined(builder, element, S3_OPERATIONS);
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, S3_BUCKET);
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, TEMPORARY_SUFFIX);
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, REMOTE_DIRECTORY);
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, AWS_ENDPOINT);
		String directory = element.getAttribute(LOCAL_DIRECTORY);
		String directoryExpression = element.getAttribute(LOCAL_DIRECTORY_EXPRESSION);
		boolean hasDirectory = StringUtils.hasText(directory);
		boolean hasDirectoryExpression = StringUtils.hasText(directoryExpression);
		if(!hasDirectory && !hasDirectoryExpression) {
			String message =
				String.format("One of attributes '%s' and '%s' is required", LOCAL_DIRECTORY, LOCAL_DIRECTORY_EXPRESSION);
			throw new BeanDefinitionStoreException(message);
		}
		if(hasDirectory && hasDirectoryExpression) {
			String message =
				String.format("Attributes '%s' and '%s' are mutually exclusive to each other", LOCAL_DIRECTORY, LOCAL_DIRECTORY_EXPRESSION);
			throw new BeanDefinitionStoreException(message);
		}
		else {
			Expression expr;
			if(hasDirectory) {
				expr = new LiteralExpression(directory);
			}
			else {
				expr = new SpelExpressionParser().parseExpression(directoryExpression);
			}
			builder.addPropertyValue("directory", expr);
		}
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, MAX_OBJECTS_PER_BATCH);
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, ACCEPT_SUB_FOLDERS);
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, FILE_NAME_WILDCARD);
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, FILE_NAME_REGEX);
		return builder.getBeanDefinition();
	}

}
