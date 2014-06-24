/*
 * Copyright 2002-2014 the original author or authors.
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

import static org.springframework.integration.smpp.core.SmppConstants.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsmpp.bean.Alphabet;
import org.jsmpp.bean.DataCoding;
import org.jsmpp.bean.DataCodings;
import org.jsmpp.bean.DeliverSm;
import org.jsmpp.bean.ESMClass;
import org.jsmpp.bean.GSMSpecificFeature;
import org.jsmpp.bean.GeneralDataCoding;
import org.jsmpp.bean.MessageClass;
import org.jsmpp.bean.NumberingPlanIndicator;
import org.jsmpp.bean.OptionalParameter;
import org.jsmpp.bean.OptionalParameters;
import org.jsmpp.bean.RegisteredDelivery;
import org.jsmpp.bean.SMSCDeliveryReceipt;
import org.jsmpp.bean.TypeOfNumber;
import org.jsmpp.session.ClientSession;
import org.jsmpp.session.SMPPSession;
import org.jsmpp.util.AbsoluteTimeFormatter;
import org.jsmpp.util.TimeFormatter;

import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Fluent API to help make specifying all these parameters just a <em>tiny</em> bit easier.
 * For internal use only.
 *
 * @author Josh Long
 * @author Edge Dalmacio
 * @since 1.0
 */
public class SmesMessageSpecification {

	private static Log log = LogFactory.getLog(SmesMessageSpecification.class);

	private TimeFormatter timeFormatter = new AbsoluteTimeFormatter();

	private int maxLengthSmsMessages = 140;

	private String sourceAddress;

	private String destinationAddress;

	private String serviceType;

	private TypeOfNumber sourceAddressTypeOfNumber;

	private NumberingPlanIndicator sourceAddressNumberingPlanIndicator;

	private TypeOfNumber destinationAddressTypeOfNumber;

	private NumberingPlanIndicator destinationAddressNumberingPlanIndicator;

	private ESMClass esmClass;

	private byte protocolId;

	private byte priorityFlag;

	private String scheduleDeliveryTime = timeFormatter.format(new Date());

	private String validityPeriod;

	private RegisteredDelivery registeredDelivery;

	private byte replaceIfPresentFlag;

	private DataCoding dataCoding;

	private byte smDefaultMsgId;

	private byte[] shortMessage;

	private List<byte[]> shortMessageParts;

	private ClientSession smppSession;

	private OptionalParameter messagePayloadParameter;

