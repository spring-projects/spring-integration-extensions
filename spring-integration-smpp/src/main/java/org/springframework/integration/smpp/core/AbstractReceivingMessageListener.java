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
 * @since 2.1
 */
abstract public class AbstractReceivingMessageListener implements MessageReceiverListener {

	private Log logger = LogFactory.getLog(getClass());

	public void onAcceptDeliverSm(DeliverSm deliverSm) throws ProcessRequestException {
		if (MessageType.SMSC_DEL_RECEIPT.containedIn(deliverSm.getEsmClass())) {	// delivery receipt
			try {
				DeliveryReceipt delReceipt = deliverSm.getShortMessageAsDeliveryReceipt();
				long id = Long.parseLong(delReceipt.getId());
				String messageId = Long.toString(id, 16).toUpperCase();
				onDeliveryReceipt(deliverSm, messageId, delReceipt);
				logger.debug("Receiving delivery receipt for message '" + messageId + "' : " + delReceipt);
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
