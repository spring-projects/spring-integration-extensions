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
package org.springframework.integration.splunk.config.xml;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.config.xml.AbstractPollingInboundChannelAdapterParser;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.springframework.integration.splunk.inbound.SplunkPollingChannelAdapter;
import org.springframework.integration.splunk.support.ConnectionFactoryFactoryBean;
import org.springframework.integration.splunk.support.SplunkConnectionFactory;
import org.springframework.integration.splunk.support.SplunkDataReader;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * The Splunk Inbound Channel adapter parser
 *
 * @author Jarred Li
 * @since 1.0
 *
 */
public class SplunkInboundChannelAdapterParser extends AbstractPollingInboundChannelAdapterParser {


	protected BeanMetadataElement parseSource(Element element, ParserContext parserContext) {

		BeanDefinitionBuilder splunkPollingChannelAdapterBuilder = BeanDefinitionBuilder.genericBeanDefinition(SplunkPollingChannelAdapter.class);

		BeanDefinitionBuilder splunkExecutorBuilder = SplunkParserUtils.getSplunkExecutorBuilder(element, parserContext);

		BeanDefinitionBuilder splunkDataReaderBuilder = BeanDefinitionBuilder.genericBeanDefinition(SplunkDataReader.class);
		IntegrationNamespaceUtils.setValueIfAttributeDefined(splunkDataReaderBuilder, element, "mode");
		String count = element.getAttribute("count");
		if (StringUtils.hasText(count)) {
			splunkDataReaderBuilder.addPropertyValue("count", count);
		}

		IntegrationNamespaceUtils.setValueIfAttributeDefined(splunkDataReaderBuilder, element, "fieldList");
		IntegrationNamespaceUtils.setValueIfAttributeDefined(splunkDataReaderBuilder, element, "search");
		IntegrationNamespaceUtils.setValueIfAttributeDefined(splunkDataReaderBuilder, element, "savedSearch");
		IntegrationNamespaceUtils.setValueIfAttributeDefined(splunkDataReaderBuilder, element, "owner");
		IntegrationNamespaceUtils.setValueIfAttributeDefined(splunkDataReaderBuilder, element, "app");
		IntegrationNamespaceUtils.setValueIfAttributeDefined(splunkDataReaderBuilder, element, "initEarliestTime");

		String earliestTime = element.getAttribute("earliestTime");
		if (StringUtils.hasText(earliestTime)) {
			splunkDataReaderBuilder.addPropertyValue("earliestTime", earliestTime);
		}

		String latestTime = element.getAttribute("latestTime");
		if (StringUtils.hasText(latestTime)) {
			splunkDataReaderBuilder.addPropertyValue("latestTime", latestTime);
		}


		BeanDefinitionBuilder connectionFactoryBuilder = BeanDefinitionBuilder.genericBeanDefinition(SplunkConnectionFactory.class);

		String splunkServerBeanName = element.getAttribute("splunk-server-ref");
		if (StringUtils.hasText(splunkServerBeanName)) {
			connectionFactoryBuilder.addConstructorArgReference(splunkServerBeanName);
		}

		BeanDefinitionBuilder connectionFactoryFactoryBeanBuilder = BeanDefinitionBuilder.genericBeanDefinition(ConnectionFactoryFactoryBean.class);
		connectionFactoryFactoryBeanBuilder.addConstructorArgValue(connectionFactoryBuilder.getBeanDefinition());
		connectionFactoryFactoryBeanBuilder.addConstructorArgValue(element.getAttribute("pool-server-connection"));
		splunkDataReaderBuilder.addConstructorArgValue(connectionFactoryFactoryBeanBuilder.getBeanDefinition());

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
