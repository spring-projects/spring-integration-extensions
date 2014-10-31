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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.integration.aggregator.AbstractCorrelatingMessageHandler;
import org.springframework.integration.aggregator.AggregatingMessageHandler;
import org.springframework.integration.aggregator.ResequencingMessageHandler;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.FixedSubscriberChannel;
import org.springframework.integration.config.SourcePollingChannelAdapterFactoryBean;
import org.springframework.integration.core.GenericSelector;
import org.springframework.integration.core.MessageSelector;
import org.springframework.integration.dsl.channel.MessageChannelSpec;
import org.springframework.integration.dsl.core.ComponentsRegistration;
import org.springframework.integration.dsl.core.ConsumerEndpointSpec;
import org.springframework.integration.dsl.core.MessageHandlerSpec;
import org.springframework.integration.dsl.support.BeanNameMessageProcessor;
import org.springframework.integration.dsl.support.Consumer;
import org.springframework.integration.dsl.support.FixedSubscriberChannelPrototype;
import org.springframework.integration.dsl.support.Function;
import org.springframework.integration.dsl.support.FunctionExpression;
import org.springframework.integration.dsl.support.GenericHandler;
import org.springframework.integration.dsl.support.MapBuilder;
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
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * @author Artem Bilan
 * @author Gary Russell
 */
public abstract class IntegrationFlowDefinition<B extends IntegrationFlowDefinition<B>> {

	private final static SpelExpressionParser PARSER = new SpelExpressionParser();

	protected final Set<Object> integrationComponents = new LinkedHashSet<Object>();

	protected MessageChannel currentMessageChannel;

	protected Object currentComponent;

	IntegrationFlowDefinition() {
	}

	B addComponent(Object component) {
		this.integrationComponents.add(component);
		return _this();
	}

	B addComponents(Collection<Object> components) {
		if (components != null) {
			for (Object component : components) {
				this.integrationComponents.add(component);
			}
		}
		return _this();
	}

	B currentComponent(Object component) {
		this.currentComponent = component;
		return _this();
	}

	public B fixedSubscriberChannel() {
		return fixedSubscriberChannel(null);
	}

	public B fixedSubscriberChannel(String messageChannelName) {
		return channel(new FixedSubscriberChannelPrototype(messageChannelName));
	}

	public B channel(String messageChannelName) {
		return channel(new MessageChannelReference(messageChannelName));
	}

	public B channel(Function<Channels, MessageChannelSpec<?, ?>> channels) {
		Assert.notNull(channels);
		return channel(channels.apply(new Channels()));
	}

	public B channel(MessageChannelSpec<?, ?> messageChannelSpec) {
		Assert.notNull(messageChannelSpec);
		return channel(messageChannelSpec.get());
	}

	public B channel(MessageChannel messageChannel) {
		Assert.notNull(messageChannel);
		if (this.currentMessageChannel != null) {
			this.register(new GenericEndpointSpec<BridgeHandler>(new BridgeHandler()), null);
		}
		this.currentMessageChannel = messageChannel;
		return registerOutputChannelIfCan(this.currentMessageChannel);
	}

	public B publishSubscribeChannel(Consumer<PublishSubscribeSpec> publishSubscribeChannelConfigurer) {
		return publishSubscribeChannel(null, publishSubscribeChannelConfigurer);
	}

	public B publishSubscribeChannel(Executor executor,
			Consumer<PublishSubscribeSpec> publishSubscribeChannelConfigurer) {
		Assert.notNull(publishSubscribeChannelConfigurer);
		PublishSubscribeSpec spec = new PublishSubscribeSpec(executor);
		publishSubscribeChannelConfigurer.accept(spec);
		return addComponents(spec.getComponentsToRegister()).channel(spec);
	}

	public B controlBus() {
		return controlBus(null);
	}

	public B controlBus(Consumer<GenericEndpointSpec<ServiceActivatingHandler>> endpointConfigurer) {
		return this.handle(new ServiceActivatingHandler(new ExpressionCommandMessageProcessor(
				new ControlBusMethodFilter())), endpointConfigurer);
	}

	public B transform(String expression) {
		Assert.hasText(expression);
		return this.transform(new ExpressionEvaluatingTransformer(PARSER.parseExpression(expression)));
	}

	public <S, T> B transform(GenericTransformer<S, T> genericTransformer) {
		return this.transform(null, genericTransformer);
	}

