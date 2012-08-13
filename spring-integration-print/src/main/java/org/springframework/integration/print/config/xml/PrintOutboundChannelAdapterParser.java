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

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.config.xml.AbstractOutboundChannelAdapterParser;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.springframework.integration.print.outbound.PrintMessageHandler;
import org.springframework.integration.print.support.ChromaticityEnum;
import org.springframework.integration.print.support.MediaSizeNameEnum;
import org.springframework.integration.print.support.MediaTrayEnum;
import org.springframework.integration.print.support.PrintSides;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;


/**
 * The parser for the Print Outbound Channel Adapter.
 *
 * @author Gunnar Hillert
 * @since 1.0
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
		final BeanDefinitionBuilder printServiceExecutorBuilder = PrintParserUtils.getPrintServiceExecutorBuilder(element, parserContext);

		final BeanDefinition printServiceExecutorBeanDefinition = printServiceExecutorBuilder.getBeanDefinition();

		final String channelAdapterId = this.resolveId(element, printServiceExecutorBuilder.getRawBeanDefinition(), parserContext);
		final String printServiceExecutorBeanName = channelAdapterId + ".jpaExecutor";

		parserContext.registerBeanComponent(new BeanComponentDefinition(printServiceExecutorBeanDefinition, printServiceExecutorBeanName));

		printOutboundChannelAdapterBuilder.addConstructorArgReference(printServiceExecutorBeanName);

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

		final String sides = element.getAttribute("sides");

		if (StringUtils.hasText(sides)) {
			final PrintSides printSides = PrintSides.fromString(sides);
			printOutboundChannelAdapterBuilder.addPropertyValue("sides", printSides.getSides());
		}

		final String mediaSizeName = element.getAttribute("media-size-name");

		if (StringUtils.hasText(mediaSizeName)) {
			printOutboundChannelAdapterBuilder.addPropertyValue("mediaSizeName", MediaSizeNameEnum.getForString(mediaSizeName));
		}

		final String mediaTray = element.getAttribute("media-tray");

		if (StringUtils.hasText(mediaTray)) {
			printOutboundChannelAdapterBuilder.addPropertyValue("mediaTray", MediaTrayEnum.getForString(mediaTray));
		}

		final String chromaticity = element.getAttribute("chromaticity");

		if (StringUtils.hasText(chromaticity)) {
			printOutboundChannelAdapterBuilder.addPropertyValue("chromaticity", ChromaticityEnum.getForString(chromaticity));
		}

		IntegrationNamespaceUtils.setValueIfAttributeDefined(printOutboundChannelAdapterBuilder, element, "copies");

		return printOutboundChannelAdapterBuilder.getBeanDefinition();

	}

}
