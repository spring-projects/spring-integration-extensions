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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.aopalliance.aop.Advice;

import org.springframework.integration.dsl.core.ConsumerEndpointSpec;
import org.springframework.integration.handler.DelayHandler;
import org.springframework.integration.store.MessageGroupStore;

/**
 * @author Artem Bilan
 */
public final class DelayerEndpointSpec extends ConsumerEndpointSpec<DelayerEndpointSpec, DelayHandler> {

	private final List<Advice> delayedAdvice = new LinkedList<Advice>();

	DelayerEndpointSpec(DelayHandler delayHandler) {
		super(delayHandler);
		this.target.getT2().setDelayedAdviceChain(this.delayedAdvice);
	}

	public DelayerEndpointSpec defaultDelay(long defaultDelay) {
		this.target.getT2().setDefaultDelay(defaultDelay);
		return _this();
	}


	public DelayerEndpointSpec ignoreExpressionFailures(boolean ignoreExpressionFailures) {
		this.target.getT2().setIgnoreExpressionFailures(ignoreExpressionFailures);
		return _this();
	}

	public DelayerEndpointSpec messageStore(MessageGroupStore messageStore) {
		this.target.getT2().setMessageStore(messageStore);
		return _this();
	}

	public DelayerEndpointSpec delayedAdvice(Advice... advice) {
		this.delayedAdvice.addAll(Arrays.asList(advice));
		return _this();
	}

}
