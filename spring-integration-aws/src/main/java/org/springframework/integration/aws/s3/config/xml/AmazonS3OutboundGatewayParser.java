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

import static org.springframework.integration.aws.s3.config.xml.AmazonS3ParserUtils.setupMessageHandler;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.integration.aws.config.xml.AbstractAWSConsumerEndpointParser;
import org.springframework.integration.aws.s3.AmazonS3OutboundGateway;
import org.springframework.integration.config.ExpressionFactoryBean;
import org.springframework.integration.core.MessageHandler;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * The parser for the AWS S3 outbound gateway
 *
 * @author Amol Nayak
 * @since 0.5
 *
 */
public class AmazonS3OutboundGatewayParser extends
			AbstractAWSConsumerEndpointParser {

	private static final String REMOTE_COMMAND					=	"remote-command";
	private static final String REMOTE_COMMAND_EXPRESSION		=	"remote-command-expression";

	/* (non-Javadoc)
	 * @see org.springframework.integration.aws.config.xml.AbstractAWSOutboundChannelAdapterParser#getMessageHandlerImplementation()
	 */
	@Override
	protected Class<? extends MessageHandler> getMessageHandlerImplementation() {
		return AmazonS3OutboundGateway.class;
	}

	@Override
	protected void processBeanDefinition(
			BeanDefinitionBuilder builder, String awsCredentialsGeneratedName,
			Element element, ParserContext context) {

		setupMessageHandler(builder, awsCredentialsGeneratedName, element, context);

		String remoteCommandLiteral = element.getAttribute(REMOTE_COMMAND);
		String remoteCommandExpression = element.getAttribute(REMOTE_COMMAND_EXPRESSION);
		boolean hasRemoteCommandExpression = StringUtils.hasText(remoteCommandExpression);
		boolean hasRemoteCommandLiteral = StringUtils.hasText(remoteCommandLiteral);
		if(!(hasRemoteCommandExpression ^ hasRemoteCommandLiteral)) {
			throw new BeanDefinitionStoreException("One of " + REMOTE_COMMAND + " or "
					+ REMOTE_COMMAND_EXPRESSION + " is required");
		}
		AbstractBeanDefinition expression;
		if(hasRemoteCommandLiteral) {
			expression = BeanDefinitionBuilder.genericBeanDefinition(LiteralExpression.class)
			.addConstructorArgValue(remoteCommandLiteral)
			.getBeanDefinition();
		}
		else {
			expression = BeanDefinitionBuilder.genericBeanDefinition(ExpressionFactoryBean.class)
			.addConstructorArgValue(remoteCommandExpression)
			.getBeanDefinition();
		}
		builder.addPropertyValue("remoteCommandExpression", expression);
	}
}