	public <P, T> B transform(Class<P> payloadType, GenericTransformer<P, T> genericTransformer) {
		return this.transform(payloadType, genericTransformer, null);
	}

	public <S, T> B transform(GenericTransformer<S, T> genericTransformer,
			Consumer<GenericEndpointSpec<MessageTransformingHandler>> endpointConfigurer) {
		return this.transform(null, genericTransformer, endpointConfigurer);
	}

	public <P, T> B transform(Class<P> payloadType, GenericTransformer<P, T> genericTransformer,
			Consumer<GenericEndpointSpec<MessageTransformingHandler>> endpointConfigurer) {
		Assert.notNull(genericTransformer);
		Transformer transformer = genericTransformer instanceof Transformer ? (Transformer) genericTransformer :
				(isLambda(genericTransformer)
						? new MethodInvokingTransformer(new LambdaMessageProcessor(genericTransformer, payloadType))
						: new MethodInvokingTransformer(genericTransformer));
		return addComponent(transformer)
				.handle(new MessageTransformingHandler(transformer), endpointConfigurer);
	}

	public B filter(String expression) {
		Assert.hasText(expression);
		return this.filter(new ExpressionEvaluatingSelector(PARSER.parseExpression(expression)));
	}

	public <S> B filter(GenericSelector<S> genericSelector) {
		return this.filter(null, genericSelector);
	}

	public <P> B filter(Class<P> payloadType, GenericSelector<P> genericSelector) {
		return this.filter(payloadType, genericSelector, null);
	}

	public <P> B filter(GenericSelector<P> genericSelector, Consumer<FilterEndpointSpec> endpointConfigurer) {
		return filter(null, genericSelector, endpointConfigurer);
	}

	public <P> B filter(Class<P> payloadType, GenericSelector<P> genericSelector,
			Consumer<FilterEndpointSpec> endpointConfigurer) {
		Assert.notNull(genericSelector);
		MessageSelector selector = genericSelector instanceof MessageSelector ? (MessageSelector) genericSelector :
				(isLambda(genericSelector)
						? new MethodInvokingSelector(new LambdaMessageProcessor(genericSelector, payloadType))
						: new MethodInvokingSelector(genericSelector));
		return this.register(new FilterEndpointSpec(new MessageFilter(selector)), endpointConfigurer);
	}

	public <H extends MessageHandler> B handleWithAdapter(Function<Adapters, MessageHandlerSpec<?, H>> adapters) {
		return handleWithAdapter(adapters, null);
	}

	public <H extends MessageHandler> B handleWithAdapter(Function<Adapters, MessageHandlerSpec<?, H>> adapters,
			Consumer<GenericEndpointSpec<H>> endpointConfigurer) {
		return handle(adapters.apply(new Adapters()), endpointConfigurer);
	}

	public B handle(MessageHandlerSpec<?, ? extends MessageHandler> messageHandlerSpec) {
		return handle(messageHandlerSpec, null);
	}

	public B handle(MessageHandler messageHandler) {
		return this.handle(messageHandler, null);
	}

	public B handle(String beanName, String methodName) {
		return this.handle(beanName, methodName, null);
	}

	public B handle(String beanName, String methodName,
			Consumer<GenericEndpointSpec<ServiceActivatingHandler>> endpointConfigurer) {
		return this.handle(new ServiceActivatingHandler(new BeanNameMessageProcessor<Object>(beanName, methodName)),
				endpointConfigurer);
	}

	public <P> B handle(GenericHandler<P> handler) {
		return handle(null, handler);
	}

	public <P> B handle(GenericHandler<P> handler,
			Consumer<GenericEndpointSpec<ServiceActivatingHandler>> endpointConfigurer) {
		return this.handle(null, handler, endpointConfigurer);
	}

	public <P> B handle(Class<P> payloadType, GenericHandler<P> handler) {
		return this.handle(payloadType, handler, null);
	}

	public <P> B handle(Class<P> payloadType, GenericHandler<P> handler,
			Consumer<GenericEndpointSpec<ServiceActivatingHandler>> endpointConfigurer) {
		ServiceActivatingHandler serviceActivatingHandler = null;
		if (isLambda(handler)) {
			serviceActivatingHandler = new ServiceActivatingHandler(new LambdaMessageProcessor(handler, payloadType));
		}
		else {
			serviceActivatingHandler = new ServiceActivatingHandler(handler);
		}
		return this.handle(serviceActivatingHandler, endpointConfigurer);
	}

