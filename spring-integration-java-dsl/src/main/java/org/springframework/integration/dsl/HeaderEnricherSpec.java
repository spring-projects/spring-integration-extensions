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

import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.integration.dsl.core.IntegrationComponentSpec;
import org.springframework.integration.dsl.support.BeanNameMessageProcessor;
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

	public HeaderEnricherSpec header(String name, Object value) {
		return this.header(name, value, null);
	}

	public HeaderEnricherSpec header(String name, Object value, Boolean overwrite) {
		AbstractHeaderValueMessageProcessor<?> headerValueMessageProcessor = new StaticHeaderValueMessageProcessor<Object>(value);
		headerValueMessageProcessor.setOverwrite(overwrite);
		return this.header(name, headerValueMessageProcessor);
	}

	public HeaderEnricherSpec headerExpression(String name, String expression) {
		return this.headerExpression(name, expression, null, null);
	}

	public HeaderEnricherSpec headerExpression(String name, String expression, Boolean overwrite) {
		return this.headerExpression(name, expression, overwrite, null);
	}

	public HeaderEnricherSpec headerExpression(String name, String expression, Class<?> type) {
		return this.headerExpression(name, expression, null, type);
	}

	public <T> HeaderEnricherSpec headerExpression(String name, String expression, Boolean overwrite, Class<T> type) {
		AbstractHeaderValueMessageProcessor<T> headerValueMessageProcessor =
				new ExpressionEvaluatingHeaderValueMessageProcessor<T>(expression, type);
		headerValueMessageProcessor.setOverwrite(overwrite);
		return this.header(name, headerValueMessageProcessor);
	}

	public HeaderEnricherSpec header(String name, HeaderValueMessageProcessor<?> headerValueMessageProcessor) {
		Assert.notNull(name);
		this.headerToAdd.put(name, headerValueMessageProcessor);
		return _this();
	}


	@Override
	protected HeaderEnricher doGet() {
		return this.headerEnricher;
	}

}
