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

package org.springframework.integration.dsl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;

import org.springframework.integration.dsl.channel.PublishSubscribeChannelSpec;
import org.springframework.integration.dsl.core.ComponentsRegistration;

/**
 * @author Artem Bilan
 */
public class PublishSubscribersSpec extends PublishSubscribeChannelSpec<PublishSubscribersSpec>
		implements ComponentsRegistration {

	private final List<Object> subscriberFlows = new ArrayList<Object>();

	PublishSubscribersSpec() {
		super();
	}

	PublishSubscribersSpec(Executor executor) {
		super(executor);
	}

	@Override
	public PublishSubscribersSpec id(String id) {
		return super.id(id);
	}

	public PublishSubscribersSpec subscriber(IntegrationFlow flow) {
		IntegrationFlowBuilder flowBuilder = IntegrationFlows.from(this.channel);
		flow.accept(flowBuilder);
		this.subscriberFlows.add(flowBuilder.get());
		return _this();
	}

	@Override
	public Collection<Object> getComponentsToRegister() {
		return this.subscriberFlows;
	}

}
