/* Copyright 2002-2013 the original author or authors.
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
 * @since 1.0
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