	public <H extends MessageHandler> B handle(MessageHandlerSpec<?, H> messageHandlerSpec,
			Consumer<GenericEndpointSpec<H>> endpointConfigurer) {
		Assert.notNull(messageHandlerSpec);
		if (messageHandlerSpec instanceof ComponentsRegistration) {
			addComponents(((ComponentsRegistration) messageHandlerSpec).getComponentsToRegister());
		}
		return handle(messageHandlerSpec.get(), endpointConfigurer);
	}

	public <H extends MessageHandler> B handle(H messageHandler, Consumer<GenericEndpointSpec<H>> endpointConfigurer) {
		Assert.notNull(messageHandler);
		return this.register(new GenericEndpointSpec<H>(messageHandler), endpointConfigurer);
	}

	public B bridge(Consumer<GenericEndpointSpec<BridgeHandler>> endpointConfigurer) {
		return this.register(new GenericEndpointSpec<BridgeHandler>(new BridgeHandler()), endpointConfigurer);
	}

	public B delay(String groupId) {
		return this.delay(groupId, (String) null);
	}

	public B delay(String groupId, Consumer<DelayerEndpointSpec> endpointConfigurer) {
		return this.delay(groupId, (String) null, endpointConfigurer);
	}

	public B delay(String groupId, String expression) {
		return this.delay(groupId, expression, null);
	}

	public <P> B delay(String groupId, Function<Message<P>, Object> function) {
		return this.delay(groupId, function, null);
	}

	public <P> B delay(String groupId, Function<Message<P>, Object> function,
			Consumer<DelayerEndpointSpec> endpointConfigurer) {
		Assert.notNull(function);
		return this.delay(groupId, new FunctionExpression<Message<P>>(function), endpointConfigurer);
	}

	public B delay(String groupId, String expression, Consumer<DelayerEndpointSpec> endpointConfigurer) {
		return delay(groupId,
				StringUtils.hasText(expression) ? PARSER.parseExpression(expression) : null,
				endpointConfigurer);
	}

	private B delay(String groupId, Expression expression, Consumer<DelayerEndpointSpec> endpointConfigurer) {
		DelayHandler delayHandler = new DelayHandler(groupId);
		if (expression != null) {
			delayHandler.setDelayExpression(expression);
		}
		return this.register(new DelayerEndpointSpec(delayHandler), endpointConfigurer);
	}

	public B enrich(Consumer<EnricherSpec> enricherConfigurer) {
		return this.enrich(enricherConfigurer, null);
	}

	public B enrich(Consumer<EnricherSpec> enricherConfigurer,
			Consumer<GenericEndpointSpec<ContentEnricher>> endpointConfigurer) {
		Assert.notNull(enricherConfigurer);
		EnricherSpec enricherSpec = new EnricherSpec();
		enricherConfigurer.accept(enricherSpec);
		return this.handle(enricherSpec.get(), endpointConfigurer);
	}

	public B enrichHeaders(MapBuilder<?, String, Object> headers) {
		return enrichHeaders(headers, null);
	}

	public B enrichHeaders(MapBuilder<?, String, Object> headers,
			Consumer<GenericEndpointSpec<MessageTransformingHandler>> endpointConfigurer) {
		return enrichHeaders(headers.get(), endpointConfigurer);
	}

	/**
	 * Accept a {@link Map} of values to be used for the
	 * {@link org.springframework.messaging.Message} header enrichment.
	 * {@code values} can apply an {@link org.springframework.expression.Expression}
	 * to be evaluated against a request {@link org.springframework.messaging.Message}.
	 * @param headers the Map of headers to enrich.
	 * @return this.
	 */
	public B enrichHeaders(Map<String, Object> headers) {
		return enrichHeaders(headers, null);
	}

	public B enrichHeaders(final Map<String, Object> headers,
			Consumer<GenericEndpointSpec<MessageTransformingHandler>> endpointConfigurer) {
		return enrichHeaders(new Consumer<HeaderEnricherSpec>() {

			@Override
			public void accept(HeaderEnricherSpec spec) {
				spec.headers(headers);
			}

		}, endpointConfigurer);
	}

	public B enrichHeaders(Consumer<HeaderEnricherSpec> headerEnricherConfigurer) {
		return this.enrichHeaders(headerEnricherConfigurer, null);
	}

