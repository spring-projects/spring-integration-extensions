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

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.Lifecycle;
import org.springframework.context.SmartLifecycle;

/**
 * @author Artem Bilan
 */
class SimpleFactoryBean<T> extends AbstractFactoryBean<T> implements BeanNameAware, SmartLifecycle {

	private final T target;

	private String name;

	public SimpleFactoryBean(T target) {
		this.target = target;
	}

	@Override
	public void setBeanName(String name) {
		this.name = name;
	}

	@Override
	public Class<?> getObjectType() {
		return this.target.getClass();
	}

	@Override
	protected T createInstance() throws Exception {
		((AutowireCapableBeanFactory) this.getBeanFactory()).initializeBean(this.target, this.name);
		return this.target;
	}

	@Override
	public boolean isAutoStartup() {
		return this.target instanceof SmartLifecycle && ((SmartLifecycle) this.target).isAutoStartup();
	}

	@Override
	public void stop(Runnable callback) {
		if (this.target instanceof SmartLifecycle) {
			((SmartLifecycle) this.target).stop(callback);
		}
	}

	@Override
	public void start() {
		if (this.target instanceof Lifecycle) {
			((Lifecycle) this.target).start();
		}
	}

	@Override
	public void stop() {
		if (this.target instanceof Lifecycle) {
			((Lifecycle) this.target).start();
		}
	}

	@Override
	public boolean isRunning() {
		return this.target instanceof SmartLifecycle && ((SmartLifecycle) this.target).isRunning();
	}

	@Override
	public int getPhase() {
		if (this.target instanceof SmartLifecycle) {
			return ((SmartLifecycle) this.target).getPhase();
		}
		else {
			return 0;
		}
	}

}
