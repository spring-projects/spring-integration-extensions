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

package org.springframework.integration.dsl.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.integration.config.IntegrationConfigurationInitializer;
import org.springframework.integration.dsl.core.IntegrationComponentSpec;
import org.springframework.integration.dsl.config.IntegrationFlowBeanPostProcessor;
import org.springframework.util.Assert;

/**
 * The Java DSL Integration infrastructure {@code beanFactory} initializer.
 *
 * @author Artem Bilan
 */
public class DslIntegrationConfigurationInitializer implements IntegrationConfigurationInitializer {

	private static final String INTEGRATION_FLOW_BPP_BEAN_NAME = IntegrationFlowBeanPostProcessor
			.class.getName();

	@Override
	public void initialize(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
		Assert.isInstanceOf(BeanDefinitionRegistry.class, configurableListableBeanFactory,
				"To use Spring Integration Java DSL the 'beanFactory' has to be an instance of " +
						"'BeanDefinitionRegistry'. Consider using 'GenericApplicationContext' implementation."
		);

		checkSpecBeans(configurableListableBeanFactory);

		BeanDefinitionRegistry registry = (BeanDefinitionRegistry) configurableListableBeanFactory;
		if (!registry.containsBeanDefinition(INTEGRATION_FLOW_BPP_BEAN_NAME)) {
			registry.registerBeanDefinition(INTEGRATION_FLOW_BPP_BEAN_NAME,
					new RootBeanDefinition(IntegrationFlowBeanPostProcessor.class));
		}
	}

	private void checkSpecBeans(ConfigurableListableBeanFactory beanFactory) {
		List<String> specBeanNames = Arrays.asList(beanFactory.getBeanNamesForType(IntegrationComponentSpec.class,
				true, false));
		if (!specBeanNames.isEmpty()) {
			throw new BeanCreationException("'IntegrationComponentSpec' beans: '" + specBeanNames +
					"' must be populated to target objects via 'get()' method call. It is important for " +
					"@Autowired injections.");
		}
	}

}
