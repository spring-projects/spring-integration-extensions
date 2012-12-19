package org.springframework.integration.smpp.session;

import org.jsmpp.bean.BindType;
import org.jsmpp.session.ClientSession;
import org.jsmpp.session.MessageReceiverListener;

/**
 * Represents an {@link org.jsmpp.session.SMPPSession} that has a few extra capabilities:
 * <p/>
 * <ol><li>supports registration of multiple {@link org.jsmpp.session.MessageReceiverListener}s</li></ol>
 *
 * @author Josh Long
 * @since 2.1
 */
public interface ExtendedSmppSession extends ClientSession {
	/**
	 * a {@link MessageReceiverListener} implementation to be added to the set of existing listeners.
	 * <p/>
	 * NB: the contract for each of these is the same as for a single instance: don't take too long when doing your processing. This is even more
	 * important now that multiple implementations need to share the same callback slice time.
	 *
	 * @param messageReceiverListener the message receiver listener
	 */
	void addMessageReceiverListener(MessageReceiverListener messageReceiverListener);

	/**
	 * We need to know this to determine whether or not this session can handle the requirements we need.
	 *
	 * @return the {@link BindType}
	 */
	BindType getBindType();

	void start() ;

	void stop() ;

}
