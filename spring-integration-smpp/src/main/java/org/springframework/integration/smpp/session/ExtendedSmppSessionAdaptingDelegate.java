package org.springframework.integration.smpp.session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsmpp.InvalidResponseException;
import org.jsmpp.PDUException;
import org.jsmpp.bean.*;
import org.jsmpp.extra.NegativeResponseException;
import org.jsmpp.extra.ResponseTimeoutException;
import org.jsmpp.extra.SessionState;
import org.jsmpp.session.*;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.Lifecycle;

import java.io.IOException;

/**
 * Adapts to the {@link ClientSession} API, while also providing the callbacks for the Spring container
 *
 * @author Josh Long
 * @since 2.1
 */
public class ExtendedSmppSessionAdaptingDelegate implements /*Lifecycle,*/ ExtendedSmppSession, InitializingBean {

	/**
	 * callback for custom lifecycle events
	 */
	private Lifecycle lifecycle;
	private Log log = LogFactory.getLog(getClass());
	private final DelegatingMessageReceiverListener delegatingMessageReceiverListener = new DelegatingMessageReceiverListener();
	private volatile boolean running;
	private BindType bindType;
	private SMPPSession session;

	public void setBindType(BindType bindType) {
		this.bindType = bindType;
	}

	public SMPPSession getTargetClientSession() {
		return this.session;
	}

	public void start() {

		if( this.running)
			return;

		lifecycle.start();
		this.running = true;
	}

	public void stop() {
		lifecycle.stop();
		this.running = false;
	}

	public boolean isRunning() {
		return this.running;
	}

	public BindType getBindType() {
		return this.bindType;
	}

	/**
	 * noops for the {@link Lifecycle} arg in {@link ExtendedSmppSessionAdaptingDelegate#ExtendedSmppSessionAdaptingDelegate(org.jsmpp.session.SMPPSession, org.springframework.context.Lifecycle)}
	 *
	 * @param session the session
	 */
	public ExtendedSmppSessionAdaptingDelegate(SMPPSession session) {
		this(session, new Lifecycle() {
			public void start() {
			}

			public void stop() {
			}

			public boolean isRunning() {
				return true;
			}
		});
	}

	public ExtendedSmppSessionAdaptingDelegate(SMPPSession session, Lifecycle lifecycle) {
		this.lifecycle = lifecycle;
		this.session = session;
		this.session.setMessageReceiverListener(this.delegatingMessageReceiverListener);
	}

	public void addMessageReceiverListener(MessageReceiverListener messageReceiverListener) {
		this.delegatingMessageReceiverListener.addMessageReceiverListener(messageReceiverListener);
	}

	public String submitShortMessage(String serviceType, TypeOfNumber sourceAddrTon, NumberingPlanIndicator sourceAddrNpi,
																	 String sourceAddr, TypeOfNumber destAddrTon, NumberingPlanIndicator destAddrNpi,
																	 String destinationAddr, ESMClass esmClass, byte protocolId, byte priorityFlag, String scheduleDeliveryTime, String validityPeriod, RegisteredDelivery registeredDelivery, byte replaceIfPresentFlag, DataCoding dataCoding, byte smDefaultMsgId, byte[] shortMessage, OptionalParameter... optionalParameters) throws PDUException, ResponseTimeoutException, InvalidResponseException, NegativeResponseException, IOException {
		return session.submitShortMessage(serviceType, sourceAddrTon, sourceAddrNpi, sourceAddr, destAddrTon, destAddrNpi, destinationAddr, esmClass, protocolId, priorityFlag, scheduleDeliveryTime, validityPeriod, registeredDelivery, replaceIfPresentFlag, dataCoding, smDefaultMsgId, shortMessage, optionalParameters);
	}

