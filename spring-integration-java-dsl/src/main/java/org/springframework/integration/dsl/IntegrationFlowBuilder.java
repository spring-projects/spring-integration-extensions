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

import java.util.Collection;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.FixedSubscriberChannel;
import org.springframework.integration.config.SourcePollingChannelAdapterFactoryBean;
import org.springframework.integration.core.GenericSelector;
import org.springframework.integration.core.MessageSelector;
import org.springframework.integration.dsl.channel.MessageChannelSpec;
import org.springframework.integration.dsl.core.ConsumerEndpointSpec;
import org.springframework.integration.dsl.support.BeanNameMessageProcessor;
import org.springframework.integration.dsl.support.ComponentConfigurer;
import org.springframework.integration.dsl.support.EndpointConfigurer;
import org.springframework.integration.dsl.support.FixedSubscriberChannelPrototype;
import org.springframework.integration.dsl.support.GenericSplitter;
import org.springframework.integration.dsl.support.MessageChannelReference;
import org.springframework.integration.filter.ExpressionEvaluatingSelector;
import org.springframework.integration.filter.MessageFilter;
import org.springframework.integration.filter.MethodInvokingSelector;
import org.springframework.integration.handler.AbstractReplyProducingMessageHandler;
import org.springframework.integration.handler.BridgeHandler;
import org.springframework.integration.handler.DelayHandler;
import org.springframework.integration.handler.ServiceActivatingHandler;
import org.springframework.integration.splitter.AbstractMessageSplitter;
import org.springframework.integration.splitter.DefaultMessageSplitter;
import org.springframework.integration.splitter.ExpressionEvaluatingSplitter;
import org.springframework.integration.splitter.MethodInvokingSplitter;
import org.springframework.integration.transformer.ContentEnricher;
import org.springframework.integration.transformer.ExpressionEvaluatingTransformer;
import org.springframework.integration.transformer.GenericTransformer;
import org.springframework.integration.transformer.HeaderEnricher;
import org.springframework.integration.transformer.HeaderFilter;
import org.springframework.integration.transformer.MessageTransformingHandler;
import org.springframework.integration.transformer.MethodInvokingTransformer;
import org.springframework.integration.transformer.Transformer;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

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

	public IntegrationFlowBuilder fixedSubscriberChannel() {
		return this.fixedSubscriberChannel(null);
	}

	public IntegrationFlowBuilder fixedSubscriberChannel(String messageChannelName) {
		return this.channel(new FixedSubscriberChannelPrototype(messageChannelName));
	}

	public IntegrationFlowBuilder channel(String messageChannelName) {
		return this.channel(new MessageChannelReference(messageChannelName));
	}

	public IntegrationFlowBuilder channel(MessageChannel messageChannel) {
		Assert.notNull(messageChannel);
		if (this.currentMessageChannel != null) {
			this.register(new GenericEndpointSpec<BridgeHandler>(new BridgeHandler()), null);
		}
		this.currentMessageChannel = messageChannel;
		return this.registerOutputChannelIfCan(this.currentMessageChannel);
	}

	public IntegrationFlowBuilder channel(MessageChannelSpec<?, ?> messageChannelSpec) {
		Assert.notNull(messageChannelSpec);
		return this.channel(messageChannelSpec.get());
	}

	public IntegrationFlowBuilder transform(String expression) {
		Assert.hasText(expression);
		return this.transform(new ExpressionEvaluatingTransformer(PARSER.parseExpression(expression)));
	}

	public <S, T> IntegrationFlowBuilder transform(GenericTransformer<S, T> genericTransformer) {
		return this.transform(genericTransformer, null);
	}

	public <S, T> IntegrationFlowBuilder transform(GenericTransformer<S, T> genericTransformer,
												   EndpointConfigurer<GenericEndpointSpec<MessageTransformingHandler>> endpointConfigurer) {
		Assert.notNull(genericTransformer);
		Transformer transformer = genericTransformer instanceof Transformer
				? (Transformer) genericTransformer : new MethodInvokingTransformer(genericTransformer);
		return this.handle(new MessageTransformingHandler(transformer), endpointConfigurer);
	}

	public IntegrationFlowBuilder filter(String expression) {
		Assert.hasText(expression);
		return this.filter(new ExpressionEvaluatingSelector(PARSER.parseExpression(expression)));
	}

	public <S> IntegrationFlowBuilder filter(GenericSelector<S> genericSelector) {
		return this.filter(genericSelector, null);
	}

	public <S> IntegrationFlowBuilder filter(GenericSelector<S> genericSelector, EndpointConfigurer<FilterEndpointSpec> endpointConfigurer) {
		Assert.notNull(genericSelector);
		MessageSelector selector = genericSelector instanceof MessageSelector
				? (MessageSelector) genericSelector : new MethodInvokingSelector(genericSelector);
		return this.register(new FilterEndpointSpec(new MessageFilter(selector)), endpointConfigurer);
	}

	public IntegrationFlowBuilder handle(MessageHandler messageHandler) {
		return this.handle(messageHandler, null);
	}

	public IntegrationFlowBuilder handle(String beanName, String methodName) {
		return this.handle(beanName, methodName, null);
	}

	public IntegrationFlowBuilder handle(String beanName, String methodName, EndpointConfigurer<GenericEndpointSpec<ServiceActivatingHandler>> endpointConfigurer) {
		return this.handle(new ServiceActivatingHandler(new BeanNameMessageProcessor<Object>(beanName, methodName)), endpointConfigurer);
	}

	public <H extends MessageHandler> IntegrationFlowBuilder handle(H messageHandler, EndpointConfigurer<GenericEndpointSpec<H>> endpointConfigurer) {
		Assert.notNull(messageHandler);
		return this.register(new GenericEndpointSpec<H>(messageHandler), endpointConfigurer);
	}

	public IntegrationFlowBuilder bridge(EndpointConfigurer<GenericEndpointSpec<BridgeHandler>> endpointConfigurer) {
		return this.register(new GenericEndpointSpec<BridgeHandler>(new BridgeHandler()), endpointConfigurer);
	}

	public IntegrationFlowBuilder delay(String groupId, String expression) {
		return this.delay(groupId, expression, null);
	}

	public IntegrationFlowBuilder delay(String groupId, String expression, EndpointConfigurer<GenericEndpointSpec<DelayHandler>> endpointConfigurer) {
		DelayHandler delayHandler = new DelayHandler(groupId);
		if (StringUtils.hasText(expression)) {
			delayHandler.setDelayExpression(PARSER.parseExpression(expression));
		}
		return this.register(new GenericEndpointSpec<DelayHandler>(delayHandler), endpointConfigurer);
	}

	public IntegrationFlowBuilder enrich(ComponentConfigurer<EnricherSpec> enricherConfigurer) {
		return this.enrich(enricherConfigurer, null);
	}

	public IntegrationFlowBuilder enrich(ComponentConfigurer<EnricherSpec> enricherConfigurer, EndpointConfigurer<GenericEndpointSpec<ContentEnricher>> endpointConfigurer) {
		Assert.notNull(enricherConfigurer);
		EnricherSpec enricherSpec = new EnricherSpec();
		enricherConfigurer.configure(enricherSpec);
		return this.enrich(enricherSpec.get(), endpointConfigurer);
	}

	public IntegrationFlowBuilder enrich(ContentEnricher contentEnricher) {
		return this.enrich(contentEnricher, null);
	}

	public IntegrationFlowBuilder enrich(ContentEnricher contentEnricher, EndpointConfigurer<GenericEndpointSpec<ContentEnricher>> endpointConfigurer) {
		return this.handle(contentEnricher, endpointConfigurer);
	}

	public IntegrationFlowBuilder enrichHeaders(ComponentConfigurer<HeaderEnricherSpec> headerEnricherConfigurer) {
		return this.enrichHeaders(headerEnricherConfigurer, null);
	}

	public IntegrationFlowBuilder enrichHeaders(ComponentConfigurer<HeaderEnricherSpec> headerEnricherConfigurer,
												EndpointConfigurer<GenericEndpointSpec<MessageTransformingHandler>> endpointConfigurer) {
		Assert.notNull(headerEnricherConfigurer);
		HeaderEnricherSpec headerEnricherSpec = new HeaderEnricherSpec();
		headerEnricherConfigurer.configure(headerEnricherSpec);
		return this.enrichHeaders(headerEnricherSpec.get(), endpointConfigurer);
	}

	public IntegrationFlowBuilder enrichHeaders(HeaderEnricher headerEnricher) {
		return this.enrichHeaders(headerEnricher, null);
	}

	public IntegrationFlowBuilder enrichHeaders(HeaderEnricher headerEnricher, EndpointConfigurer<GenericEndpointSpec<MessageTransformingHandler>> endpointConfigurer) {
		return this.transform(headerEnricher, endpointConfigurer);
	}

	public IntegrationFlowBuilder split() {
		return this.split((EndpointConfigurer<GenericEndpointSpec<DefaultMessageSplitter>>) null);
	}

	public IntegrationFlowBuilder split(EndpointConfigurer<GenericEndpointSpec<DefaultMessageSplitter>> endpointConfigurer) {
		return this.split(new DefaultMessageSplitter(), endpointConfigurer);
	}

	public IntegrationFlowBuilder split(String expression) {
		return this.split(expression, (EndpointConfigurer<GenericEndpointSpec<ExpressionEvaluatingSplitter>>) null);
	}

	public IntegrationFlowBuilder split(String expression, EndpointConfigurer<GenericEndpointSpec<ExpressionEvaluatingSplitter>> endpointConfigurer) {
		return this.split(new ExpressionEvaluatingSplitter(PARSER.parseExpression(expression)), endpointConfigurer);
	}

	public IntegrationFlowBuilder split(String beanName, String methodName) {
		return this.split(beanName, methodName, null);
	}

	public IntegrationFlowBuilder split(String beanName, String methodName,
										EndpointConfigurer<GenericEndpointSpec<MethodInvokingSplitter>> endpointConfigurer) {
		return this.split(new MethodInvokingSplitter(new BeanNameMessageProcessor<Collection<?>>(beanName, methodName)),
				endpointConfigurer);
	}

	public IntegrationFlowBuilder split(AbstractMessageSplitter splitter) {
		return this.split(splitter, null);
	}

	public <T> IntegrationFlowBuilder split(GenericSplitter<T> splitter) {
		return this.split(splitter, null);
	}

	public <T> IntegrationFlowBuilder split(GenericSplitter<T> splitter,
											EndpointConfigurer<GenericEndpointSpec<MethodInvokingSplitter>> endpointConfigurer) {
		return this.split(new MethodInvokingSplitter(splitter, "split"), endpointConfigurer);
	}

	public <T extends AbstractMessageSplitter> IntegrationFlowBuilder split(T splitter, EndpointConfigurer<GenericEndpointSpec<T>> endpointConfigurer) {
		return this.handle(splitter, endpointConfigurer);
	}

	/**
	 * Provides the {@link HeaderFilter} to the current {@link IntegrationFlow}.
	 *
	 * @param headersToRemove the array of headers (or patterns) to remove from {@link org.springframework.messaging.MessageHeaders}.
	 * @return the {@link IntegrationFlowBuilder}.
	 */
	public IntegrationFlowBuilder headerFilter(String... headersToRemove) {
		return this.headerFilter(new HeaderFilter(headersToRemove), null);
	}

	/**
	 * Provides the {@link HeaderFilter} to the current {@link IntegrationFlow}.
	 *
	 * @param headersToRemove the comma separated headers (or patterns) to remove from {@link org.springframework.messaging.MessageHeaders}.
	 * @param patternMatch the {@code boolean} flag to indicate if {@code headersToRemove} should be interpreted as patterns or direct header names.
	 * @return the {@link IntegrationFlowBuilder}.
	 */

	public IntegrationFlowBuilder headerFilter(String headersToRemove, boolean patternMatch) {
		HeaderFilter headerFilter = new HeaderFilter(StringUtils.delimitedListToStringArray(headersToRemove, ",", " "));
		headerFilter.setPatternMatch(patternMatch);
		return this.headerFilter(headerFilter, null);
	}

	public IntegrationFlowBuilder headerFilter(HeaderFilter headerFilter, EndpointConfigurer<GenericEndpointSpec<MessageTransformingHandler>> endpointConfigurer) {
		return this.transform(headerFilter, endpointConfigurer);
	}

	private <S extends ConsumerEndpointSpec<?, ?>> IntegrationFlowBuilder register(S endpointSpec, EndpointConfigurer<S> endpointConfigurer) {
		if (endpointConfigurer != null) {
			endpointConfigurer.configure(endpointSpec);
		}
		MessageChannel inputChannel = this.currentMessageChannel;
		this.currentMessageChannel = null;
		if (inputChannel == null) {
			inputChannel = new DirectChannel();
			this.registerOutputChannelIfCan(inputChannel);
		}

		if (inputChannel instanceof MessageChannelReference) {
			endpointSpec.get().getT1().setInputChannelName(((MessageChannelReference) inputChannel).getName());
		}
		else {
			if (inputChannel instanceof FixedSubscriberChannelPrototype) {
				String beanName = ((FixedSubscriberChannelPrototype) inputChannel).getName();
				inputChannel = new FixedSubscriberChannel(endpointSpec.get().getT2());
				if (beanName != null) {
					((FixedSubscriberChannel) inputChannel).setBeanName(beanName);
				}
				this.registerOutputChannelIfCan(inputChannel);
			}
			endpointSpec.get().getT1().setInputChannel(inputChannel);
		}

		return this.addComponent(endpointSpec).currentComponent(endpointSpec.get().getT2());
	}

	private IntegrationFlowBuilder registerOutputChannelIfCan(MessageChannel outputChannel) {
		if (!(outputChannel instanceof FixedSubscriberChannelPrototype)) {
			this.flow.addComponent(outputChannel);
			if (this.currentComponent != null) {
				String channelName = null;
				if (outputChannel instanceof MessageChannelReference) {
					channelName = ((MessageChannelReference) outputChannel).getName();
				}
				if (this.currentComponent instanceof AbstractReplyProducingMessageHandler) {
					AbstractReplyProducingMessageHandler messageProducer = (AbstractReplyProducingMessageHandler) this.currentComponent;
					if (channelName != null) {
						messageProducer.setOutputChannelName(channelName);
					}
					else {
						messageProducer.setOutputChannel(outputChannel);
					}
				}
				else if (this.currentComponent instanceof SourcePollingChannelAdapterFactoryBean) {
					SourcePollingChannelAdapterFactoryBean pollingChannelAdapterFactoryBean = (SourcePollingChannelAdapterFactoryBean) this.currentComponent;
					if (channelName != null) {
						pollingChannelAdapterFactoryBean.setOutputChannelName(channelName);
					}
					else {
						pollingChannelAdapterFactoryBean.setOutputChannel(outputChannel);
					}
				}
				else {
					throw new BeanCreationException("The 'currentComponent' (" + this.currentComponent + ") is a one-way 'MessageHandler'" +
							" and it isn't appropriate to configure 'outputChannel'. This is the end of the integration flow.");
				}
				this.currentComponent = null;
			}
		}
		return this;
	}

	public IntegrationFlow get() {
		if (this.currentMessageChannel instanceof FixedSubscriberChannelPrototype) {
			throw new BeanCreationException("The 'currentMessageChannel' (" + this.currentMessageChannel + ") is a prototype" +
					" for FixedSubscriberChannel which can't be created without MessageHandler constructor argument. " +
					"That means that '.fixedSubscriberChannel()' can't be the last EIP-method in the IntegrationFlow definition.");
		}
		return this.flow;
	}

}