	public B enrichHeaders(Consumer<HeaderEnricherSpec> headerEnricherConfigurer,
			Consumer<GenericEndpointSpec<MessageTransformingHandler>> endpointConfigurer) {
		Assert.notNull(headerEnricherConfigurer);
		HeaderEnricherSpec headerEnricherSpec = new HeaderEnricherSpec();
		headerEnricherConfigurer.accept(headerEnricherSpec);
		return transform(headerEnricherSpec.get(), endpointConfigurer);
	}

	public B split() {
		return this.split((Consumer<SplitterEndpointSpec<DefaultMessageSplitter>>) null);
	}

	public B split(Consumer<SplitterEndpointSpec<DefaultMessageSplitter>> endpointConfigurer) {
		return this.split(new DefaultMessageSplitter(), endpointConfigurer);
	}

	public B split(String expression,
			Consumer<SplitterEndpointSpec<ExpressionEvaluatingSplitter>> endpointConfigurer) {
		return this.split(new ExpressionEvaluatingSplitter(PARSER.parseExpression(expression)), endpointConfigurer);
	}

	public B split(String beanName, String methodName) {
		return this.split(beanName, methodName, null);
	}

	public B split(String beanName, String methodName,
			Consumer<SplitterEndpointSpec<MethodInvokingSplitter>> endpointConfigurer) {
		return this.split(new MethodInvokingSplitter(new BeanNameMessageProcessor<Object>(beanName, methodName)),
				endpointConfigurer);
	}

	public <P> B split(Class<P> payloadType, Function<P, ?> splitter) {
		return split(payloadType, splitter, null);
	}

	public <P> B split(Function<P, ?> splitter,
			Consumer<SplitterEndpointSpec<MethodInvokingSplitter>> endpointConfigurer) {
		return split(null, splitter, endpointConfigurer);
	}

	public <P> B split(Class<P> payloadType, Function<P, ?> splitter,
			Consumer<SplitterEndpointSpec<MethodInvokingSplitter>> endpointConfigurer) {
		MethodInvokingSplitter split = isLambda(splitter)
				? new MethodInvokingSplitter(new LambdaMessageProcessor(splitter, payloadType))
				: new MethodInvokingSplitter(splitter);
		return this.split(split, endpointConfigurer);
	}

	public <S extends AbstractMessageSplitter> B split(S splitter,
			Consumer<SplitterEndpointSpec<S>> endpointConfigurer) {
		Assert.notNull(splitter);
		return this.register(new SplitterEndpointSpec<S>(splitter), endpointConfigurer);
	}

	/**
	 * Provides the {@link HeaderFilter} to the current {@link StandardIntegrationFlow}.
	 * @param headersToRemove the array of headers (or patterns)
	 * to remove from {@link org.springframework.messaging.MessageHeaders}.
	 * @return this {@link IntegrationFlowDefinition}.
	 */
	public B headerFilter(String... headersToRemove) {
		return this.headerFilter(new HeaderFilter(headersToRemove), null);
	}

	/**
	 * Provides the {@link HeaderFilter} to the current {@link StandardIntegrationFlow}.
	 * @param headersToRemove the comma separated headers (or patterns) to remove from
	 * {@link org.springframework.messaging.MessageHeaders}.
	 * @param patternMatch    the {@code boolean} flag to indicate if {@code headersToRemove}
	 * should be interpreted as patterns or direct header names.
	 * @return this {@link IntegrationFlowDefinition}.
	 */
	public B headerFilter(String headersToRemove, boolean patternMatch) {
		HeaderFilter headerFilter = new HeaderFilter(StringUtils.delimitedListToStringArray(headersToRemove, ",", " "));
		headerFilter.setPatternMatch(patternMatch);
		return this.headerFilter(headerFilter, null);
	}

	public B headerFilter(HeaderFilter headerFilter,
			Consumer<GenericEndpointSpec<MessageTransformingHandler>> endpointConfigurer) {
		return this.transform(headerFilter, endpointConfigurer);
	}

	public B claimCheckIn(MessageStore messageStore) {
		return this.claimCheckIn(messageStore, null);
	}

	public B claimCheckIn(MessageStore messageStore,
			Consumer<GenericEndpointSpec<MessageTransformingHandler>> endpointConfigurer) {
		return this.transform(new ClaimCheckInTransformer(messageStore), endpointConfigurer);
	}