	/**
	 * This method takes an inbound SMS message and converts it to a Spring Integration message
	 * @param dsm the {@link DeliverSm} from
	 * {@link AbstractReceivingMessageListener#onTextMessage(org.jsmpp.bean.DeliverSm, String)}
	 * @param txtMessage the String from
	 * {@link AbstractReceivingMessageListener#onTextMessage(org.jsmpp.bean.DeliverSm, String)}
	 * @return a Spring Integration message
	 */
	public static Message<?> toMessageFromSms(DeliverSm dsm, String txtMessage) {

		Assert.isTrue(!dsm.isSmscDeliveryReceipt(), "the message should not be a delivery confirmation receipt!");

		MessageBuilder<String> mb = MessageBuilder.withPayload(txtMessage);
		mb.setHeader(SmppConstants.SMS, dsm);
		mb.setHeader(SmppConstants.REPLACE_IF_PRESENT, dsm.getReplaceIfPresent());
		mb.setHeader(SmppConstants.SHORT_MESSAGE, dsm.getShortMessage());
		mb.setHeader(SmppConstants.OPTIONAL_PARAMETERS, dsm.getOptionalParameters());
		mb.setHeader(SmppConstants.UDHI_AND_REPLY_PATH, dsm.isUdhiAndReplyPath());
		mb.setHeader(SmppConstants.VALIDITY_PERIOD, dsm.getValidityPeriod());
		mb.setHeader(SmppConstants.COMMAND_LENGTH, dsm.getCommandLength());
		mb.setHeader(SmppConstants.COMMAND_ID, dsm.getCommandId());
		mb.setHeader(SmppConstants.SME_ACK_NOT_REQUESTED, dsm.isSmeAckNotRequested());
		mb.setHeader(SmppConstants.DATA_CODING, dsm.getDataCoding());
		mb.setHeader(SmppConstants.REPLY_PATH, dsm.isReplyPath());
		mb.setHeader(SmppConstants.SOURCE_ADDR_TON, dsm.getSourceAddrTon());
		mb.setHeader(SmppConstants.SM_DEFAULT_MSG_ID, dsm.getSmDefaultMsgId());
		mb.setHeader(SmppConstants.UDHI, dsm.isUdhi());
		mb.setHeader(SmppConstants.SME_MANUAL_ACKNOWLEDGMENT, dsm.isSmeManualAcknowledgment());
		mb.setHeader(SmppConstants.CONVERSATION_ABORT, dsm.isConversationAbort());
		mb.setHeader(SmppConstants.DEST_ADDRESS, dsm.getDestAddress());
		mb.setHeader(SmppConstants.ESM_CLASS, dsm.getEsmClass());
		mb.setHeader(SmppConstants.COMMAND_ID_AS_HEX, dsm.getCommandIdAsHex());
		mb.setHeader(SmppConstants.SME_DELIVERY_AND_MANUAL_ACK_REQUESTED, dsm.isSmeDeliveryAndManualAckRequested());
		mb.setHeader(SmppConstants.SMSC_DELIVERY_RECEIPT, dsm.isSmscDeliveryReceipt());
		mb.setHeader(SmppConstants.SME_MANUAL_ACK_REQUESTED, dsm.isSmeManualAckRequested());
		mb.setHeader(SmppConstants.PRIORITY_FLAG, dsm.getPriorityFlag());
		mb.setHeader(SmppConstants.DEST_ADDR_TON, dsm.getDestAddrTon());
		mb.setHeader(SmppConstants.COMMAND_STATUS_AS_HEX, dsm.getCommandStatusAsHex());
		mb.setHeader(SmppConstants.SERVICE_TYPE, dsm.getServiceType());
		mb.setHeader(SmppConstants.INTERMEDIATE_DELIVERY_NOTIFICATION, dsm.isIntermedietDeliveryNotification());
		mb.setHeader(SmppConstants.SOURCE_ADDR_NPI, dsm.getSourceAddrNpi());
		mb.setHeader(SmppConstants.REGISTERED_DELIVERY, dsm.getRegisteredDelivery());
		mb.setHeader(SmppConstants.DEST_ADDR_NPI, dsm.getDestAddrNpi());
		mb.setHeader(SmppConstants.COMMAND_STATUS, dsm.getCommandStatus());
		mb.setHeader(SmppConstants.DEFAULT_MESSAGE_TYPE, dsm.isDefaultMessageType());
		mb.setHeader(SmppConstants.PROTOCOL_ID, dsm.getProtocolId());
		mb.setHeader(SmppConstants.SOURCE_ADDR, dsm.getSourceAddr());
		mb.setHeader(SmppConstants.SEQUENCE_NUMBER, dsm.getSequenceNumber());
		mb.setHeader(SmppConstants.SCHEDULE_DELIVERY_TIME, dsm.getScheduleDeliveryTime());
		mb.setHeader(SmppConstants.SME_DELIVERY_ACK_REQUESTED, dsm.isSmeDeliveryAckRequested());
		return mb.build();
	}

