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

package org.springframework.integration.dsl.core;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.aopalliance.aop.Advice;

import org.springframework.integration.config.ConsumerEndpointFactoryBean;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.integration.handler.AbstractReplyProducingMessageHandler;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.messaging.MessageHandler;

/**
 * @author Artem Bilan
 */
public abstract class ConsumerEndpointSpec<S extends ConsumerEndpointSpec<S, H>, H extends MessageHandler>
		extends EndpointSpec<S, ConsumerEndpointFactoryBean, H> {

	private final List<Advice> adviceChain = new LinkedList<Advice>();

	protected ConsumerEndpointSpec(H messageHandler) {
		super(messageHandler);
		this.target.getT1().setHandler(messageHandler);
		if (messageHandler instanceof AbstractReplyProducingMessageHandler) {
			((AbstractReplyProducingMessageHandler) messageHandler).setAdviceChain(this.adviceChain);
		}
		else {
			this.target.getT1().setAdviceChain(this.adviceChain);
		}
	}

	public S phase(int phase) {
		this.target.getT1().setPhase(phase);
		return _this();
	}

	public S autoStartup(boolean autoStartup) {
		this.target.getT1().setAutoStartup(autoStartup);
		return _this();
	}

	public S poller(PollerMetadata pollerMetadata) {
		this.target.getT1().setPollerMetadata(pollerMetadata);
		return _this();
	}

	public S advice(Advice... advice) {
		this.adviceChain.addAll(Arrays.asList(advice));
		return _this();
	}

	public S requiresReply(boolean requiresReply) {
		H handler = this.target.getT2();
		if (handler instanceof AbstractReplyProducingMessageHandler) {
			((AbstractReplyProducingMessageHandler) handler).setRequiresReply(requiresReply);
		}
		else {
			logger.warn("'requiresReply' can be applied only for AbstractReplyProducingMessageHandler");
		}
		return _this();
	}

	public S sendTimeout(long sendTimeout) {
		H handler = this.target.getT2();
		if (handler instanceof AbstractReplyProducingMessageHandler) {
			((AbstractReplyProducingMessageHandler) handler).setSendTimeout(sendTimeout);
		}
		else {
			logger.warn("'sendTimeout' can be applied only for AbstractReplyProducingMessageHandler");
		}
		return _this();
	}

	public S order(int order) {
		H handler = this.target.getT2();
		if (handler instanceof AbstractMessageHandler) {
			((AbstractMessageHandler) handler).setOrder(order);
		}
		else {
			logger.warn("'order' can be applied only for AbstractMessageHandler");
		}
		return _this();
	}

}