	public B claimCheckOut(MessageStore messageStore) {
		return this.claimCheckOut(messageStore, false);
	}

	public B claimCheckOut(MessageStore messageStore, boolean removeMessage) {
		return this.claimCheckOut(messageStore, removeMessage, null);
	}

	public B claimCheckOut(MessageStore messageStore, boolean removeMessage,
			Consumer<GenericEndpointSpec<MessageTransformingHandler>> endpointConfigurer) {
		ClaimCheckOutTransformer claimCheckOutTransformer = new ClaimCheckOutTransformer(messageStore);
		claimCheckOutTransformer.setRemoveMessage(removeMessage);
		return this.transform(claimCheckOutTransformer, endpointConfigurer);
	}

	public B resequence() {
		return this.resequence((Consumer<GenericEndpointSpec<ResequencingMessageHandler>>) null);
	}

	public B resequence(Consumer<GenericEndpointSpec<ResequencingMessageHandler>> endpointConfigurer) {
		return this.handle(new ResequencerSpec().get(), endpointConfigurer);
	}

	public B resequence(Consumer<ResequencerSpec> resequencerConfigurer,
			Consumer<GenericEndpointSpec<ResequencingMessageHandler>> endpointConfigurer) {
		Assert.notNull(resequencerConfigurer);
		ResequencerSpec spec = new ResequencerSpec();
		resequencerConfigurer.accept(spec);
		return this.handle(spec.get(), endpointConfigurer);
	}

	public B aggregate() {
		return aggregate((Consumer<GenericEndpointSpec<AggregatingMessageHandler>>) null);
	}

	public B
	aggregate(Consumer<GenericEndpointSpec<AggregatingMessageHandler>> endpointConfigurer) {
		return handle(new AggregatorSpec().get(), endpointConfigurer);
	}

	public B aggregate(Consumer<AggregatorSpec> aggregatorConfigurer,
			Consumer<GenericEndpointSpec<AggregatingMessageHandler>> endpointConfigurer) {
		Assert.notNull(aggregatorConfigurer);
		AggregatorSpec spec = new AggregatorSpec();
		aggregatorConfigurer.accept(spec);
		return this.handle(spec.get(), endpointConfigurer);
	}

	public B route(String beanName, String method) {
		return this.route(beanName, method, null);
	}

	public B route(String beanName, String method, Consumer<RouterSpec<MethodInvokingRouter>> routerConfigurer) {
		return this.route(beanName, method, routerConfigurer, null);
	}

	public B route(String beanName, String method, Consumer<RouterSpec<MethodInvokingRouter>> routerConfigurer,
			Consumer<GenericEndpointSpec<MethodInvokingRouter>> endpointConfigurer) {
		return this.route(new MethodInvokingRouter(new BeanNameMessageProcessor<Object>(beanName, method)),
				routerConfigurer, endpointConfigurer);
	}

	public B route(String expression) {
		return this.route(expression, (Consumer<RouterSpec<ExpressionEvaluatingRouter>>) null);
	}

	public B route(String expression, Consumer<RouterSpec<ExpressionEvaluatingRouter>> routerConfigurer) {
		return this.route(expression, routerConfigurer, null);
	}

	public B route(String expression, Consumer<RouterSpec<ExpressionEvaluatingRouter>> routerConfigurer,
			Consumer<GenericEndpointSpec<ExpressionEvaluatingRouter>> endpointConfigurer) {
		return this.route(new ExpressionEvaluatingRouter(PARSER.parseExpression(expression)), routerConfigurer,
				endpointConfigurer);
	}

	public <S, T> B route(Function<S, T> router) {
		return this.route(null, router);
	}

	public <S, T> B route(Function<S, T> router, Consumer<RouterSpec<MethodInvokingRouter>> routerConfigurer) {
		return this.route(null, router, routerConfigurer);
	}

	public <P, T> B route(Class<P> payloadType, Function<P, T> router) {
		return this.route(payloadType, router, null, null);
	}

	public <P, T> B route(Class<P> payloadType, Function<P, T> router,
			Consumer<RouterSpec<MethodInvokingRouter>> routerConfigurer) {
		return this.route(payloadType, router, routerConfigurer, null);
	}

	public <S, T> B route(Function<S, T> router, Consumer<RouterSpec<MethodInvokingRouter>> routerConfigurer,
			Consumer<GenericEndpointSpec<MethodInvokingRouter>> endpointConfigurer) {
		return route(null, router, routerConfigurer, endpointConfigurer);
	}