	/**
	 * this method will take an inbound Spring Integration {@link Message}
	 * and map it to a {@link SmesMessageSpecification}
	 * which we can use to send the SMS message.
	 * @param msg a new {@link Message}
	 * @param smppSession the SMPPSession
	 * @return a {@link SmesMessageSpecification}
	 */
	public static SmesMessageSpecification fromMessage(ClientSession smppSession, Message<?> msg) {
		if (log.isDebugEnabled()) {
			log.debug("Message: " + msg);
		}
		String srcAddy = valueIfHeaderExists(SRC_ADDR, msg);
		String dstAddy = valueIfHeaderExists(DST_ADDR, msg);
		String smsTxt = valueIfHeaderExists(SMS_MSG, msg);
		if (!StringUtils.hasText(smsTxt)) {
			Object payload = msg.getPayload();
			if (payload instanceof String) {
				smsTxt = (String) payload;
			}
		}
		final DataCoding dataCodingFromHeader = SmesMessageSpecification.dataCodingFromHeader(msg);
		final SmesMessageSpecification spec = new SmesMessageSpecification()
				.reset()
				.setSmppSession(smppSession)
				.setSourceAddress(srcAddy)
				.setDestinationAddress(dstAddy)
				.setDataCoding(dataCodingFromHeader);
		spec.setMaxLengthSmsMessages(maximumCharactersFromHeader(msg));
		spec.setEsmClass(SmesMessageSpecification.esmClassFromHeader(msg));
		if (msg.getHeaders().containsKey(SmppConstants.USE_MSG_PAYLOAD_PARAM)) {
			spec.setShortMessageUsingPayload(smsTxt);
		}
		else {
			spec.setShortTextMessage(smsTxt);
		}
		spec.setDestinationAddressNumberingPlanIndicator(
				SmesMessageSpecification.<NumberingPlanIndicator>valueIfHeaderExists(DST_NPI, msg));
		spec.setSourceAddressNumberingPlanIndicator(
				SmesMessageSpecification.<NumberingPlanIndicator>valueIfHeaderExists(SRC_NPI, msg));
		spec.setDestinationAddressTypeOfNumber(SmesMessageSpecification.<TypeOfNumber>valueIfHeaderExists(DST_TON, msg));
		spec.setSourceAddressTypeOfNumber(SmesMessageSpecification.<TypeOfNumber>valueIfHeaderExists(SRC_TON, msg));
		spec.setServiceType(SmesMessageSpecification.<String>valueIfHeaderExists(SERVICE_TYPE, msg));
		spec.setScheduleDeliveryTime(SmesMessageSpecification.<Date>valueIfHeaderExists(SCHEDULED_DELIVERY_TIME, msg));
		spec.setValidityPeriod(SmesMessageSpecification.<String>valueIfHeaderExists(VALIDITY_PERIOD, msg));

		// byte landmine. autoboxing causes havoc with <em>null</em> bytes.
		Byte priorityFlag1 = SmesMessageSpecification.<Byte>valueIfHeaderExists(PRIORITY_FLAG, msg);
		if (priorityFlag1 != null) {
			spec.setPriorityFlag(priorityFlag1);
		}

		Byte smDefaultMsgId1 = SmesMessageSpecification.<Byte>valueIfHeaderExists(SM_DEFAULT_MSG_ID, msg);
		if (smDefaultMsgId1 != null) {
			spec.setSmDefaultMsgId(smDefaultMsgId1);
		}

		Byte replaceIfPresentFlag1 = SmesMessageSpecification.<Byte>valueIfHeaderExists(REPLACE_IF_PRESENT_FLAG, msg);
		if (replaceIfPresentFlag1 != null) {
			spec.setReplaceIfPresentFlag(replaceIfPresentFlag1);
		}

		Byte protocolId1 = SmesMessageSpecification.<Byte>valueIfHeaderExists(PROTOCOL_ID, msg);
		if (null != protocolId1) {
			spec.setProtocolId(protocolId1);
		}

		spec.setRegisteredDelivery(registeredDeliveryFromHeader(msg));

		return spec;
	}

	private static DataCoding dataCodingFromHeader(Message<?> msg) {
		Object dc = msg.getHeaders().get(DATA_CODING);
		if (dc instanceof DataCoding) {
			return (DataCoding) dc;
		}
		if (dc instanceof Byte) {
			return DataCodings.newInstance((Byte) dc);
		}

		return null;
	}

	/**
	 * Getting maximum characters from header. This will allow checking maximum character based on
	 * {@link SmppConstants#MAXIMUM_CHARACTERS} header or determine the maximum character based on data coding
	 * header {@link SmppConstants#DATA_CODING}.
	 * <p/>
	 * The order of the selection is
	 * <ol>
	 *     <li>If {@link SmppConstants#MAXIMUM_CHARACTERS} is set, use it</li>
	 *     <li>If {@link SmppConstants#DATA_CODING} is set, find maximum character for the data coding</li>
	 *     <li>Using default maximum character which is 140</li>
	 * </ol>
	 * @param msg the Spring Integration message
	 * @return maximum character can be sent through the session
	 */
	private static int maximumCharactersFromHeader(Message<?> msg) {
		if (msg.getHeaders().containsKey(MAXIMUM_CHARACTERS)) {
			return msg.getHeaders().get(MAXIMUM_CHARACTERS, Integer.class);
		}
		if (msg.getHeaders().containsKey(DATA_CODING)) {
			final Object dc = msg.getHeaders().get(DATA_CODING);
			if (dc instanceof Byte) {
				return DataCodingSpecification.getMaxCharacters((Byte) dc);
			}
			else {
				return DataCodingSpecification.getMaxCharacters(((DataCoding) dc).toByte());
			}
		}
		return 140;
	}

