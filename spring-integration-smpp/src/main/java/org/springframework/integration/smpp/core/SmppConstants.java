package org.springframework.integration.smpp.core;

/**
 *  
 *
 * @author Josh Long
 */
public abstract class SmppConstants {

	public static final String SMS="SMS", SRC_ADDR = "SRC_ADDRESS", DST_ADDR =  "DEST_ADDRESS", SMS_MSG = "SMS_SHORT_MESSAGE";

	public static final String REGISTERED_DELIVERY_MODE = "REGISTERED_DELIVERY_MODE", REPLACE_IF_PRESENT_FLAG = "REPLACE_IF_PRESENT_FLAG";
	public static final String SRC_TON = "SRC_ADDR_TON", DST_TON = "DST_ADDR_TON", DST_NPI = "DST_ADDR_NPI", SRC_NPI = "SRC_ADDR_NPI";
	public static final String SCHEDULED_DELIVERY_TIME = "SCHEDULED_DELIVERY_TIME";
	public static final String SOURCE_ADDR_NPI = "SOURCE_ADDR_NPI";
	public static final String PRIORITY_FLAG = "PRIORITY_FLAG";
	public static final String COMMAND_LENGTH = "COMMAND_LENGTH";
	public static final String UDHI_AND_REPLY_PATH = "UDHI_AND_REPLY_PATH";
	public static final String SEQUENCE_NUMBER = "SEQUENCE_NUMBER";
	public static final String SME_MANUAL_ACK_REQUESTED = "SME_MANUAL_ACK_REQUESTED";
	public static final String DEST_ADDR_TON = "DEST_ADDR_TON";
	public static final String COMMAND_ID = "COMMAND_ID";
	public static final String SME_DELIVERY_AND_MANUAL_ACK_REQUESTED = "SME_DELIVERY_AND_MANUAL_ACK_REQUESTED";
	public static final String VALIDITY_PERIOD = "VALIDITY_PERIOD";
	public static final String SOURCE_ADDR = SRC_ADDR;
	public static final String ESM_CLASS = "ESM_CLASS";
	public static final String PROTOCOL_ID = "PROTOCOL_ID";
	public static final String SERVICE_TYPE = "SERVICE_TYPE";
	public static final String COMMAND_ID_AS_HEX = "COMMAND_ID_AS_HEX";
	public static final String CONVERSATION_ABORT = "CONVERSATION_ABORT";
	public static final String SME_ACK_NOT_REQUESTED = "SME_ACK_NOT_REQUESTED";
	public static final String DEST_ADDR_NPI = "DEST_ADDR_NPI";
	public static final String REPLACE_IF_PRESENT = "REPLACE_IF_PRESENT";
	public static final String SMSC_DELIVERY_RECEIPT = "SMSC_DELIVERY_RECEIPT";
	public static final String INTERMEDIATE_DELIVERY_NOTIFICATION = "INTERMEDIATE_DELIVERY_NOTIFICATION";
	public static final String REGISTERED_DELIVERY = "REGISTERED_DELIVERY";
//	public static final String SHORT_MESSAGE_AS_DELIVERY_RECEIPT = "SHORT_MESSAGE_AS_DELIVERY_RECEIPT";
	public static final String SCHEDULE_DELIVERY_TIME = "SCHEDULE_DELIVERY_TIME";
	public static final String COMMAND_STATUS = "COMMAND_STATUS";
	public static final String SHORT_MESSAGE = "SHORT_MESSAGE";
	public static final String SME_MANUAL_ACKNOWLEDGMENT = "SME_MANUAL_ACKNOWLEDGMENT";
	public static final String COMMAND_STATUS_AS_HEX = "COMMAND_STATUS_AS_HEX";
	public static final String UDHI = "UDHI";
	public static final String SME_DELIVERY_ACK_REQUESTED = "SME_DELIVERY_ACK_REQUESTED";
	public static final String DATA_CODING = "DATA_CODING";
	public static final String SOURCE_ADDR_TON = "SOURCE_ADDR_TON";
	public static final String DEFAULT_MESSAGE_TYPE = "DEFAULT_MESSAGE_TYPE";
	public static final String SM_DEFAULT_MSG_ID = "SM_DEFAULT_MSG_ID";
	public static final String REPLY_PATH = "REPLY_PATH";
	public static final String DEST_ADDRESS = DST_ADDR;
	public static final String OPTIONAL_PARAMETES = "OPTIONAL_PARAMETES";
}
