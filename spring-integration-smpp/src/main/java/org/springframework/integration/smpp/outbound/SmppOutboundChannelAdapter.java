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

import org.springframework.integration.context.IntegrationObjectSupport;
import org.springframework.integration.smpp.core.SmesMessageSpecification;
import org.springframework.integration.smpp.session.ExtendedSmppSession;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Sends messages to an SMS gateway using SMPP. Most of the work in terms
 * of converting inbound message headers (whose keys, by the way, live in
 * {@link org.springframework.integration.smpp.core.SmppConstants}) is done by
 * {@link org.springframework.integration.smpp.core.SmesMessageSpecification}, which
 * handles <em>all</em> the tedium of converting and validating the configuration.
 * <p/>
 * This adapter supports  <em>mobile terminated (MT)</em> messaging, where the recipient
 * is a directory phone number.
 *
 * @author Josh Long
 * @author Edge Dalmacio
 * @since 1.0
 */
public class SmppOutboundChannelAdapter extends IntegrationObjectSupport implements MessageHandler {

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

	@Override
	protected void onInit() throws Exception {
		if (this.timeFormatter == null) {
			this.timeFormatter = new AbsoluteTimeFormatter();
		}

		Assert.notNull(this.smppSession, "the smppSession must not be null");
		Assert.isTrue(!this.smppSession.getBindType().equals(BindType.BIND_RX),
				"the BindType must support message production: BindType.TX or BindType.TRX only supported");

		this.smppSession.start();

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
	public void handleMessage(Message<?> message) throws MessagingException {

		try {
			// todo support a gateway and have that gateway also handle message delivery receipt notifications
			// that will correlate this smsMessageId with the ID that comes back asynchronously
			// from the SMSC indicating that the message has been delivered.
			// this could require that we keep a correlation map since its possible upstream SMSC
			// unused return value -- see gateway

			SmesMessageSpecification specification = applyDefaultsIfNecessary(
					SmesMessageSpecification.fromMessage(this.smppSession, message)
							.setTimeFormatter(this.timeFormatter));

			List<String> smsMessageId = specification.send();
			logger.debug( "sent message : "+message.getPayload());
			logger.debug("message ID(s) for the sent message: " + smsMessageId);
		} catch (Exception e) {
			throw new RuntimeException("Exception in trying to process the inbound SMPP message", e);
		}
	}

	@Override
	public String getComponentType() {
		return "smpp:outbound-channel-adapter";
	}

}
