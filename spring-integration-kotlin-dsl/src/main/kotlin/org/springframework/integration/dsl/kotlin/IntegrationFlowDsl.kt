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

import org.springframework.integration.core.MessageSource
import org.springframework.integration.dsl.GatewayProxySpec
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.IntegrationFlowBuilder
import org.springframework.integration.dsl.IntegrationFlowDefinition
import org.springframework.integration.dsl.IntegrationFlows
import org.springframework.integration.dsl.SourcePollingChannelAdapterSpec
import java.util.function.Consumer

/**
 * Functional [IntegrationFlow] definition in Kotlin DSL for
 * [IntegrationFlows.from(Class<*>, Consumer<GatewayProxySpec>)]
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
 * Functional [IntegrationFlow] definition in Kotlin DSL for
 * [IntegrationFlows.from(String, Boolean)]
 *
 * @author Artem Bilan
 */
fun integrationFlow(channelName: String, fixedSubscriber: Boolean = false,
					flow: (IntegrationFlowDefinition<*>) -> Unit): IntegrationFlow {

	val flowBuilder = IntegrationFlows.from(channelName, fixedSubscriber)
	return buildIntegrationFlow(flowBuilder, flow)
}

/**
 * Functional [IntegrationFlow] definition in Kotlin DSL for
 * [IntegrationFlows.from(MessageSource<*>, Consumer<SourcePollingChannelAdapterSpec>)]
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
 * Functional [IntegrationFlow] definition in Kotlin DSL for
 * [IntegrationFlows.from(Supplier<*>, Consumer<SourcePollingChannelAdapterSpec>)]
 *
 * @author Artem Bilan
 */
fun integrationFlow(source: () -> Any,
						options: (SourcePollingChannelAdapterSpec) -> Unit = {},
						flow: (IntegrationFlowDefinition<*>) -> Unit): IntegrationFlow {

	val flowBuilder = IntegrationFlows.from(source, options)
	return buildIntegrationFlow(flowBuilder, flow)
}

private fun buildIntegrationFlow(flowBuilder: IntegrationFlowBuilder,
								 flow: (IntegrationFlowDefinition<*>) -> Unit): IntegrationFlow {

	flow.invoke(flowBuilder)
	return flowBuilder.get()
}
