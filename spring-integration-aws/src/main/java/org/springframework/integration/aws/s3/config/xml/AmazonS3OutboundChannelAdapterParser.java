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
package org.springframework.integration.aws.s3.config.xml;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.integration.aws.config.xml.AbstractAWSOutboundChannelAdapterParser;
import org.springframework.integration.aws.s3.AmazonS3MessageHandler;
import org.springframework.integration.aws.s3.DefaultFileNameGenerationStrategy;
import org.springframework.integration.aws.s3.core.DefaultAmazonS3Operations;
import org.springframework.integration.config.ExpressionFactoryBean;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.springframework.messaging.MessageHandler;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * The namespace parser for outbound-channel-parser for the aws-s3 namespace
 *
 * @author Amol Nayak
 * @author Rob Harrop
 *
 * @since 0.5
 *
 */
public class AmazonS3OutboundChannelAdapterParser extends
		AbstractAWSOutboundChannelAdapterParser {

	private static final String S3_OPERATIONS					=	"s3-operations";
	private static final String AWS_ENDPOINT					=	"aws-endpoint";
	private static final String S3_BUCKET 						= 	"bucket";
	private static final String CHARSET 						=	"charset";
	private static final String MULTIPART_THRESHOLD				=	"multipart-upload-threshold";
	private static final String TEMPORARY_DIRECTORY				=	"temporary-directory";
	private static final String TEMPORARY_SUFFIX				=	"temporary-suffix";
	private static final String THREADPOOL_EXECUTOR				=	"thread-pool-executor";
	private static final String REMOTE_DIRECTORY				=	"remote-directory";
	private static final String REMOTE_DIRECTORY_EXPRESSION		=	"remote-directory-expression";
	private static final String FILE_NAME_GENERATOR				=	"file-name-generator";
	private static final String FILE_NAME_GENERATION_EXPRESSION	=	"file-name-generation-expression";


	/* (non-Javadoc)
	 * @see org.springframework.integration.aws.core.config.AbstractAWSOutboundChannelAdapterParser#getMessageHandlerImplementation()
	 */

	@Override
	protected Class<? extends MessageHandler> getMessageHandlerImplementation() {
		return AmazonS3MessageHandler.class;
	}

	/**
	 * This is where we will be instantiating the AmazonS3Operations instance and
	 * passing it to the MessageHandler
	 */
	@Override
	protected void processBeanDefinition(BeanDefinitionBuilder builder,
			String  awsCredentialsGeneratedName,Element element, ParserContext context) {

		//TODO: When we will have more than one implementations, also provision with an enum
		//for the operation

		String s3Operations = element.getAttribute(S3_OPERATIONS);
		String operationsService;
		if(StringUtils.hasText(s3Operations)) {
			//custom implementation provided
			if(element.hasAttribute(MULTIPART_THRESHOLD)
					|| element.hasAttribute(TEMPORARY_DIRECTORY)
					|| element.hasAttribute(TEMPORARY_SUFFIX)
					|| element.hasAttribute(THREADPOOL_EXECUTOR)) {
				throw new BeanDefinitionStoreException("Attributes '" + MULTIPART_THRESHOLD + "', '"
						+ TEMPORARY_DIRECTORY + "', '" + TEMPORARY_SUFFIX + "' and '" + THREADPOOL_EXECUTOR
						+ " are mutually exclusive to the '" + S3_OPERATIONS + "' attribute");
			}
			operationsService = s3Operations;
		}
		else {
			BeanDefinitionBuilder s3OpBuilder = BeanDefinitionBuilder.genericBeanDefinition(DefaultAmazonS3Operations.class);
			s3OpBuilder.addConstructorArgReference(awsCredentialsGeneratedName);
			IntegrationNamespaceUtils.setValueIfAttributeDefined(s3OpBuilder, element, MULTIPART_THRESHOLD);
			IntegrationNamespaceUtils.setValueIfAttributeDefined(s3OpBuilder, element, TEMPORARY_DIRECTORY);
			IntegrationNamespaceUtils.setValueIfAttributeDefined(s3OpBuilder, element, TEMPORARY_SUFFIX,"temporaryFileSuffix");
			IntegrationNamespaceUtils.setReferenceIfAttributeDefined(s3OpBuilder, element, THREADPOOL_EXECUTOR);
			IntegrationNamespaceUtils.setValueIfAttributeDefined(s3OpBuilder, element, AWS_ENDPOINT);
			operationsService = BeanDefinitionReaderUtils.registerWithGeneratedName(s3OpBuilder.getBeanDefinition(), context.getRegistry());
		}

		//Set the bucket and charset
		builder.addConstructorArgReference(operationsService);
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, CHARSET);
		builder.addPropertyValue(S3_BUCKET, element.getAttribute(S3_BUCKET));		//Mandatory

		//Get the remote directory expression or remote directory literal string
		String remoteDirectoryLiteral = element.getAttribute(REMOTE_DIRECTORY);
		String remoteDirectoryExpression = element.getAttribute(REMOTE_DIRECTORY_EXPRESSION);
		boolean hasRemoteDirectoryExpression = StringUtils.hasText(remoteDirectoryExpression);
		boolean hasRemoteDirectoryLiteral = StringUtils.hasText(remoteDirectoryLiteral);
		if(!(hasRemoteDirectoryExpression ^ hasRemoteDirectoryLiteral)) {
			throw new BeanDefinitionStoreException("Exactly one of " + REMOTE_DIRECTORY + " or "
					+ REMOTE_DIRECTORY_EXPRESSION + " is required");
		}
		AbstractBeanDefinition expression;
		if(hasRemoteDirectoryLiteral) {
			expression = BeanDefinitionBuilder.genericBeanDefinition(LiteralExpression.class)
			.addConstructorArgValue(remoteDirectoryLiteral)
			.getBeanDefinition();
		}
		else {
			expression = BeanDefinitionBuilder.genericBeanDefinition(ExpressionFactoryBean.class)
			.addConstructorArgValue(remoteDirectoryExpression)
			.getBeanDefinition();
		}
		builder.addPropertyValue("remoteDirectoryExpression", expression);

		//Get the File generation strategy
		String fileNameGenerator = element.getAttribute(FILE_NAME_GENERATOR);
		String fileNameGenerationExpression = element.getAttribute(FILE_NAME_GENERATION_EXPRESSION);
		boolean hasFileGenerator = StringUtils.hasText(fileNameGenerator);
		boolean hasFileGenerationExpression = StringUtils.hasText(fileNameGenerationExpression);

		if(hasFileGenerationExpression && hasFileGenerator) {
			throw new BeanDefinitionStoreException("Attributes '" + FILE_NAME_GENERATION_EXPRESSION + "' and '"
					+ FILE_NAME_GENERATOR + "' are mutually exclusive, at most one might be specified");
		}

		if(hasFileGenerator) {
			builder.addPropertyReference("fileNameGenerator", fileNameGenerator);
		}
		else {
			BeanDefinitionBuilder fileNameGeneratorBuilder =
			BeanDefinitionBuilder.genericBeanDefinition(DefaultFileNameGenerationStrategy.class);
			String tempDirectorySuffix = element.getAttribute(TEMPORARY_SUFFIX);
			if(StringUtils.hasText(tempDirectorySuffix)) {
				fileNameGeneratorBuilder.addPropertyValue("temporarySuffix",tempDirectorySuffix);
			}
			if(hasFileGenerationExpression) {
				fileNameGeneratorBuilder.addPropertyValue("fileNameExpression", fileNameGenerationExpression);
			}
			builder.addPropertyValue("fileNameGenerator", fileNameGeneratorBuilder.getBeanDefinition());
		}
	}
}
