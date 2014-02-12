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

package org.springframework.integration.dsl;

import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.integration.dsl.config.InstanceBeanDefinition;

/**
 * @author Artem Bilan
 */
public final class IntegrationFlow {

	private final Set<AbstractBeanDefinition> integrationComponents = new LinkedHashSet<AbstractBeanDefinition>();

	IntegrationFlow() {
	}

	public Set<AbstractBeanDefinition> getIntegrationComponents() {
		return integrationComponents;
	}

	IntegrationFlow addComponent(Object component) {
		AbstractBeanDefinition beanDefinition = component instanceof AbstractBeanDefinition
				? (AbstractBeanDefinition) component : new InstanceBeanDefinition(component);
		this.integrationComponents.add(beanDefinition);
		return this;
	}

}
