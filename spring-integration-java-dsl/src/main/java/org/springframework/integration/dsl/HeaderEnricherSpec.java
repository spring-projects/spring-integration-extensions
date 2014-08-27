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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.integration.context.IntegrationContextUtils;
import org.springframework.integration.dsl.core.IntegrationComponentSpec;
import org.springframework.integration.dsl.support.BeanNameMessageProcessor;
import org.springframework.integration.dsl.support.MapBuilder;
import org.springframework.integration.dsl.support.MapBuilder.MapBuilderConfigurer;
import org.springframework.integration.dsl.support.StringStringMapBuilder;
import org.springframework.integration.handler.ExpressionEvaluatingMessageProcessor;
import org.springframework.integration.handler.MessageProcessor;
import org.springframework.integration.transformer.HeaderEnricher;
import org.springframework.integration.transformer.support.AbstractHeaderValueMessageProcessor;
import org.springframework.integration.transformer.support.ExpressionEvaluatingHeaderValueMessageProcessor;
import org.springframework.integration.transformer.support.HeaderValueMessageProcessor;
import org.springframework.integration.transformer.support.StaticHeaderValueMessageProcessor;
import org.springframework.util.Assert;

/**
 * @author Artem Bilan
 * @author Gary Russell
 */
public class HeaderEnricherSpec extends IntegrationComponentSpec<HeaderEnricherSpec, HeaderEnricher> {

	private final static SpelExpressionParser PARSER = new SpelExpressionParser();

	private final Map<String, HeaderValueMessageProcessor<?>> headerToAdd = new HashMap<String, HeaderValueMessageProcessor<?>>();

	private final HeaderEnricher headerEnricher = new HeaderEnricher(headerToAdd);

	HeaderEnricherSpec() {
	}

	public HeaderEnricherSpec defaultOverwrite(boolean defaultOverwrite) {
		this.headerEnricher.setDefaultOverwrite(defaultOverwrite);
		return _this();
	}

	public HeaderEnricherSpec shouldSkipNulls(boolean shouldSkipNulls) {
		this.headerEnricher.setShouldSkipNulls(shouldSkipNulls);
		return _this();
	}


	public HeaderEnricherSpec messageProcessor(MessageProcessor<?> messageProcessor) {
		this.headerEnricher.setMessageProcessor(messageProcessor);
		return _this();
	}

	public HeaderEnricherSpec messageProcessor(String expression) {
		return this.messageProcessor(new ExpressionEvaluatingMessageProcessor<Object>(PARSER.parseExpression(expression)));
	}

	public HeaderEnricherSpec messageProcessor(String beanName, String methodName) {
		return this.messageProcessor(new BeanNameMessageProcessor<Object>(beanName, methodName));
	}

	public HeaderEnricherSpec headers(MapBuilder<?, String, Object> headers) {
		return headers(headers.get());
	}

	public HeaderEnricherSpec headers(Map<String, Object> headers) {
		Assert.notNull(headers);
		for (Entry<String, Object> entry : headers.entrySet()) {
			String name = entry.getKey();
			Object value = entry.getValue();
			if (value instanceof Expression) {
				header(name, new ExpressionEvaluatingHeaderValueMessageProcessor<Object>((Expression) value, null));
			}
			else {
				header(name, value);
			}
		}
		return this;
	}

	public HeaderEnricherSpec headerExpressions(MapBuilder<?, String, String> headers) {
		return headerExpressions(headers.get());
	}

	public HeaderEnricherSpec headerExpressions(MapBuilderConfigurer<StringStringMapBuilder, String, String> configurer) {
		StringStringMapBuilder builder = new StringStringMapBuilder();
		configurer.configure(builder);
		return headerExpressions(builder.get());
	}

	public HeaderEnricherSpec headerExpressions(Map<String, String> headers) {
		Assert.notNull(headers);
		for (Entry<String, String> entry : headers.entrySet()) {
			header(entry.getKey(), new ExpressionEvaluatingHeaderValueMessageProcessor<Object>(entry.getValue(), null));
		}
		return this;
	}

	public <V> HeaderEnricherSpec header(String name, V value) {
		return this.header(name, value, null);
	}

	public <V> HeaderEnricherSpec header(String name, V value, Boolean overwrite) {
		AbstractHeaderValueMessageProcessor<V> headerValueMessageProcessor =
				new StaticHeaderValueMessageProcessor<V>(value);
		headerValueMessageProcessor.setOverwrite(overwrite);
		return this.header(name, headerValueMessageProcessor);
	}

	public HeaderEnricherSpec headerExpression(String name, String expression) {
		return this.headerExpression(name, expression, null);
	}

	public HeaderEnricherSpec headerExpression(String name, String expression, Boolean overwrite) {
		AbstractHeaderValueMessageProcessor<?> headerValueMessageProcessor =
				new ExpressionEvaluatingHeaderValueMessageProcessor<Object>(expression, null);
		headerValueMessageProcessor.setOverwrite(overwrite);
		return this.header(name, headerValueMessageProcessor);
	}

	public <V> HeaderEnricherSpec header(String name, HeaderValueMessageProcessor<V> headerValueMessageProcessor) {
		Assert.notNull(name);
		this.headerToAdd.put(name, headerValueMessageProcessor);
		return _this();
	}

	public <V> HeaderEnricherSpec headerChannelsToString() {
		return headerExpression("replyChannel",
				"@" + IntegrationContextUtils.INTEGRATION_HEADER_CHANNEL_REGISTRY_BEAN_NAME
						+ ".channelToChannelName(headers.replyChannel)",
				true)
				.headerExpression("errorChannel",
						"@" + IntegrationContextUtils.INTEGRATION_HEADER_CHANNEL_REGISTRY_BEAN_NAME
								+ ".channelToChannelName(headers.errorChannel)",
						true);
	}

	@Override
	protected HeaderEnricher doGet() {
		return this.headerEnricher;
	}

}
