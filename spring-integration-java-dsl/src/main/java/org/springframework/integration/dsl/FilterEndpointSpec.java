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

import java.util.Collection;
import java.util.Collections;

import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.core.ComponentsRegistration;
import org.springframework.integration.dsl.core.ConsumerEndpointSpec;
import org.springframework.integration.filter.MessageFilter;
import org.springframework.messaging.MessageChannel;
import org.springframework.util.Assert;

/**
 * @author Artem Bilan
 */
public final class FilterEndpointSpec extends ConsumerEndpointSpec<FilterEndpointSpec, MessageFilter>
		implements ComponentsRegistration {

	private IntegrationFlow discardFlow;

	FilterEndpointSpec(MessageFilter messageFilter) {
		super(messageFilter);
	}

	public FilterEndpointSpec throwExceptionOnRejection(boolean throwExceptionOnRejection) {
		this.target.getT2().setThrowExceptionOnRejection(throwExceptionOnRejection);
		return _this();
	}

	public FilterEndpointSpec discardChannel(MessageChannel discardChannel) {
		this.target.getT2().setDiscardChannel(discardChannel);
		return _this();
	}

	public FilterEndpointSpec discardChannel(String discardChannelName) {
		this.target.getT2().setDiscardChannelName(discardChannelName);
		return _this();
	}

	public FilterEndpointSpec discardFlow(IntegrationFlow discardFlow) {
		Assert.notNull(discardFlow);
		DirectChannel channel = new DirectChannel();
		IntegrationFlowBuilder flowBuilder = IntegrationFlows.from(channel);
		discardFlow.accept(flowBuilder);
		this.discardFlow = flowBuilder.get();
		return discardChannel(channel);
	}

	public FilterEndpointSpec discardWithinAdvice(boolean discardWithinAdvice) {
		this.target.getT2().setDiscardWithinAdvice(discardWithinAdvice);
		return _this();
	}

	@Override
	public Collection<Object> getComponentsToRegister() {
		if (this.discardFlow != null) {
			return Collections.<Object>singletonList(this.discardFlow);
		}
		return null;
	}

}
