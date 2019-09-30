/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.integration.dsl.kotlin

import org.reactivestreams.Publisher
import org.springframework.integration.core.MessageSource
import org.springframework.integration.dsl.GatewayProxySpec
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.IntegrationFlowBuilder
import org.springframework.integration.dsl.IntegrationFlowDefinition
import org.springframework.integration.dsl.IntegrationFlows
import org.springframework.integration.dsl.MessageProducerSpec
import org.springframework.integration.dsl.MessageSourceSpec
import org.springframework.integration.dsl.MessagingGatewaySpec
import org.springframework.integration.dsl.SourcePollingChannelAdapterSpec
import org.springframework.integration.endpoint.MessageProducerSupport
import org.springframework.integration.gateway.MessagingGatewaySupport
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import java.util.function.Consumer

private fun buildIntegrationFlow(flowBuilder: IntegrationFlowBuilder,
								 flow: (IntegrationFlowDefinition<*>) -> Unit): IntegrationFlow {

	flow.invoke(flowBuilder)
	return flowBuilder.get()
}

/**
 * Functional [IntegrationFlow] definition in Kotlin DSL for [IntegrationFlows.from] -
 * `IntegrationFlows.from(Class<?>, Consumer<GatewayProxySpec>)` factory method.
 *
 * @author Artem Bilan
 */
inline fun <reified T> integrationFlow(crossinline gateway: (GatewayProxySpec) -> Unit = {},
									   crossinline flow: (IntegrationFlowDefinition<*>) -> Unit): IntegrationFlow {

	val flowBuilder = IntegrationFlows.from(T::class.java) { gateway(it) }
	flow.invoke(flowBuilder)
	return flowBuilder.get()
}

/**
 * Functional [IntegrationFlow] definition in Kotlin DSL for [IntegrationFlows.from] -
 * `IntegrationFlows.from(String, Boolean)` factory method.
 *
 * @author Artem Bilan
 */
fun integrationFlow(channelName: String, fixedSubscriber: Boolean = false,
					flow: (IntegrationFlowDefinition<*>) -> Unit): IntegrationFlow {

	val flowBuilder = IntegrationFlows.from(channelName, fixedSubscriber)
	return buildIntegrationFlow(flowBuilder, flow)
}

/**
 * Functional [IntegrationFlow] definition in Kotlin DSL for [IntegrationFlows.from] -
 * `IntegrationFlows.from(MessageChannel)` factory method.
 *
 * @author Artem Bilan
 */
fun integrationFlow(channel: MessageChannel, flow: (IntegrationFlowDefinition<*>) -> Unit): IntegrationFlow {
	val flowBuilder = IntegrationFlows.from(channel)
	return buildIntegrationFlow(flowBuilder, flow)
}

/**
 * Functional [IntegrationFlow] definition in Kotlin DSL for [IntegrationFlows.from]  -
 * `IntegrationFlows.from(MessageSource<*>, Consumer<SourcePollingChannelAdapterSpec>)` factory method.
 *
 * @author Artem Bilan
 */
fun integrationFlow(messageSource: MessageSource<*>,
					options: (SourcePollingChannelAdapterSpec) -> Unit = {},
					flow: (IntegrationFlowDefinition<*>) -> Unit): IntegrationFlow {

	val flowBuilder = IntegrationFlows.from(messageSource, Consumer { options(it) })
	return buildIntegrationFlow(flowBuilder, flow)
}

/**
 * Functional [IntegrationFlow] definition in Kotlin DSL for [IntegrationFlows.from]  -
 * `IntegrationFlows.from(MessageSourceSpec<*>, Consumer<SourcePollingChannelAdapterSpec>)` factory method.
 *
 * @author Artem Bilan
 */
fun integrationFlow(messageSource: MessageSourceSpec<*, out MessageSource<*>>,
					options: (SourcePollingChannelAdapterSpec) -> Unit = {},
					flow: (IntegrationFlowDefinition<*>) -> Unit): IntegrationFlow {

	val flowBuilder = IntegrationFlows.from(messageSource, options)
	return buildIntegrationFlow(flowBuilder, flow)
}

/**
 * Functional [IntegrationFlow] definition in Kotlin DSL for [IntegrationFlows.from] -
 * `IntegrationFlows.from(Supplier<*>, Consumer<SourcePollingChannelAdapterSpec>)` factory method.
 *
 * @author Artem Bilan
 */
fun integrationFlow(source: () -> Any,
					options: (SourcePollingChannelAdapterSpec) -> Unit = {},
					flow: (IntegrationFlowDefinition<*>) -> Unit): IntegrationFlow {

	val flowBuilder = IntegrationFlows.from(source, options)
	return buildIntegrationFlow(flowBuilder, flow)
}

/**
 * Functional [IntegrationFlow] definition in Kotlin DSL for [IntegrationFlows.from] -
 * `IntegrationFlows.from(Publisher<out Message<*>>)` factory method.
 *
 * @author Artem Bilan
 */
fun integrationFlow(publisher: Publisher<out Message<*>>,
					flow: (IntegrationFlowDefinition<*>) -> Unit): IntegrationFlow {

	val flowBuilder = IntegrationFlows.from(publisher)
	return buildIntegrationFlow(flowBuilder, flow)
}

/**
 * Functional [IntegrationFlow] definition in Kotlin DSL for [IntegrationFlows.from] -
 * `IntegrationFlows.from(MessagingGatewaySupport)` factory method.
 *
 * @author Artem Bilan
 */
fun integrationFlow(gateway: MessagingGatewaySupport,
					flow: (IntegrationFlowDefinition<*>) -> Unit): IntegrationFlow {

	val flowBuilder = IntegrationFlows.from(gateway)
	return buildIntegrationFlow(flowBuilder, flow)
}

/**
 * Functional [IntegrationFlow] definition in Kotlin DSL for [IntegrationFlows.from] -
 * `IntegrationFlows.from(MessagingGatewaySpec<*, *>)` factory method.
 *
 * @author Artem Bilan
 */
fun integrationFlow(gatewaySpec: MessagingGatewaySpec<*, *>,
					flow: (IntegrationFlowDefinition<*>) -> Unit): IntegrationFlow {

	val flowBuilder = IntegrationFlows.from(gatewaySpec)
	return buildIntegrationFlow(flowBuilder, flow)
}

/**
 * Functional [IntegrationFlow] definition in Kotlin DSL for [IntegrationFlows.from] -
 * `IntegrationFlows.from(MessageProducerSupport)` factory method.
 *
 * @author Artem Bilan
 */
fun integrationFlow(producer: MessageProducerSupport,
					flow: (IntegrationFlowDefinition<*>) -> Unit): IntegrationFlow {

	val flowBuilder = IntegrationFlows.from(producer)
	return buildIntegrationFlow(flowBuilder, flow)
}

/**
 * Functional [IntegrationFlow] definition in Kotlin DSL for [IntegrationFlows.from] -
 * `IntegrationFlows.from(MessageProducerSpec<*, *>)` factory method.
 *
 * @author Artem Bilan
 */
fun integrationFlow(producerSpec: MessageProducerSpec<*, *>,
					flow: (IntegrationFlowDefinition<*>) -> Unit): IntegrationFlow {

	val flowBuilder = IntegrationFlows.from(producerSpec)
	return buildIntegrationFlow(flowBuilder, flow)
}
