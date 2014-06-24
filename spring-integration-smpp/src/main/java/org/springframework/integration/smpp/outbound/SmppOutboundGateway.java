/* Copyright 2002-2014 the original author or authors.
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
package org.springframework.integration.smpp.outbound;

import java.util.List;

import org.jsmpp.bean.BindType;
import org.jsmpp.bean.TypeOfNumber;
import org.jsmpp.util.AbsoluteTimeFormatter;
import org.jsmpp.util.TimeFormatter;

import org.springframework.integration.handler.AbstractReplyProducingMessageHandler;
import org.springframework.integration.smpp.core.SmesMessageSpecification;
import org.springframework.integration.smpp.session.ExtendedSmppSession;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Support for request/reply exchanges over SMPP to a SMSC.
 * <p/>
 * The request is an outbound SMS message, as in the
 * {@link org.springframework.integration.smpp.outbound.SmppOutboundChannelAdapter},
 * and the reply can either be the messageId of the outbound message which can ultimately
 * be used to track the confirmation, or the confirmation of the receipt of the outbound
 * message itself. In the latter case, this class simply does the work
 * of waiting for the reply and correlating it to the outbound request.
 * <p/>
 * By default this component assumes one {@link org.jsmpp.session.SMPPSession} in
 * "transceiver" mode - it can both request and reply. Conceptually it should be possible
 * to support two {@link org.jsmpp.session.SMPPSession}running, one in "sender" mode,
 * and another in "receiver" mode and handle the duplexing manually.
 * The correlation logic is the same, in any event.
 * <p/>
 *
 * @author Josh Long
 * @author Edge Dalmacio
 * @since 1.0
 */
public class SmppOutboundGateway extends AbstractReplyProducingMessageHandler {
	@Override
	protected void doInit() {
        super.doInit();
		Assert.isTrue(
				this.smppSession.getBindType().equals(BindType.BIND_TX) ||
						this.smppSession.getBindType().equals(BindType.BIND_TRX),
				"the smppSession's bindType must be BindType.BIND_TX or BindType.BIND_TRX");

		this.smppSession.start();
	}

	@Override
	protected Object handleRequestMessage(Message<?> requestMessage) {
		try {

			SmesMessageSpecification specification = applyDefaultsIfNecessary(
					SmesMessageSpecification.fromMessage(this.smppSession, requestMessage)
							.setTimeFormatter(this.timeFormatter));

			List<String> smsMessageId = specification.send();

			logger.debug("message ID(s) for the sent message: " + smsMessageId);

			return MessageBuilder.withPayload(smsMessageId).build();
		} catch (Exception e) {
			throw new RuntimeException("Exception in trying to process the inbound SMPP message", e);
		}
	}

	private String defaultSourceAddress;

	private TypeOfNumber defaultSourceAddressTypeOfNumber = TypeOfNumber.UNKNOWN;

	private TimeFormatter timeFormatter = new AbsoluteTimeFormatter();

	private ExtendedSmppSession smppSession;

	public void setDefaultSourceAddress(String defaultSourceAddress) {
		this.defaultSourceAddress = defaultSourceAddress;
	}

	public void setDefaultSourceAddressTypeOfNumber(TypeOfNumber defaultSourceAddressTypeOfNumber) {
		this.defaultSourceAddressTypeOfNumber = defaultSourceAddressTypeOfNumber;
	}

	public void setTimeFormatter(TimeFormatter timeFormatter) {
		this.timeFormatter = timeFormatter;
	}

	private SmesMessageSpecification applyDefaultsIfNecessary(SmesMessageSpecification smsSpec) {

		if (defaultSourceAddressTypeOfNumber != null) {
			smsSpec.setSourceAddressTypeOfNumberIfRequired(this.defaultSourceAddressTypeOfNumber);
		}
		if (StringUtils.hasText(this.defaultSourceAddress)) {
			smsSpec.setSourceAddressIfRequired(this.defaultSourceAddress);
		}

		return smsSpec;
	}

	public void setSmppSession(ExtendedSmppSession s) {
		this.smppSession = s;
	}

	@Override
	public String getComponentType() {
		return "smpp:outbound-gateway";
	}

}
