/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.integration.dsl.core;

import java.util.Collection;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.integration.config.ConsumerEndpointFactoryBean;
import org.springframework.integration.config.IntegrationConfigurationInitializer;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.config.InstanceBeanDefinition;
import org.springframework.messaging.MessageHandler;
import org.springframework.util.Assert;

/**
 * The Java DSL Integration infrastructure {@code beanFactory} initializer.
 *
 * @author Artem Bilan
 */
public class DslIntegrationConfigurationInitializer implements IntegrationConfigurationInitializer {

	@Override
	public void initialize(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
		Assert.isInstanceOf(BeanDefinitionRegistry.class, configurableListableBeanFactory,
				"To use Spring Integration Java DSL the 'beanFactory' has to be an instance of 'BeanDefinitionRegistry'." +
						"Consider using 'GenericApplicationContext' implementation.");
		this.initializeIntegrationFlows(configurableListableBeanFactory);
		this.populateBeansFromSpecs(configurableListableBeanFactory);
	}

	private void initializeIntegrationFlows(ConfigurableListableBeanFactory beanFactory) {
		Map<String, IntegrationFlow> integrationFlows = beanFactory.getBeansOfType(IntegrationFlow.class, false, false);
		BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
		for (Map.Entry<String, IntegrationFlow> integrationFlowEntry : integrationFlows.entrySet()) {
			String flowName = integrationFlowEntry.getKey();
			String flowNamePrefix = flowName + ":";
			IntegrationFlow flow = integrationFlowEntry.getValue();
			int channelNameIndex = 0;
			for (AbstractBeanDefinition beanDefinition : flow.getIntegrationComponents()) {
				if (beanDefinition instanceof InstanceBeanDefinition) {
					final Object instance = beanDefinition.getSource();
					Collection<?> values = beanFactory.getBeansOfType(instance.getClass(), false, false).values();
					if (!values.contains(instance)) {
						if (instance instanceof AbstractMessageChannel) {
							String channelBeanName = ((AbstractMessageChannel) instance).getComponentName();
							if (channelBeanName == null) {
								channelBeanName = flowNamePrefix + "channel" + BeanFactoryUtils.GENERATED_BEAN_NAME_SEPARATOR + channelNameIndex++;
							}
							registry.registerBeanDefinition(channelBeanName, beanDefinition);
						}
						else if (instance instanceof ConsumerEndpointSpec) {
							ConsumerEndpointSpec<?, ?> endpointSpec = (ConsumerEndpointSpec<?, ?>) instance;
							MessageHandler messageHandler = endpointSpec.get().getT2();
							ConsumerEndpointFactoryBean endpoint = endpointSpec.get().getT1();
							String id = endpointSpec.getId();

							String handlerBeanName = generateInstanceBeanDefinitionName(registry, messageHandler);
							String[] handlerAlias = id != null ? new String[]{id + IntegrationNamespaceUtils.HANDLER_ALIAS_SUFFIX} : null;
							BeanComponentDefinition definitionHolder = new BeanComponentDefinition(new InstanceBeanDefinition(messageHandler), handlerBeanName, handlerAlias);
							BeanDefinitionReaderUtils.registerBeanDefinition(definitionHolder, registry);

							String endpointBeanName = id;
							if (endpointBeanName == null) {
								endpointBeanName = generateInstanceBeanDefinitionName(registry, endpoint);
							}
							registry.registerBeanDefinition(endpointBeanName, new InstanceBeanDefinition(endpoint));
						}
						else {
							String beanName = generateInstanceBeanDefinitionName(registry, instance);
							registry.registerBeanDefinition(beanName, beanDefinition);
						}
					}
				}
				else {
					BeanDefinitionReaderUtils.registerWithGeneratedName(beanDefinition, registry);
				}
			}
			registry.removeBeanDefinition(flowName);
			beanFactory.destroyBean(flowName);
		}

	}

	private void populateBeansFromSpecs(ConfigurableListableBeanFactory beanFactory) {
		Map<String, ?> specs = beanFactory.getBeansOfType(IntegrationComponentSpec.class, false, false);
		BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
		for (Map.Entry<String, ?> specEntry : specs.entrySet()) {
			String id = specEntry.getKey();
			IntegrationComponentSpec<?, ?> spec = (IntegrationComponentSpec<?, ?>) specEntry.getValue();
			registry.removeBeanDefinition(id);
			beanFactory.registerSingleton(id, spec.get());
			beanFactory.initializeBean(spec.get(), id);
		}
	}

	@SuppressWarnings("serial")
	private static String generateInstanceBeanDefinitionName(BeanDefinitionRegistry registry, final Object instance) {
		return BeanDefinitionReaderUtils.generateBeanName(new GenericBeanDefinition() {

			@Override
			public String getBeanClassName() {
				return instance.getClass().getName();
			}
		}, registry);
	}

}