	/**
	 * need to be a little flexibile about what we take in as
	 * {@link SmppConstants#REGISTERED_DELIVERY_MODE}. The value can
	 * be a String or a member of the {@link SMSCDeliveryReceipt} enum.
	 * @param msg the Spring Integration message
	 * @return a value for {@link RegisteredDelivery} or null, which is
	 * good because it'll simply let the existing default work
	 */
	private static RegisteredDelivery registeredDeliveryFromHeader(Message<?> msg) {
		Object rd = valueIfHeaderExists(REGISTERED_DELIVERY_MODE, msg);

		if (rd instanceof String) {
			String rdString = (String) rd;
			SMSCDeliveryReceipt smscDeliveryReceipt = SMSCDeliveryReceipt.valueOf(rdString);
			Assert.notNull(smscDeliveryReceipt, "the registeredDelivery can't be null");
			return new RegisteredDelivery(smscDeliveryReceipt);
		}

		if (rd instanceof SMSCDeliveryReceipt) {
			SMSCDeliveryReceipt smscDeliveryReceipt = (SMSCDeliveryReceipt) rd;
			return new RegisteredDelivery(smscDeliveryReceipt);
		}

		if (rd instanceof RegisteredDelivery) {
			return (RegisteredDelivery) rd;
		}
		return null;
	}

	/**
	 * you need to use the builder API
	 *
	 * @param smppSession the SMPPSession instance against which we should work.
	 * @see SmesMessageSpecification#SmesMessageSpecification()
	 */
	SmesMessageSpecification(SMPPSession smppSession) {
		this.smppSession = smppSession;
	}

	/**
	 * tries to safely extract the ESMClass
	 * @param im message
	 * @return esm class
	 */
	static private ESMClass esmClassFromHeader(Message<?> im) {
		String h = ESM_CLASS;
		Object o = valueIfHeaderExists(h, im);
		ESMClass response = null;
		if (o instanceof Byte) {
			response = new ESMClass((Byte) o);

		}
		else if (o instanceof ESMClass) {
			response = (ESMClass) o;
		}
		return response;
	}

	@SuppressWarnings("unchecked")
	static private <T> T valueIfHeaderExists(String h, Message<?> msg) {
		if (msg != null && msg.getHeaders().containsKey(h)) {
			return (T) msg.getHeaders().get(h);
		}
		return null;
	}

	/**
	 * Everybody else has to use the builder API. DO NOT make this private
	 * or it will not be proxied and that will make me sad!
	 * @param ss the {@link SMPPSession}
	 * @return the current spec
	 */
	SmesMessageSpecification setSmppSession(ClientSession ss) {
		this.smppSession = ss;
		return this;
	}

	/**
	 * use the builder API, but we need this to cleanly proxy
	 */
	SmesMessageSpecification() {
		this(null);
	}

	/**
	 * Conceptually, you could get away with just specifying these three parameters,
	 * though I don't know how likely that is in practice.
	 * @param srcAddress    the source address
	 * @param destAddress the destination address
	 * @param txtMessage    the message to send (must be  no more than 140 characters
	 * @param ss the SMPPSession
	 * @return the {@link SmesMessageSpecification}
	 */
	public static SmesMessageSpecification newSmesMessageSpecification(ClientSession ss, String srcAddress,
			String destAddress, String txtMessage) {

		SmesMessageSpecification smesMessageSpecification = new SmesMessageSpecification();

		smesMessageSpecification
				.reset()
				.setSmppSession(ss)
				.setSourceAddress(srcAddress)
				.setDestinationAddress(destAddress)
				.setShortTextMessage(txtMessage);

		return smesMessageSpecification;
	}