	public SubmitMultiResult submitMultiple(String serviceType, TypeOfNumber sourceAddrTon, NumberingPlanIndicator sourceAddrNpi, String sourceAddr, Address[] destinationAddresses, ESMClass esmClass, byte protocolId, byte priorityFlag, String scheduleDeliveryTime, String validityPeriod, RegisteredDelivery registeredDelivery, ReplaceIfPresentFlag replaceIfPresentFlag, DataCoding dataCoding, byte smDefaultMsgId, byte[] shortMessage, OptionalParameter[] optionalParameters) throws PDUException, ResponseTimeoutException, InvalidResponseException, NegativeResponseException, IOException {
		return session.submitMultiple(
				serviceType, sourceAddrTon, sourceAddrNpi, sourceAddr, destinationAddresses, esmClass, protocolId, priorityFlag, scheduleDeliveryTime, validityPeriod, registeredDelivery, replaceIfPresentFlag, dataCoding, smDefaultMsgId, shortMessage, optionalParameters
		);
	}

	public QuerySmResult queryShortMessage(String messageId, TypeOfNumber sourceAddrTon, NumberingPlanIndicator sourceAddrNpi, String sourceAddr) throws PDUException, ResponseTimeoutException, InvalidResponseException, NegativeResponseException, IOException {
		return session.queryShortMessage(messageId, sourceAddrTon, sourceAddrNpi, sourceAddr);
	}

	public void cancelShortMessage(String serviceType, String messageId, TypeOfNumber sourceAddrTon, NumberingPlanIndicator sourceAddrNpi, String sourceAddr,
																 TypeOfNumber destAddrTon, NumberingPlanIndicator destAddrNpi, String destinationAddress) throws PDUException, ResponseTimeoutException, InvalidResponseException, NegativeResponseException, IOException {
		session.cancelShortMessage(serviceType, messageId, sourceAddrTon, sourceAddrNpi, sourceAddr, destAddrTon, destAddrNpi, destinationAddress);
	}

	public void replaceShortMessage(String messageId, TypeOfNumber sourceAddrTon, NumberingPlanIndicator sourceAddrNpi, String sourceAddr, String scheduleDeliveryTime, String validityPeriod, RegisteredDelivery registeredDelivery, byte smDefaultMsgId, byte[] shortMessage) throws PDUException, ResponseTimeoutException, InvalidResponseException, NegativeResponseException, IOException {
		session.replaceShortMessage(messageId, sourceAddrTon, sourceAddrNpi, sourceAddr, scheduleDeliveryTime, validityPeriod, registeredDelivery, smDefaultMsgId, shortMessage);
	}

	public DataSmResult dataShortMessage(String serviceType, TypeOfNumber sourceAddrTon, NumberingPlanIndicator sourceAddrNpi, String sourceAddr, TypeOfNumber destAddrTon, NumberingPlanIndicator destAddrNpi, String destinationAddr, ESMClass esmClass, RegisteredDelivery registeredDelivery, DataCoding dataCoding, OptionalParameter... optionalParameters) throws PDUException, ResponseTimeoutException, InvalidResponseException, NegativeResponseException, IOException {
		return session.dataShortMessage(serviceType, sourceAddrTon, sourceAddrNpi, sourceAddr, destAddrTon, destAddrNpi, destinationAddr, esmClass, registeredDelivery, dataCoding, optionalParameters);
	}

	public String getSessionId() {
		return session.getSessionId();
	}

	public void setEnquireLinkTimer(int enquireLinkTimer) {
		session.setEnquireLinkTimer(enquireLinkTimer);
	}

	public int getEnquireLinkTimer() {
		return session.getEnquireLinkTimer();
	}

	public void setTransactionTimer(long transactionTimer) {
		session.setTransactionTimer(transactionTimer);
	}

	public long getTransactionTimer() {
		return session.getTransactionTimer();
	}

	public SessionState getSessionState() {
		return session.getSessionState();
	}

	public void addSessionStateListener(SessionStateListener l) {
		session.addSessionStateListener(l);
	}

	public void removeSessionStateListener(SessionStateListener l) {
		session.removeSessionStateListener(l);
	}

	public long getLastActivityTimestamp() {
		return session.getLastActivityTimestamp();
	}

	public void close() {
		session.close();
	}

	public void unbindAndClose() {
		session.unbindAndClose();
	}

	public void afterPropertiesSet() throws Exception {
	    log.debug( "afterPropertiesSet!");
	}
}
