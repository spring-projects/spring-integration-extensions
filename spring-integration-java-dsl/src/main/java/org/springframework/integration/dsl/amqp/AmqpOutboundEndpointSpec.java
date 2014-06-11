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

package org.springframework.integration.dsl.amqp;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.integration.amqp.outbound.AmqpOutboundEndpoint;
import org.springframework.integration.amqp.support.AmqpHeaderMapper;
import org.springframework.integration.amqp.support.DefaultAmqpHeaderMapper;
import org.springframework.integration.dsl.core.MessageHandlerSpec;
import org.springframework.messaging.MessageChannel;
import org.springframework.util.Assert;

/**
 * @author Artem Bilan
 */
public class AmqpOutboundEndpointSpec extends MessageHandlerSpec<AmqpOutboundEndpointSpec, AmqpOutboundEndpoint> {

	private final AmqpOutboundEndpoint endpoint;

	private final boolean expectReply;

	private final DefaultAmqpHeaderMapper headerMapper = new DefaultAmqpHeaderMapper();

	AmqpOutboundEndpointSpec(AmqpTemplate amqpTemplate, boolean expectReply) {
		this.endpoint = new AmqpOutboundEndpoint(amqpTemplate);
		this.expectReply = expectReply;
		this.endpoint.setExpectReply(expectReply);
		this.endpoint.setHeaderMapper(this.headerMapper);
	}

	public AmqpOutboundEndpointSpec headerMapper(AmqpHeaderMapper headerMapper) {
		endpoint.setHeaderMapper(headerMapper);
		return this;
	}

	public AmqpOutboundEndpointSpec routingKey(String routingKey) {
		endpoint.setRoutingKey(routingKey);
		return this;
	}

	public AmqpOutboundEndpointSpec defaultDeliveryMode(MessageDeliveryMode defaultDeliveryMode) {
		endpoint.setDefaultDeliveryMode(defaultDeliveryMode);
		return this;
	}

	public AmqpOutboundEndpointSpec exchangeName(String exchangeName) {
		endpoint.setExchangeName(exchangeName);
		return this;
	}

	public AmqpOutboundEndpointSpec routingKeyExpression(String routingKeyExpression) {
		endpoint.setRoutingKeyExpression(routingKeyExpression);
		return this;
	}

	public AmqpOutboundEndpointSpec returnChannel(MessageChannel returnChannel) {
		endpoint.setReturnChannel(returnChannel);
		return this;
	}

	public AmqpOutboundEndpointSpec confirmAckChannel(MessageChannel ackChannel) {
		endpoint.setConfirmAckChannel(ackChannel);
		return this;
	}

	public AmqpOutboundEndpointSpec exchangeNameExpression(String exchangeNameExpression) {
		endpoint.setExchangeNameExpression(exchangeNameExpression);
		return this;
	}

	public AmqpOutboundEndpointSpec confirmNackChannel(MessageChannel nackChannel) {
		endpoint.setConfirmNackChannel(nackChannel);
		return this;
	}

	public AmqpOutboundEndpointSpec confirmCorrelationExpression(String confirmCorrelationExpression) {
		endpoint.setConfirmCorrelationExpression(confirmCorrelationExpression);
		return this;
	}

	public AmqpOutboundEndpointSpec mappedRequestHeaders(String... headers) {
		this.headerMapper.setRequestHeaderNames(headers);
		return this;
	}

	public AmqpOutboundEndpointSpec mappedReplyHeaders(String... headers) {
		Assert.isTrue(expectReply, "'mappedReplyHeaders' can be applied on for gateway");
		this.headerMapper.setReplyHeaderNames(headers);
		return this;
	}


	@Override
	protected AmqpOutboundEndpoint doGet() {
		return this.endpoint;
	}

}
