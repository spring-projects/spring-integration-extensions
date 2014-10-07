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

import java.util.Set;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.integration.dsl.support.FixedSubscriberChannelPrototype;

/**
 * @author Artem Bilan
 */
public final class IntegrationFlowBuilder extends IntegrationFlowDefinition<IntegrationFlowBuilder> {

	public StandardIntegrationFlow get() {
		if (this.currentMessageChannel instanceof FixedSubscriberChannelPrototype) {
			throw new BeanCreationException("The 'currentMessageChannel' (" + this.currentMessageChannel +
					") is a prototype for FixedSubscriberChannel which can't be created without MessageHandler " +
					"constructor argument. That means that '.fixedSubscriberChannel()' can't be the last EIP-method " +
					"in the IntegrationFlow definition.");
		}

		if (this.integrationComponents.size() == 1) {
			if (this.currentComponent != null) {
				if (this.currentComponent instanceof SourcePollingChannelAdapterSpec) {
					throw new BeanCreationException("The 'SourcePollingChannelAdapter' (" + this.currentComponent
							+ ") " + "must be configured with at least one 'MessageChanel' or 'MessageHandler'.");
				}
			}
			else if (this.currentMessageChannel != null) {
				throw new BeanCreationException("The 'IntegrationFlow' can't consist of only one 'MessageChannel'. " +
						"Add at lest '.bridge()' EIP-method before the end of flow.");
			}
		}
		return new StandardIntegrationFlow(this.integrationComponents);
	}

	public static final class StandardIntegrationFlow implements IntegrationFlow {

		private final Set<Object> integrationComponents;

		StandardIntegrationFlow(Set<Object> integrationComponents) {
			this.integrationComponents = integrationComponents;
		}

		public Set<Object> getIntegrationComponents() {
			return integrationComponents;
		}

		@Override
		public void define(IntegrationFlowDefinition<?> flow) {
			throw new UnsupportedOperationException();
		}

	}

}
