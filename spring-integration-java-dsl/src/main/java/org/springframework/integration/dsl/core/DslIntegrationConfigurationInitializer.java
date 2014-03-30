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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.integration.channel.FixedSubscriberChannel;
import org.springframework.integration.config.ConsumerEndpointFactoryBean;
import org.springframework.integration.config.IntegrationConfigUtils;
import org.springframework.integration.config.IntegrationConfigurationInitializer;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.config.InstanceBeanDefinition;
import org.springframework.integration.dsl.support.MessageChannelReference;
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
						"Consider using 'GenericApplicationContext' implementation."
		);
		this.checkSpecBeans(configurableListableBeanFactory);
		this.initializeIntegrationFlows(configurableListableBeanFactory);
	}

	private void checkSpecBeans(ConfigurableListableBeanFactory beanFactory) {
		List<String> specBeanNames = Arrays.asList(beanFactory.getBeanNamesForType(IntegrationComponentSpec.class, true, false));
		if (!specBeanNames.isEmpty()) {
			throw new BeanCreationException("'IntegrationComponentSpec' beans: '" + specBeanNames + "' must be populated " +
					"to target objects via 'get()' method call. It is important for @Autowired injections.");
		}
	}

	private void initializeIntegrationFlows(ConfigurableListableBeanFactory beanFactory) {
		AutowiredAnnotationBeanPostProcessor autowiredAnnotationBeanPostProcessor = beanFactory.getBean(AutowiredAnnotationBeanPostProcessor.class);
		BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
		String[] integrationFlowBeanNames = beanFactory.getBeanNamesForType(IntegrationFlow.class, false, false);
		Set<String> processedConfigurations = new HashSet<String>();
		for (String flowName : integrationFlowBeanNames) {
			BeanDefinition flowBeanDefinition = beanFactory.getBeanDefinition(flowName);
			String configurationBeanName = flowBeanDefinition.getFactoryBeanName();
			if (processedConfigurations.add(configurationBeanName)) {
				autowiredAnnotationBeanPostProcessor.processInjection(beanFactory.getBean(configurationBeanName));
			}
			String flowNamePrefix = flowName + ":";
			IntegrationFlow flow = beanFactory.getBean(flowName, IntegrationFlow.class);
			int channelNameIndex = 0;
			for (AbstractBeanDefinition component : flow.getIntegrationComponents()) {
				if (component instanceof InstanceBeanDefinition) {
					final Object instance = component.getSource();
					Collection<?> values = beanFactory.getBeansOfType(instance.getClass(), false, false).values();
					if (!values.contains(instance)) {
						if (instance instanceof AbstractMessageChannel) {
							String channelBeanName = ((AbstractMessageChannel) instance).getComponentName();
							if (channelBeanName == null) {
								channelBeanName = flowNamePrefix + "channel" + BeanFactoryUtils.GENERATED_BEAN_NAME_SEPARATOR + channelNameIndex++;
							}
							registry.registerBeanDefinition(channelBeanName, component);
						}
						else if (instance instanceof ConsumerEndpointSpec) {
							ConsumerEndpointSpec<?, ?> endpointSpec = (ConsumerEndpointSpec<?, ?>) instance;
							MessageHandler messageHandler = endpointSpec.get().getT2();
							ConsumerEndpointFactoryBean endpoint = endpointSpec.get().getT1();
							String id = endpointSpec.getId();

							Collection<?> messageHandlers = beanFactory.getBeansOfType(messageHandler.getClass(), false, false).values();

							if (!messageHandlers.contains(messageHandler)) {
								String handlerBeanName = generateInstanceBeanDefinitionName(registry, messageHandler);
								String[] handlerAlias = id != null ? new String[]{id + IntegrationConfigUtils.HANDLER_ALIAS_SUFFIX} : null;
								BeanComponentDefinition definitionHolder = new BeanComponentDefinition(new InstanceBeanDefinition(messageHandler),
										handlerBeanName, handlerAlias);
								BeanDefinitionReaderUtils.registerBeanDefinition(definitionHolder, registry);
							}

							String endpointBeanName = id;
							if (endpointBeanName == null) {
								endpointBeanName = generateInstanceBeanDefinitionName(registry, endpoint);
							}
							registry.registerBeanDefinition(endpointBeanName, new InstanceBeanDefinition(endpoint));
						}
						else if (instance instanceof MessageChannelReference) {
							String channelName = ((MessageChannelReference) instance).getName();
							if (!registry.containsBeanDefinition(channelName)) {
								IntegrationConfigUtils.autoCreateDirectChannel(channelName, registry);
							}
						}
						else if (instance instanceof FixedSubscriberChannel) {
							FixedSubscriberChannel fixedSubscriberChannel = (FixedSubscriberChannel) instance;
							String channelBeanName = fixedSubscriberChannel.getComponentName();
							if ("Unnamed fixed subscriber channel".equals(channelBeanName)) {
								channelBeanName = flowNamePrefix + "channel" + BeanFactoryUtils.GENERATED_BEAN_NAME_SEPARATOR + channelNameIndex++;
							}
							registry.registerBeanDefinition(channelBeanName, component);
						}
						else {
							String beanName = generateInstanceBeanDefinitionName(registry, instance);
							registry.registerBeanDefinition(beanName, component);
						}
					}
				}
				else {
					BeanDefinitionReaderUtils.registerWithGeneratedName(component, registry);
				}
			}
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
