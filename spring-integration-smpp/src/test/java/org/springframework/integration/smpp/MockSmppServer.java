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
package org.springframework.integration.smpp;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.jsmpp.PDUStringException;
import org.jsmpp.SMPPConstant;
import org.jsmpp.bean.CancelSm;
import org.jsmpp.bean.DataCodings;
import org.jsmpp.bean.DataSm;
import org.jsmpp.bean.DeliveryReceipt;
import org.jsmpp.bean.ESMClass;
import org.jsmpp.bean.GSMSpecificFeature;
import org.jsmpp.bean.MessageMode;
import org.jsmpp.bean.MessageType;
import org.jsmpp.bean.NumberingPlanIndicator;
import org.jsmpp.bean.QuerySm;
import org.jsmpp.bean.RegisteredDelivery;
import org.jsmpp.bean.ReplaceSm;
import org.jsmpp.bean.SMSCDeliveryReceipt;
import org.jsmpp.bean.SubmitMulti;
import org.jsmpp.bean.SubmitMultiResult;
import org.jsmpp.bean.SubmitSm;
import org.jsmpp.bean.TypeOfNumber;
import org.jsmpp.bean.UnsuccessDelivery;
import org.jsmpp.extra.ProcessRequestException;
import org.jsmpp.extra.SessionState;
import org.jsmpp.session.BindRequest;
import org.jsmpp.session.DataSmResult;
import org.jsmpp.session.QuerySmResult;
import org.jsmpp.session.SMPPServerSession;
import org.jsmpp.session.SMPPServerSessionListener;
import org.jsmpp.session.ServerMessageReceiverListener;
import org.jsmpp.session.ServerResponseDeliveryAdapter;
import org.jsmpp.session.Session;
import org.jsmpp.util.DeliveryReceiptState;
import org.jsmpp.util.MessageIDGenerator;
import org.jsmpp.util.MessageId;
import org.jsmpp.util.RandomMessageIDGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is mock SMPP server connection which copied from jsmpp-examples (git clone https://github.com/otnateos/jsmpp.git)
 * with additional functionality:
 * <ul>
 *     <li>Forward incoming submit_sm to client connected as Receiver/Transceiver</li>
 * </ul>
 * @author Johanes Soetanto
 * @since 1.0
 */
public class MockSmppServer extends ServerResponseDeliveryAdapter implements Runnable, ServerMessageReceiverListener {

    /** Agreement to make it easy to test some functionality */
    public static final class Agreement {
        /** Agreement to throw error when destination set to this value */
        public static final String THROW_NO_DESTINATION_EXCEPTION = "NoRouteDestination";
        /** Agreement to delay processing of sms message. Useful to simulate slow connection */
        public static final String DELAY_PROCESSING = "DelayMe";
        /** Agreement to delay sending back delivery receipt. Useful for testing Delivery Receipt */
        public static final String DELAY_DELIVERY_RECEIPT = "DelayDeliveryReceipt";
    }
    private static int messageDelay = 3000;

	private static final Logger logger = LoggerFactory.getLogger(MockSmppServer.class);
	private String systemId;
	private String password;
	private int port;
	private Map<SMPPServerSession,String> connectionSessionMap = new HashMap<SMPPServerSession,String>();
    private boolean run = true;
    private int acceptConnectionTimeout = 5000;
    private SMPPServerSessionListener sessionListener;
    private boolean initServerAtStart = true;

	private final ExecutorService execService = Executors.newFixedThreadPool(5);
	private final ExecutorService execServiceDelReceipt = Executors.newFixedThreadPool(100);
	private final MessageIDGenerator messageIDGenerator = new RandomMessageIDGenerator();

	public MockSmppServer(int port, String systemId, String password) throws IOException {
		this.systemId = systemId;
		this.password = password;
		this.port = port;
	}

	public void run() {
		try {
			this.sessionListener = new SMPPServerSessionListener(port);
            sessionListener.setTimeout(acceptConnectionTimeout);

			logger.info("Listening on port {}", port);
			while (run) {
                try {
                    SMPPServerSession serverSession = sessionListener.accept();
                    logger.info("Accepting connection for session {}", serverSession.getSessionId());
                    serverSession.setMessageReceiverListener(this);
                    serverSession.setResponseDeliveryListener(this);
                    execService.execute(new WaitBindTask(serverSession, systemId, password, connectionSessionMap));
                }
                catch (SocketTimeoutException ste) {
                    logger.info("SocketTimeoutException: {}", ste.getMessage());
                }
                catch (SocketException se) {
                    logger.info("SocketException: {}", se.getMessage());
                }
			}
		}
		catch (IOException e) {
			logger.error("IO error occurred", e);
		}
	}

    /**
     * Forcing the server to stop.
     * @throws InterruptedException
     * @throws IOException
     */
    public void stop() throws InterruptedException, IOException {
        run = false;
        execService.shutdown();
        sessionListener.close();
        for (SMPPServerSession server : connectionSessionMap.keySet()) {
            server.close();
        }
    }

	public QuerySmResult onAcceptQuerySm(QuerySm querySm,
										 SMPPServerSession source) throws ProcessRequestException {
		logger.info("Accepting query sm, but not implemented");
		return null;
	}

    /* Perform special handling just to simulate something on the SMSC */
    private void onSpecialHandling(SubmitSm submitSm,
                                   SMPPServerSession source) throws ProcessRequestException {
        if (submitSm.getDestAddress().equals(Agreement.THROW_NO_DESTINATION_EXCEPTION)) {
            throw new ProcessRequestException ("Invalid Dest Addr", 0x0B);
        }
        if (new String(submitSm.getShortMessage()).equals(Agreement.DELAY_PROCESSING)) {
            try {
                logger.debug("Delaying handling for {} ms", messageDelay);
                Thread.sleep(3000);
            } catch (InterruptedException ignored) {}
        }
    }

	public MessageId onAcceptSubmitSm(SubmitSm submitSm,
									  SMPPServerSession source) throws ProcessRequestException {
        onSpecialHandling(submitSm, source);

		MessageId messageId = messageIDGenerator.newMessageId();
		logger.debug("Receiving submit_sm '{}', and will return message id {}",
				new String(submitSm.getShortMessage()), messageId);
		if (SMSCDeliveryReceipt.SUCCESS.containedIn(submitSm.getRegisteredDelivery())
				|| SMSCDeliveryReceipt.SUCCESS_FAILURE.containedIn(submitSm.getRegisteredDelivery())) {
			execServiceDelReceipt.execute(new DeliveryReceiptTask(source, submitSm, messageId));

		}
		// on single submit_sm we forward it to any receiver listening to specific address range
		execServiceDelReceipt.execute(new MessageForwardTask(submitSm, connectionSessionMap));
		return messageId;
	}

	public void onSubmitSmRespSent(MessageId messageId,
								   SMPPServerSession source) {
		logger.debug("submit_sm_resp with message_id {} has been sent", messageId);
	}

	public SubmitMultiResult onAcceptSubmitMulti(SubmitMulti submitMulti,
												 SMPPServerSession source) throws ProcessRequestException {
		MessageId messageId = messageIDGenerator.newMessageId();
		logger.debug("Receiving submit_multi_sm '{}', and will return message id {}",
				new String(submitMulti.getShortMessage()), messageId);
		if (SMSCDeliveryReceipt.SUCCESS.containedIn(submitMulti.getRegisteredDelivery())
				|| SMSCDeliveryReceipt.SUCCESS_FAILURE.containedIn(submitMulti.getRegisteredDelivery())) {
			execServiceDelReceipt.execute(new DeliveryReceiptTask(source, submitMulti, messageId));
		}

		return new SubmitMultiResult(messageId.getValue(), new UnsuccessDelivery[0]);
	}

	public DataSmResult onAcceptDataSm(DataSm dataSm, Session source)
			throws ProcessRequestException {
		return null;
	}

	public void onAcceptCancelSm(CancelSm cancelSm, SMPPServerSession source)
			throws ProcessRequestException {
	}

	public void onAcceptReplaceSm(ReplaceSm replaceSm, SMPPServerSession source)
			throws ProcessRequestException {
	}

    public void setAcceptConnectionTimeout(int acceptConnectionTimeout) {
        this.acceptConnectionTimeout = acceptConnectionTimeout;
    }

    private static class WaitBindTask implements Runnable {
		private final SMPPServerSession serverSession;
		private final String systemId;
		private final String password;
		private final Map<SMPPServerSession,String> connectionSessionMap;

		public WaitBindTask(SMPPServerSession serverSession, String systemId, String password,
							Map<SMPPServerSession,String> connectionSessionMap) {
			this.serverSession = serverSession;
			this.systemId = systemId;
			this.password = password;
			this.connectionSessionMap = connectionSessionMap;
		}

		private void registerBindRequest(BindRequest bindRequest) {
			final String range = bindRequest.getAddressRange() == null ? "" : bindRequest.getAddressRange();
			connectionSessionMap.put(serverSession, range);
			logger.debug("Register bind session {} on address range {}", serverSession.getSessionId(), range);
		}

		public void run() {
			try {
				BindRequest bindRequest = serverSession.waitForBind(1000);
				try {
					if(bindRequest.getSystemId().equals(systemId) && bindRequest.getPassword().equals(password)) {
						bindRequest.accept(systemId);
						registerBindRequest(bindRequest);
					}
					else {
						logger.error("Invalid systemId/password");
						bindRequest.reject(SMPPConstant.STAT_ESME_RINVPASWD);
					}
				}
				catch (PDUStringException e) {
					logger.error("Invalid system id", e);
					bindRequest.reject(SMPPConstant.STAT_ESME_RSYSERR);
				}

			}
			catch (IllegalStateException e) {
				logger.error("System error", e);
			}
			catch (TimeoutException e) {
				logger.warn("Wait for bind has reach timeout", e);
			}
			catch (IOException e) {
				logger.error("Failed accepting bind request for session {}", serverSession.getSessionId());
			}
		}
	}

	private static class DeliveryReceiptTask implements Runnable {
		private final SMPPServerSession session;
		private final MessageId messageId;

		private final TypeOfNumber sourceAddrTon;
		private final NumberingPlanIndicator sourceAddrNpi;
		private final String sourceAddress;

		private final TypeOfNumber destAddrTon;
		private final NumberingPlanIndicator destAddrNpi;
		private final String destAddress;

		private final int totalSubmitted;
		private final int totalDelivered;

		private final byte[] shortMessage;

		public DeliveryReceiptTask(SMPPServerSession session,
								   SubmitSm submitSm, MessageId messageId) {
			this.session = session;
			this.messageId = messageId;

			// reversing destination to source
			sourceAddrTon = TypeOfNumber.valueOf(submitSm.getDestAddrTon());
			sourceAddrNpi = NumberingPlanIndicator.valueOf(submitSm.getDestAddrNpi());
			sourceAddress = submitSm.getDestAddress();

			// reversing source to destination
			destAddrTon = TypeOfNumber.valueOf(submitSm.getSourceAddrTon());
			destAddrNpi = NumberingPlanIndicator.valueOf(submitSm.getSourceAddrNpi());
			destAddress = submitSm.getSourceAddr();

			totalSubmitted = totalDelivered = 1;

			shortMessage = submitSm.getShortMessage();
		}

		public DeliveryReceiptTask(SMPPServerSession session,
								   SubmitMulti submitMulti, MessageId messageId) {
			this.session = session;
			this.messageId = messageId;

			// set to unknown and null, since it was submit_multi
			sourceAddrTon = TypeOfNumber.UNKNOWN;
			sourceAddrNpi = NumberingPlanIndicator.UNKNOWN;
			sourceAddress = null;

			// reversing source to destination
			destAddrTon = TypeOfNumber.valueOf(submitMulti.getSourceAddrTon());
			destAddrNpi = NumberingPlanIndicator.valueOf(submitMulti.getSourceAddrNpi());
			destAddress = submitMulti.getSourceAddr();

			// distribution list assumed only contains single address
			totalSubmitted = totalDelivered = submitMulti.getDestAddresses().length;

			shortMessage = submitMulti.getShortMessage();
		}

		public void run() {
			try {
                if (new String(shortMessage).equals(Agreement.DELAY_DELIVERY_RECEIPT)) {
                    logger.debug("Receive request to delay sending of delivery receipt");
                    Thread.sleep(messageDelay);
                } else {
                    Thread.sleep(300); // just give a little bit of delay
                }
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			SessionState state = session.getSessionState();
			if (!state.isReceivable()) {
				logger.debug("Not sending delivery receipt for message id {} since session state is {}",
						messageId, state);
				return;
			}
			String stringValue = Integer.valueOf(messageId.getValue(), 16).toString();
			try {

				DeliveryReceipt delRec = new DeliveryReceipt(stringValue, totalSubmitted, totalDelivered, new Date(),
						new Date(), DeliveryReceiptState.DELIVRD,  null, new String(shortMessage));
                logger.debug("Sending delivery receipt for message id " + messageId + ":" + stringValue);
				session.deliverShortMessage(
						"mc",
						sourceAddrTon,
						sourceAddrNpi,
						sourceAddress,
						destAddrTon,
						destAddrNpi,
						destAddress,
						new ESMClass(MessageMode.DEFAULT, MessageType.SMSC_DEL_RECEIPT, GSMSpecificFeature.DEFAULT),
						(byte)0,
						(byte)0,
						new RegisteredDelivery(0),
						DataCodings.ZERO,
						delRec.toString().getBytes());
                logger.debug("Delivery receipt sent");
			} catch (Exception e) {
				logger.error("Failed sending delivery_receipt for message id " + messageId + ":" + stringValue, e);
			}
		}
	}

	private static class MessageForwardTask implements Runnable {
		private final SubmitSm submitSm;
		private final SMPPServerSession destination;

		public MessageForwardTask(SubmitSm submitSm, Map<SMPPServerSession,String> connectedSessionMap) {
			this.submitSm = submitSm;

			// I choose possible receiver
			final List<SMPPServerSession> possibleReceivers = new ArrayList<SMPPServerSession>();
			final String destAddress = submitSm.getDestAddress();
			if (destAddress != null) {
				for (SMPPServerSession receiver : connectedSessionMap.keySet()) {
					if (receiver.getSessionState().isReceivable()) {
						// the connected session can receive and address match
						if (destAddressMatch(destAddress, connectedSessionMap.get(receiver))) {
							possibleReceivers.add(receiver);
						}
					}
				}
			}
			if(possibleReceivers.size() > 1)
				logger.warn("There are {} receivers for number {}. This may have unintended result in your test"
						, possibleReceivers.size(), submitSm.getDestAddress());
			this.destination = possibleReceivers.isEmpty() ? null : possibleReceivers.get(0);
		}

		private boolean destAddressMatch(String destAddress, String receiverAddressRange) {
			if(receiverAddressRange.equals(""))
				return false; // not listening to any address
			if (destAddress.equals(receiverAddressRange))
				return true;
			String[] listeningAddressRange = receiverAddressRange.split(",");
			for (String addr : listeningAddressRange) {
				if(destAddress.equals(addr))
					return true;
				if(destAddress.matches(addr))
					return true;
			}
			return false;
		}

		public void run() {
			if (destination == null) {
				// no receiver listening for message, so nothing to do here
				return;
			}

			try {
				final byte[] message = submitSm.getShortMessage();
				logger.debug("Forwards incoming message {} to session {}. from {} to {}", new String(message),
						destination.getSessionId(), submitSm.getSourceAddr(), submitSm.getDestAddress());
				destination.deliverShortMessage("mc",
						TypeOfNumber.valueOf(submitSm.getSourceAddrTon()),
						NumberingPlanIndicator.valueOf(submitSm.getSourceAddrNpi()),
						submitSm.getSourceAddr(),
						TypeOfNumber.valueOf(submitSm.getDestAddrTon()),
						NumberingPlanIndicator.valueOf(submitSm.getDestAddrNpi()),
						submitSm.getDestAddress(),
						new ESMClass(MessageMode.DEFAULT, MessageType.DEFAULT, GSMSpecificFeature.DEFAULT),
						(byte) 0,
						(byte) 0,
						new RegisteredDelivery(0),
						DataCodings.ZERO,
						message);
			} catch (Exception e) {
				logger.error("Fail forwarding message to consumer: " + destination.getSessionId(), e);
			}
		}
	}

	@PostConstruct
	public void onPostConstruct() {
        if (initServerAtStart) {
            startServer();
        }
    }

    public void startServer() {
        logger.debug("Starting mock SMPP server");
        execService.submit(this);
    }

    public void restartServer() throws InterruptedException, IOException {
        logger.debug("Stopping server");
        stop();
        connectionSessionMap.clear();
        startServer();
    }

	@PreDestroy
    public void onDestroy() throws InterruptedException, IOException {
		logger.debug("Destroying mock SMPP server");
        stop();
		connectionSessionMap.clear();
	}
}