	/**
	 * Only sets the #sourceAddressTypeOfNumber if the current value is null, otherwise, it leaves it.
	 * @param sourceAddressTypeOfNumberIfRequired
	 *         the {@link TypeOfNumber}
	 * @return this
	 */
	public
	SmesMessageSpecification setSourceAddressTypeOfNumberIfRequired(TypeOfNumber sourceAddressTypeOfNumberIfRequired) {
		if (this.sourceAddressTypeOfNumber == null) {
			this.sourceAddressTypeOfNumber = sourceAddressTypeOfNumberIfRequired;
		}
		return this;
	}

	/**
	 * send the message on its way.
	 * <p/>
	 * todo can we do something smart here or through an adapter to handle the situation
	 * where we have asked for a message receipt? what about if we're using a message
	 * receipt <em>and</em> we're only a receiver or a sender connection and not a transceiver?
	 * We need gateway semantics across two unidirectional SMPPSessions, then
	 * @return the messageId(s) (required if you want to then track it
	 * or correlate it with message receipt confirmations)
	 * @throws Exception the
	 * {@link SMPPSession#submitShortMessage(String, org.jsmpp.bean.TypeOfNumber,
	 * org.jsmpp.bean.NumberingPlanIndicator, String, org.jsmpp.bean.TypeOfNumber,
	 * org.jsmpp.bean.NumberingPlanIndicator, String, org.jsmpp.bean.ESMClass,
	 * byte, byte, String, String, org.jsmpp.bean.RegisteredDelivery, byte,
	 * org.jsmpp.bean.DataCoding, byte, byte[], org.jsmpp.bean.OptionalParameter...)}
	 * method throws lots of Exceptions, including {@link java.io.IOException}
	 */
	public List<String> send() throws Exception {
		validate();
		List<String> msgIds = new LinkedList<String>();
		if (messagePayloadParameter == null) {
			if (this.shortMessageParts.isEmpty()) {
				String msgId = this.smppSession.submitShortMessage(
						this.serviceType,
						this.sourceAddressTypeOfNumber,
						this.sourceAddressNumberingPlanIndicator,
						this.sourceAddress,

						this.destinationAddressTypeOfNumber,
						this.destinationAddressNumberingPlanIndicator,
						this.destinationAddress,

						this.esmClass,
						this.protocolId,
						this.priorityFlag,
						this.scheduleDeliveryTime,
						this.validityPeriod,
						this.registeredDelivery,
						this.replaceIfPresentFlag,
						this.dataCoding,
						this.smDefaultMsgId,
						this.shortMessage);
				msgIds.add(msgId);
			}
			else {
				if (log.isDebugEnabled()) {
					log.debug("Sending message using sar_msg_ref_num, sar_segment_seqnum and sar_total_segments");
				}
				OptionalParameter sarMsgRefNum = OptionalParameters.newSarMsgRefNum(RandomUtils.nextInt(0x10000));
				OptionalParameter sarTotalSegments = OptionalParameters.newSarTotalSegments(shortMessageParts.size());
				String charsetName = DataCodingSpecification.getCharsetName(dataCoding.toByte());
				for (int i = 0; i < shortMessageParts.size(); i++) {
					byte[] shortMessagePart = shortMessageParts.get(i);
					String msgId = this.smppSession.submitShortMessage(
							this.serviceType,
							this.sourceAddressTypeOfNumber,
							this.sourceAddressNumberingPlanIndicator,
							this.sourceAddress,

							this.destinationAddressTypeOfNumber,
							this.destinationAddressNumberingPlanIndicator,
							this.destinationAddress,

							this.esmClass,
							this.protocolId,
							this.priorityFlag,
							this.scheduleDeliveryTime,
							this.validityPeriod,
							this.registeredDelivery,
							this.replaceIfPresentFlag,
							this.dataCoding,
							this.smDefaultMsgId,
							shortMessagePart,
							sarMsgRefNum,
							OptionalParameters.newSarSegmentSeqnum(i + 1),
							sarTotalSegments);
					if (log.isDebugEnabled()) {
						log.debug("sent message : " + new String(shortMessagePart, charsetName));
						log.debug("message ID for the sent message is: " + msgId);
					}
					msgIds.add(msgId);
				}
			}
		}
		else {
			// SPEC 3.2.3
			log.debug("Sending message using message_payload");
			String msgId = this.smppSession.submitShortMessage(
					this.serviceType,
					this.sourceAddressTypeOfNumber,
					this.sourceAddressNumberingPlanIndicator,
					this.sourceAddress,

					this.destinationAddressTypeOfNumber,
					this.destinationAddressNumberingPlanIndicator,
					this.destinationAddress,

					this.esmClass,
					this.protocolId,
					this.priorityFlag,
					this.scheduleDeliveryTime,
					this.validityPeriod,
					this.registeredDelivery,
					this.replaceIfPresentFlag,
					this.dataCoding,
					this.smDefaultMsgId,
					new byte[0],
					this.messagePayloadParameter
			);
			msgIds.add(msgId);
		}

		return Collections.unmodifiableList(msgIds);
	}

