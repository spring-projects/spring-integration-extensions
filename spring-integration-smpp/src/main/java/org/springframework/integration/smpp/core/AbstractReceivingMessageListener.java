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
package org.springframework.integration.smpp.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsmpp.bean.*;
import org.jsmpp.extra.ProcessRequestException;
import org.jsmpp.session.DataSmResult;
import org.jsmpp.session.MessageReceiverListener;
import org.jsmpp.session.Session;

/**
 * @author Josh Long
 * @since 1.0
 */
abstract public class AbstractReceivingMessageListener implements MessageReceiverListener {

	private Log logger = LogFactory.getLog(getClass());

	public void onAcceptDeliverSm(DeliverSm deliverSm) throws ProcessRequestException {
		if (MessageType.SMSC_DEL_RECEIPT.containedIn(deliverSm.getEsmClass())) {	// delivery receipt
			try {
				DeliveryReceipt delReceipt = deliverSm.getShortMessageAsDeliveryReceipt();
				String messageId;
                try {
                    long id = Long.parseLong(delReceipt.getId());
                    messageId = Long.toString(id, 16).toUpperCase();
                } catch (NumberFormatException nfe) {
                    logger.debug("Fail parsing message id into hex format from " + delReceipt.getId()
                            + ". Now using id as it is");
                    messageId = delReceipt.getId();
                }
				logger.debug("Receiving delivery receipt for message '" + messageId + "' : " + delReceipt);
                onDeliveryReceipt(deliverSm, messageId, delReceipt);
			} catch (Exception e) {
				logger.error("Failed getting delivery receipt", e);
				throw new RuntimeException(e);
			}
		} else {
			try {// this is an actual SMS message
				byte[] shortMessage = deliverSm.getShortMessage();
				String txtSms = shortMessage == null ? new String() : new String(shortMessage);
				logger.debug("Receiving message : " + txtSms);
				onTextMessage(deliverSm, txtSms);
			} catch (Exception e) {
				logger.error("Failed getting short message", e);
				throw new RuntimeException(e);
			}
		}
	}

	public void onAcceptAlertNotification(AlertNotification alertNotification) {

	}

	public DataSmResult onAcceptDataSm(DataSm dataSm, Session source) throws ProcessRequestException {
		return null;
	}

	/**
	 * specific callback for a <em>receipt</em>, which you'll only get if the outbound message had a specific delivery receipt setting.
	 */
	abstract protected void onDeliveryReceipt(DeliverSm deliverSm, String ogMessageId, DeliveryReceipt deliveryReceipt) throws Exception;

	/**
	 * specific callback for proper SMS, text-based messages.
	 */
	abstract protected void onTextMessage(DeliverSm deliverSm, String txtMessage) throws Exception;
}
