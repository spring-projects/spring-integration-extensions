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

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.aws.config.xml.AbstractAWSOutboundChannelAdapterParser;
import org.springframework.integration.aws.s3.AmazonS3MessageHandler;
import org.springframework.integration.core.MessageHandler;
import org.w3c.dom.Element;

/**
 * The namespace parser for outbound-channel-parser for the aws-s3 namespace
 *
 * @author Amol Nayak
 *
 * @since 0.5
 *
 */
public class AmazonS3OutboundChannelAdapterParser extends
		AbstractAWSOutboundChannelAdapterParser {

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
		setupMessageHandler(builder, awsCredentialsGeneratedName, element,
				context);
	}
}
