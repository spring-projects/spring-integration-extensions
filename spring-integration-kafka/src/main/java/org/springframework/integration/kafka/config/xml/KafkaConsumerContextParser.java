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
package org.springframework.integration.kafka.config.xml;

import static org.apache.commons.lang.StringUtils.stripToEmpty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.springframework.integration.kafka.support.ConsumerConfigFactoryBean;
import org.springframework.integration.kafka.support.ConsumerConfiguration;
import org.springframework.integration.kafka.support.ConsumerConnectionProvider;
import org.springframework.integration.kafka.support.ConsumerMetadata;
import org.springframework.integration.kafka.support.KafkaConsumerContext;
import org.springframework.integration.kafka.support.MessageLeftOverTracker;
import org.springframework.integration.kafka.support.TopicFilterConfiguration;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * @author Soby Chacko
 * @author Rajasekar Elango
 * @since 0.5
 */
public class KafkaConsumerContextParser extends AbstractSingleBeanDefinitionParser {

	@Override
	protected Class<?> getBeanClass(final Element element) {
		return KafkaConsumerContext.class;
	}

	@Override
	protected void doParse(final Element element, final ParserContext parserContext, final BeanDefinitionBuilder builder) {
		super.doParse(element, parserContext, builder);

		final Element consumerConfigurations = DomUtils.getChildElementByTagName(element, "consumer-configurations");
		parseConsumerConfigurations(consumerConfigurations, parserContext, builder, element);
	}

	private void parseConsumerConfigurations(final Element consumerConfigurations, final ParserContext parserContext,
			final BeanDefinitionBuilder builder, final Element parentElem) {

		for (final Element consumerConfiguration : DomUtils.getChildElementsByTagName(consumerConfigurations, "consumer-configuration")) {
			final BeanDefinitionBuilder consumerConfigurationBuilder = BeanDefinitionBuilder
					.genericBeanDefinition(ConsumerConfiguration.class);
			final BeanDefinitionBuilder consumerMetadataBuilder = BeanDefinitionBuilder.genericBeanDefinition(ConsumerMetadata.class);

			IntegrationNamespaceUtils.setValueIfAttributeDefined(consumerMetadataBuilder, consumerConfiguration, "group-id");

			IntegrationNamespaceUtils.setReferenceIfAttributeDefined(consumerMetadataBuilder, consumerConfiguration, "value-decoder");
			IntegrationNamespaceUtils.setReferenceIfAttributeDefined(consumerMetadataBuilder, consumerConfiguration, "key-decoder");
			IntegrationNamespaceUtils.setValueIfAttributeDefined(consumerMetadataBuilder, consumerConfiguration, "key-class-type");
			IntegrationNamespaceUtils.setValueIfAttributeDefined(consumerMetadataBuilder, consumerConfiguration, "value-class-type");
			IntegrationNamespaceUtils.setValueIfAttributeDefined(consumerConfigurationBuilder, consumerConfiguration, "max-messages");
			IntegrationNamespaceUtils.setValueIfAttributeDefined(consumerMetadataBuilder, parentElem, "consumer-timeout");

			addTopics(consumerConfiguration, consumerMetadataBuilder);

			addTopicFilter(consumerConfiguration, consumerMetadataBuilder);

			// group id suffix, replaces special characters to make sure that
			// bean names will not be expanded
			final String groupIdSuffix = stripToEmpty(consumerConfiguration.getAttribute("group-id")).replaceAll("[#${}]", "_");

			final String consumerMetadataBeanName = registerConsumerMetadataBean(parserContext, consumerMetadataBuilder, groupIdSuffix);

			final String consumerConfigFactoryBeanName = registerConsumerConfigFactoryBean(parserContext, builder, parentElem, groupIdSuffix,
					consumerMetadataBeanName);

			final String consumerConnectionProviderBeanName = registerConnectionProviderBean(parserContext, groupIdSuffix, consumerConfigFactoryBeanName);

			final String messageLeftOverBeanName = registerMessageLeftOverBean(parserContext, groupIdSuffix);

			consumerConfigurationBuilder.addConstructorArgReference(consumerMetadataBeanName);
			consumerConfigurationBuilder.addConstructorArgReference(consumerConnectionProviderBeanName);
			consumerConfigurationBuilder.addConstructorArgReference(messageLeftOverBeanName);

			final AbstractBeanDefinition consumerConfigurationBeanDefinition = consumerConfigurationBuilder.getBeanDefinition();

			final String consumerConfigurationBeanName = "consumerConfiguration_" + groupIdSuffix;
			registerBeanDefinition(new BeanDefinitionHolder(consumerConfigurationBeanDefinition, consumerConfigurationBeanName),
					parserContext.getRegistry());
		}
	}