	public <P, T> B route(Class<P> payloadType, Function<P, T> router,
			Consumer<RouterSpec<MethodInvokingRouter>> routerConfigurer,
			Consumer<GenericEndpointSpec<MethodInvokingRouter>> endpointConfigurer) {
		MethodInvokingRouter methodInvokingRouter = isLambda(router)
				? new MethodInvokingRouter(new LambdaMessageProcessor(router, payloadType))
				: new MethodInvokingRouter(router);
		return route(methodInvokingRouter, routerConfigurer, endpointConfigurer);
	}

	public <R extends AbstractMappingMessageRouter> B route(R router, Consumer<RouterSpec<R>> routerConfigurer,
			Consumer<GenericEndpointSpec<R>> endpointConfigurer) {
		Collection<Object> componentsToRegister = null;
		if (routerConfigurer != null) {
			RouterSpec<R> routerSpec = new RouterSpec<R>(router);
			routerConfigurer.accept(routerSpec);
			componentsToRegister = routerSpec.getComponentsToRegister();
		}

		route(router, endpointConfigurer);

		final MessageChannel afterRouterChannel = new DirectChannel();
		boolean hasSubFlows = false;
		if (!CollectionUtils.isEmpty(componentsToRegister)) {
			for (Object component : componentsToRegister) {
				if (component instanceof IntegrationFlowDefinition) {
					hasSubFlows = true;
					IntegrationFlowDefinition<?> flowBuilder = (IntegrationFlowDefinition<?>) component;
					addComponent(flowBuilder.fixedSubscriberChannel()
							.bridge(new Consumer<GenericEndpointSpec<BridgeHandler>>() {

								@Override
								public void accept(GenericEndpointSpec<BridgeHandler> bridge) {
									bridge.get().getT2().setOutputChannel(afterRouterChannel);
								}

							})
							.get());
				}
				else {
					addComponent(component);
				}
			}
		}
		if (hasSubFlows) {
			channel(afterRouterChannel);
		}
		return _this();
	}

	public B routeToRecipients(Consumer<RecipientListRouterSpec> routerConfigurer) {
		return routeToRecipients(routerConfigurer, null);
	}

	public B routeToRecipients(Consumer<RecipientListRouterSpec> routerConfigurer,
			Consumer<GenericEndpointSpec<RecipientListRouter>> endpointConfigurer) {
		Assert.notNull(routerConfigurer);
		RecipientListRouterSpec spec = new RecipientListRouterSpec();
		routerConfigurer.accept(spec);
		addComponents(spec.getComponentsToRegister());
		return route(spec.get(), endpointConfigurer);
	}

	public B route(AbstractMessageRouter router) {
		return route(router, null);
	}

	public <R extends AbstractMessageRouter> B route(R router, Consumer<GenericEndpointSpec<R>> endpointConfigurer) {
		return handle(router, endpointConfigurer);
	}

	public B gateway(String requestChannel) {
		return gateway(requestChannel, null);
	}

	public B gateway(String requestChannel, Consumer<GatewayEndpointSpec> endpointConfigurer) {
		return register(new GatewayEndpointSpec(requestChannel), endpointConfigurer);
	}

	public B gateway(MessageChannel requestChannel) {
		return gateway(requestChannel, null);
	}

	public B gateway(MessageChannel requestChannel, Consumer<GatewayEndpointSpec> endpointConfigurer) {
		return register(new GatewayEndpointSpec(requestChannel), endpointConfigurer);
	}

	public B gateway(IntegrationFlow flow) {
		return gateway(flow, null);
	}

	public B gateway(IntegrationFlow flow, Consumer<GatewayEndpointSpec> endpointConfigurer) {
		Assert.notNull(flow);
		final DirectChannel requestChannel = new DirectChannel();
		IntegrationFlowBuilder flowBuilder = IntegrationFlows.from(requestChannel);
		flow.accept(flowBuilder);
		addComponent(flowBuilder.get());
		return gateway(requestChannel, endpointConfigurer);
	}