	protected void validate() {
		Assert.notNull(this.sourceAddress, "the source address must not be null");
		Assert.notNull(this.destinationAddress, "the destination address must not be null");
		final boolean shortMessageSet = this.shortMessage != null && this.shortMessage.length > 0
				|| !shortMessageParts.isEmpty();
		Assert.isTrue(messagePayloadParameter != null ^ shortMessageSet,
				"message can only be set in payload or short message. cannot be both");
		if (messagePayloadParameter == null) {
			Assert.isTrue(shortMessageSet, "the message must not be null");
		}
	}

	public SmesMessageSpecification setSourceAddress(String sourceAddr) {
		if (!nullHeaderWillOverwriteDefault(sourceAddr)) {
			this.sourceAddress = sourceAddr;
		}
		return this;
	}

	/**
	 * the 'to' phone number
	 *
	 * @param destinationAddr the phone number
	 * @return the current spec
	 */
	public SmesMessageSpecification setDestinationAddress(String destinationAddr) {
		this.destinationAddress = destinationAddr;
		return this;
	}

	public SmesMessageSpecification setServiceType(String serviceType) {
		if (!nullHeaderWillOverwriteDefault(serviceType)) {
			this.serviceType = serviceType;
		}
		return this;
	}

	public SmesMessageSpecification setSourceAddressTypeOfNumber(TypeOfNumber sourceAddrTon) {
		if (!nullHeaderWillOverwriteDefault(sourceAddrTon)) {
			this.sourceAddressTypeOfNumber = sourceAddrTon;
		}
		return this;
	}

	public SmesMessageSpecification setSourceAddressNumberingPlanIndicator(NumberingPlanIndicator sourceAddrNpi) {
		if (!nullHeaderWillOverwriteDefault(sourceAddrNpi)) {
			this.sourceAddressNumberingPlanIndicator = sourceAddrNpi;
		}
		return this;
	}

	public SmesMessageSpecification setDestinationAddressTypeOfNumber(TypeOfNumber destAddrTon) {
		if (!nullHeaderWillOverwriteDefault(destAddrTon)) {
			this.destinationAddressTypeOfNumber = destAddrTon;
		}
		return this;
	}

	/**
	 * guard against overwriting perfectly good defaults with null values.
	 *
	 * @param v value the value
	 * @return can the write proceed unabated?
	 */
	private boolean nullHeaderWillOverwriteDefault(Object v) {
		if (v == null) {
			if (log.isDebugEnabled()) {
				log.debug("There is a default in place for this property; don't overwrite it with null");
			}
			return true;
		}
		return false;
	}

	public SmesMessageSpecification setDestinationAddressNumberingPlanIndicator(NumberingPlanIndicator destAddrNpi) {
		if (!nullHeaderWillOverwriteDefault(destAddrNpi)) {
			this.destinationAddressNumberingPlanIndicator = destAddrNpi;
		}
		return this;
	}

	public SmesMessageSpecification setEsmClass(ESMClass esmClass) {
		if (!nullHeaderWillOverwriteDefault(esmClass)) {
			this.esmClass = esmClass;
		}
		return this;
	}

	public SmesMessageSpecification setProtocolId(byte protocolId) {
		if (!nullHeaderWillOverwriteDefault(protocolId)) {
			this.protocolId = protocolId;
		}
		return this;
	}

