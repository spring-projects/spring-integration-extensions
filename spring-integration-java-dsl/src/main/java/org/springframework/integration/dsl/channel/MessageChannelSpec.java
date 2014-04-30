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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.integration.dsl.core.IntegrationComponentSpec;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.util.Assert;

/**
 * @author Artem Bilan
 */
public abstract class MessageChannelSpec<S extends MessageChannelSpec<S, C>, C extends AbstractMessageChannel>
		extends IntegrationComponentSpec<S, C> {

	protected C channel;

	private final List<Class<?>> datatypes = new ArrayList<Class<?>>();

	private final List<ChannelInterceptor> interceptors = new LinkedList<ChannelInterceptor>();

	private MessageConverter messageConverter;

	@Override
	protected S id(String id) {
		return super.id(id);
	}

	public S datatype(Class<?>... datatypes) {
		Assert.noNullElements(datatypes);
		this.datatypes.addAll(Arrays.asList(datatypes));
		return _this();
	}

	public S interceptor(ChannelInterceptor... interceptors) {
		Assert.noNullElements(interceptors);
		this.interceptors.addAll(Arrays.asList(interceptors));
		return _this();
	}

	public S messageConverter(MessageConverter messageConverter) {
		this.messageConverter = messageConverter;
		return _this();
	}

	@Override
	protected C doGet() {
		this.channel.setDatatypes(this.datatypes.toArray(new Class<?>[this.datatypes.size()]));
		this.channel.setBeanName(this.id);
		this.channel.setInterceptors(this.interceptors);
		this.channel.setMessageConverter(this.messageConverter);
		return this.channel;
	}

}
