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
import org.springframework.integration.config.xml.AbstractInboundGatewayParser;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.springframework.integration.smpp.inbound.SmppInboundGateway;
import org.w3c.dom.Element;

/**
 * The Parser for Smpp Inbound Gateway.
 *
 * @author Johanes Soetanto
 * @since 1.0
 *
 */
public class SmppInboundGatewayParser extends AbstractInboundGatewayParser {

	@Override
	protected Class<?> getBeanClass(Element element) {
		return SmppInboundGateway.class;
	}

	@Override
	protected boolean isEligibleAttribute(String n) {
		return !n.equals("source-address") && !n.equals("source-ton") && !n.equals("smpp-session-ref")
				&& !n.equals("request-mapper") && !n.equals("reply-mapper")
				&& super.isEligibleAttribute(n);
	}

	@Override
	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
		super.doParse(element, parserContext, builder);
		// because session need parserContext
		SmppParserUtils.setSession(element, "smpp-session-ref", "session", "smppSession", parserContext, builder);
	}

	@Override
	protected void doPostProcess(BeanDefinitionBuilder builder, Element e) {
		// value
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, e, "source-address", "defaultSourceAddress");
		SmppParserUtils.setTon(e, "source-ton", "defaultSourceAddressTypeOfNumber", builder);
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, e, "reply-timeout", "replyTimeout");
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, e, "request-timeout", "requestTimeout");

		// reference
		IntegrationNamespaceUtils.setReferenceIfAttributeDefined(builder, e, "request-mapper", "requestMapper");
		IntegrationNamespaceUtils.setReferenceIfAttributeDefined(builder, e, "reply-mapper", "replyMapper");
	}
}