	public SmesMessageSpecification setPriorityFlag(byte pf) {
		if (!nullHeaderWillOverwriteDefault(pf)) {
			this.priorityFlag = pf;
		}
		return this;
	}

	/**
	 * When you submit a message to an SMSC, it is possible to sometimes specify a
	 * <em>validity period</em> for the message. This setting is an instruction to
	 * the SMSC that stipulates that if the message cannot be delivered to
	 * the recipient within the next N minutes or hours or days,
	 * the SMSC should discard the message. This would mean that if the recipient'running
	 * mobile phone is turned off, or outSession of coverage for x minutes/hours/days
	 * after the message is submitted, the SMSC should not perform further delivery
	 * retry and should discard the message.
	 * <p/>
	 * Of course, there is no guarantee that the operator SMSC will respect this setting,
	 * so it needs to be tested with a particular operator first to determine if
	 * it can be used reliably.
	 * <p/>
	 * That information came from <a href="http://www.nowsms.com/smpp-information">the NowSMS website.</a>.
	 * @param v the period of validity. There are specific formats for this,
	 * however this method provides no validation.
	 *          <p/>
	 *          todo provide format validation if possible
	 * @return the current SmesMessageSpecification
	 */
	public SmesMessageSpecification setValidityPeriod(String v) {
		if (!nullHeaderWillOverwriteDefault(v)) {
			this.validityPeriod = v;
		}
		return this;
	}

	public SmesMessageSpecification setScheduleDeliveryTime(Date d) {
		if (!nullHeaderWillOverwriteDefault(d)) {
			this.scheduleDeliveryTime = timeFormatter.format(d);
		}
		return this;
	}

	public SmesMessageSpecification setRegisteredDelivery(RegisteredDelivery rd) {
		if (!nullHeaderWillOverwriteDefault(rd)) {
			this.registeredDelivery = rd;
		}
		return this;
	}

	public SmesMessageSpecification setReplaceIfPresentFlag(byte replaceIfPresentFlag) {
		if (!nullHeaderWillOverwriteDefault(replaceIfPresentFlag)) {
			this.replaceIfPresentFlag = replaceIfPresentFlag;
		}
		return this;
	}

	public SmesMessageSpecification setDataCoding(DataCoding dataCoding) {
		if (!nullHeaderWillOverwriteDefault(dataCoding)) {
			this.dataCoding = dataCoding;
		}
		return this;
	}

	public SmesMessageSpecification setSmDefaultMsgId(byte smDefaultMsgId) {
		this.smDefaultMsgId = smDefaultMsgId;
		return this;
	}

	public SmesMessageSpecification setTimeFormatter(TimeFormatter timeFormatter) {
		if (!nullHeaderWillOverwriteDefault(timeFormatter)) {
			this.timeFormatter = timeFormatter;
		}
		return this;
	}

	/**
	 * Setting short message. This will take into account if {@link #dataCoding}
	 * or if {@link #maxLengthSmsMessages}
	 * is set through header to validate the maximum characters can be set.
	 * @param s the text message body
	 * @return the SmesMessageSpecification
	 */
	public SmesMessageSpecification setShortTextMessage(String s) {
		Assert.notNull(s, "the SMS message payload must not be null");
		if (esmClass != null && GSMSpecificFeature.UDHI.containedIn(esmClass)) {
			log.debug("Setting short message with UDH");
			this.shortMessage = UdhUtil.getMessageWithUdhInBytes(s, dataCoding.toByte());
		}
		else {
			if (s.length() > this.maxLengthSmsMessages) {
				for (String split : s.split("(?<=\\G.{" + String.valueOf(this.maxLengthSmsMessages - 5) + "})")) {
					this.shortMessageParts.add(DataCodingSpecification.getMessageInBytes(split, dataCoding.toByte()));
				}
			}
			else {
				this.shortMessage = DataCodingSpecification.getMessageInBytes(s, dataCoding.toByte());
			}
		}
		return this;
	}

