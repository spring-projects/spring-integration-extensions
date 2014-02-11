package org.springframework.integration.dsl;

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
import org.springframework.integration.dsl.config.InstanceBeanDefinition;
import org.springframework.messaging.MessageHandler;

/**
 * The Java DSL Integration infrastructure {@code beanFactory} initializer.
 *
 * @author Artem Bilan
 */
public class DslIntegrationConfigurationInitializer implements IntegrationConfigurationInitializer {

	@Override
	public void initialize(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
		this.initializeIntegrationFlows(configurableListableBeanFactory);
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
						else if (instance instanceof EndpointSpec) {
							EndpointSpec<?, ?> endpointSpec = (EndpointSpec<?, ?>) instance;
							MessageHandler messageHandler = endpointSpec.getHandler();
							ConsumerEndpointFactoryBean endpoint = endpointSpec.getEndpoint();
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
