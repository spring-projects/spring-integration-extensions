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
package org.springframework.integration.smpp.config.xml;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.config.xml.AbstractChannelAdapterParser;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.springframework.integration.smpp.inbound.SmppInboundChannelAdapter;
import org.w3c.dom.Element;

/**
 * The Smpp Inbound Channel adapter parser
 *
 * @author Johanes Soetanto
 * @since 1.0
 *
 */
public class SmppInboundChannelAdapterParser extends AbstractChannelAdapterParser {

	@Override
	protected boolean shouldGenerateId() {
		return false;
	}

	@Override
	protected boolean shouldGenerateIdAsFallback() {
		return true;
	}

	@Override
	protected AbstractBeanDefinition doParse(Element e, ParserContext context, String channelName) {
		final BeanDefinitionBuilder builder = BeanDefinitionBuilder
				.genericBeanDefinition(SmppInboundChannelAdapter.class);
		SmppParserUtils.setSession(e, "smpp-session-ref", "session", "smppSession", context, builder);
		builder.addPropertyReference("channel", channelName);
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, e, "auto-startup","autoStartup");
		return builder.getBeanDefinition();
	}

}
