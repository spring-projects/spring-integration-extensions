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

import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.GenericBeanDefinition;

/**
 * @author Artem Bilan
 */
@SuppressWarnings("serial")
public class InstanceBeanDefinition extends GenericBeanDefinition {

	private final Object instance;

	public InstanceBeanDefinition(Object instance) {
		this.instance = instance;
		ConstructorArgumentValues args = new ConstructorArgumentValues();
		args.addGenericArgumentValue(this.instance);
		this.setConstructorArgumentValues(args);
		this.setBeanClass(SimpleFactoryBean.class);
	}

	public InstanceBeanDefinition(InstanceBeanDefinition original) {
		super(original);
		this.instance = original.instance;
	}

	@Override
	public InstanceBeanDefinition cloneBeanDefinition() {
		return new InstanceBeanDefinition(this);
	}

	@Override
	public Object getSource() {
		return this.instance;
	}

}
