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

package org.springframework.integration.splunk.config.xml;

import org.w3c.dom.Element;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.config.xml.AbstractPollingInboundChannelAdapterParser;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.springframework.integration.splunk.inbound.SplunkPollingChannelAdapter;
import org.springframework.integration.splunk.support.SplunkDataReader;
import org.springframework.integration.splunk.support.SplunkServiceFactory;
import org.springframework.util.StringUtils;

/**
 * The Splunk Inbound Channel adapter parser
 *
 * @author Jarred Li
 * @author Olivier Lamy
 * @since 1.0
 *
 */
public class SplunkInboundChannelAdapterParser extends AbstractPollingInboundChannelAdapterParser {


	protected BeanMetadataElement parseSource(Element element, ParserContext parserContext) {

		BeanDefinitionBuilder splunkPollingChannelAdapterBuilder =
				BeanDefinitionBuilder.genericBeanDefinition(SplunkPollingChannelAdapter.class);

		BeanDefinitionBuilder splunkExecutorBuilder = SplunkParserUtils.getSplunkExecutorBuilder(element, parserContext);

		BeanDefinitionBuilder splunkDataReaderBuilder = BeanDefinitionBuilder.genericBeanDefinition(SplunkDataReader.class);
		IntegrationNamespaceUtils.setValueIfAttributeDefined(splunkDataReaderBuilder, element, "mode");
		IntegrationNamespaceUtils.setValueIfAttributeDefined(splunkDataReaderBuilder, element, "count");
		IntegrationNamespaceUtils.setValueIfAttributeDefined(splunkDataReaderBuilder, element, "field-list");
		IntegrationNamespaceUtils.setValueIfAttributeDefined(splunkDataReaderBuilder, element, "search");
		IntegrationNamespaceUtils.setValueIfAttributeDefined(splunkDataReaderBuilder, element, "saved-search");
		IntegrationNamespaceUtils.setValueIfAttributeDefined(splunkDataReaderBuilder, element, "owner");
		IntegrationNamespaceUtils.setValueIfAttributeDefined(splunkDataReaderBuilder, element, "app");
		IntegrationNamespaceUtils.setValueIfAttributeDefined(splunkDataReaderBuilder, element, "init-earliest-time");
		IntegrationNamespaceUtils.setValueIfAttributeDefined(splunkDataReaderBuilder, element, "earliest-time");
		IntegrationNamespaceUtils.setValueIfAttributeDefined(splunkDataReaderBuilder, element, "latest-time");

		// initialize splunk servers references
		BeanDefinitionBuilder serviceFactoryBuilder = BeanDefinitionBuilder.genericBeanDefinition(SplunkServiceFactory.class);

		String splunkServerBeanNames = element.getAttribute("splunk-server-ref");
		if (StringUtils.hasText(splunkServerBeanNames)) {

			ManagedList<RuntimeBeanReference> splunkServersList = new ManagedList<RuntimeBeanReference>();

			for (String splunkServerBeanName : StringUtils.commaDelimitedListToStringArray(splunkServerBeanNames)) {
				splunkServersList.add(new RuntimeBeanReference(splunkServerBeanName));
			}
			serviceFactoryBuilder.addConstructorArgValue(splunkServersList);
			splunkDataReaderBuilder.addConstructorArgValue(serviceFactoryBuilder.getBeanDefinition());
		}

		String channelAdapterId = this.resolveId(element, splunkPollingChannelAdapterBuilder.getRawBeanDefinition(),
				parserContext);
		String splunkExecutorBeanName = channelAdapterId + ".splunkExecutor";
		String splunkDataReaderBeanName = splunkExecutorBeanName + ".reader";

		parserContext.registerBeanComponent(new BeanComponentDefinition(splunkDataReaderBuilder.getBeanDefinition(),
				splunkDataReaderBeanName));
		splunkExecutorBuilder.addPropertyReference("reader", splunkDataReaderBeanName);

		BeanDefinition splunkExecutorBuilderBeanDefinition = splunkExecutorBuilder.getBeanDefinition();
		parserContext.registerBeanComponent(new BeanComponentDefinition(splunkExecutorBuilderBeanDefinition,
				splunkExecutorBeanName));

		splunkPollingChannelAdapterBuilder.addConstructorArgReference(splunkExecutorBeanName);

		return splunkPollingChannelAdapterBuilder.getBeanDefinition();
	}

}
