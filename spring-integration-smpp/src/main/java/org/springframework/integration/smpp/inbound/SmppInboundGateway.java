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
import org.jsmpp.bean.TypeOfNumber;
import org.springframework.messaging.Message;
import org.springframework.integration.gateway.MessagingGatewaySupport;
import org.springframework.integration.smpp.core.AbstractReceivingMessageListener;
import org.springframework.integration.smpp.core.SmesMessageSpecification;
import org.springframework.integration.smpp.core.SmppConstants;
import org.springframework.integration.smpp.session.ExtendedSmppSession;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * What'running an inbound gateway in this case? Receive a text message and then automatically send a response.
 *
 * @author Josh Long
 * @since 1.0
 */
public class SmppInboundGateway extends MessagingGatewaySupport {

	private ExtendedSmppSession smppSession;
	private TypeOfNumber defaultSourceAddressTypeOfNumber;
	private String defaultSourceAddress;

	/**
	 * Set default source address type of number.
	 * @param defaultSourceAddressTypeOfNumber default address type of number.
	 */
	public void setDefaultSourceAddressTypeOfNumber(TypeOfNumber defaultSourceAddressTypeOfNumber) {
		this.defaultSourceAddressTypeOfNumber = defaultSourceAddressTypeOfNumber;
	}

	/**
	 * Set default source address.
	 * @param defaultSourceAddress default source address
	 */
	public void setDefaultSourceAddress(String defaultSourceAddress) {
		this.defaultSourceAddress = defaultSourceAddress;
	}

	/**
	 * for configuration purposes.
	 *
	 * @param s the session to use
	 */
	public void setSmppSession(ExtendedSmppSession s) {
		this.smppSession = s;
	}

	@Override
	protected void onInit() throws Exception {
		Assert.notNull(this.smppSession, "the 'smppSession' property must be set");
		Assert.isTrue(this.smppSession.getBindType().isReceiveable() ||
				this.smppSession.getBindType().equals(BindType.BIND_TRX),
				"this session's bind type should support " +
						"receiving messages or both sending *and* receiving messages!");
	}

	private AbstractReceivingMessageListener abstractReceivingMessageListener =
			new AbstractReceivingMessageListener() {
				@Override
				protected void onDeliveryReceipt(DeliverSm deliverSm, String ogMessageId, DeliveryReceipt deliveryReceipt) throws Exception {
					// noop don't care
				}

				@Override
				protected void onTextMessage(DeliverSm deliverSm, String txtMessage) throws Exception {
					// we receive sms
					logger.debug("received an SMS in " + getClass() + ". Processing it.");
					Message<?> msg = SmesMessageSpecification.toMessageFromSms(deliverSm, txtMessage);

					// send it INTO SI, where it can be processed. The reply message is sent BACK to this, which we then send BACK outSession through SMS
					logger.debug("sending the SMS inbound to be processed; awaiting a reply.");

					Message<?> response = sendAndReceiveMessage(msg);
					logger.debug("received a reply message; will handle as in outbound adapter");

					/// todo figure out relationship between inbound-gw and replyChannel
					applyDefaults(msg, response, SmesMessageSpecification.fromMessage(smppSession, response)).send();
					logger.debug("the reply SMS message has been sent.");
				}
			};

	/**
	 * among other things this method simply 'flips' the src/dst
	 *
	 * @param request req
	 * @param response res
	 * @param smesMessageSpecification spec
	 * @return same spec reflecting new switches
	 */
	SmesMessageSpecification applyDefaults(Message<?> request, Message<?> response, SmesMessageSpecification smesMessageSpecification) {

		String from = null, to = null;
		if (request.getHeaders().containsKey(SmppConstants.SRC_ADDR)) {
			to = (String) request.getHeaders().get(SmppConstants.SRC_ADDR);
			if (StringUtils.hasText(to)) {
				smesMessageSpecification.setDestinationAddress(to);
			}
		}
		if (request.getHeaders().containsKey(SmppConstants.DEST_ADDRESS)) {
			from = (String) request.getHeaders().get(SmppConstants.DEST_ADDRESS);
			if (StringUtils.hasText(from)) {
				smesMessageSpecification.setSourceAddress(from);
			}
		}
		if (defaultSourceAddressTypeOfNumber != null) {
			smesMessageSpecification.setSourceAddressTypeOfNumberIfRequired(this.defaultSourceAddressTypeOfNumber);
		}
		if (StringUtils.hasText(this.defaultSourceAddress)) {
			smesMessageSpecification.setSourceAddressIfRequired(this.defaultSourceAddress);
		}
		return smesMessageSpecification;
	}

	@Override
	protected void doStart() {
		super.doStart();
		this.smppSession.addMessageReceiverListener(this.abstractReceivingMessageListener);
		this.smppSession.start();
	}

	@Override
	protected void doStop() {
		super.doStop();
		this.smppSession.stop();
	}

	@Override
	public String getComponentType() {
		return "smpp:inbound-gateway";
	}
}
