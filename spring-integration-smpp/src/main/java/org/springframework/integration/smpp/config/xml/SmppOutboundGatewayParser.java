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
package org.springframework.integration.smpp.config.xml;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.config.xml.AbstractConsumerEndpointParser;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.springframework.integration.smpp.outbound.SmppOutboundGateway;
import org.w3c.dom.Element;

/**
 * The Parser for Smpp Outbound Gateway.
 *
 * @author Johanes Soetanto
 * @since 1.0
 *
 */
public class SmppOutboundGatewayParser extends AbstractConsumerEndpointParser  {
	@Override
	protected BeanDefinitionBuilder parseHandler(Element e, ParserContext parserContext) {
		final BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(SmppOutboundGateway.class);
		// value attributes
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, e, "source-address", "defaultSourceAddress");
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, e, "reply-timeout", "sendTimeout");
		SmppParserUtils.setTon(e, "source-ton", "defaultSourceAddressTypeOfNumber", builder);
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, e, "order");
		// reference attributes
		SmppParserUtils.setSession(e, "smpp-session-ref", "session", "smppSession", parserContext, builder);
		IntegrationNamespaceUtils.setReferenceIfAttributeDefined(builder, e, "reply-channel", "outputChannel");
		IntegrationNamespaceUtils.setReferenceIfAttributeDefined(builder, e, "time-formatter", "timeFormatter");
		return builder;
	}

	@Override
	protected String getInputChannelAttributeName() {
		return "request-channel";
	}
}
