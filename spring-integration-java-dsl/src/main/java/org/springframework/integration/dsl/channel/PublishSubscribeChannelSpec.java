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

import java.util.concurrent.Executor;

import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.util.ErrorHandler;

/**
 * @author Artem Bilan
 */
public class PublishSubscribeChannelSpec
		extends MessageChannelSpec<PublishSubscribeChannelSpec, PublishSubscribeChannel> {

	PublishSubscribeChannelSpec() {
		this.channel = new PublishSubscribeChannel();
	}

	PublishSubscribeChannelSpec(Executor executor) {
		this.channel = new PublishSubscribeChannel(executor);
	}

	PublishSubscribeChannelSpec errorHandler(ErrorHandler errorHandler) {
		this.channel.setErrorHandler(errorHandler);
		return this;
	}

	public PublishSubscribeChannelSpec ignoreFailures(boolean ignoreFailures) {
		this.channel.setIgnoreFailures(ignoreFailures);
		return this;
	}

	public PublishSubscribeChannelSpec applySequence(boolean applySequence) {
		this.channel.setApplySequence(applySequence);
		return this;
	}

	public PublishSubscribeChannelSpec maxSubscribers(Integer maxSubscribers) {
		this.channel.setMaxSubscribers(maxSubscribers);
		return this;
	}

	public PublishSubscribeChannelSpec minSubscribers(int minSubscribers) {
		this.channel.setMinSubscribers(minSubscribers);
		return this;
	}

}
