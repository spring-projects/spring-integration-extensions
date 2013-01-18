package org.springframework.integration.smpp;

import org.jsmpp.PDUStringException;
import org.jsmpp.SMPPConstant;
import org.jsmpp.bean.*;
import org.jsmpp.extra.ProcessRequestException;
import org.jsmpp.extra.SessionState;
import org.jsmpp.session.*;
import org.jsmpp.util.DeliveryReceiptState;
import org.jsmpp.util.MessageIDGenerator;
import org.jsmpp.util.MessageId;
import org.jsmpp.util.RandomMessageIDGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

/**
 * This is mock SMPP server connection which copied from jsmpp-examples (git clone https://github.com/otnateos/jsmpp.git)
 * with additional functionality:
 * <ul>
 *     <li>Forward incoming submit_sm to client connected as Receiver/Transceiver</li>
 * </ul>
 * @author Johanes Soetanto
 * @since 2.2
 */
public class MockSmppServer extends ServerResponseDeliveryAdapter implements Runnable, ServerMessageReceiverListener {
    private static final Logger logger = LoggerFactory.getLogger(MockSmppServer.class);
    private String systemId;
    private String password;
    private int port;
    private Map<SMPPServerSession,String> connectionSessionMap = new HashMap<SMPPServerSession,String>();

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
            SMPPServerSessionListener sessionListener = new SMPPServerSessionListener(port);
            logger.info("Listening on port {}", port);
            while (true) {
                SMPPServerSession serverSession = sessionListener.accept();
                logger.info("Accepting connection for session {}", serverSession.getSessionId());
                serverSession.setMessageReceiverListener(this);
                serverSession.setResponseDeliveryListener(this);
                execService.execute(new WaitBindTask(serverSession, systemId, password, connectionSessionMap));
            }
        } catch (IOException e) {
            logger.error("IO error occurred", e);
        }
    }

    public QuerySmResult onAcceptQuerySm(QuerySm querySm,
                                         SMPPServerSession source) throws ProcessRequestException {
        logger.info("Accepting query sm, but not implemented");
        return null;
    }

    public MessageId onAcceptSubmitSm(SubmitSm submitSm,
                                      SMPPServerSession source) throws ProcessRequestException {
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
                    } else {
                        logger.error("Invalid systemId/password");
                        bindRequest.reject(SMPPConstant.STAT_ESME_RINVPASWD);
                    }
                } catch (PDUStringException e) {
                    logger.error("Invalid system id", e);
                    bindRequest.reject(SMPPConstant.STAT_ESME_RSYSERR);
                }

            } catch (IllegalStateException e) {
                logger.error("System error", e);
            } catch (TimeoutException e) {
                logger.warn("Wait for bind has reach timeout", e);
            } catch (IOException e) {
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
                Thread.sleep(1000);
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
                logger.debug("Sending delivery receipt for message id " + messageId + ":" + stringValue);
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
                logger.debug("Forwards incoming message {} to session {}. from {} to {}",
                        new String[] {new String(message), destination.getSessionId(),
                                submitSm.getSourceAddr(), submitSm.getDestAddress()});
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
        logger.debug("Starting mock SMPP server");
        execService.submit(this);
    }

    @PreDestroy
    public void onDestroy() {
        logger.debug("Destroying mock SMPP server");
        connectionSessionMap.clear();
    }
}
