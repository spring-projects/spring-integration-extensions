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

import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.SourcePollingChannelAdapterFactoryBean;
import org.springframework.integration.core.GenericSelector;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.core.MessageSelector;
import org.springframework.integration.dsl.support.EndpointConfigurer;
import org.springframework.integration.filter.ExpressionEvaluatingSelector;
import org.springframework.integration.filter.MessageFilter;
import org.springframework.integration.filter.MethodInvokingSelector;
import org.springframework.integration.handler.AbstractReplyProducingMessageHandler;
import org.springframework.integration.transformer.ExpressionEvaluatingTransformer;
import org.springframework.integration.transformer.GenericTransformer;
import org.springframework.integration.transformer.MessageTransformingHandler;
import org.springframework.integration.transformer.MethodInvokingTransformer;
import org.springframework.integration.transformer.Transformer;
import org.springframework.messaging.MessageChannel;

/**
 * @author Artem Bilan
 */
public final class IntegrationFlowBuilder {

	private final static SpelExpressionParser PARSER = new SpelExpressionParser();

	private final IntegrationFlow flow = new IntegrationFlow();

	private MessageChannel currentMessageChannel;

	private Object currentComponent;

	IntegrationFlowBuilder() {
	}

	IntegrationFlowBuilder addComponent(Object component) {
		this.flow.addComponent(component);
		return this;
	}

	IntegrationFlowBuilder currentComponent(Object component) {
		this.currentComponent = component;
		return this;
	}

	public IntegrationFlowBuilder channel(MessageChannel messageChannel) {
		this.currentMessageChannel = messageChannel;
		return this.addComponent(this.currentMessageChannel).registerOutputChannelIfCan(this.currentMessageChannel);
	}

	private IntegrationFlowBuilder registerOutputChannelIfCan(MessageChannel outputChannel) {
		this.flow.addComponent(outputChannel);
		if (this.currentComponent != null) {
			if (this.currentComponent instanceof MessageProducer) {
				((MessageProducer) this.currentComponent).setOutputChannel(outputChannel);
			}
			if (this.currentComponent instanceof AbstractReplyProducingMessageHandler) {
				((AbstractReplyProducingMessageHandler) this.currentComponent).setOutputChannel(outputChannel);
			}
			else if (this.currentComponent instanceof SourcePollingChannelAdapterFactoryBean) {
				((SourcePollingChannelAdapterFactoryBean) this.currentComponent).setOutputChannel(outputChannel);
			}
			this.currentComponent = null;
		}
		return this;
	}

	public IntegrationFlowBuilder transform(String expression) {
		return this.transform(PARSER.parseExpression(expression));
	}

	public IntegrationFlowBuilder transform(Expression expression) {
		return this.transform(new ExpressionEvaluatingTransformer(expression));
	}

	public <S, T> IntegrationFlowBuilder transform(GenericTransformer<S, T> genericTransformer) {
		Transformer transformer = genericTransformer instanceof Transformer
				? (Transformer) genericTransformer : new MethodInvokingTransformer(genericTransformer);
		return this.transform(genericTransformer, new DefaultEndpointConfigurer<GenericEndpointSpec<MessageTransformingHandler>>());
	}

	public <S, T> IntegrationFlowBuilder transform(GenericTransformer<S, T> genericTransformer,
												   EndpointConfigurer<GenericEndpointSpec<MessageTransformingHandler>> endpointConfigurer) {
		Transformer transformer = genericTransformer instanceof Transformer
				? (Transformer) genericTransformer : new MethodInvokingTransformer(genericTransformer);
		GenericEndpointSpec<MessageTransformingHandler> spec = new GenericEndpointSpec<MessageTransformingHandler>(new MessageTransformingHandler(transformer));
		endpointConfigurer.configure(spec);
		return this.register(spec);
	}

	public IntegrationFlowBuilder filter(String expression) {
		return this.filter(PARSER.parseExpression(expression));
	}

	public IntegrationFlowBuilder filter(Expression expression) {
		return this.filter(new ExpressionEvaluatingSelector(expression));
	}

	public <S> IntegrationFlowBuilder filter(GenericSelector<S> genericSelector) {
		return this.filter(genericSelector, new DefaultEndpointConfigurer<FilterEndpointSpec>());
	}

	public <S> IntegrationFlowBuilder filter(GenericSelector<S> genericSelector, EndpointConfigurer<FilterEndpointSpec> endpointConfigurer) {
		MessageSelector selector = genericSelector instanceof MessageSelector
				? (MessageSelector) genericSelector : new MethodInvokingSelector(genericSelector);
		FilterEndpointSpec spec = new FilterEndpointSpec(new MessageFilter(selector));
		endpointConfigurer.configure(spec);
		return this.register(spec);
	}

	private IntegrationFlowBuilder register(EndpointSpec<?, ?> endpointSpec) {
		MessageChannel inputChannel = this.currentMessageChannel;
		this.currentMessageChannel = null;
		if (inputChannel == null) {
			inputChannel = new DirectChannel();
			this.registerOutputChannelIfCan(inputChannel);
		}

		endpointSpec.getEndpoint().setInputChannel(inputChannel);

		return this.addComponent(endpointSpec).currentComponent(endpointSpec.getHandler());
	}

	public IntegrationFlow get() {
		return this.flow;
	}

	private class DefaultEndpointConfigurer<S extends EndpointSpec<?, ?>> implements EndpointConfigurer<S> {

		@Override
		public void configure(S spec) {

		}
	}

}
