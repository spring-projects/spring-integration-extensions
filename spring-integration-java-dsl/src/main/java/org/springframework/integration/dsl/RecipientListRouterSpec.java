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

import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageSelector;
import org.springframework.integration.dsl.core.ComponentsRegistration;
import org.springframework.integration.router.RecipientListRouter;
import org.springframework.util.Assert;

/**
 * @author Artem Bilan
 */
public class RecipientListRouterSpec extends AbstractRouterSpec<RecipientListRouterSpec, RecipientListRouter>
		implements ComponentsRegistration {

	private final List<Object> subFlows = new ArrayList<Object>();

	RecipientListRouterSpec() {
		super(new DslRecipientListRouter());
	}

	public RecipientListRouterSpec recipient(String channelName, String expression) {
		Assert.hasText(channelName);
		((DslRecipientListRouter) this.target).add(channelName, expression);
		return _this();
	}

	public RecipientListRouterSpec recipient(String channelName, MessageSelector selector) {
		Assert.hasText(channelName);
		((DslRecipientListRouter) this.target).add(channelName, selector);
		return _this();
	}

	public RecipientListRouterSpec recipientFlow(MessageSelector selector, IntegrationFlow subFlow) {
		Assert.notNull(subFlow);
		DirectChannel channel = populateSubFlow(subFlow);
		((DslRecipientListRouter) this.target).add(channel, selector);
		return _this();
	}

	public RecipientListRouterSpec recipientFlow(String expression, IntegrationFlow subFlow) {
		Assert.notNull(subFlow);
		DirectChannel channel = populateSubFlow(subFlow);
		((DslRecipientListRouter) this.target).add(channel, expression);
		return _this();
	}

	private DirectChannel populateSubFlow(IntegrationFlow subFlow) {
		DirectChannel channel = new DirectChannel();
		IntegrationFlowBuilder flowBuilder = IntegrationFlows.from(channel);
		subFlow.accept(flowBuilder);
		this.subFlows.add(flowBuilder.get());
		return channel;
	}

	@Override
	public Collection<Object> getComponentsToRegister() {
		return subFlows;
	}

}