	private void addTopicFilter(final Element consumerConfiguration, final BeanDefinitionBuilder consumerMetadataBuilder) {
		final Element topicFilter = DomUtils.getChildElementByTagName(consumerConfiguration, "topic-filter");

		if (topicFilter != null) {
			final TopicFilterConfiguration topicFilterConfiguration = new TopicFilterConfiguration(topicFilter.getAttribute("pattern"),
					Integer.valueOf(topicFilter.getAttribute("streams")), Boolean.valueOf(topicFilter.getAttribute("exclude")));
			consumerMetadataBuilder.addPropertyValue("topicFilterConfiguration", topicFilterConfiguration);
		}
	}

	private void addTopics(final Element consumerConfiguration, final BeanDefinitionBuilder consumerMetadataBuilder) {
		final Map<String, Integer> topicStreamsMap = new HashMap<String, Integer>();

		final List<Element> topicConfigurations = DomUtils.getChildElementsByTagName(consumerConfiguration, "topic");

		if (topicConfigurations != null) {
			for (final Element topicConfiguration : topicConfigurations) {
				final String topic = topicConfiguration.getAttribute("id");
				final String streams = topicConfiguration.getAttribute("streams");
				final Integer streamsInt = Integer.valueOf(streams);
				topicStreamsMap.put(topic, streamsInt);
			}
			consumerMetadataBuilder.addPropertyValue("topicStreamMap", topicStreamsMap);
		}
	}

	private String registerMessageLeftOverBean(final ParserContext parserContext, String groupIdSuffix) {
		final BeanDefinitionBuilder messageLeftOverBeanDefinitionBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(MessageLeftOverTracker.class);
		final BeanDefinition messageLeftOverBeanDefinition = messageLeftOverBeanDefinitionBuilder.getBeanDefinition();
		String messageLeftOverBeanName = "messageLeftOver_" + groupIdSuffix;
		registerBeanDefinition(new BeanDefinitionHolder(messageLeftOverBeanDefinition, messageLeftOverBeanName),
				parserContext.getRegistry());
		return messageLeftOverBeanName;
	}

	private String registerConnectionProviderBean(final ParserContext parserContext, String groupIdSuffix, String consumerConfigFactoryBeanName) {
		final BeanDefinitionBuilder consumerConnectionProviderBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(ConsumerConnectionProvider.class);
		consumerConnectionProviderBuilder.addConstructorArgReference(consumerConfigFactoryBeanName);

		final BeanDefinition consumerConnectionProviderBuilderBeanDefinition = consumerConnectionProviderBuilder.getBeanDefinition();
		String consumerConnectionProviderBeanName = "consumerConnectionProvider_" + groupIdSuffix;
		registerBeanDefinition(new BeanDefinitionHolder(consumerConnectionProviderBuilderBeanDefinition,
				consumerConnectionProviderBeanName), parserContext.getRegistry());
		return consumerConnectionProviderBeanName;
	}

	private String registerConsumerConfigFactoryBean(final ParserContext parserContext, final BeanDefinitionBuilder builder,
			final Element parentElem, String groupIdSuffix, String consumerMetadataBeanName) {
		final String zookeeperConnectBean = parentElem.getAttribute("zookeeper-connect");
		IntegrationNamespaceUtils.setReferenceIfAttributeDefined(builder, parentElem, zookeeperConnectBean);

		final String consumerPropertiesBean = parentElem.getAttribute("consumer-properties");

		final BeanDefinitionBuilder consumerConfigFactoryBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(ConsumerConfigFactoryBean.class);
		consumerConfigFactoryBuilder.addConstructorArgReference(consumerMetadataBeanName);

		if (StringUtils.hasText(zookeeperConnectBean)) {
			consumerConfigFactoryBuilder.addConstructorArgReference(zookeeperConnectBean);
		}

		if (StringUtils.hasText(consumerPropertiesBean)) {
			consumerConfigFactoryBuilder.addConstructorArgReference(consumerPropertiesBean);
		}
		final BeanDefinition consumerConfigFactoryBuilderBeanDefinition = consumerConfigFactoryBuilder.getBeanDefinition();
		String consumerConfigFactoryBeanName1 = "consumerConfigFactory_" + groupIdSuffix;
		registerBeanDefinition(new BeanDefinitionHolder(consumerConfigFactoryBuilderBeanDefinition, consumerConfigFactoryBeanName1),
				parserContext.getRegistry());

		String consumerConfigFactoryBeanName = consumerConfigFactoryBeanName1;
		return consumerConfigFactoryBeanName;
	}

	private String registerConsumerMetadataBean(final ParserContext parserContext, final BeanDefinitionBuilder consumerMetadataBuilder,
			String groupIdSuffix) {
		final BeanDefinition consumerMetadataBeanDef = consumerMetadataBuilder.getBeanDefinition();
		String consumerMetadataBeanName = "consumerMetadata_" + groupIdSuffix;
		registerBeanDefinition(new BeanDefinitionHolder(consumerMetadataBeanDef, consumerMetadataBeanName), parserContext.getRegistry());
		return consumerMetadataBeanName;
	}
}
