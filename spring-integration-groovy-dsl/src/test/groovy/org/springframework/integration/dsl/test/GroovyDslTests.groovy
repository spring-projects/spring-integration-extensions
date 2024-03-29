/*
 * Copyright 2021 the original author or authors.
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

package org.springframework.integration.dsl.test

import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.channel.FluxMessageChannel
import org.springframework.integration.channel.QueueChannel
import org.springframework.integration.config.EnableIntegration
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.IntegrationFlowDefinition
import org.springframework.integration.dsl.IntegrationFlows
import org.springframework.integration.dsl.Pollers
import org.springframework.integration.dsl.Transformers
import org.springframework.integration.dsl.context.IntegrationFlowContext
import org.springframework.integration.handler.LoggingHandler
import org.springframework.integration.scheduling.PollerMetadata
import org.springframework.integration.support.MessageBuilder
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.PollableChannel
import org.springframework.messaging.support.GenericMessage
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import spock.lang.Specification

import java.time.Duration
import java.util.function.Function

import static org.springframework.integration.dsl.IntegrationGroovyDsl.integrationFlow

@SpringJUnitConfig
@DirtiesContext
class GroovyDslTests extends Specification {

	@Autowired
	private BeanFactory beanFactory

	@Autowired
	private IntegrationFlowContext integrationFlowContext

	@Autowired
	private PollableChannel pollerResultChannel

	@Autowired
	@Qualifier('requestReplyFlow.input')
	private MessageChannel requestReplyFlowInput

	@Autowired
	private MessageChannel requestReplyFixedFlowInput

	@Autowired
	@Qualifier('functionGateway')
	private Function<byte[], String> upperCaseFunction

	def 'when application starts, it emits message to pollerResultChannel'() {
		expect: 'message in the pollerResultChannel'
		this.pollerResultChannel.receive(10000)
		this.pollerResultChannel.receive(10000)
	}

	def 'requestReplyFlow has to reply'() {
		given:
		def replyChannel = new QueueChannel()
		def testMessage =
				MessageBuilder.withPayload('hello')
						.setHeader(MessageHeaders.REPLY_CHANNEL, replyChannel)
						.build()

		when:
		this.requestReplyFlowInput.send(testMessage)

		then:
		replyChannel.receive(1000).payload == 'HELLO'
	}

	def 'requestReplyFixedFlow has to reply'() {
		given:
		def replyChannel = new QueueChannel()
		def testMessage =
				MessageBuilder.withPayload(4)
						.setHeader(MessageHeaders.REPLY_CHANNEL, replyChannel)
						.build()

		when:
		this.requestReplyFixedFlowInput.send(testMessage)

		then:
		replyChannel.receive(1000).payload == 16
	}

	def 'uppercase function'() {
		expect: 'uppercase function flow works'
		this.upperCaseFunction.apply('test'.bytes) == 'TEST'
	}

	def 'reactive publisher flow'() {
		given:
		def fluxChannel = new FluxMessageChannel()

		def verifyLater =
				StepVerifier
						.create(Flux.from(fluxChannel).map { it.payload })
						.expectNext(4, 6)
						.thenCancel()
						.verifyLater()

		def publisher = Flux.just(2, 3).map { new GenericMessage<>(it) }

		def integrationFlow =
				integrationFlow(publisher)
						{
							transform Message<Integer>, { it.payload * 2 }, { id 'foo' }
							channel fluxChannel
						}

		when:
		def registration = this.integrationFlowContext.registration(integrationFlow).register()

		then:
		verifyLater.verify(Duration.ofSeconds(10))

		registration.destroy()
	}

	@Autowired
	@Qualifier('scatterGatherFlow.input')
	private MessageChannel scatterGatherFlowInput

	def 'Scatter-Gather'() {
		given:
		def replyChannel = new QueueChannel()
		def request =
				MessageBuilder.withPayload("foo")
						.setReplyChannel(replyChannel)
						.build()

		when:
		this.scatterGatherFlowInput.send(request)

		then:
		def bestQuoteMessage = replyChannel.receive(10000)
		(bestQuoteMessage?.payload as List).size() >= 1
	}

	@Autowired
	@Qualifier('oddFlow.input')
	private MessageChannel oddFlowInput

	def 'oddFlow must reply'() {
		given:
		def replyChannel = new QueueChannel()
		def testMessage =
				MessageBuilder.withPayload('test')
						.setHeader(MessageHeaders.REPLY_CHANNEL, replyChannel)
						.build()

		when:
		this.oddFlowInput.send(testMessage)

		then:
		replyChannel.receive(1000).payload == 'odd'
	}


	@Autowired
	@Qualifier('flowLambda.input')
	private MessageChannel flowLambdaInput

	@Autowired
	private PollableChannel wireTapChannel

	def 'flow from lambda'() {
		given:
		def replyChannel = new QueueChannel()
		def message = MessageBuilder.withPayload('test').setReplyChannel(replyChannel).build()

		when:
		this.flowLambdaInput.send message

		then:
		replyChannel.receive(10_000)?.payload == 'TEST'
		this.wireTapChannel.receive(10_000)?.payload == 'test'
	}

	@Configuration
	@EnableIntegration
	static class Config {

		@Bean(PollerMetadata.DEFAULT_POLLER)
		poller() {
			Pollers.fixedDelay(1000).get()
		}


		@Bean
		someFlow() {
			integrationFlow { 'test' }
					{
						log LoggingHandler.Level.WARN, 'test.category'
						channel { queue 'pollerResultChannel' }
					}
		}

		@Bean
		requestReplyFlow() {
			integrationFlow {
				fluxTransform { it.map { it } }
				transform String, { it.toUpperCase() }
			}
		}

		@Bean
		requestReplyFixedFlow() {
			integrationFlow 'requestReplyFixedFlowInput', true,
					{
						handle Integer, { p, h -> p**2 }
					}
		}

		@Bean
		functionFlow() {
			integrationFlow Function<byte[], String>,
					{ beanName 'functionGateway' },
					{
						transform Transformers.objectToString(), { id 'objectToStringTransformer' }
						transform String, { it.toUpperCase() }
						split Message<?>, { it.payload }
						split Object, { it }, { id 'splitterEndpoint' }
						resequence()
						aggregate {
							id 'aggregator'
							outputProcessor { it.one }
						}
					}
		}

		@Bean
		scatterGatherFlow() {
			integrationFlow {
				scatterGather(
						{
							applySequence true
							recipientFlow({ true }, recipientSubFlow())
							recipientFlow({ true },
									integrationFlow { handle Void, { p, h -> Math.random() * 10 } })
							recipientFlow({ true },
									integrationFlow { handle Void, { p, h -> Math.random() * 10 } })
						},
						{
							releaseStrategy {
								it.size() == 3 || it.messages.any { it.payload as Double > 5 }
							}
						})
						{
							gatherTimeout 10_000
						}
			}
		}

		static recipientSubFlow() {
			integrationFlow { handle Void, { p, h -> Math.random() * 10 } }
		}

		@Bean
		IntegrationFlow oddFlow() {
			{ IntegrationFlowDefinition flow ->
				flow.handle(Object, { p, h -> 'odd' })
			}
		}

		@Bean
		flowLambda() {
			integrationFlow {
				filter String, { it == 'test' }, { id 'filterEndpoint' }
				wireTap integrationFlow {
					channel { queue 'wireTapChannel' }
				}
				delay 'delayGroup', { defaultDelay 100 }
				transform String, { it.toUpperCase() }
			}
		}

		@Bean
		flowFromSupplier() {
			IntegrationFlows.fromSupplier({ 'bar' }) { e -> e.poller { p -> p.fixedDelay(10).maxMessagesPerPoll(1) } }
					.channel({ c -> c.queue('fromSupplierQueue') } as Function)
					.get()
		}


	}

}
