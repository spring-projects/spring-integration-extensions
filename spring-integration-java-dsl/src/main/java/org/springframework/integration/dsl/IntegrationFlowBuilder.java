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
import org.springframework.integration.aggregator.AbstractCorrelatingMessageHandler;
import org.springframework.integration.aggregator.AggregatingMessageHandler;
import org.springframework.integration.aggregator.DefaultAggregatingMessageGroupProcessor;
import org.springframework.integration.aggregator.ResequencingMessageHandler;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.FixedSubscriberChannel;
import org.springframework.integration.config.SourcePollingChannelAdapterFactoryBean;
import org.springframework.integration.core.GenericSelector;
import org.springframework.integration.core.MessageSelector;
import org.springframework.integration.dsl.channel.MessageChannelSpec;
import org.springframework.integration.dsl.core.ConsumerEndpointSpec;
import org.springframework.integration.dsl.core.MessageHandlerSpec;
import org.springframework.integration.dsl.support.BeanNameMessageProcessor;
import org.springframework.integration.dsl.support.ComponentConfigurer;
import org.springframework.integration.dsl.support.EndpointConfigurer;
import org.springframework.integration.dsl.support.FixedSubscriberChannelPrototype;
import org.springframework.integration.dsl.support.GenericHandler;
import org.springframework.integration.dsl.support.GenericRouter;
import org.springframework.integration.dsl.support.GenericSplitter;
import org.springframework.integration.dsl.support.MessageChannelReference;
import org.springframework.integration.expression.ControlBusMethodFilter;
import org.springframework.integration.filter.ExpressionEvaluatingSelector;
import org.springframework.integration.filter.MessageFilter;
import org.springframework.integration.filter.MethodInvokingSelector;
import org.springframework.integration.handler.AbstractReplyProducingMessageHandler;
import org.springframework.integration.handler.BridgeHandler;
import org.springframework.integration.handler.DelayHandler;
import org.springframework.integration.handler.ExpressionCommandMessageProcessor;
import org.springframework.integration.handler.ServiceActivatingHandler;
import org.springframework.integration.router.AbstractMappingMessageRouter;
import org.springframework.integration.router.AbstractMessageRouter;
import org.springframework.integration.router.ExpressionEvaluatingRouter;
import org.springframework.integration.router.MethodInvokingRouter;
import org.springframework.integration.router.RecipientListRouter;
import org.springframework.integration.splitter.AbstractMessageSplitter;
import org.springframework.integration.splitter.DefaultMessageSplitter;
import org.springframework.integration.splitter.ExpressionEvaluatingSplitter;
import org.springframework.integration.splitter.MethodInvokingSplitter;
import org.springframework.integration.store.MessageStore;
import org.springframework.integration.transformer.ClaimCheckInTransformer;
import org.springframework.integration.transformer.ClaimCheckOutTransformer;
import org.springframework.integration.transformer.ContentEnricher;
import org.springframework.integration.transformer.ExpressionEvaluatingTransformer;
import org.springframework.integration.transformer.GenericTransformer;
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

	public IntegrationFlowBuilder controlBus() {
		return controlBus(null);
	}

	public IntegrationFlowBuilder controlBus(EndpointConfigurer<GenericEndpointSpec<ServiceActivatingHandler>> endpointConfigurer) {
		return this.handle(new ServiceActivatingHandler(new ExpressionCommandMessageProcessor(new ControlBusMethodFilter())), endpointConfigurer);
	}

	public IntegrationFlowBuilder transform(String expression) {
		Assert.hasText(expression);
		return this.transform(new ExpressionEvaluatingTransformer(PARSER.parseExpression(expression)));
	}

	public <S, T> IntegrationFlowBuilder transform(GenericTransformer<S, T> genericTransformer) {
		return this.transform(null, genericTransformer);
	}

	public <P, T> IntegrationFlowBuilder transform(Class<P> payloadType, GenericTransformer<P, T> genericTransformer) {
		return this.transform(payloadType, genericTransformer, null);
	}

	public <S, T> IntegrationFlowBuilder transform(GenericTransformer<S, T> genericTransformer,
			EndpointConfigurer<GenericEndpointSpec<MessageTransformingHandler>> endpointConfigurer) {
		return this.transform(null, genericTransformer, endpointConfigurer);
	}

	public <P, T> IntegrationFlowBuilder transform(Class<P> payloadType, GenericTransformer<P, T> genericTransformer,
			EndpointConfigurer<GenericEndpointSpec<MessageTransformingHandler>> endpointConfigurer) {
		Assert.notNull(genericTransformer);
		Transformer transformer = genericTransformer instanceof Transformer ? (Transformer) genericTransformer :
				(isLambda(genericTransformer)
						? new MethodInvokingTransformer(new LambdaMessageProcessor(genericTransformer, payloadType))
						: new MethodInvokingTransformer(genericTransformer));
		return addComponent(transformer)
				.handle(new MessageTransformingHandler(transformer), endpointConfigurer);
	}

	public IntegrationFlowBuilder filter(String expression) {
		Assert.hasText(expression);
		return this.filter(new ExpressionEvaluatingSelector(PARSER.parseExpression(expression)));
	}

	public <S> IntegrationFlowBuilder filter(GenericSelector<S> genericSelector) {
		return this.filter(null, genericSelector);
	}

	public <P> IntegrationFlowBuilder filter(Class<P> payloadType, GenericSelector<P> genericSelector) {
		return this.filter(payloadType, genericSelector, null);
	}

	public <P> IntegrationFlowBuilder filter(GenericSelector<P> genericSelector,
			EndpointConfigurer<FilterEndpointSpec> endpointConfigurer) {
		return filter(null, genericSelector, endpointConfigurer);
	}

	public <P> IntegrationFlowBuilder filter(Class<P> payloadType, GenericSelector<P> genericSelector,
			EndpointConfigurer<FilterEndpointSpec> endpointConfigurer) {
		Assert.notNull(genericSelector);
		MessageSelector selector = genericSelector instanceof MessageSelector ? (MessageSelector) genericSelector :
				(isLambda(genericSelector)
						? new MethodInvokingSelector(new LambdaMessageProcessor(genericSelector, payloadType))
						: new MethodInvokingSelector(genericSelector));
		return this.register(new FilterEndpointSpec(new MessageFilter(selector)), endpointConfigurer);
	}

	public <S extends MessageHandlerSpec<S, ? extends MessageHandler>> IntegrationFlowBuilder handle(S messageHandlerSpec) {
		Assert.notNull(messageHandlerSpec);
		return handle(messageHandlerSpec.get());
	}

	public IntegrationFlowBuilder handle(MessageHandler messageHandler) {
		return this.handle(messageHandler, null);
	}

	public IntegrationFlowBuilder handle(String beanName, String methodName) {
		return this.handle(beanName, methodName, null);
	}

	public IntegrationFlowBuilder handle(String beanName, String methodName,
			EndpointConfigurer<GenericEndpointSpec<ServiceActivatingHandler>> endpointConfigurer) {
		return this.handle(new ServiceActivatingHandler(new BeanNameMessageProcessor<Object>(beanName, methodName)),
				endpointConfigurer);
	}

	public <P> IntegrationFlowBuilder handle(GenericHandler<P> handler) {
		return this.handle(null, handler);
	}

	public <P> IntegrationFlowBuilder handle(GenericHandler<P> handler,
			EndpointConfigurer<GenericEndpointSpec<ServiceActivatingHandler>> endpointConfigurer) {
		return this.handle(null, handler, endpointConfigurer);
	}


	public <P> IntegrationFlowBuilder handle(Class<P> payloadType, GenericHandler<P> handler) {
		return this.handle(payloadType, handler, null);
	}

	public <P> IntegrationFlowBuilder handle(Class<P> payloadType, GenericHandler<P> handler,
			EndpointConfigurer<GenericEndpointSpec<ServiceActivatingHandler>> endpointConfigurer) {
		ServiceActivatingHandler serviceActivatingHandler = null;
		if (isLambda(handler)) {
			serviceActivatingHandler = new ServiceActivatingHandler(new LambdaMessageProcessor(handler, payloadType));
		}
		else {
			serviceActivatingHandler = new ServiceActivatingHandler(handler);
		}
		return this.handle(serviceActivatingHandler, endpointConfigurer);
	}

	public <H extends MessageHandler, S extends MessageHandlerSpec<S, H>>
	IntegrationFlowBuilder handle(S messageHandlerSpec, EndpointConfigurer<GenericEndpointSpec<H>> endpointConfigurer) {
		Assert.notNull(messageHandlerSpec);
		return handle(messageHandlerSpec.get(), endpointConfigurer);
	}

	public <H extends MessageHandler> IntegrationFlowBuilder handle(H messageHandler,
			EndpointConfigurer<GenericEndpointSpec<H>> endpointConfigurer) {
		Assert.notNull(messageHandler);
		return this.register(new GenericEndpointSpec<H>(messageHandler), endpointConfigurer);
	}

	public IntegrationFlowBuilder bridge(EndpointConfigurer<GenericEndpointSpec<BridgeHandler>> endpointConfigurer) {
		return this.register(new GenericEndpointSpec<BridgeHandler>(new BridgeHandler()), endpointConfigurer);
	}

	public IntegrationFlowBuilder delay(String groupId, String expression) {
		return this.delay(groupId, expression, null);
	}

	public IntegrationFlowBuilder delay(String groupId, String expression,
			EndpointConfigurer<DelayerEndpointSpec> endpointConfigurer) {
		DelayHandler delayHandler = new DelayHandler(groupId);
		if (StringUtils.hasText(expression)) {
			delayHandler.setDelayExpression(PARSER.parseExpression(expression));
		}
		return this.register(new DelayerEndpointSpec(delayHandler), endpointConfigurer);
	}

	public IntegrationFlowBuilder enrich(ComponentConfigurer<EnricherSpec> enricherConfigurer) {
		return this.enrich(enricherConfigurer, null);
	}

	public IntegrationFlowBuilder enrich(ComponentConfigurer<EnricherSpec> enricherConfigurer,
			EndpointConfigurer<GenericEndpointSpec<ContentEnricher>> endpointConfigurer) {
		Assert.notNull(enricherConfigurer);
		EnricherSpec enricherSpec = new EnricherSpec();
		enricherConfigurer.configure(enricherSpec);
		return this.handle(enricherSpec.get(), endpointConfigurer);
	}

	public IntegrationFlowBuilder enrichHeaders(ComponentConfigurer<HeaderEnricherSpec> headerEnricherConfigurer) {
		return this.enrichHeaders(headerEnricherConfigurer, null);
	}

	public IntegrationFlowBuilder enrichHeaders(ComponentConfigurer<HeaderEnricherSpec> headerEnricherConfigurer,
			EndpointConfigurer<GenericEndpointSpec<MessageTransformingHandler>> endpointConfigurer) {
		Assert.notNull(headerEnricherConfigurer);
		HeaderEnricherSpec headerEnricherSpec = new HeaderEnricherSpec();
		headerEnricherConfigurer.configure(headerEnricherSpec);
		return transform(headerEnricherSpec.get(), endpointConfigurer);
	}

	public IntegrationFlowBuilder split(EndpointConfigurer<SplitterEndpointSpec<DefaultMessageSplitter>> endpointConfigurer) {
		return this.split(new DefaultMessageSplitter(), endpointConfigurer);
	}

	public IntegrationFlowBuilder split(String expression,
			EndpointConfigurer<SplitterEndpointSpec<ExpressionEvaluatingSplitter>> endpointConfigurer) {
		return this.split(new ExpressionEvaluatingSplitter(PARSER.parseExpression(expression)), endpointConfigurer);
	}

	public IntegrationFlowBuilder split(String beanName, String methodName) {
		return this.split(beanName, methodName, null);
	}

	public IntegrationFlowBuilder split(String beanName, String methodName,
			EndpointConfigurer<SplitterEndpointSpec<MethodInvokingSplitter>> endpointConfigurer) {
		return this.split(new MethodInvokingSplitter(new BeanNameMessageProcessor<Collection<?>>(beanName, methodName)),
				endpointConfigurer);
	}

	public <P> IntegrationFlowBuilder split(Class<P> payloadType, GenericSplitter<P> splitter) {
		return this.split(payloadType, splitter, null);
	}

	public <T> IntegrationFlowBuilder split(GenericSplitter<T> splitter,
			EndpointConfigurer<SplitterEndpointSpec<MethodInvokingSplitter>> endpointConfigurer) {
		return split(null, splitter, endpointConfigurer);
	}

	public <P> IntegrationFlowBuilder split(Class<P> payloadType, GenericSplitter<P> splitter,
			EndpointConfigurer<SplitterEndpointSpec<MethodInvokingSplitter>> endpointConfigurer) {
		MethodInvokingSplitter split = isLambda(splitter)
				? new MethodInvokingSplitter(new LambdaMessageProcessor(splitter, payloadType))
				: new MethodInvokingSplitter(splitter, "split");
		return this.split(split, endpointConfigurer);
	}

	public <S extends AbstractMessageSplitter> IntegrationFlowBuilder split(S splitter,
			EndpointConfigurer<SplitterEndpointSpec<S>> endpointConfigurer) {
		Assert.notNull(splitter);
		return this.register(new SplitterEndpointSpec<S>(splitter), endpointConfigurer);
	}

	/**
	 * Provides the {@link HeaderFilter} to the current {@link IntegrationFlow}.
	 * @param headersToRemove the array of headers (or patterns)
	 * to remove from {@link org.springframework.messaging.MessageHeaders}.
	 * @return the {@link IntegrationFlowBuilder}.
	 */
	public IntegrationFlowBuilder headerFilter(String... headersToRemove) {
		return this.headerFilter(new HeaderFilter(headersToRemove), null);
	}

	/**
	 * Provides the {@link HeaderFilter} to the current {@link IntegrationFlow}.
	 * @param headersToRemove the comma separated headers (or patterns) to remove from
	 * {@link org.springframework.messaging.MessageHeaders}.
	 * @param patternMatch    the {@code boolean} flag to indicate if {@code headersToRemove}
	 * should be interpreted as patterns or direct header names.
	 * @return the {@link IntegrationFlowBuilder}.
	 */

	public IntegrationFlowBuilder headerFilter(String headersToRemove, boolean patternMatch) {
		HeaderFilter headerFilter = new HeaderFilter(StringUtils.delimitedListToStringArray(headersToRemove, ",", " "));
		headerFilter.setPatternMatch(patternMatch);
		return this.headerFilter(headerFilter, null);
	}

	public IntegrationFlowBuilder headerFilter(HeaderFilter headerFilter,
			EndpointConfigurer<GenericEndpointSpec<MessageTransformingHandler>> endpointConfigurer) {
		return this.transform(headerFilter, endpointConfigurer);
	}

	public IntegrationFlowBuilder claimCheckIn(MessageStore messageStore) {
		return this.claimCheckIn(messageStore, null);
	}

	public IntegrationFlowBuilder claimCheckIn(MessageStore messageStore,
			EndpointConfigurer<GenericEndpointSpec<MessageTransformingHandler>> endpointConfigurer) {
		return this.transform(new ClaimCheckInTransformer(messageStore), endpointConfigurer);
	}

	public IntegrationFlowBuilder claimCheckOut(MessageStore messageStore) {
		return this.claimCheckOut(messageStore, false);
	}

	public IntegrationFlowBuilder claimCheckOut(MessageStore messageStore, boolean removeMessage) {
		return this.claimCheckOut(messageStore, removeMessage, null);
	}

	public IntegrationFlowBuilder claimCheckOut(MessageStore messageStore, boolean removeMessage,
			EndpointConfigurer<GenericEndpointSpec<MessageTransformingHandler>> endpointConfigurer) {
		ClaimCheckOutTransformer claimCheckOutTransformer = new ClaimCheckOutTransformer(messageStore);
		claimCheckOutTransformer.setRemoveMessage(removeMessage);
		return this.transform(claimCheckOutTransformer, endpointConfigurer);
	}

	public IntegrationFlowBuilder resequence() {
		return this.resequence((EndpointConfigurer<GenericEndpointSpec<ResequencingMessageHandler>>) null);
	}

	public IntegrationFlowBuilder resequence(EndpointConfigurer<GenericEndpointSpec<ResequencingMessageHandler>> endpointConfigurer) {
		return this.handle(new ResequencerSpec().get(), endpointConfigurer);
	}

	public IntegrationFlowBuilder resequence(ComponentConfigurer<ResequencerSpec> resequencerConfigurer,
			EndpointConfigurer<GenericEndpointSpec<ResequencingMessageHandler>> endpointConfigurer) {
		Assert.notNull(resequencerConfigurer);
		ResequencerSpec spec = new ResequencerSpec();
		resequencerConfigurer.configure(spec);
		return this.handle(spec.get(), endpointConfigurer);
	}

	public IntegrationFlowBuilder aggregate() {
		return aggregate((EndpointConfigurer<GenericEndpointSpec<AggregatingMessageHandler>>) null);
	}

	public IntegrationFlowBuilder
	aggregate(EndpointConfigurer<GenericEndpointSpec<AggregatingMessageHandler>> endpointConfigurer) {
		return handle(new AggregatorSpec().outputProcessor(new DefaultAggregatingMessageGroupProcessor()).get(),
				endpointConfigurer);
	}

	public IntegrationFlowBuilder aggregate(ComponentConfigurer<AggregatorSpec> aggregatorConfigurer,
			EndpointConfigurer<GenericEndpointSpec<AggregatingMessageHandler>> endpointConfigurer) {
		Assert.notNull(aggregatorConfigurer);
		AggregatorSpec spec = new AggregatorSpec();
		aggregatorConfigurer.configure(spec);
		return this.handle(spec.get(), endpointConfigurer);
	}

	public IntegrationFlowBuilder route(String beanName, String method) {
		return this.route(beanName, method, null);
	}

	public IntegrationFlowBuilder route(String beanName, String method,
			ComponentConfigurer<RouterSpec<MethodInvokingRouter>> routerConfigurer) {
		return this.route(beanName, method, routerConfigurer, null);
	}

	public IntegrationFlowBuilder route(String beanName, String method,
			ComponentConfigurer<RouterSpec<MethodInvokingRouter>> routerConfigurer,
			EndpointConfigurer<GenericEndpointSpec<MethodInvokingRouter>> endpointConfigurer) {
		return this.route(new MethodInvokingRouter(new BeanNameMessageProcessor<Object>(beanName, method)),
				routerConfigurer, endpointConfigurer);
	}


	public IntegrationFlowBuilder route(String expression) {
		return this.route(expression, (ComponentConfigurer<RouterSpec<ExpressionEvaluatingRouter>>) null);
	}

	public IntegrationFlowBuilder route(String expression,
			ComponentConfigurer<RouterSpec<ExpressionEvaluatingRouter>> routerConfigurer) {
		return this.route(expression, routerConfigurer, null);
	}

	public IntegrationFlowBuilder route(String expression,
			ComponentConfigurer<RouterSpec<ExpressionEvaluatingRouter>> routerConfigurer,
			EndpointConfigurer<GenericEndpointSpec<ExpressionEvaluatingRouter>> endpointConfigurer) {
		return this.route(new ExpressionEvaluatingRouter(PARSER.parseExpression(expression)), routerConfigurer,
				endpointConfigurer);
	}

	public <S, T> IntegrationFlowBuilder route(GenericRouter<S, T> router) {
		return this.route(null, router);
	}

	public <S, T> IntegrationFlowBuilder route(GenericRouter<S, T> router,
			ComponentConfigurer<RouterSpec<MethodInvokingRouter>> routerConfigurer) {
		return this.route(null, router, routerConfigurer);
	}

	public <P, T> IntegrationFlowBuilder route(Class<P> payloadType, GenericRouter<P, T> router) {
		return this.route(payloadType, router, null, null);
	}

	public <P, T> IntegrationFlowBuilder route(Class<P> payloadType, GenericRouter<P, T> router,
			ComponentConfigurer<RouterSpec<MethodInvokingRouter>> routerConfigurer) {
		return this.route(payloadType, router, routerConfigurer, null);
	}

	public <S, T> IntegrationFlowBuilder route(GenericRouter<S, T> router,
			ComponentConfigurer<RouterSpec<MethodInvokingRouter>> routerConfigurer,
			EndpointConfigurer<GenericEndpointSpec<MethodInvokingRouter>> endpointConfigurer) {
		return route(null, router, routerConfigurer, endpointConfigurer);
	}

	public <P, T> IntegrationFlowBuilder route(Class<P> payloadType, GenericRouter<P, T> router,
			ComponentConfigurer<RouterSpec<MethodInvokingRouter>> routerConfigurer,
			EndpointConfigurer<GenericEndpointSpec<MethodInvokingRouter>> endpointConfigurer) {
		MethodInvokingRouter methodInvokingRouter = isLambda(router)
				? new MethodInvokingRouter(new LambdaMessageProcessor(router, payloadType))
				: new MethodInvokingRouter(router);
		return this.route(methodInvokingRouter, routerConfigurer, endpointConfigurer);
	}

	public <R extends AbstractMappingMessageRouter> IntegrationFlowBuilder route(R router,
			ComponentConfigurer<RouterSpec<R>> routerConfigurer,
			EndpointConfigurer<GenericEndpointSpec<R>> endpointConfigurer) {
		if (routerConfigurer != null) {
			RouterSpec<R> routerSpec = new RouterSpec<R>(router);
			routerConfigurer.configure(routerSpec);
		}
		return this.route(router, endpointConfigurer);
	}

	public IntegrationFlowBuilder recipientListRoute(ComponentConfigurer<RecipientListRouterSpec> routerConfigurer) {
		return this.recipientListRoute(routerConfigurer, null);
	}

	public IntegrationFlowBuilder recipientListRoute(ComponentConfigurer<RecipientListRouterSpec> routerConfigurer,
			EndpointConfigurer<GenericEndpointSpec<RecipientListRouter>> endpointConfigurer) {
		Assert.notNull(routerConfigurer);
		RecipientListRouterSpec spec = new RecipientListRouterSpec();
		routerConfigurer.configure(spec);
		DslRecipientListRouter recipientListRouter = (DslRecipientListRouter) spec.get();
		Assert.notEmpty(recipientListRouter.getRecipients(), "recipient list must not be empty");
		return this.route(recipientListRouter, endpointConfigurer);
	}

	public IntegrationFlowBuilder route(AbstractMessageRouter router) {
		return this.route(router, null);
	}

	public <R extends AbstractMessageRouter> IntegrationFlowBuilder route(R router,
			EndpointConfigurer<GenericEndpointSpec<R>> endpointConfigurer) {
		return this.handle(router, endpointConfigurer);
	}

	public IntegrationFlowBuilder gateway(String requestChannel) {
		return gateway(requestChannel, null);
	}

	public IntegrationFlowBuilder gateway(String requestChannel,
			EndpointConfigurer<GatewayEndpointSpec> endpointConfigurer) {
		return register(new GatewayEndpointSpec(requestChannel), endpointConfigurer);
	}

	public IntegrationFlowBuilder gateway(MessageChannel requestChannel) {
		return gateway(requestChannel, null);
	}

	public IntegrationFlowBuilder gateway(MessageChannel requestChannel,
			EndpointConfigurer<GatewayEndpointSpec> endpointConfigurer) {
		return register(new GatewayEndpointSpec(requestChannel), endpointConfigurer);
	}

	private <S extends ConsumerEndpointSpec<S, ?>> IntegrationFlowBuilder register(S endpointSpec,
			EndpointConfigurer<S> endpointConfigurer) {
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
					AbstractReplyProducingMessageHandler messageProducer =
							(AbstractReplyProducingMessageHandler) this.currentComponent;
					if (channelName != null) {
						messageProducer.setOutputChannelName(channelName);
					}
					else {
						messageProducer.setOutputChannel(outputChannel);
					}
				}
				else if (this.currentComponent instanceof SourcePollingChannelAdapterSpec) {
					SourcePollingChannelAdapterFactoryBean pollingChannelAdapterFactoryBean =
							((SourcePollingChannelAdapterSpec) this.currentComponent).get().getT1();
					if (channelName != null) {
						pollingChannelAdapterFactoryBean.setOutputChannelName(channelName);
					}
					else {
						pollingChannelAdapterFactoryBean.setOutputChannel(outputChannel);
					}
				}
				else if (this.currentComponent instanceof AbstractCorrelatingMessageHandler) {
					AbstractCorrelatingMessageHandler messageProducer =
							(AbstractCorrelatingMessageHandler) this.currentComponent;
					if (channelName != null) {
						messageProducer.setOutputChannelName(channelName);
					}
					else {
						messageProducer.setOutputChannel(outputChannel);
					}
				}
				else {
					throw new BeanCreationException("The 'currentComponent' (" + this.currentComponent +
							") is a one-way 'MessageHandler' and it isn't appropriate to configure 'outputChannel'. " +
							"This is the end of the integration flow.");
				}
				this.currentComponent = null;
			}
		}
		return this;
	}

	public IntegrationFlow get() {
		if (this.currentMessageChannel instanceof FixedSubscriberChannelPrototype) {
			throw new BeanCreationException("The 'currentMessageChannel' (" + this.currentMessageChannel +
					") is a prototype for FixedSubscriberChannel which can't be created without MessageHandler " +
					"constructor argument. That means that '.fixedSubscriberChannel()' can't be the last EIP-method " +
					"in the IntegrationFlow definition.");
		}

		if (this.flow.getIntegrationComponents().size() == 1) {
			if (this.currentComponent != null) {
				if (this.currentComponent instanceof SourcePollingChannelAdapterSpec) {
					throw new BeanCreationException("The 'SourcePollingChannelAdapter' (" + this.currentComponent + ") " +
							"must be configured with at least one 'MessageChanel' or 'MessageHandler'.");
				}
			}
			else if (this.currentMessageChannel != null) {
				throw new BeanCreationException("The 'IntegrationFlow' can't consist of only one 'MessageChannel'. " +
						"Add at lest '.bridge()' EIP-method before the end of flow.");
			}
		}
		return this.flow;
	}

	private static boolean isLambda(Object o) {
		Class<?> aClass = o.getClass();
		return aClass.isSynthetic() && !aClass.isAnonymousClass() && !aClass.isLocalClass();
	}

}
