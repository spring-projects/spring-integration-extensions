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

package org.springframework.integration.dsl

import org.springframework.integration.router.MethodInvokingRouter
import org.springframework.integration.splitter.MethodInvokingSplitter
import org.springframework.integration.transformer.MessageTransformingHandler
import org.springframework.messaging.MessageChannel

/**
 * A [BaseIntegrationFlowDefinition] extension for Kotlin-specif inline functions with reified
 * generic types.
 *
 * @property adaptee the [IntegrationFlowDefinition] this instance is adapted.
 *
 * @author Artem Bilan
 */
class KotlinIntegrationFlowDefinition(private val adaptee: IntegrationFlowDefinition<*>) :
		BaseIntegrationFlowDefinition<KotlinIntegrationFlowDefinition>() {

	/**
	 * Delegate a provided component into an `adaptee` set of components.
	 */
	override fun addComponent(component: Any): KotlinIntegrationFlowDefinition {
		return addComponent(component, null)
	}

	/**
	 * Delegate a provided component into an `adaptee` set of components.
	 */
	override fun addComponent(component: Any, beanName: String?): KotlinIntegrationFlowDefinition {
		this.adaptee.addComponent(component, beanName)
		return _this()
	}

	/**
	 * Delegate provided components into an `adaptee` set of components.
	 */
	override fun addComponents(components: Map<Any, String>?): KotlinIntegrationFlowDefinition {
		this.adaptee.addComponents(components)
		return _this()
	}

	/**
	 * Get a [Map] of components from `adaptee`.
	 */
	override fun getIntegrationComponents(): Map<Any, String> {
		return this.adaptee.getIntegrationComponents()
	}

	/**
	 * Set a provided [MessageChannel] as a current in the `adaptee`.
	 */
	override fun currentMessageChannel(currentMessageChannel: MessageChannel?): KotlinIntegrationFlowDefinition {
		this.adaptee.currentMessageChannel(currentMessageChannel)
		return _this()
	}

	/**
	 * Get a current [MessageChannel] from the `adaptee`.
	 */
	override fun getCurrentMessageChannel(): MessageChannel? {
		return this.adaptee.getCurrentMessageChannel()
	}

	/**
	 * Delegate a provided component into an `adaptee` current component.
	 */
	override fun currentComponent(component: Any?): KotlinIntegrationFlowDefinition {
		this.adaptee.currentComponent(component)
		return _this()
	}

	/**
	 * Get a current component from `adaptee`.
	 */
	override fun getCurrentComponent(): Any? {
		return this.adaptee.getCurrentComponent()
	}

	/**
	 * Set a flag for an implicit channel on the `adaptee`.
	 */
	override fun setImplicitChannel(implicitChannel: Boolean) {
		this.adaptee.setImplicitChannel(implicitChannel)
	}

	/**
	 * Get an implicit channel flag from the `adaptee`.
	 */
	override fun isImplicitChannel(): Boolean {
		return this.adaptee.isImplicitChannel()
	}

	/**
	 * Inline function for [IntegrationFlowDefinition.convert] providing a `convert<MyType>()` variant
	 * with reified generic type.
	 */
	inline fun <reified T> convert(
			crossinline configurer: (GenericEndpointSpec<MessageTransformingHandler>) -> Unit = {}) {

		convert(T::class.java) { configurer(it) }
	}

	/**
	 * Inline function for [IntegrationFlowDefinition.transform] providing a `transform<MyTypeIn, MyTypeOut>()` variant
	 * with reified generic type.
	 */
	inline fun <reified P, T> transform(crossinline function: (P) -> T) {
		transform(P::class.java) { function(it) }
	}

	/**
	 * Inline function for [IntegrationFlowDefinition.transform] providing a `transform<MyTypeIn, MyTypeOut>()` variant
	 * with reified generic type.
	 */
	inline fun <reified P, T> transform(
			crossinline function: (P) -> T,
			crossinline configurer: (GenericEndpointSpec<MessageTransformingHandler>) -> Unit) {

		transform(P::class.java, { function(it) }) { configurer(it) }
	}

	/**
	 * Inline function for [IntegrationFlowDefinition.split] providing a `split<MyTypeIn>()` variant
	 * with reified generic type.
	 */
	inline fun <reified P> split(crossinline function: (P) -> Any) {
		split(P::class.java) { function(it) }
	}


	/**
	 * Inline function for [IntegrationFlowDefinition.split] providing a `split<MyTypeIn>()` variant
	 * with reified generic type.
	 */
	inline fun <reified P> split(
			crossinline function: (P) -> Any,
			crossinline configurer: (SplitterEndpointSpec<MethodInvokingSplitter>) -> Unit) {

		split(P::class.java, { function(it) }) { configurer(it) }
	}

	/**
	 * Inline function for [IntegrationFlowDefinition.filter] providing a `filter<MyTypeIn>()` variant
	 * with reified generic type.
	 */
	inline fun <reified P> filter(crossinline function: (P) -> Boolean) {
		filter(P::class.java) { function(it) }
	}

	/**
	 * Inline function for [IntegrationFlowDefinition.filter] providing a `filter<MyTypeIn>()` variant
	 * with reified generic type.
	 */
	inline fun <reified P> filter(
			crossinline function: (P) -> Boolean,
			crossinline configurer: (FilterEndpointSpec) -> Unit) {

		filter(P::class.java, { function(it) }) { configurer(it) }
	}


	/**
	 * Inline function for [IntegrationFlowDefinition.filter] providing a `filter<MyTypeIn>()` variant
	 * with reified generic type.
	 */
	inline fun <reified P, T> route(crossinline function: (P) -> T) {
		route(P::class.java) { function(it) }
	}

	/**
	 * Inline function for [IntegrationFlowDefinition.filter] providing a `filter<MyTypeIn>()` variant
	 * with reified generic type.
	 */
	inline fun <reified P, T> route(
			crossinline function: (P) -> T,
			crossinline configurer: (RouterSpec<T, MethodInvokingRouter>) -> Unit) {

		route(P::class.java, { function(it) }) { configurer(it) }
	}

}
