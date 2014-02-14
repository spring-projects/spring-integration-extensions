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

package org.springframework.integration.dsl.channel;

import java.util.Arrays;

import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.integration.dsl.core.IntegrationComponentSpec;
import org.springframework.messaging.support.ChannelInterceptor;

/**
 * @author Artem Bilan
 */
public abstract class MessageChannelSpec<S extends MessageChannelSpec<S, C>, C extends AbstractMessageChannel> extends IntegrationComponentSpec<S, C> {

	protected C channel;

	private Class<?>[] datatypes;

	private ChannelInterceptor[] interceptors;

	@Override
	protected S id(String id) {
		return super.id(id);
	}

	public S datatypes(Class<?>... datatypes) {
		this.datatypes = datatypes;
		return _this();
	}

	public S interceptors(ChannelInterceptor... interceptors) {
		this.interceptors = interceptors;
		return _this();
	}

	@Override
	protected C doGet() {
		this.channel.setDatatypes(this.datatypes);
		this.channel.setBeanName(this.id);
		if (this.interceptors != null) {
			this.channel.setInterceptors(Arrays.asList(this.interceptors));
		}
		return this.channel;
	}

}
