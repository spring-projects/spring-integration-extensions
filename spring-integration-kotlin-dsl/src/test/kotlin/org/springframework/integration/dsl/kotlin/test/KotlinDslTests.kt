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

package org.springframework.integration.dsl.kotlin.test

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.channel.QueueChannel
import org.springframework.integration.config.EnableIntegration
import org.springframework.integration.dsl.kotlin.convert
import org.springframework.integration.dsl.kotlin.integrationFlow
import org.springframework.integration.endpoint.MessageProcessorMessageSource
import org.springframework.integration.support.MessageBuilder
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.PollableChannel
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import java.util.*
import java.util.function.Function

/**
 * @author Artem Bilan
 */
@SpringJUnitConfig
@DirtiesContext
class KotlinDslTests {

	@Autowired
	private lateinit var beanFactory: BeanFactory

	@Autowired
	private lateinit var convertFlowInput: MessageChannel

	@Test
	fun `test convert extension`() {
		assertThat(this.beanFactory.containsBean("kotlinConverter"))

		val replyChannel = QueueChannel()
		val date = Date()
		val testMessage =
				MessageBuilder.withPayload("{\"name\": \"Test\",\"date\": " + date.time + "}")
						.setHeader(MessageHeaders.CONTENT_TYPE, "application/json")
						.setReplyChannel(replyChannel)
						.build()
		this.convertFlowInput.send(testMessage)

		assertThat(replyChannel.receive(10000)?.payload)
				.isNotNull()
				.isInstanceOf(TestPojo::class.java)
				.isEqualTo(TestPojo("Test", date))
	}

	@Autowired
	private lateinit var upperCaseFunction: Function<String, String>

	@Test
	fun `test uppercase function`() {
		assertThat(this.upperCaseFunction.apply("test")).isEqualTo("TEST")
	}

	@Autowired
	private lateinit var fromSupplierQueue: PollableChannel

	@Test
	fun `verify supplier flow`() {
		assertThat(this.fromSupplierQueue.receive(10_000)).isNotNull()
	}


	@Configuration
	@EnableIntegration
	class Config {

		@Bean
		fun convertFlow() =
				integrationFlow("convertFlowInput") {
					it.convert<TestPojo>()
							.convert<TestPojo> { it.id("kotlinConverter") }
				}

		@Bean
		fun functionFlow() =
				integrationFlow<Function<String, String>>({ it.beanName("functionGateway") }) {
					it.transform<String, String> { it.toUpperCase() }
				}

		@Bean
		fun messageSourceFlow() =
				integrationFlow(MessageProcessorMessageSource { "testSource" },
						{ it.poller { it.fixedDelay(10).maxMessagesPerPoll(1) } }) {
					it.channel { c -> c.queue("fromSupplierQueue") }
				}
	}

	data class TestPojo(val name: String?, val date: Date?)

}