	/**
	 * Setting short message using message_payload ({@link org.jsmpp.bean.OptionalParameter.Tag#MESSAGE_PAYLOAD})
	 * optional parameter
	 * @param s the text messages body
	 * @return the SmesMessageSpecification
	 */
	public SmesMessageSpecification setShortMessageUsingPayload(String s) {
		final byte[] content = DataCodingSpecification.getMessageInBytes(s, dataCoding.toByte());
		this.messagePayloadParameter =
				new OptionalParameter.OctetString(OptionalParameter.Tag.MESSAGE_PAYLOAD.code(), content);
		return this;
	}

	/**
	 * this is a good value, but not strictly speaking universal.
	 * This is intended only for exceptional configuration cases
	 * <p/>
	 * See: http://www.nowsms.com/long-sms-text-messages-and-the-160-character-limit
	 *
	 * @param maxLengthSmsMessages the length of sms messages
	 * @see #setShortTextMessage(String)
	 */
	public void setMaxLengthSmsMessages(int maxLengthSmsMessages) {
		this.maxLengthSmsMessages = maxLengthSmsMessages;
	}

	/**
	 * Resets the thread local, pooled objects to a known state before reuse.
	 * <p/>
	 * Resetting the variables is trivially cheap compared to proxying a new one each time.
	 *
	 * @return the cleaned up {@link SmesMessageSpecification}
	 */
	protected SmesMessageSpecification reset() {

		// configuration params - should they be reset?
		maxLengthSmsMessages = 140;
		timeFormatter = new AbsoluteTimeFormatter();

		sourceAddress = null;
		destinationAddress = null;
		serviceType = "CMT";
		sourceAddressTypeOfNumber = TypeOfNumber.UNKNOWN;
		sourceAddressNumberingPlanIndicator = NumberingPlanIndicator.UNKNOWN;
		destinationAddressTypeOfNumber = TypeOfNumber.UNKNOWN;
		destinationAddressNumberingPlanIndicator = NumberingPlanIndicator.UNKNOWN;
		esmClass = new ESMClass();
		protocolId = 0;
		priorityFlag = 1;
		scheduleDeliveryTime = null;
		validityPeriod = null;
		registeredDelivery = new RegisteredDelivery(SMSCDeliveryReceipt.DEFAULT);
		replaceIfPresentFlag = 0;
		dataCoding = new GeneralDataCoding(Alphabet.ALPHA_DEFAULT, MessageClass.CLASS1, false);
		smDefaultMsgId = 0;
		shortMessage = null; // the bytes to the 140 character text message
		shortMessageParts = new ArrayList<byte[]>();
		smppSession = null;
		messagePayloadParameter = null;
		return this;
	}

	public SmesMessageSpecification setSourceAddressIfRequired(String defaultSourceAddress) {
		if (!StringUtils.hasText(this.sourceAddress)) {
			this.sourceAddress = defaultSourceAddress;
		}
		return this;
	}

}

/*	private static String fromPropertyToHeaderConstant(String n) {

		StringBuffer stringBuffer = new StringBuffer();

		for (char c : n.toCharArray()) {
			if (Character.isUpperCase(c)) {
				stringBuffer.append("_");
			}
			stringBuffer.append(c);
		}

		String nn = stringBuffer.toString().toUpperCase();

		String is = "IS_",
				get = "GET_";
		if (nn.startsWith(is)) nn = nn.substring(is.length());
		if (nn.startsWith(get)) nn = nn.substring(get.length());

		return nn;
	}

	static public void main(String[] args) throws Throwable {
		String m = "mb.setHeader( SmppConstants.%s,  dsm.%s() );";
		String h = "public static final String %s = \"%s\";";
		Set<String> marshalling = new HashSet<String>();
		Set<String> headers = new HashSet<String>();
		PropertyDescriptor[] pds = PropertyUtils.getPropertyDescriptors(DeliverSm.class);
		for (PropertyDescriptor propertyDescriptor : pds) {
			Method reader = propertyDescriptor.getReadMethod();
			String readerName = reader.getName();

			String header = fromPropertyToHeaderConstant(readerName);
			headers.add(header);
			marshalling.add(readerName + ":" + header);
		}

		for (String s : headers) System.outSession.println(String.format(h, s, s));

		for (String s : marshalling) {

			String[] tuple = s.split(":");

			System.outSession.println(String.format(m, tuple[1], tuple[0]));
		}
	}
*/
