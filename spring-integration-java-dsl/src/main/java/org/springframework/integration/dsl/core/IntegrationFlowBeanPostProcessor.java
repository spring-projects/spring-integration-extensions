/*
 * Copyright 2014 the original author or authors.
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

package org.springframework.integration.dsl.core;

import java.util.Collection;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.FixedSubscriberChannel;
import org.springframework.integration.config.ConsumerEndpointFactoryBean;
import org.springframework.integration.config.IntegrationConfigUtils;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.support.MessageChannelReference;
import org.springframework.messaging.MessageHandler;
import org.springframework.util.Assert;

/**
 * @author Artem Bilan
 */
public class IntegrationFlowBeanPostProcessor implements BeanPostProcessor, BeanFactoryAware {

	private ConfigurableListableBeanFactory beanFactory;

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		Assert.isInstanceOf(ConfigurableListableBeanFactory.class, beanFactory,
				"To use Spring Integration Java DSL the 'beanFactory' has to be an instance of " +
						"'ConfigurableListableBeanFactory'. Consider using 'GenericApplicationContext' implementation."
		);

		this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof IntegrationFlow) {
			IntegrationFlow flow = (IntegrationFlow) bean;
			String flowNamePrefix = beanName + ":";
			int channelNameIndex = 0;
			for (Object component : flow.getIntegrationComponents()) {
				if (component instanceof ConsumerEndpointSpec) {
					ConsumerEndpointSpec<?, ?> endpointSpec = (ConsumerEndpointSpec<?, ?>) component;
					MessageHandler messageHandler = endpointSpec.get().getT2();
					ConsumerEndpointFactoryBean endpoint = endpointSpec.get().getT1();
					String id = endpointSpec.getId();

					Collection<?> messageHandlers =
							this.beanFactory.getBeansOfType(messageHandler.getClass(), false, false).values();

					if (!messageHandlers.contains(messageHandler)) {
						String handlerBeanName = generateBeanName(messageHandler);
						String[] handlerAlias = id != null
								? new String[] {id + IntegrationConfigUtils.HANDLER_ALIAS_SUFFIX}
								: null;

						registerComponent(messageHandler, handlerBeanName);
						if (handlerAlias != null) {
							for (String alias : handlerAlias) {
								this.beanFactory.registerAlias(handlerBeanName, alias);
							}
						}
					}

					String endpointBeanName = id;
					if (endpointBeanName == null) {
						endpointBeanName = generateBeanName(endpoint);
					}
					registerComponent(endpoint, endpointBeanName);
				}
				else {
					Collection<?> values = this.beanFactory.getBeansOfType(component.getClass(), false, false).values();
					if (!values.contains(component)) {
						if (component instanceof AbstractMessageChannel) {
							String channelBeanName = ((AbstractMessageChannel) component).getComponentName();
							if (channelBeanName == null) {
								channelBeanName = flowNamePrefix + "channel" +
										BeanFactoryUtils.GENERATED_BEAN_NAME_SEPARATOR + channelNameIndex++;
							}
							registerComponent(component, channelBeanName);
						}
						else if (component instanceof MessageChannelReference) {
							String channelBeanName = ((MessageChannelReference) component).getName();
							if (!this.beanFactory.containsBean(channelBeanName)) {
								DirectChannel directChannel = new DirectChannel();
								registerComponent(directChannel, channelBeanName);
							}
						}
						else if (component instanceof FixedSubscriberChannel) {
							FixedSubscriberChannel fixedSubscriberChannel = (FixedSubscriberChannel) component;
							String channelBeanName = fixedSubscriberChannel.getComponentName();
							if ("Unnamed fixed subscriber channel".equals(channelBeanName)) {
								channelBeanName = flowNamePrefix + "channel" +
										BeanFactoryUtils.GENERATED_BEAN_NAME_SEPARATOR + channelNameIndex++;
							}
							registerComponent(component, channelBeanName);
						}
						else {
							registerComponent(component, generateBeanName(component));
						}
					}
				}
			}
		}
		return bean;
	}

	private void registerComponent(Object component, String beanName) {
		this.beanFactory.registerSingleton(beanName, component);
		this.beanFactory.initializeBean(component, beanName);
	}

	private String generateBeanName(Object instance) {
		String generatedBeanName = instance.getClass().getName();
		String id = instance.getClass().getName();
		int counter = -1;
		while (counter == -1 || this.beanFactory.containsBean(id)) {
			counter++;
			id = generatedBeanName + BeanFactoryUtils.GENERATED_BEAN_NAME_SEPARATOR + counter;
		}
		return id;
	}

}
