package org.springframework.integration.smpp.session;

import org.jsmpp.bean.AlertNotification;
import org.jsmpp.bean.DataSm;
import org.jsmpp.bean.DeliverSm;
import org.jsmpp.extra.ProcessRequestException;
import org.jsmpp.session.DataSmResult;
import org.jsmpp.session.MessageReceiverListener;
import org.jsmpp.session.Session;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * We're normally allowed to register only one {@link MessageReceiverListener} instance.
 * Additionally, that instance must be registered <em>before</em> connection.
 * <p/>
 * This class delegates all calls to as many {@link MessageReceiverListener}s as you'd like, regardless of when the registered listener was added.
 *
 * @author Josh Long
 * @since 2.1
 */
public class DelegatingMessageReceiverListener implements MessageReceiverListener {

	private volatile Set<MessageReceiverListener> messageReceiverListenerSet =
			new CopyOnWriteArraySet<MessageReceiverListener>();

	public void onAcceptDeliverSm(DeliverSm deliverSm) throws ProcessRequestException {
		for (MessageReceiverListener l : this.messageReceiverListenerSet)
			l.onAcceptDeliverSm(deliverSm);
	}

	public void onAcceptAlertNotification(AlertNotification alertNotification) {
		for (MessageReceiverListener l : this.messageReceiverListenerSet)
			l.onAcceptAlertNotification(alertNotification);
	}

	public DataSmResult onAcceptDataSm(DataSm dataSm, Session source) throws ProcessRequestException {
		DataSmResult dataSmResult = null;
		for (MessageReceiverListener l : this.messageReceiverListenerSet) {
			DataSmResult tmpV = l.onAcceptDataSm(dataSm, source);
			if (tmpV != null) {
				dataSmResult = tmpV;
			}
		}
		return dataSmResult; // could still be null
	}

	public void addMessageReceiverListener(MessageReceiverListener messageReceiverListener) {
		this.messageReceiverListenerSet.add(messageReceiverListener);
	}
}
