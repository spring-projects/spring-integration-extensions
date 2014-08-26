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

package org.springframework.integration.dsl.core;

import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.messaging.MessageChannel;

/**
 * @author Artem Bilan
 */
public abstract class MessageProducerSpec<S extends MessageProducerSpec<S, P>, P extends MessageProducerSupport>
		extends IntegrationComponentSpec<S, P> {

	public MessageProducerSpec(P producer) {
		this.target = producer;
	}

	public S id(String id) {
		this.target.setBeanName(id);
		return super.id(id);
	}

	public S phase(int phase) {
		this.target.setPhase(phase);
		return _this();
	}

	public S autoStartup(boolean autoStartup) {
		this.target.setAutoStartup(autoStartup);
		return _this();
	}

	public S outputChannel(MessageChannel outputChannel) {
		target.setOutputChannel(outputChannel);
		return _this();
	}

	public S errorChannel(MessageChannel errorChannel) {
		target.setErrorChannel(errorChannel);
		return _this();
	}

	@Override
	protected P doGet() {
		throw new UnsupportedOperationException();
	}

}
