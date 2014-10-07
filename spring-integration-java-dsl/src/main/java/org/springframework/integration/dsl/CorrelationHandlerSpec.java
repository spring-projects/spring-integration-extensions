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

import org.springframework.expression.Expression;
import org.springframework.integration.aggregator.AbstractCorrelatingMessageHandler;
import org.springframework.integration.aggregator.CorrelationStrategy;
import org.springframework.integration.aggregator.ExpressionEvaluatingCorrelationStrategy;
import org.springframework.integration.aggregator.ExpressionEvaluatingReleaseStrategy;
import org.springframework.integration.aggregator.ReleaseStrategy;
import org.springframework.integration.config.CorrelationStrategyFactoryBean;
import org.springframework.integration.config.ReleaseStrategyFactoryBean;
import org.springframework.integration.dsl.core.MessageHandlerSpec;
import org.springframework.integration.dsl.support.Function;
import org.springframework.integration.dsl.support.FunctionExpression;
import org.springframework.integration.expression.ValueExpression;
import org.springframework.integration.store.MessageGroup;
import org.springframework.integration.store.MessageGroupStore;
import org.springframework.messaging.MessageChannel;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.util.StringUtils;

/**
 * @author Artem Bilan
 */
public abstract class
		CorrelationHandlerSpec<S extends CorrelationHandlerSpec<S, H>, H extends AbstractCorrelatingMessageHandler>
		extends MessageHandlerSpec<S, H> {

	protected MessageGroupStore messageStore;

	protected boolean sendPartialResultOnExpiry;

	private long minimumTimeoutForEmptyGroups;

	private Expression groupTimeoutExpression;

	private TaskScheduler taskScheduler;

	private MessageChannel discardChannel;

	private String discardChannelName;

	private CorrelationStrategy correlationStrategy;

	private ReleaseStrategy releaseStrategy;

	public S messageStore(MessageGroupStore messageStore) {
		this.messageStore = messageStore;
		return _this();
	}

	public S sendPartialResultOnExpiry(boolean sendPartialResultOnExpiry) {
		this.sendPartialResultOnExpiry = sendPartialResultOnExpiry;
		return _this();
	}

	public S minimumTimeoutForEmptyGroups(long minimumTimeoutForEmptyGroups) {
		this.minimumTimeoutForEmptyGroups = minimumTimeoutForEmptyGroups;
		return _this();
	}

	public S groupTimeout(long groupTimeout) {
		this.groupTimeoutExpression = new ValueExpression<Long>(groupTimeout);
		return _this();
	}

	public S groupTimeoutExpression(String groupTimeoutExpression) {
		this.groupTimeoutExpression = PARSER.parseExpression(groupTimeoutExpression);
		return _this();
	}

	public S groupTimeout(Function<MessageGroup, Long> groupTimeoutFunction) {
		this.groupTimeoutExpression = new FunctionExpression<MessageGroup>(groupTimeoutFunction);
		return _this();
	}

	public S taskScheduler(TaskScheduler taskScheduler) {
		this.taskScheduler = taskScheduler;
		return _this();
	}

	public S discardChannel(MessageChannel discardChannel) {
		this.discardChannel = discardChannel;
		return _this();
	}

	public S discardChannel(String discardChannelName) {
		this.discardChannelName = discardChannelName;
		return _this();
	}

	public S processor(Object target, String methodName) {
		try {
			return correlationStrategy(new CorrelationStrategyFactoryBean(target, methodName).getObject())
					.releaseStrategy(new ReleaseStrategyFactoryBean(target, methodName).getObject());
		}
		catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public S correlationExpression(String correlationExpression) {
		return correlationStrategy(new ExpressionEvaluatingCorrelationStrategy(correlationExpression));
	}

	public S correlationStrategy(Object target, String methodName) {
		try {
			return correlationStrategy(new CorrelationStrategyFactoryBean(target, methodName).getObject());
		}
		catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public S correlationStrategy(CorrelationStrategy correlationStrategy) {
		this.correlationStrategy = correlationStrategy;
		return _this();
	}

	public S releaseExpression(String releaseExpression) {
		return releaseStrategy(new ExpressionEvaluatingReleaseStrategy(releaseExpression));
	}

	public S releaseStrategy(Object target, String methodName) {
		try {
			return releaseStrategy(new ReleaseStrategyFactoryBean(target, methodName).getObject());
		}
		catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public S releaseStrategy(ReleaseStrategy releaseStrategy) {
		this.releaseStrategy = releaseStrategy;
		return _this();
	}

	protected H configure(H handler) {
		if (this.discardChannel != null) {
			handler.setDiscardChannel(this.discardChannel);
		}
		if (StringUtils.hasText(this.discardChannelName)) {
			handler.setDiscardChannelName(this.discardChannelName);
		}
		if (this.messageStore != null) {
			handler.setMessageStore(this.messageStore);
		}
		handler.setMinimumTimeoutForEmptyGroups(this.minimumTimeoutForEmptyGroups);
		handler.setGroupTimeoutExpression(this.groupTimeoutExpression);
		if (this.taskScheduler != null) {
			handler.setTaskScheduler(this.taskScheduler);
		}
		handler.setSendPartialResultOnExpiry(this.sendPartialResultOnExpiry);
		if (this.correlationStrategy != null) {
			handler.setCorrelationStrategy(this.correlationStrategy);
		}
		if (this.releaseStrategy != null) {
			handler.setReleaseStrategy(this.releaseStrategy);
		}
		return handler;
	}

	CorrelationHandlerSpec() {
	}

}
