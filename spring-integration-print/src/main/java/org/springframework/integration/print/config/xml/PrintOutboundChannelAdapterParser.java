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
package org.springframework.integration.print.config.xml;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.config.xml.AbstractOutboundChannelAdapterParser;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.springframework.integration.print.outbound.PrintMessageHandler;
import org.springframework.integration.print.support.PrintSides;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;


/**
 * The parser for the Print Outbound Channel Adapter.
 *
 * @author Gunnar Hillert
 * @since 2.2
 *
 */
public class PrintOutboundChannelAdapterParser extends AbstractOutboundChannelAdapterParser {

	@Override
	protected boolean shouldGenerateId() {
		return false;
	}

	@Override
	protected boolean shouldGenerateIdAsFallback() {
		return true;
	}

	@Override
	protected AbstractBeanDefinition parseConsumer(Element element, ParserContext parserContext) {

		final BeanDefinitionBuilder printOutboundChannelAdapterBuilder = BeanDefinitionBuilder.genericBeanDefinition(PrintMessageHandler.class);

		String docFlavor = element.getAttribute("doc-flavor");
		String mimeType = element.getAttribute("mime-type");

		if ((StringUtils.hasText(docFlavor) && !StringUtils.hasText(mimeType))
				|| (!StringUtils.hasText(docFlavor) && StringUtils.hasText(mimeType))) {
			parserContext.getReaderContext().error("Both the 'doc-flavor' and the 'mime-type' attribute are required", element);
		}
		else if (StringUtils.hasText(docFlavor) && StringUtils.hasText(mimeType)) {
			printOutboundChannelAdapterBuilder.addConstructorArgValue(mimeType);
			printOutboundChannelAdapterBuilder.addConstructorArgValue(docFlavor);
		}

		String sides = element.getAttribute("sides");

		PrintSides printSides = PrintSides.fromString(sides);

		printOutboundChannelAdapterBuilder.addPropertyValue("sides", printSides.getSides());

		IntegrationNamespaceUtils.setValueIfAttributeDefined(printOutboundChannelAdapterBuilder, element, "copies");

		return printOutboundChannelAdapterBuilder.getBeanDefinition();

	}

}