	private <S extends ConsumerEndpointSpec<S, ?>> B register(S endpointSpec, Consumer<S> endpointConfigurer) {
		if (endpointConfigurer != null) {
			endpointConfigurer.accept(endpointSpec);
		}
		if (endpointSpec instanceof ComponentsRegistration) {
			addComponents(((ComponentsRegistration) endpointSpec).getComponentsToRegister());
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

	private B registerOutputChannelIfCan(MessageChannel outputChannel) {
		if (!(outputChannel instanceof FixedSubscriberChannelPrototype)) {
			this.integrationComponents.add(outputChannel);
			if (this.currentComponent != null) {
				String channelName = null;
				if (outputChannel instanceof MessageChannelReference) {
					channelName = ((MessageChannelReference) outputChannel).getName();
				}

				Object currentComponent = this.currentComponent;

				if (AopUtils.isAopProxy(currentComponent)) {
					currentComponent = extractProxyTarget(currentComponent);
				}

				if (currentComponent instanceof AbstractReplyProducingMessageHandler) {
					AbstractReplyProducingMessageHandler messageProducer =
							(AbstractReplyProducingMessageHandler) currentComponent;
					if (channelName != null) {
						messageProducer.setOutputChannelName(channelName);
					}
					else {
						messageProducer.setOutputChannel(outputChannel);
					}
				}
				else if (currentComponent instanceof SourcePollingChannelAdapterSpec) {
					SourcePollingChannelAdapterFactoryBean pollingChannelAdapterFactoryBean =
							((SourcePollingChannelAdapterSpec) currentComponent).get().getT1();
					if (channelName != null) {
						pollingChannelAdapterFactoryBean.setOutputChannelName(channelName);
					}
					else {
						pollingChannelAdapterFactoryBean.setOutputChannel(outputChannel);
					}
				}
				else if (currentComponent instanceof AbstractCorrelatingMessageHandler) {
					AbstractCorrelatingMessageHandler messageProducer =
							(AbstractCorrelatingMessageHandler) currentComponent;
					if (channelName != null) {
						messageProducer.setOutputChannelName(channelName);
					}
					else {
						messageProducer.setOutputChannel(outputChannel);
					}
				}
				else if (this.currentComponent instanceof AbstractMessageRouter) {
					AbstractMessageRouter router = (AbstractMessageRouter) this.currentComponent;
					if (channelName != null) {
						router.setDefaultOutputChannelName(channelName);
					}
					else {
						router.setDefaultOutputChannel(outputChannel);
					}
				}
				else {
					throw new BeanCreationException("The 'currentComponent' (" + currentComponent +
							") is a one-way 'MessageHandler' and it isn't appropriate to configure 'outputChannel'. " +
							"This is the end of the integration flow.");
				}
				this.currentComponent = null;
			}
		}
		return _this();
	}

	@SuppressWarnings("unchecked")
	protected final B _this() {
		return (B) this;
	}

	private static boolean isLambda(Object o) {
		Class<?> aClass = o.getClass();
		return aClass.isSynthetic() && !aClass.isAnonymousClass() && !aClass.isLocalClass();
	}

	private static Object extractProxyTarget(Object target) {
		if (!(target instanceof Advised)) {
			return target;
		}
		Advised advised = (Advised) target;
		if (advised.getTargetSource() == null) {
			return null;
		}
		try {
			return extractProxyTarget(advised.getTargetSource().getTarget());
		}
		catch (Exception e) {
			throw new BeanCreationException("Could not extract target", e);
		}
	}

	protected StandardIntegrationFlow get() {
		if (this.currentMessageChannel instanceof FixedSubscriberChannelPrototype) {
			throw new BeanCreationException("The 'currentMessageChannel' (" + this.currentMessageChannel +
					") is a prototype for FixedSubscriberChannel which can't be created without MessageHandler " +
					"constructor argument. That means that '.fixedSubscriberChannel()' can't be the last EIP-method " +
					"in the IntegrationFlow definition.");
		}

		if (this.integrationComponents.size() == 1) {
			if (this.currentComponent != null) {
				if (this.currentComponent instanceof SourcePollingChannelAdapterSpec) {
					throw new BeanCreationException("The 'SourcePollingChannelAdapter' (" + this.currentComponent
							+ ") " + "must be configured with at least one 'MessageChanel' or 'MessageHandler'.");
				}
			}
			else if (this.currentMessageChannel != null) {
				throw new BeanCreationException("The 'IntegrationFlow' can't consist of only one 'MessageChannel'. " +
						"Add at lest '.bridge()' EIP-method before the end of flow.");
			}
		}
		return new StandardIntegrationFlow(this.integrationComponents);
	}

}
