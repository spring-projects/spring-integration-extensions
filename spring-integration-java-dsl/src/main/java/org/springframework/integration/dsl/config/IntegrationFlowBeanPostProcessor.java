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

package org.springframework.integration.dsl.config;

import java.util.Collection;

import org.springframework.aop.support.AopUtils;
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
import org.springframework.integration.config.SourcePollingChannelAdapterFactoryBean;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlowBuilder;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.SourcePollingChannelAdapterSpec;
import org.springframework.integration.dsl.StandardIntegrationFlow;
import org.springframework.integration.dsl.core.ConsumerEndpointSpec;
import org.springframework.integration.dsl.support.MessageChannelReference;
import org.springframework.integration.support.context.NamedComponent;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

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
		if (bean instanceof StandardIntegrationFlow) {
			return processStandardIntegrationFlow((StandardIntegrationFlow) bean, beanName);
		}
		else if (bean instanceof IntegrationFlow) {
			return processIntegrationFlowImpl((IntegrationFlow) bean, beanName);
		}
		return bean;
	}

	private Object processStandardIntegrationFlow(StandardIntegrationFlow flow,
			String beanName) {
		String flowNamePrefix = beanName + ".";
		int subFlowNameIndex = 0;
		int channelNameIndex = 0;
		for (Object component : flow.getIntegrationComponents()) {
			if (component instanceof ConsumerEndpointSpec) {
				ConsumerEndpointSpec<?, ?> endpointSpec = (ConsumerEndpointSpec<?, ?>) component;
				MessageHandler messageHandler = endpointSpec.get().getT2();
				ConsumerEndpointFactoryBean endpoint = endpointSpec.get().getT1();
				String id = endpointSpec.getId();

				Collection<?> messageHandlers = this.beanFactory.getBeansOfType(MessageHandler.class, false,
						false).values();

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
				//TODO workaround until SF will fix 'TypeDescriptor.forObject'
				if (component instanceof MessageChannel) {
					Collection<?> messageChannels =
							this.beanFactory.getBeansOfType(MessageChannel.class, false, false).values();
					if (!messageChannels.contains(component)) {
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
					}
				}
				else if (component instanceof SourcePollingChannelAdapterSpec) {
					SourcePollingChannelAdapterSpec spec = (SourcePollingChannelAdapterSpec) component;
					SourcePollingChannelAdapterFactoryBean pollingChannelAdapterFactoryBean = spec.get().getT1();
					String id = spec.getId();
					if (!StringUtils.hasText(id)) {
						id = generateBeanName(pollingChannelAdapterFactoryBean);
					}
					registerComponent(pollingChannelAdapterFactoryBean, id);

					MessageSource<?> messageSource = spec.get().getT2();
					if (!this.beanFactory
							.getBeansOfType(MessageSource.class, false, false)
							.values()
							.contains(messageSource)) {
						String messageSourceId = id + ".source";
						if (messageSource instanceof NamedComponent
								&& ((NamedComponent) messageSource).getComponentName() != null) {
							messageSourceId = ((NamedComponent) messageSource).getComponentName();
						}
						registerComponent(messageSource, messageSourceId);
					}
				}
				else if (component instanceof StandardIntegrationFlow) {
					String subFlowBeanName = flowNamePrefix + "subFlow" +
							BeanFactoryUtils.GENERATED_BEAN_NAME_SEPARATOR + subFlowNameIndex++;
					registerComponent(component, subFlowBeanName);
				}
				else if (!this.beanFactory
						.getBeansOfType(AopUtils.getTargetClass(component), false, false)
						.values()
						.contains(component)) {
					registerComponent(component, generateBeanName(component));
				}
			}
		}
		return flow;
	}

	private Object processIntegrationFlowImpl(IntegrationFlow flow, String beanName) {
		IntegrationFlowBuilder flowBuilder = IntegrationFlows.from(beanName + ".input");
		flow.accept(flowBuilder);
		return processStandardIntegrationFlow(flowBuilder.get(), beanName);
	}

	private void registerComponent(Object component, String beanName) {
		this.beanFactory.registerSingleton(beanName, component);
		this.beanFactory.initializeBean(component, beanName);
	}

	private String generateBeanName(Object instance) {
		if (instance instanceof NamedComponent && ((NamedComponent) instance).getComponentName() != null) {
			return ((NamedComponent) instance).getComponentName();
		}
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
