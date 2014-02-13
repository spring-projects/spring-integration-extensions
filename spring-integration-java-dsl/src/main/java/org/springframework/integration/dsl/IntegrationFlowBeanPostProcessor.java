package org.springframework.integration.dsl;

import java.util.Collection;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.integration.config.ConsumerEndpointFactoryBean;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.springframework.integration.dsl.config.InstanceBeanDefinition;
import org.springframework.messaging.MessageHandler;
import org.springframework.util.Assert;

/**
 * @author Artem Bilan
 * @since 4.0
 */
public class IntegrationFlowBeanPostProcessor implements BeanPostProcessor {

	private final ConfigurableListableBeanFactory beanFactory;

	private final BeanDefinitionRegistry registry;


	public IntegrationFlowBeanPostProcessor(ConfigurableListableBeanFactory beanFactory) {
		Assert.isInstanceOf(BeanDefinitionRegistry.class, beanFactory,
				"To use Spring Integration Java DSL the 'beanFactory' has to be an instance of 'BeanDefinitionRegistry'." +
						"Consider using 'GenericApplicationContext' implementation."
		);
		this.beanFactory = beanFactory;
		this.registry = (BeanDefinitionRegistry) beanFactory;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof IntegrationFlow) {
			String flowNamePrefix = beanName + ":";
			int channelNameIndex = 0;
			for (AbstractBeanDefinition beanDefinition : ((IntegrationFlow) bean).getIntegrationComponents()) {
				if (beanDefinition instanceof InstanceBeanDefinition) {
					final Object instance = beanDefinition.getSource();
					Collection<?> values = this.beanFactory.getBeansOfType(instance.getClass(), false, false).values();
					if (!values.contains(instance)) {
						if (instance instanceof AbstractMessageChannel) {
							String channelBeanName = ((AbstractMessageChannel) instance).getComponentName();
							if (channelBeanName == null) {
								channelBeanName = flowNamePrefix + "channel" + BeanFactoryUtils.GENERATED_BEAN_NAME_SEPARATOR + channelNameIndex++;
							}
							registry.registerBeanDefinition(channelBeanName, beanDefinition);
						}
						else if (instance instanceof EndpointSpec) {
							EndpointSpec<?, ?> endpointSpec = (EndpointSpec<?, ?>) instance;
							MessageHandler messageHandler = endpointSpec.getHandler();
							ConsumerEndpointFactoryBean endpoint = endpointSpec.getEndpoint();
							String id = endpointSpec.getId();

							String handlerBeanName = generateInstanceBeanDefinitionName(registry, messageHandler);
							String[] handlerAlias = id != null ? new String[]{id + IntegrationNamespaceUtils.HANDLER_ALIAS_SUFFIX} : null;
							BeanComponentDefinition definitionHolder = new BeanComponentDefinition(
									new InstanceBeanDefinition(messageHandler), handlerBeanName, handlerAlias);
							BeanDefinitionReaderUtils.registerBeanDefinition(definitionHolder, registry);

							String endpointBeanName = id;
							if (endpointBeanName == null) {
								endpointBeanName = generateInstanceBeanDefinitionName(registry, endpoint);
							}
							registry.registerBeanDefinition(endpointBeanName, new InstanceBeanDefinition(endpoint));
						}
						else {
							String name = generateInstanceBeanDefinitionName(registry, instance);
							registry.registerBeanDefinition(name, beanDefinition);
						}
					}
				}
				else {
					BeanDefinitionReaderUtils.registerWithGeneratedName(beanDefinition, registry);
				}
			}
		}
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
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
