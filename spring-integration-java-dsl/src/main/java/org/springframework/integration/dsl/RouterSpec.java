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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.core.ComponentsRegistration;
import org.springframework.integration.router.AbstractMappingMessageRouter;
import org.springframework.integration.router.MappingMessageRouterManagement;
import org.springframework.integration.support.context.NamedComponent;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * @author Artem Bilan
 */
public final class RouterSpec<R extends AbstractMappingMessageRouter> extends AbstractRouterSpec<RouterSpec<R>, R>
		implements ComponentsRegistration {

	private final List<Object> subFlows = new ArrayList<Object>();

	private String prefix;

	private String suffix;

	private RouterSubFlowMappingProvider mappingProvider;

	RouterSpec(R router) {
		super(router);
	}

	public RouterSpec<R> resolutionRequired(boolean resolutionRequired) {
		this.target.setResolutionRequired(resolutionRequired);
		return _this();
	}

	public RouterSpec<R> prefix(String prefix) {
		Assert.state(this.subFlows.isEmpty(), "The 'prefix'('suffix') and 'subFlowMapping' are mutually exclusive");
		this.prefix = prefix;
		this.target.setPrefix(prefix);
		return _this();
	}

	public RouterSpec<R> suffix(String suffix) {
		Assert.state(this.subFlows.isEmpty(), "The 'prefix'('suffix') and 'subFlowMapping' are mutually exclusive");
		this.suffix = suffix;
		this.target.setSuffix(suffix);
		return _this();
	}

	public RouterSpec<R> channelMapping(String key, String channelName) {
		Assert.hasText(key);
		Assert.hasText(channelName);
		this.target.setChannelMapping(key, channelName);
		return _this();
	}

	public RouterSpec<R> subFlowMapping(String key, IntegrationFlow subFlow) {
		Assert.hasText(key);
		Assert.notNull(subFlow);
		Assert.state(!(StringUtils.hasText(this.prefix) || StringUtils.hasText(this.suffix)),
				"The 'prefix'('suffix') and 'subFlowMapping' are mutually exclusive");

		DirectChannel channel = new DirectChannel();
		IntegrationFlowBuilder flowBuilder = IntegrationFlows.from(channel);
		subFlow.accept(flowBuilder);

		this.subFlows.add(flowBuilder);

		if (this.mappingProvider == null) {
			this.mappingProvider = new RouterSubFlowMappingProvider(this.target);
		}
		this.mappingProvider.addMapping(key, channel);
		return _this();
	}

	@Override
	public Collection<Object> getComponentsToRegister() {
		if (this.mappingProvider != null) {
			this.subFlows.add(this.mappingProvider);
		}
		return this.subFlows;
	}

	private static class RouterSubFlowMappingProvider {

		private final MappingMessageRouterManagement router;

		private final Map<String, NamedComponent> mapping = new HashMap<String, NamedComponent>();

		public RouterSubFlowMappingProvider(MappingMessageRouterManagement router) {
			this.router = router;
		}

		void addMapping(String key, NamedComponent channel) {
			this.mapping.put(key, channel);
		}

		@PostConstruct
		public void init() {
			for (Map.Entry<String, NamedComponent> entry : this.mapping.entrySet()) {
				this.router.setChannelMapping(entry.getKey(), entry.getValue().getComponentName());

			}
		}

	}

}
