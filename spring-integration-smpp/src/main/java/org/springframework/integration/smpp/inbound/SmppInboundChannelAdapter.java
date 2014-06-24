/*
 * Copyright 2002-2013 the original author or authors.
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
package org.springframework.integration.smpp.inbound;

import org.jsmpp.bean.BindType;
import org.jsmpp.bean.DeliverSm;
import org.jsmpp.bean.DeliveryReceipt;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.endpoint.AbstractEndpoint;
import org.springframework.integration.smpp.core.AbstractReceivingMessageListener;
import org.springframework.integration.smpp.core.SmesMessageSpecification;
import org.springframework.integration.smpp.session.ExtendedSmppSession;
import org.springframework.util.Assert;

/**
 * Supports receiving messages of a payload specified by the SMPP protocol from a <em>short message service center</em> (SMSC).
 *
 * @author Josh Long
 * @since 1.0
 *
 * TODO find some way to configure the {@link java.util.concurrent.Executor} running for the JSMPP library
 */
public class SmppInboundChannelAdapter extends AbstractEndpoint {

	private MessagingTemplate messagingTemplate;
	private MessageChannel channel;
	private ExtendedSmppSession smppSession;

	/**
	 * the channel on which inbound SMS messages should be delivered to Spring Integration components.
	 *
	 * @param channel the channel
	 */
	public void setChannel(MessageChannel channel) {
		this.channel = channel;
		this.messagingTemplate = new MessagingTemplate(this.channel);
	}

	@Override
	protected void onInit() throws Exception {
		Assert.notNull(this.channel, "the 'channel' property must be set");
		Assert.notNull(this.smppSession, "the 'smppSession' property must be set");
		Assert.isTrue(this.smppSession.getBindType().isReceiveable() ||
				this.smppSession.getBindType().equals(BindType.BIND_TRX),
				"this session's bind type should support " +
						"receiving messages or both sending *and* receiving messages!");
	}

	/**
	 * Set smpp session
	 * @param s smpp session
	 */
	public void setSmppSession(ExtendedSmppSession s) {
		this.smppSession = s;
	}

	private AbstractReceivingMessageListener abstractReceivingMessageListener =
		new AbstractReceivingMessageListener() {
			@Override
			protected void onDeliveryReceipt(DeliverSm deliverSm, String ogMessageId, DeliveryReceipt deliveryReceipt) throws Exception {
				// noop don't care
			}

			@Override
			protected void onTextMessage(DeliverSm deliverSm, String txtMessage) throws Exception {
				Message<?> msg = SmesMessageSpecification.toMessageFromSms(deliverSm, txtMessage);
				messagingTemplate.send(msg);
			}
		};

	@Override
	protected void doStart() {
		this.smppSession.addMessageReceiverListener(this.abstractReceivingMessageListener);
		this.smppSession.start();
	}

	@Override
	protected void doStop() {
		this.smppSession.stop();
	}

	@Override
	public String getComponentType() {
		return "smpp:inbound-channel-adapter";
	}
}
