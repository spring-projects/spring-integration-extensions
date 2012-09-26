/*
 * Copyright 2011-2012 the original author or authors.
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
package org.springframework.integration.splunk.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.time.FastDateFormat;

/**
 * Splunk data entity
 *
 * @author Jarred Li
 * @author Damien Dallimore damien@dtdsoftware.com
 * @since 1.0
 *
 */
public class SplunkData implements Serializable {

	private static final long serialVersionUID = -7369254824093658523L;


	private Map<String, String> eventData;

	/**
	 * Contents of the event message
	 */
	private StringBuffer eventMessage;

	/**
	 * Whether or not to put quotes around values
	 */
	private boolean quoteValues = true;

	/**
	 * Whether or not to add a date to the event string
	 */
	private boolean useInternalDate = true;

	/**
	 * default key value delimiter
	 */
	private static final String KVDELIM = "=";
	/**
	 * default pair delimiter
	 */
	private static final String PAIRDELIM = " ";
	/**
	 * default quote char
	 */
	private static final char QUOTE = '"';
	/**
	 * default date format is using internal generated date
	 */
	private static final String DATEFORMATPATTERN = "yyyy-MM-dd HH:mm:ss:SSSZ";
	/**
	 * Date Formatter instance
	 */
	private static FastDateFormat DATEFORMATTER = FastDateFormat.getInstance(DATEFORMATPATTERN);

	/**
	 * Event prefix fields
	 */
	private static final String PREFIX_NAME = "name";
	private static final String PREFIX_EVENT_ID = "event_id";

	/**
	 * Java Throwable type fields
	 */
	private static final String THROWABLE_CLASS = "throwable_class";
	private static final String THROWABLE_MESSAGE = "throwable_message";
	private static final String THROWABLE_STACKTRACE_ELEMENTS = "stacktrace_elements";

	/**
	 * Splunk Common Information Model(CIM) Fields
	 */

	// ------------------
	// Account management
	// ------------------

	/**
	 * The domain containing the user that is affected by the account management
	 * event.
	 */
	public static String AC_MANAGEMENT_DEST_NT_DOMAIN = "dest_nt_domain";
	/**
	 * Description of the account management change performed.
	 */
	public static String AC_MANAGEMENT_SIGNATURE = "signature";
	/**
	 * The NT source of the destination. In the case of an account management
	 * event, this is the domain that contains the user that generated the
	 * event.
	 */
	public static String AC_MANAGEMENT_SRC_NT_DOMAIN = "src_nt_domain";

	// ----------------------------------
	// Authentication - Access protection
	// ----------------------------------

	/**
	 * The action performed on the resource. success, failure
	 */
	public static String AUTH_ACTION = "action";
	/**
	 * The application involved in the event (such as ssh, spunk, win:local).
	 */
	public static String AUTH_APP = "app";
	/**
	 * The target involved in the authentication. If your field is named
	 * dest_host, dest_ip, dest_ipv6, or dest_nt_host you can alias it as dest
	 * to make it CIM-compliant.
	 */
	public static String AUTH_DEST = "dest";
	/**
	 * The source involved in the authentication. In the case of endpoint
	 * protection authentication the src is the client. If your field is named
	 * src_host, src_ip, src_ipv6, or src_nt_host you can alias it as src to
	 * make it CIM-compliant.. It is required for all events dealing with
	 * endpoint protection (Authentication, change analysis, malware, system
	 * center, and update). Note: Do not confuse this with the event source or
	 * sourcetype fields.
	 */
	public static String AUTH_SRC = "src";
	/**
	 * In privilege escalation events, src_user represents the user who
	 * initiated the privilege escalation.
	 */
	public static String AUTH_SRC_USER = "src_user";
	/**
	 * The name of the user involved in the event, or who initiated the event.
	 * For authentication privilege escalation events this should represent the
	 * user targeted by the escalation.
	 */
	public static String AUTH_USER = "user";

	// ----------------------------------
	// Change analysis - Endpoint protection
	// ----------------------------------

	/**
	 * The action performed on the resource.
	 */
	public static String CHANGE_ENDPOINT_PROTECTION_ACTION = "action";
	/**
	 * The type of change discovered in the change analysis event.
	 */
	public static String CHANGE_ENDPOINT_PROTECTION_CHANGE_TYPE = "change_type";
	/**
	 * The host that was affected by the change. If your field is named
	 * dest_host,dest_ip,dest_ipv6, or dest_nt_host you can alias it as dest to
	 * make it CIM-compliant.
	 */
	public static String CHANGE_ENDPOINT_PROTECTION_DEST = "dest";
	/**
	 * The hash signature of the modified resource.
	 */
	public static String CHANGE_ENDPOINT_PROTECTION_HASH = "hash";
	/**
	 * The group ID of the modified resource.
	 */
	public static String CHANGE_ENDPOINT_PROTECTION_GID = "gid";
	/**
	 * Indicates whether or not the modified resource is a directory.
	 */
	public static String CHANGE_ENDPOINT_PROTECTION_ISDR = "isdr";
	/**
	 * The permissions mode of the modified resource.
	 */
	public static String CHANGE_ENDPOINT_PROTECTION_MODE = "mode";
	/**
	 * The modification time of the modified resource.
	 */
	public static String CHANGE_ENDPOINT_PROTECTION_MODTIME = "modtime";
	/**
	 * The file path of the modified resource.
	 */
	public static String CHANGE_ENDPOINT_PROTECTION_PATH = "path";
	/**
	 * The size of the modified resource.
	 */
	public static String CHANGE_ENDPOINT_PROTECTION_SIZE = "size";
	/**
	 * The user ID of the modified resource.
	 */
	public static String CHANGE_ENDPOINT_PROTECTION_UID = "uid";

	// ----------------------------------
	// Change analysis - Network protection
	// ----------------------------------

	/**
	 * The type of change observed.
	 */
	public static String CHANGE_NETWORK_PROTECTION_ACTION = "action";
	/**
	 * The command that initiated the change.
	 */
	public static String CHANGE_NETWORK_PROTECTION_COMMAND = "command";
	/**
	 * The device that is directly affected by the change.
	 */
	public static String CHANGE_NETWORK_PROTECTION_DVC = "dvc";
	/**
	 * The user that initiated the change.
	 */
	public static String CHANGE_NETWORK_PROTECTION_USER = "user";

	// ----------------------------------
	// Common event fields
	// ----------------------------------

	/**
	 * A device-specific classification provided as part of the event.
	 */
	public static String COMMON_CATEGORY = "category";
	/**
	 * A device-specific classification provided as part of the event.
	 */
	public static String COMMON_COUNT = "count";
	/**
	 * The free-form description of a particular event.
	 */
	public static String COMMON_DESC = "desc";
	/**
	 * The name of a given DHCP pool on a DHCP server.
	 */
	public static String COMMON_DHCP_POOL = "dhcp_pool";
	/**
	 * The amount of time the event lasted.
	 */
	public static String COMMON_DURATION = "duration";
	/**
	 * The fully qualified domain name of the device transmitting or recording
	 * the log record.
	 */
	public static String COMMON_DVC_HOST = "dvc_host";
	/**
	 * The IPv4 address of the device reporting the event.
	 */
	public static String COMMON_DVC_IP = "dvc_ip";
	/**
	 * The IPv6 address of the device reporting the event.
	 */
	public static String COMMON_DVC_IP6 = "dvc_ip6";
	/**
	 * The free-form description of the device's physical location.
	 */
	public static String COMMON_DVC_LOCATION = "dvc_location";
	/**
	 * The MAC (layer 2) address of the device reporting the event.
	 */
	public static String COMMON_DVC_MAC = "dvc_mac";
	/**
	 * The Windows NT domain of the device recording or transmitting the event.
	 */
	public static String COMMON_DVC_NT_DOMAIN = "dvc_nt_domain";
	/**
	 * The Windows NT host name of the device recording or transmitting the
	 * event.
	 */
	public static String COMMON_DVC_NT_HOST = "dvc_nt_host";
	/**
	 * Time at which the device recorded the event.
	 */
	public static String COMMON_DVC_TIME = "dvc_time";
	/**
	 * The event's specified end time.
	 */
	public static String COMMON_END_TIME = "end_time";
	/**
	 * A unique identifier that identifies the event. This is unique to the
	 * reporting device.
	 */
	public static String COMMON_EVENT_ID = "event_id";
	/**
	 * The length of the datagram, event, message, or packet.
	 */
	public static String COMMON_LENGTH = "length";
	/**
	 * The log-level that was set on the device and recorded in the event.
	 */
	public static String COMMON_LOG_LEVEL = "log_level";
	/**
	 * The name of the event as reported by the device. The name should not
	 * contain information that's already being parsed into other fields from
	 * the event, such as IP addresses.
	 */
	public static String COMMON_NAME = "name";
	/**
	 * An integer assigned by the device operating system to the process
	 * creating the record.
	 */
	public static String COMMON_PID = "pid";
	/**
	 * An environment-specific assessment of the event's importance, based on
	 * elements such as event severity, business function of the affected
	 * system, or other locally defined variables.
	 */
	public static String COMMON_PRIORITY = "priority";
	/**
	 * The product that generated the event.
	 */
	public static String COMMON_PRODUCT = "product";
	/**
	 * The version of the product that generated the event.
	 */
	public static String COMMON_PRODUCT_VERSION = "product_version";
	/**
	 * The result root cause, such as connection refused, timeout, crash, and so
	 * on.
	 */
	public static String COMMON_REASON = "reason";
	/**
	 * The action result. Often is a binary choice: succeeded and failed,
	 * allowed and denied, and so on.
	 */
	public static String COMMON_RESULT = "result";
	/**
	 * The severity (or priority) of an event as reported by the originating
	 * device.
	 */
	public static String COMMON_SEVERITY = "severity";
	/**
	 * The event's specified start time.
	 */
	public static String COMMON_START_TIME = "start_time";
	/**
	 * The transaction identifier.
	 */
	public static String COMMON_TRANSACTION_ID = "transaction_id";
	/**
	 * A uniform record locator (a web address, in other words) included in a
	 * record.
	 */
	public static String COMMON_URL = "url";
	/**
	 * The vendor who made the product that generated the event.
	 */
	public static String COMMON_VENDOR = "vendor";

	// ----------------------------------
	// DNS protocol
	// ----------------------------------

	/**
	 * The DNS domain that has been queried.
	 */
	public static String DNS_DEST_DOMAIN = "dest_domain";
	/**
	 * The remote DNS resource record being acted upon.
	 */
	public static String DNS_DEST_RECORD = "dest_record";
	/**
	 * The DNS zone that is being received by the slave as part of a zone
	 * transfer.
	 */
	public static String DNS_DEST_ZONE = "dest_zone";
	/**
	 * The DNS resource record class.
	 */
	public static String DNS_RECORD_CLASS = "record_class";
	/**
	 * The DNS resource record type.
	 *
	 * @see <a
	 *      href="https://secure.wikimedia.org/wikipedia/en/wiki/List_of_DNS_record_types">see
	 *      this Wikipedia article on DNS record types</a>
	 */
	public static String DNS_RECORD_TYPE = "record_type";
	/**
	 * The local DNS domain that is being queried.
	 */
	public static String DNS_SRC_DOMAIN = "src_domain";
	/**
	 * The local DNS resource record being acted upon.
	 */
	public static String DNS_SRC_RECORD = "src_record";
	/**
	 * The DNS zone that is being transferred by the master as part of a zone
	 * transfer.
	 */
	public static String DNS_SRC_ZONE = "src_zone";

	// ----------------------------------
	// Email tracking
	// ----------------------------------

	/**
	 * The person to whom an email is sent.
	 */
	public static String EMAIL_RECIPIENT = "recipient";
	/**
	 * The person responsible for sending an email.
	 */
	public static String EMAIL_SENDER = "sender";
	/**
	 * The email subject line.
	 */
	public static String EMAIL_SUBJECT = "subject";

	// ----------------------------------
	// File management
	// ----------------------------------

	/**
	 * The time the file (the object of the event) was accessed.
	 */
	public static String FILE_ACCESS_TIME = "file_access_time";
	/**
	 * The time the file (the object of the event) was created.
	 */
	public static String FILE_CREATE_TIME = "file_create_time";
	/**
	 * A cryptographic identifier assigned to the file object affected by the
	 * event.
	 */
	public static String FILE_HASH = "file_hash";
	/**
	 * The time the file (the object of the event) was altered.
	 */
	public static String FILE_MODIFY_TIME = "file_modify_time";
	/**
	 * The name of the file that is the object of the event (without location
	 * information related to local file or directory structure).
	 */
	public static String FILE_NAME = "file_name";
	/**
	 * The location of the file that is the object of the event, in terms of
	 * local file and directory structure.
	 */
	public static String FILE_PATH = "file_path";
	/**
	 * Access controls associated with the file affected by the event.
	 */
	public static String FILE_PERMISSION = "file_permission";
	/**
	 * The size of the file that is the object of the event. Indicate whether
	 * Bytes, KB, MB, GB.
	 */
	public static String FILE_SIZE = "file_size";

	// ----------------------------------
	// Intrusion detection
	// ----------------------------------

	/**
	 * The category of the triggered signature.
	 */
	public static String INTRUSION_DETECTION_CATEGORY = "category";
	/**
	 * The destination of the attack detected by the intrusion detection system
	 * (IDS). If your field is named dest_host, dest_ip, dest_ipv6, or
	 * dest_nt_host you can alias it as dest to make it CIM-compliant.
	 */
	public static String INTRUSION_DETECTION_DEST = "dest";
	/**
	 * The device that detected the intrusion event.
	 */
	public static String INTRUSION_DETECTION_DVC = "dvc";
	/**
	 * The type of IDS that generated the event.
	 */
	public static String INTRUSION_DETECTION_IDS_TYPE = "ids_type";
	/**
	 * The product name of the vendor technology generating network protection
	 * data, such as IDP, Providentia, and ASA.
	 *
	 * Note: Required for all events dealing with network protection (Change
	 * analysis, proxy, malware, intrusion detection, packet filtering, and
	 * vulnerability).
	 */
	public static String INTRUSION_DETECTION_PRODUCT = "product";
	/**
	 * The severity of the network protection event (such as critical, high,
	 * medium, low, or informational).
	 *
	 * Note: This field is a string. Please use a severity_id field for severity
	 * ID fields that are integer data types.
	 */
	public static String INTRUSION_DETECTION_SEVERITY = "severity";
	/**
	 * The name of the intrusion detected on the client (the src), such as
	 * PlugAndPlay_BO and JavaScript_Obfuscation_Fre.
	 */
	public static String INTRUSION_DETECTION_SIGNATURE = "signature";
	/**
	 * The source involved in the attack detected by the IDS. If your field is
	 * named src_host, src_ip, src_ipv6, or src_nt_host you can alias it as src
	 * to make it CIM-compliant.
	 */
	public static String INTRUSION_DETECTION_SRC = "src";
	/**
	 * The user involved with the intrusion detection event.
	 */
	public static String INTRUSION_DETECTION_USER = "user";
	/**
	 * The vendor technology used to generate network protection data, such as
	 * IDP, Providentia, and ASA.
	 *
	 * Note: Required for all events dealing with network protection (Change
	 * analysis, proxy, malware, intrusion detection, packet filtering, and
	 * vulnerability).
	 */
	public static String INTRUSION_DETECTION_VENDOR = "vendor";

	// ----------------------------------
	// Malware - Endpoint protection
	// ----------------------------------

	/**
	 * The outcome of the infection
	 */
	public static String MALWARE_ENDPOINT_PROTECTION_ACTION = "action";
	/**
	 * The NT domain of the destination (the dest_bestmatch).
	 */
	public static String MALWARE_ENDPOINT_PROTECTION_DEST_NT_DOMAIN = "dest_nt_domain";
	/**
	 * The cryptographic hash of the file associated with the malware event
	 * (such as the malicious or infected file).
	 */
	public static String MALWARE_ENDPOINT_PROTECTION_FILE_HASH = "file_hash";
	/**
	 * The name of the file involved in the malware event (such as the infected
	 * or malicious file).
	 */
	public static String MALWARE_ENDPOINT_PROTECTION_FILE_NAME = "file_name";
	/**
	 * The path of the file involved in the malware event (such as the infected
	 * or malicious file).
	 */
	public static String MALWARE_ENDPOINT_PROTECTION_FILE_PATH = "file_path";
	/**
	 * The product name of the vendor technology (the vendor field) that is
	 * generating malware data (such as Antivirus or EPO).
	 */
	public static String MALWARE_ENDPOINT_PROTECTION_PRODUCT = "product";
	/**
	 * The product version number of the vendor technology installed on the
	 * client (such as 10.4.3 or 11.0.2).
	 */
	public static String MALWARE_ENDPOINT_PROTECTION_PRODUCT_VERSION = "product_version";
	/**
	 * The name of the malware infection detected on the client (the src), such
	 * as Trojan.Vundo,Spyware.Gaobot,W32.Nimbda).
	 *
	 * Note: This field is a string. Please use a signature_id field for
	 * signature ID fields that are integer data types.
	 */
	public static String MALWARE_ENDPOINT_PROTECTION_SIGNATURE = "signature";
	/**
	 * The current signature definition set running on the client, such as
	 * 11hsvx)
	 */
	public static String MALWARE_ENDPOINT_PROTECTION_SIGNATURE_VERSION = "signature_version";
	/**
	 * The target affected or infected by the malware. If your field is named
	 * dest_host, dest_ip, dest_ipv6, or dest_nt_host you can alias it as dest
	 * to make it CIM-compliant.
	 */
	public static String MALWARE_ENDPOINT_PROTECTION_DEST = "dest";
	/**
	 * The NT domain of the source (the src).
	 */
	public static String MALWARE_ENDPOINT_PROTECTION_SRC_NT_DOMAIN = "src_nt_domain";
	/**
	 * The name of the user involved in the malware event.
	 */
	public static String MALWARE_ENDPOINT_PROTECTION_USER = "user";
	/**
	 * The name of the vendor technology generating malware data, such as
	 * Symantec or McAfee.
	 */
	public static String MALWARE_ENDPOINT_PROTECTION_VENDOR = "vendor";

	// ----------------------------------
	// Malware - Network protection
	// ----------------------------------

	/**
	 * The product name of the vendor technology generating network protection
	 * data, such as IDP, Proventia, and ASA.
	 *
	 * Note: Required for all events dealing with network protection (Change
	 * analysis, proxy, malware, intrusion detection, packet filtering, and
	 * vulnerability).
	 */
	public static String MALWARE_NETWORK_PROTECTION_PRODUCT = "product";
	/**
	 * The severity of the network protection event (such as critical, high,
	 * medium, low, or informational).
	 *
	 * Note: This field is a string. Please use a severity_id field for severity
	 * ID fields that are integer data types.
	 */
	public static String MALWARE_NETWORK_PROTECTION_SEVERITY = "severity";
	/**
	 * The vendor technology used to generate network protection data, such as
	 * IDP, Proventia, and ASA.
	 *
	 * Note: Required for all events dealing with network protection (Change
	 * analysis, proxy, malware, intrusion detection, packet filtering, and
	 * vulnerability).
	 */
	public static String MALWARE_NETWORK_PROTECTION_VENDOR = "vendor";

	// ----------------------------------
	// Network traffic - ESS
	// ----------------------------------

	/**
	 * The action of the network traffic.
	 */
	public static String NETWORK_TRAFFIC_ESS_ACTION = "action";
	/**
	 * The destination port of the network traffic.
	 */
	public static String NETWORK_TRAFFIC_ESS_DEST_PORT = "dest_port";
	/**
	 * The product name of the vendor technology generating NetworkProtection
	 * data, such as IDP, Proventia, and ASA.
	 *
	 * Note: Required for all events dealing with network protection (Change
	 * analysis, proxy, malware, intrusion detection, packet filtering, and
	 * vulnerability).
	 */
	public static String NETWORK_TRAFFIC_ESS_PRODUCT = "product";
	/**
	 * The source port of the network traffic.
	 */
	public static String NETWORK_TRAFFIC_ESS_SRC_PORT = "src_port";
	/**
	 * The vendor technology used to generate NetworkProtection data, such as
	 * IDP, Proventia, and ASA.
	 *
	 * Note: Required for all events dealing with network protection (Change
	 * analysis, proxy, malware, intrusion detection, packet filtering, and
	 * vulnerability).
	 */
	public static String NETWORK_TRAFFIC_ESS_VENDOR = "vendor";

	// ----------------------------------
	// Network traffic - Generic
	// ----------------------------------

	/**
	 * The ISO layer 7 (application layer) protocol, such as HTTP, HTTPS, SSH,
	 * and IMAP.
	 */
	public static String NETWORK_TRAFFIC_GENERIC_APP_LAYER = "app_layer";
	/**
	 * How many bytes this device/interface received.
	 */
	public static String NETWORK_TRAFFIC_GENERIC_BYTES_IN = "bytes_in";
	/**
	 * How many bytes this device/interface transmitted.
	 */
	public static String NETWORK_TRAFFIC_GENERIC_BYTES_OUT = "bytes_out";
	/**
	 * 802.11 channel number used by a wireless network.
	 */
	public static String NETWORK_TRAFFIC_GENERIC_CHANNEL = "channel";
	/**
	 * The Common Vulnerabilities and Exposures (CVE) reference value.
	 */
	public static String NETWORK_TRAFFIC_GENERIC_CVE = "cve";
	/**
	 * The destination application being targeted.
	 */
	public static String NETWORK_TRAFFIC_GENERIC_DEST_APP = "dest_app";
	/**
	 * The destination command and control service channel.
	 */
	public static String NETWORK_TRAFFIC_GENERIC_DEST_CNC_CHANNEL = "dest_cnc_channel";
	/**
	 * The destination command and control service name.
	 */
	public static String NETWORK_TRAFFIC_GENERIC_DEST_CNC_NAME = "dest_cnc_name";
	/**
	 * The destination command and control service port.
	 */
	public static String NETWORK_TRAFFIC_GENERIC_DEST_CNC_PORT = "dest_cnc_port";
	/**
	 * The country associated with a packet's recipient.
	 */
	public static String NETWORK_TRAFFIC_GENERIC_DEST_COUNTRY = "dest_country";
	/**
	 * The fully qualified host name of a packet's recipient. For HTTP sessions,
	 * this is the host header.
	 */
	public static String NETWORK_TRAFFIC_GENERIC_DEST_HOST = "dest_host";
	/**
	 * The interface that is listening remotely or receiving packets locally.
	 */
	public static String NETWORK_TRAFFIC_GENERIC_DEST_INT = "dest_int";
	/**
	 * The IPv4 address of a packet's recipient.
	 */
	public static String NETWORK_TRAFFIC_GENERIC_DEST_IP = "dest_ip";
	/**
	 * The IPv6 address of a packet's recipient.
	 */
	public static String NETWORK_TRAFFIC_GENERIC_DEST_IPV6 = "dest_ipv6";
	/**
	 * The (physical) latitude of a packet's destination.
	 */
	public static String NETWORK_TRAFFIC_GENERIC_DEST_LAT = "dest_lat";
	/**
	 * The (physical) longitude of a packet's destination.
	 */
	public static String NETWORK_TRAFFIC_GENERIC_DEST_LONG = "dest_long";
	/**
	 * The destination TCP/IP layer 2 Media Access Control (MAC) address of a
	 * packet's destination.
	 */
	public static String NETWORK_TRAFFIC_GENERIC_DEST_MAC = "dest_mac";
	/**
	 * The Windows NT domain containing a packet's destination.
	 */
	public static String NETWORK_TRAFFIC_GENERIC_DEST_NT_DOMAIN = "dest_nt_domain";
	/**
	 * The Windows NT host name of a packet's destination.
	 */
	public static String NETWORK_TRAFFIC_GENERIC_DEST_NT_HOST = "dest_nt_host";
	/**
	 * TCP/IP port to which a packet is being sent.
	 */
	public static String NETWORK_TRAFFIC_GENERIC_DEST_PORT = "dest_port";
	/**
	 * The NATed IPv4 address to which a packet has been sent.
	 */
	public static String NETWORK_TRAFFIC_GENERIC_DEST_TRANSLATED_IP = "dest_translated_ip";
	/**
	 * The NATed port to which a packet has been sent.
	 */
	public static String NETWORK_TRAFFIC_GENERIC_DEST_TRANSLATED_PORT = "dest_translated_port";
	/**
	 * The numbered Internet Protocol version.
	 */
	public static String NETWORK_TRAFFIC_GENERIC_IP_VERSION = "ip_version";
	/**
	 * The network interface through which a packet was transmitted.
	 */
	public static String NETWORK_TRAFFIC_GENERIC_OUTBOUND_INTERFACE = "outbound_interface";
	/**
	 * How many packets this device/interface received.
	 */
	public static String NETWORK_TRAFFIC_GENERIC_PACKETS_IN = "packets_in";
	/**
	 * How many packets this device/interface transmitted.
	 */
	public static String NETWORK_TRAFFIC_GENERIC_PACKETS_OUT = "packets_out";
	/**
	 * The OSI layer 3 (Network Layer) protocol, such as IPv4/IPv6, ICMP, IPsec,
	 * IGMP or RIP.
	 */
	public static String NETWORK_TRAFFIC_GENERIC_PROTO = "proto";
	/**
	 * The session identifier. Multiple transactions build a session.
	 */
	public static String NETWORK_TRAFFIC_GENERIC_SESSION_ID = "session_id";
	/**
	 * The 802.11 service set identifier (ssid) assigned to a wireless session.
	 */
	public static String NETWORK_TRAFFIC_GENERIC_SSID = "ssid";
	/**
	 * The country from which the packet was sent.
	 */
	public static String NETWORK_TRAFFIC_GENERIC_SRC_COUNTRY = "src_country";
	/**
	 * The fully qualified host name of the system that transmitted the packet.
	 * For Web logs, this is the HTTP client.
	 */
	public static String NETWORK_TRAFFIC_GENERIC_SRC_HOST = "src_host";
	/**
	 * The interface that is listening locally or sending packets remotely.
	 */
	public static String NETWORK_TRAFFIC_GENERIC_SRC_INT = "src_int";
	/**
	 * The IPv4 address of the packet's source. For Web logs, this is the http
	 * client.
	 */
	public static String NETWORK_TRAFFIC_GENERIC_SRC_IP = "src_ip";
	/**
	 * The IPv6 address of the packet's source.
	 */
	public static String NETWORK_TRAFFIC_GENERIC_SRC_IPV6 = "src_ipv6";
	/**
	 * The (physical) latitude of the packet's source.
	 */
	public static String NETWORK_TRAFFIC_GENERIC_SRC_LAT = "src_lat";
	/**
	 * The (physical) longitude of the packet's source.
	 */
	public static String NETWORK_TRAFFIC_GENERIC_SRC_LONG = "src_long";
	/**
	 * The Media Access Control (MAC) address from which a packet was
	 * transmitted.
	 */
	public static String NETWORK_TRAFFIC_GENERIC_SRC_MAC = "src_mac";
	/**
	 * The Windows NT domain containing the machines that generated the event.
	 */
	public static String NETWORK_TRAFFIC_GENERIC_SRC_NT_DOMAIN = "src_nt_domain";
	/**
	 * The Windows NT hostname of the system that generated the event.
	 */
	public static String NETWORK_TRAFFIC_GENERIC_SRC_NT_HOST = "src_nt_host";
	/**
	 * The network port from which a packet originated.
	 */
	public static String NETWORK_TRAFFIC_GENERIC_SRC_PORT = "src_port";
	/**
	 * The NATed IPv4 address from which a packet has been sent.
	 */
	public static String NETWORK_TRAFFIC_GENERIC_SRC_TRANSLATED_IP = "src_translated_ip";
	/**
	 * The NATed network port from which a packet has been sent.
	 */
	public static String NETWORK_TRAFFIC_GENERIC_SRC_TRANSLATED_PORT = "src_translated_port";
	/**
	 * The application, process, or OS subsystem that generated the event.
	 */
	public static String NETWORK_TRAFFIC_GENERIC_SYSLOG_ID = "syslog_id";
	/**
	 * The criticality of an event, as recorded by UNIX syslog.
	 */
	public static String NETWORK_TRAFFIC_GENERIC_SYSLOG_PRIORITY = "syslog_priority";
	/**
	 * The TCP flag(s) specified in the event.
	 */
	public static String NETWORK_TRAFFIC_GENERIC_TCP_FLAG = "tcp_flag";
	/**
	 * The hex bit that specifies TCP 'type of service'
	 *
	 * @see <a href="http://en.wikipedia.org/wiki/Type_of_Service">Type of
	 *      Service</a>
	 */
	public static String NETWORK_TRAFFIC_GENERIC_TOS = "tos";
	/**
	 * The transport protocol.
	 */
	public static String NETWORK_TRAFFIC_GENERIC_TRANSPORT = "transport";
	/**
	 * The "time to live" of a packet or datagram.
	 */
	public static String NETWORK_TRAFFIC_GENERIC_TTL = "ttl";
	/**
	 * The numeric identifier assigned to the virtual local area network (VLAN)
	 * specified in the record.
	 */
	public static String NETWORK_TRAFFIC_GENERIC_VLAN_ID = "vlan_id";
	/**
	 * The name assigned to the virtual local area network (VLAN) specified in
	 * the record.
	 */
	public static String NETWORK_TRAFFIC_GENERIC_VLAN_NAME = "vlan_name";

	// ----------------------------------
	// Packet filtering
	// ----------------------------------

	/**
	 * The action the filtering device (the dvc_bestmatch field) performed on
	 * the communication.
	 */
	public static String PACKET_FILTERING_ACTION = "action";
	/**
	 * The IP port of the packet's destination, such as 22.
	 */
	public static String PACKET_FILTERING_DEST_PORT = "dest_port";
	/**
	 * The direction the packet is traveling.
	 */
	public static String PACKET_FILTERING_DIRECTION = "direction";
	/**
	 * The name of the packet filtering device. If your field is named dvc_host,
	 * dvc_ip, or dvc_nt_host you can alias it as dvc to make it CIM-compliant.
	 */
	public static String PACKET_FILTERING_DVC = "dvc";
	/**
	 * The rule which took action on the packet, such as 143.
	 */
	public static String PACKET_FILTERING_RULE = "rule";
	/**
	 * The IP port of the packet's source, such as 34541.
	 */
	public static String PACKET_FILTERING_SVC_PORT = "svc_port";

	// ----------------------------------
	// Proxy
	// ----------------------------------

	/**
	 * The action taken by the proxy.
	 */
	public static String PROXY_ACTION = "action";
	/**
	 * The destination of the network traffic (the remote host).
	 */
	public static String PROXY_DEST = "dest";
	/**
	 * The content-type of the requested HTTP resource.
	 */
	public static String PROXY_HTTP_CONTENT_TYPE = "http_content_type";
	/**
	 * The HTTP method used to request the resource.
	 */
	public static String PROXY_HTTP_METHOD = "http_method";
	/**
	 * The HTTP referrer used to request the HTTP resource.
	 */
	public static String PROXY_HTTP_REFER = "http_refer";
	/**
	 * The HTTP response code.
	 */
	public static String PROXY_HTTP_RESPONSE = "http_response";
	/**
	 * The user agent used to request the HTTP resource.
	 */
	public static String PROXY_HTTP_USER_AGENT = "http_user_agent";
	/**
	 * The product name of the vendor technology generating Network Protection
	 * data, such as IDP, Providentia, and ASA.
	 */
	public static String PROXY_PRODUCT = "product";
	/**
	 * The source of the network traffic (the client requesting the connection).
	 */
	public static String PROXY_SRC = "src";
	/**
	 * The HTTP response code indicating the status of the proxy request.
	 */
	public static String PROXY_STATUS = "status";
	/**
	 * The user that requested the HTTP resource.
	 */
	public static String PROXY_USER = "user";
	/**
	 * The URL of the requested HTTP resource.
	 */
	public static String PROXY_URL = "url";
	/**
	 * The vendor technology generating Network Protection data, such as IDP,
	 * Providentia, and ASA.
	 */
	public static String PROXY_VENDOR = "vendor";

	// ----------------------------------
	// System center
	// ----------------------------------

	/**
	 * The running application or service on the system (the src field), such as
	 * explorer.exe or sshd.
	 */
	public static String SYSTEM_CENTER_APP = "app";
	/**
	 * The amount of disk space available per drive or mount (the mount field)
	 * on the system (the src field).
	 */
	public static String SYSTEM_CENTER_FREEMBYTES = "FreeMBytes";
	/**
	 * The version of operating system installed on the host (the src field),
	 * such as 6.0.1.4 or 2.6.27.30-170.2.82.fc10.x86_64.
	 */
	public static String SYSTEM_CENTER_KERNEL_RELEASE = "kernel_release";
	/**
	 * Human-readable version of the SystemUptime value.
	 */
	public static String SYSTEM_CENTER_LABEL = "label";
	/**
	 * The drive or mount reporting available disk space (the FreeMBytes field)
	 * on the system (the src field).
	 */
	public static String SYSTEM_CENTER_MOUNT = "mount";
	/**
	 * The name of the operating system installed on the host (the src), such as
	 * Microsoft Windows Server 2003 or GNU/Linux).
	 */
	public static String SYSTEM_CENTER_OS = "os";
	/**
	 * The percentage of processor utilization.
	 */
	public static String SYSTEM_CENTER_PERCENTPROCESSORTIME = "PercentProcessorTime";
	/**
	 * The setlocaldefs setting from the SE Linux configuration.
	 */
	public static String SYSTEM_CENTER_SETLOCALDEFS = "setlocaldefs";
	/**
	 * Values from the SE Linux configuration file.
	 */
	public static String SYSTEM_CENTER_SELINUX = "selinux";
	/**
	 * The SE Linux type (such as targeted).
	 */
	public static String SYSTEM_CENTER_SELINUXTYPE = "selinuxtype";
	/**
	 * The shell provided to the User Account (the user field) upon logging into
	 * the system (the src field).
	 */
	public static String SYSTEM_CENTER_SHELL = "shell";
	/**
	 * The TCP/UDP source port on the system (the src field).
	 */
	public static String SYSTEM_CENTER_SRC_PORT = "src_port";
	/**
	 * The sshd protocol version.
	 */
	public static String SYSTEM_CENTER_SSHD_PROTOCOL = "sshd_protocol";
	/**
	 * The start mode of the given service.
	 */
	public static String SYSTEM_CENTER_STARTMODE = "Startmode";
	/**
	 * The number of seconds since the system (the src) has been "up."
	 */
	public static String SYSTEM_CENTER_SYSTEMUPTIME = "SystemUptime";
	/**
	 * The total amount of available memory on the system (the src).
	 */
	public static String SYSTEM_CENTER_TOTALMBYTES = "TotalMBytes";
	/**
	 * The amount of used memory on the system (the src).
	 */
	public static String SYSTEM_CENTER_USEDMBYTES = "UsedMBytes";
	/**
	 * The User Account present on the system (the src).
	 */
	public static String SYSTEM_CENTER_USER = "user";
	/**
	 * The number of updates the system (the src) is missing.
	 */
	public static String SYSTEM_CENTER_UPDATES = "updates";

	// ----------------------------------
	// Traffic
	// ----------------------------------

	/**
	 * The destination of the network traffic. If your field is named dest_host,
	 * dest_ip, dest_ipv6, or dest_nt_host you can alias it as dest to make it
	 * CIM-compliant.
	 */
	public static String TRAFFIC_DEST = "dest";
	/**
	 * The name of the packet filtering device. If your field is named dvc_host,
	 * dvc_ip, or dvc_nt_host you can alias it as dvc to make it CIM-compliant.
	 */
	public static String TRAFFIC_DVC = "dvc";
	/**
	 * The source of the network traffic. If your field is named src_host,
	 * src_ip, src_ipv6, or src_nt_host you can alias it as src to make it
	 * CIM-compliant.
	 */
	public static String TRAFFIC_SRC = "src";

	// ----------------------------------
	// Update
	// ----------------------------------

	/**
	 * The name of the installed update.
	 */
	public static String UPDATE_PACKAGE = "package";

	// ----------------------------------
	// User information updates
	// ----------------------------------

	/**
	 * A user that has been affected by a change. For example, user fflanda
	 * changed the name of user rhallen, so affected_user=rhallen.
	 */
	public static String USER_INFO_UPDATES_AFFECTED_USER = "affected_user";
	/**
	 * The user group affected by a change.
	 */
	public static String USER_INFO_UPDATES_AFFECTED_USER_GROUP = "affected_user_group";
	/**
	 * The identifier of the user group affected by a change.
	 */
	public static String USER_INFO_UPDATES_AFFECTED_USER_GROUP_ID = "affected_user_group_id";
	/**
	 * The identifier of the user affected by a change.
	 */
	public static String USER_INFO_UPDATES_AFFECTED_USER_ID = "affected_user_id";
	/**
	 * The security context associated with the user affected by a change.
	 */
	public static String USER_INFO_UPDATES_AFFECTED_USER_PRIVILEGE = "affected_user_privilege";
	/**
	 * The name of the user affected by the recorded event.
	 */
	public static String USER_INFO_UPDATES_USER = "user";
	/**
	 * A user group that is the object of an event, expressed in human-readable
	 * terms.
	 */
	public static String USER_INFO_UPDATES_USER_GROUP = "user_group";
	/**
	 * The numeric identifier assigned to the user group event object.
	 */
	public static String USER_INFO_UPDATES_USER_GROUP_ID = "user_group_id";
	/**
	 * The system-assigned identifier for the user affected by an event.
	 */
	public static String USER_INFO_UPDATES_USER_ID = "user_id";
	/**
	 * The security context associated with the object of an event (the affected
	 * user).
	 */
	public static String USER_INFO_UPDATES_USER_PRIVILEGE = "user_privilege";
	/**
	 * The name of the user that is the subject of an event--the user executing
	 * the action, in other words.
	 */
	public static String USER_INFO_UPDATES_USER_SUBJECT = "user_subject";
	/**
	 * The ID number of the user that is the subject of an event.
	 */
	public static String USER_INFO_UPDATES_USER_SUBJECT_ID = "user_subject_id";
	/**
	 * The security context associated with the subject of an event (the user
	 * causing a change).
	 */
	public static String USER_INFO_UPDATES_USER_SUBJECT_PRIVILEGE = "user_subject_privilege";

	// ----------------------------------
	// Vulnerability
	// ----------------------------------

	/**
	 * The category of the discovered vulnerability.
	 */
	public static String VULNERABILITY_CATEGORY = "category";
	/**
	 * The host with the discovered vulnerability. If your field is named
	 * dest_host, dest_ip, dest_ipv6, or dest_nt_host you can alias it as dest
	 * to make it CIM-compliant.
	 */
	public static String VULNERABILITY_DEST = "dest";
	/**
	 * The operating system of the host containing the vulnerability detected on
	 * the client (the src field), such as SuSE Security Update, or cups
	 * security update.
	 */
	public static String VULNERABILITY_OS = "os";
	/**
	 * The severity of the discovered vulnerability.
	 */
	public static String VULNERABILITY_SEVERITY = "severity";
	/**
	 * The name of the vulnerability detected on the client (the src field),
	 * such as SuSE Security Update, or cups security update.
	 */
	public static String VULNERABILITY_SIGNATURE = "signature";

	// ----------------------------------
	// Windows administration
	// ----------------------------------

	/**
	 * The object name (associated only with Windows).
	 */
	public static String WINDOWS_ADMIN_OBJECT_NAME = "object_name";
	/**
	 * The object type (associated only with Windows).
	 */
	public static String WINDOWS_ADMIN_OBJECT_TYPE = "object_type";
	/**
	 * The object handle (associated only with Windows).
	 */
	public static String WINDOWS_ADMIN_OBJECT_HANDLE = "object_handle";


	public SplunkData(Map<String, String> data) {
		this.eventMessage = new StringBuffer();
		this.eventData = data;
		for (String key : data.keySet()) {
			this.addPair(key, data.get(key));
		}
	}

	/**
	 * Constructor.
	 *
	 * @param eventName
	 *            the event name
	 * @param eventID
	 *            the event id
	 * @param useInternalDate
	 *            Whether or not to add a date to the event string
	 * @param quoteValues
	 *            Whether or not to put quotes around values
	 */
	public SplunkData(String eventName, String eventID, boolean useInternalDate, boolean quoteValues) {

		this.eventMessage = new StringBuffer();
		this.quoteValues = quoteValues;
		this.useInternalDate = useInternalDate;

		addPair(PREFIX_NAME, eventName);
		addPair(PREFIX_EVENT_ID, eventID);
	}

	/**
	 * Constructor.Will add internally generated date and put quotes around
	 * values.
	 *
	 * @param eventName
	 *            the event name
	 * @param eventID
	 *            the event ID
	 */
	public SplunkData(String eventName, String eventID) {

		this(eventName, eventID, true, true);
	}

	/**
	 * Default constructor
	 */
	public SplunkData() {
		this.eventMessage = new StringBuffer();
	}

	/**
	 * Simple shallow cloning method
	 */
	public SplunkData clone() {
		SplunkData clone = new SplunkData();
		clone.quoteValues = this.quoteValues;
		clone.useInternalDate = this.useInternalDate;
		clone.eventMessage.append(this.eventMessage);

		return clone;
	}


	public Map<String, String> getEventData() {
		return eventData;
	}

	/**
	 * Add a key value pair
	 *
	 * @param key
	 * @param value
	 */
	public void addPair(String key, char value) {
		addPair(key, String.valueOf(value));
	}

	/**
	 * Add a key value pair
	 *
	 * @param key
	 * @param value
	 */
	public void addPair(String key, boolean value) {
		addPair(key, String.valueOf(value));
	}

	/**
	 * Add a key value pair
	 *
	 * @param key
	 * @param value
	 */
	public void addPair(String key, double value) {
		addPair(key, String.valueOf(value));
	}

	/**
	 * Add a key value pair
	 *
	 * @param key
	 * @param value
	 */
	public void addPair(String key, long value) {
		addPair(key, String.valueOf(value));
	}

	/**
	 * Add a key value pair
	 *
	 * @param key
	 * @param value
	 */
	public void addPair(String key, int value) {
		addPair(key, String.valueOf(value));
	}

	/**
	 * Add a key value pair
	 *
	 * @param key
	 * @param value
	 */
	public void addPair(String key, Object value) {
		addPair(key, value.toString());
	}

	/**
	 * Utility method for formatting Throwable,Error,Exception objects in a more
	 * linear and Splunk friendly manner than printStackTrace
	 *
	 * @param throwable
	 *            the Throwable object to add to the event
	 */
	public void addThrowable(Throwable throwable) {

		addThrowableObject(throwable, -1);
	}

	/**
	 * Utility method for formatting Throwable,Error,Exception objects in a more
	 * linear and Splunk friendly manner than printStackTrace
	 *
	 * @param throwable
	 *            the Throwable object to add to the event
	 * @param stackTraceDepth
	 *            maximum number of stacktrace elements to log
	 */
	public void addThrowable(Throwable throwable, int stackTraceDepth) {

		addThrowableObject(throwable, stackTraceDepth);
	}

	/**
	 * Internal private method for formatting Throwable,Error,Exception objects
	 * in a more linear and Splunk friendly manner than printStackTrace
	 *
	 * @param throwable
	 *            the Throwable object to add to the event
	 * @param stackTraceDepth
	 *            maximum number of stacktrace elements to log, -1 for all
	 */

	private void addThrowableObject(Throwable throwable, int stackTraceDepth) {

		addPair(THROWABLE_CLASS, throwable.getClass().getCanonicalName());
		addPair(THROWABLE_MESSAGE, throwable.getMessage());
		StackTraceElement[] elements = throwable.getStackTrace();
		StringBuffer sb = new StringBuffer();
		int depth = 0;
		for (StackTraceElement element : elements) {
			depth++;
			if (stackTraceDepth == -1 || stackTraceDepth >= depth)
				sb.append(element.toString()).append(",");
			else
				break;

		}
		addPair(THROWABLE_STACKTRACE_ELEMENTS, sb.toString());
	}

	/**
	 * Add a key value pair
	 *
	 * @param key
	 * @param value
	 */
	public void addPair(String key, String value) {

		if (quoteValues)
			this.eventMessage.append(key).append(KVDELIM).append(QUOTE).append(value).append(QUOTE).append(PAIRDELIM);
		else
			this.eventMessage.append(key).append(KVDELIM).append(value).append(PAIRDELIM);

	}

	@Override
	/**
	 * return the completed event message
	 */
	public String toString() {

		String event = "";

		if (useInternalDate) {
			StringBuffer clonedMessage = new StringBuffer();
			clonedMessage.append(DATEFORMATTER.format(new Date())).append(PAIRDELIM).append(this.eventMessage);
			event = clonedMessage.toString();
		}
		else
			event = eventMessage.toString();
		// trim off trailing pair delim char(s)
		return event.substring(0, event.length() - PAIRDELIM.length());
	}

	public void setAcManagementDestNtDomain(String acManagementDestNtDomain) {
		addPair(AC_MANAGEMENT_DEST_NT_DOMAIN, acManagementDestNtDomain);
	}

	public void setAcManagementSignature(String acManagementSignature) {
		addPair(AC_MANAGEMENT_SIGNATURE, acManagementSignature);
	}

	public void setAcManagementSrcNtDomain(String acManagementSrcNtDomain) {
		addPair(AC_MANAGEMENT_SRC_NT_DOMAIN, acManagementSrcNtDomain);
	}

	public void setAuthAction(String authAction) {
		addPair(AUTH_ACTION, authAction);
	}

	public void setAuthApp(String authApp) {
		addPair(AUTH_APP, authApp);
	}

	public void setAuthDest(String authDest) {
		addPair(AUTH_DEST, authDest);
	}

	public void setAuthSrc(String authSrc) {
		addPair(AUTH_SRC, authSrc);
	}

	public void setAuthSrcUser(String authSrcUser) {
		addPair(AUTH_SRC_USER, authSrcUser);
	}

	public void setAuthUser(String authUser) {
		addPair(AUTH_USER, authUser);
	}

	public void setChangeEndpointProtectionAction(String changeEndpointProtectionAction) {
		addPair(CHANGE_ENDPOINT_PROTECTION_ACTION, changeEndpointProtectionAction);
	}

	public void setChangeEndpointProtectionChangeType(String changeEndpointProtectionChangeType) {
		addPair(CHANGE_ENDPOINT_PROTECTION_CHANGE_TYPE, changeEndpointProtectionChangeType);
	}

	public void setChangeEndpointProtectionDest(String changeEndpointProtectionDest) {
		addPair(CHANGE_ENDPOINT_PROTECTION_DEST, changeEndpointProtectionDest);
	}

	public void setChangeEndpointProtectionHash(String changeEndpointProtectionHash) {
		addPair(CHANGE_ENDPOINT_PROTECTION_HASH, changeEndpointProtectionHash);
	}

	public void setChangeEndpointProtectionGid(long changeEndpointProtectionGid) {
		addPair(CHANGE_ENDPOINT_PROTECTION_GID, changeEndpointProtectionGid);
	}

	public void setChangeEndpointProtectionIsdr(boolean changeEndpointProtectionIsdr) {
		addPair(CHANGE_ENDPOINT_PROTECTION_ISDR, changeEndpointProtectionIsdr);
	}

	public void setChangeEndpointProtectionMode(long changeEndpointProtectionMode) {
		addPair(CHANGE_ENDPOINT_PROTECTION_MODE, changeEndpointProtectionMode);
	}

	public void setChangeEndpointProtectionModtime(String changeEndpointProtectionModtime) {
		addPair(CHANGE_ENDPOINT_PROTECTION_MODTIME, changeEndpointProtectionModtime);
	}

	public void setChangeEndpointProtectionPath(String changeEndpointProtectionPath) {
		addPair(CHANGE_ENDPOINT_PROTECTION_PATH, changeEndpointProtectionPath);
	}

	public void setChangeEndpointProtectionSize(long changeEndpointProtectionSize) {
		addPair(CHANGE_ENDPOINT_PROTECTION_SIZE, changeEndpointProtectionSize);
	}

	public void setChangeEndpointProtectionUid(long changeEndpointProtectionUid) {
		addPair(CHANGE_ENDPOINT_PROTECTION_UID, changeEndpointProtectionUid);
	}

	public void setChangeNetworkProtectionAction(String changeNetworkProtectionAction) {
		addPair(CHANGE_NETWORK_PROTECTION_ACTION, changeNetworkProtectionAction);
	}

	public void setChangeNetworkProtectionCommand(String changeNetworkProtectionCommand) {
		addPair(CHANGE_NETWORK_PROTECTION_COMMAND, changeNetworkProtectionCommand);
	}

	public void setChangeNetworkProtectionDvc(String changeNetworkProtectionDvc) {
		addPair(CHANGE_NETWORK_PROTECTION_DVC, changeNetworkProtectionDvc);
	}

	public void setChangeNetworkProtectionUser(String changeNetworkProtectionUser) {
		addPair(CHANGE_NETWORK_PROTECTION_USER, changeNetworkProtectionUser);
	}

	public void setCommonCategory(String commonCategory) {
		addPair(COMMON_CATEGORY, commonCategory);
	}

	public void setCommonCount(String commonCount) {
		addPair(COMMON_COUNT, commonCount);
	}

	public void setCommonDesc(String commonDesc) {
		addPair(COMMON_DESC, commonDesc);
	}

	public void setCommonDhcpPool(String commonDhcpPool) {
		addPair(COMMON_DHCP_POOL, commonDhcpPool);
	}

	public void setCommonDuration(long commonDuration) {
		addPair(COMMON_DURATION, commonDuration);
	}

	public void setCommonDvcHost(String commonDvcHost) {
		addPair(COMMON_DVC_HOST, commonDvcHost);
	}

	public void setCommonDvcIp(String commonDvcIp) {
		addPair(COMMON_DVC_IP, commonDvcIp);
	}

	public void setCommonDvcIp6(String commonDvcIp6) {
		addPair(COMMON_DVC_IP6, commonDvcIp6);
	}

	public void setCommonDvcLocation(String commonDvcLocation) {
		addPair(COMMON_DVC_LOCATION, commonDvcLocation);
	}

	public void setCommonDvcMac(String commonDvcMac) {
		addPair(COMMON_DVC_MAC, commonDvcMac);
	}

	public void setCommonDvcNtDomain(String commonDvcNtDomain) {
		addPair(COMMON_DVC_NT_DOMAIN, commonDvcNtDomain);
	}

	public void setCommonDvcNtHost(String commonDvcNtHost) {
		addPair(COMMON_DVC_NT_HOST, commonDvcNtHost);
	}

	public void setCommonDvcTime(long commonDvcTime) {
		addPair(COMMON_DVC_TIME, commonDvcTime);
	}

	public void setCommonEndTime(long commonEndTime) {
		addPair(COMMON_END_TIME, commonEndTime);
	}

	public void setCommonEventId(long commonEventId) {
		addPair(COMMON_EVENT_ID, commonEventId);
	}

	public void setCommonLength(long commonLength) {
		addPair(COMMON_LENGTH, commonLength);
	}

	public void setCommonLogLevel(String commonLogLevel) {
		addPair(COMMON_LOG_LEVEL, commonLogLevel);
	}

	public void setCommonName(String commonName) {
		addPair(COMMON_NAME, commonName);
	}

	public void setCommonPid(long commonPid) {
		addPair(COMMON_PID, commonPid);
	}

	public void setCommonPriority(long commonPriority) {
		addPair(COMMON_PRIORITY, commonPriority);
	}

	public void setCommonProduct(String commonProduct) {
		addPair(COMMON_PRODUCT, commonProduct);
	}

	public void setCommonProductVersion(long commonProductVersion) {
		addPair(COMMON_PRODUCT_VERSION, commonProductVersion);
	}

	public void setCommonReason(String commonReason) {
		addPair(COMMON_REASON, commonReason);
	}

	public void setCommonResult(String commonResult) {
		addPair(COMMON_RESULT, commonResult);
	}

	public void setCommonSeverity(String commonSeverity) {
		addPair(COMMON_SEVERITY, commonSeverity);
	}

	public void setCommonStartTime(long commonStartTime) {
		addPair(COMMON_START_TIME, commonStartTime);
	}

	public void setCommonTransactionId(String commonTransactionId) {
		addPair(COMMON_TRANSACTION_ID, commonTransactionId);
	}

	public void setCommonUrl(String commonUrl) {
		addPair(COMMON_URL, commonUrl);
	}

	public void setCommonVendor(String commonVendor) {
		addPair(COMMON_VENDOR, commonVendor);
	}

	public void setDnsDestDomain(String dnsDestDomain) {
		addPair(DNS_DEST_DOMAIN, dnsDestDomain);
	}

	public void setDnsDestRecord(String dnsDestRecord) {
		addPair(DNS_DEST_RECORD, dnsDestRecord);
	}

	public void setDnsDestZone(String dnsDestZone) {
		addPair(DNS_DEST_ZONE, dnsDestZone);
	}

	public void setDnsRecordClass(String dnsRecordClass) {
		addPair(DNS_RECORD_CLASS, dnsRecordClass);
	}

	public void setDnsRecordType(String dnsRecordType) {
		addPair(DNS_RECORD_TYPE, dnsRecordType);
	}

	public void setDnsSrcDomain(String dnsSrcDomain) {
		addPair(DNS_SRC_DOMAIN, dnsSrcDomain);
	}

	public void setDnsSrcRecord(String dnsSrcRecord) {
		addPair(DNS_SRC_RECORD, dnsSrcRecord);
	}

	public void setDnsSrcZone(String dnsSrcZone) {
		addPair(DNS_SRC_ZONE, dnsSrcZone);
	}

	public void setEmailRecipient(String emailRecipient) {
		addPair(EMAIL_RECIPIENT, emailRecipient);
	}

	public void setEmailSender(String emailSender) {
		addPair(EMAIL_SENDER, emailSender);
	}

	public void setEmailSubject(String emailSubject) {
		addPair(EMAIL_SUBJECT, emailSubject);
	}

	public void setFileAccessTime(long fileAccessTime) {
		addPair(FILE_ACCESS_TIME, fileAccessTime);
	}

	public void setFileCreateTime(long fileCreateTime) {
		addPair(FILE_CREATE_TIME, fileCreateTime);
	}

	public void setFileHash(String fileHash) {
		addPair(FILE_HASH, fileHash);
	}

	public void setFileModifyTime(long fileModifyTime) {
		addPair(FILE_MODIFY_TIME, fileModifyTime);
	}

	public void setFileName(String fileName) {
		addPair(FILE_NAME, fileName);
	}

	public void setFilePath(String filePath) {
		addPair(FILE_PATH, filePath);
	}

	public void setFilePermission(String filePermission) {
		addPair(FILE_PERMISSION, filePermission);
	}

	public void setFileSize(long fileSize) {
		addPair(FILE_SIZE, fileSize);
	}

	public void setIntrusionDetectionCategory(String intrusionDetectionCategory) {
		addPair(INTRUSION_DETECTION_CATEGORY, intrusionDetectionCategory);
	}

	public void setIntrusionDetectionDest(String intrusionDetectionDest) {
		addPair(INTRUSION_DETECTION_DEST, intrusionDetectionDest);
	}

	public void setIntrusionDetectionDvc(String intrusionDetectionDvc) {
		addPair(INTRUSION_DETECTION_DVC, intrusionDetectionDvc);
	}

	public void setIntrusionDetectionIdsType(String intrusionDetectionIdsType) {
		addPair(INTRUSION_DETECTION_IDS_TYPE, intrusionDetectionIdsType);
	}

	public void setIntrusionDetectionProduct(String intrusionDetectionProduct) {
		addPair(INTRUSION_DETECTION_PRODUCT, intrusionDetectionProduct);
	}

	public void setIntrusionDetectionSeverity(String intrusionDetectionSeverity) {
		addPair(INTRUSION_DETECTION_SEVERITY, intrusionDetectionSeverity);
	}

	public void setIntrusionDetectionSignature(String intrusionDetectionSignature) {
		addPair(INTRUSION_DETECTION_SIGNATURE, intrusionDetectionSignature);
	}

	public void setIntrusionDetectionSrc(String intrusionDetectionSrc) {
		addPair(INTRUSION_DETECTION_SRC, intrusionDetectionSrc);
	}

	public void setIntrusionDetectionUser(String intrusionDetectionUser) {
		addPair(INTRUSION_DETECTION_USER, intrusionDetectionUser);
	}

	public void setIntrusionDetectionVendor(String intrusionDetectionVendor) {
		addPair(INTRUSION_DETECTION_VENDOR, intrusionDetectionVendor);
	}

	public void setMalwareEndpointProtectionAction(String malwareEndpointProtectionAction) {
		addPair(MALWARE_ENDPOINT_PROTECTION_ACTION, malwareEndpointProtectionAction);
	}

	public void setMalwareEndpointProtectionDestNtDomain(String malwareEndpointProtectionDestNtDomain) {
		addPair(MALWARE_ENDPOINT_PROTECTION_DEST_NT_DOMAIN, malwareEndpointProtectionDestNtDomain);
	}

	public void setMalwareEndpointProtectionFileHash(String malwareEndpointProtectionFileHash) {
		addPair(MALWARE_ENDPOINT_PROTECTION_FILE_HASH, malwareEndpointProtectionFileHash);
	}

	public void setMalwareEndpointProtectionFileName(String malwareEndpointProtectionFileName) {
		addPair(MALWARE_ENDPOINT_PROTECTION_FILE_NAME, malwareEndpointProtectionFileName);
	}

	public void setMalwareEndpointProtectionFilePath(String malwareEndpointProtectionFilePath) {
		addPair(MALWARE_ENDPOINT_PROTECTION_FILE_PATH, malwareEndpointProtectionFilePath);
	}

	public void setMalwareEndpointProtectionProduct(String malwareEndpointProtectionProduct) {
		addPair(MALWARE_ENDPOINT_PROTECTION_PRODUCT, malwareEndpointProtectionProduct);
	}

	public void setMalwareEndpointProtectionProductVersion(String malwareEndpointProtectionProductVersion) {
		addPair(MALWARE_ENDPOINT_PROTECTION_PRODUCT_VERSION, malwareEndpointProtectionProductVersion);
	}

	public void setMalwareEndpointProtectionSignature(String malwareEndpointProtectionSignature) {
		addPair(MALWARE_ENDPOINT_PROTECTION_SIGNATURE, malwareEndpointProtectionSignature);
	}

	public void setMalwareEndpointProtectionSignatureVersion(String malwareEndpointProtectionSignatureVersion) {
		addPair(MALWARE_ENDPOINT_PROTECTION_SIGNATURE_VERSION, malwareEndpointProtectionSignatureVersion);
	}

	public void setMalwareEndpointProtectionDest(String malwareEndpointProtectionDest) {
		addPair(MALWARE_ENDPOINT_PROTECTION_DEST, malwareEndpointProtectionDest);
	}

	public void setMalwareEndpointProtectionSrcNtDomain(String malwareEndpointProtectionSrcNtDomain) {
		addPair(MALWARE_ENDPOINT_PROTECTION_SRC_NT_DOMAIN, malwareEndpointProtectionSrcNtDomain);
	}

	public void setMalwareEndpointProtectionUser(String malwareEndpointProtectionUser) {
		addPair(MALWARE_ENDPOINT_PROTECTION_USER, malwareEndpointProtectionUser);
	}

	public void setMalwareEndpointProtectionVendor(String malwareEndpointProtectionVendor) {
		addPair(MALWARE_ENDPOINT_PROTECTION_VENDOR, malwareEndpointProtectionVendor);
	}

	public void setMalwareNetworkProtectionProduct(String malwareNetworkProtectionProduct) {
		addPair(MALWARE_NETWORK_PROTECTION_PRODUCT, malwareNetworkProtectionProduct);
	}

	public void setMalwareNetworkProtectionSeverity(String malwareNetworkProtectionSeverity) {
		addPair(MALWARE_NETWORK_PROTECTION_SEVERITY, malwareNetworkProtectionSeverity);
	}

	public void setMalwareNetworkProtectionVendor(String malwareNetworkProtectionVendor) {
		addPair(MALWARE_NETWORK_PROTECTION_VENDOR, malwareNetworkProtectionVendor);
	}

	public void setNetworkTrafficEssAction(String networkTrafficEssAction) {
		addPair(NETWORK_TRAFFIC_ESS_ACTION, networkTrafficEssAction);
	}

	public void setNetworkTrafficEssDestPort(int networkTrafficEssDestPort) {
		addPair(NETWORK_TRAFFIC_ESS_DEST_PORT, networkTrafficEssDestPort);
	}

	public void setNetworkTrafficEssProduct(String networkTrafficEssProduct) {
		addPair(NETWORK_TRAFFIC_ESS_PRODUCT, networkTrafficEssProduct);
	}

	public void setNetworkTrafficEssSrcPort(int networkTrafficEssSrcPort) {
		addPair(NETWORK_TRAFFIC_ESS_SRC_PORT, networkTrafficEssSrcPort);
	}

	public void setNetworkTrafficEssVendor(String networkTrafficEssVendor) {
		addPair(NETWORK_TRAFFIC_ESS_VENDOR, networkTrafficEssVendor);
	}

	public void setNetworkTrafficGenericAppLayer(String networkTrafficGenericAppLayer) {
		addPair(NETWORK_TRAFFIC_GENERIC_APP_LAYER, networkTrafficGenericAppLayer);
	}

	public void setNetworkTrafficGenericBytesIn(long networkTrafficGenericBytesIn) {
		addPair(NETWORK_TRAFFIC_GENERIC_BYTES_IN, networkTrafficGenericBytesIn);
	}

	public void setNetworkTrafficGenericBytesOut(long networkTrafficGenericBytesOut) {
		addPair(NETWORK_TRAFFIC_GENERIC_BYTES_OUT, networkTrafficGenericBytesOut);
	}

	public void setNetworkTrafficGenericChannel(String networkTrafficGenericChannel) {
		addPair(NETWORK_TRAFFIC_GENERIC_CHANNEL, networkTrafficGenericChannel);
	}

	public void setNetworkTrafficGenericCve(String networkTrafficGenericCve) {
		addPair(NETWORK_TRAFFIC_GENERIC_CVE, networkTrafficGenericCve);
	}

	public void setNetworkTrafficGenericDestApp(String networkTrafficGenericDestApp) {
		addPair(NETWORK_TRAFFIC_GENERIC_DEST_APP, networkTrafficGenericDestApp);
	}

	public void setNetworkTrafficGenericDestCncChannel(String networkTrafficGenericDestCncChannel) {
		addPair(NETWORK_TRAFFIC_GENERIC_DEST_CNC_CHANNEL, networkTrafficGenericDestCncChannel);
	}

	public void setNetworkTrafficGenericDestCncName(String networkTrafficGenericDestCncName) {
		addPair(NETWORK_TRAFFIC_GENERIC_DEST_CNC_NAME, networkTrafficGenericDestCncName);
	}

	public void setNetworkTrafficGenericDestCncPort(String networkTrafficGenericDestCncPort) {
		addPair(NETWORK_TRAFFIC_GENERIC_DEST_CNC_PORT, networkTrafficGenericDestCncPort);
	}

	public void setNetworkTrafficGenericDestCountry(String networkTrafficGenericDestCountry) {
		addPair(NETWORK_TRAFFIC_GENERIC_DEST_COUNTRY, networkTrafficGenericDestCountry);
	}

	public void setNetworkTrafficGenericDestHost(String networkTrafficGenericDestHost) {
		addPair(NETWORK_TRAFFIC_GENERIC_DEST_HOST, networkTrafficGenericDestHost);
	}

	public void setNetworkTrafficGenericDestInt(String networkTrafficGenericDestInt) {
		addPair(NETWORK_TRAFFIC_GENERIC_DEST_INT, networkTrafficGenericDestInt);
	}

	public void setNetworkTrafficGenericDestIp(String networkTrafficGenericDestIp) {
		addPair(NETWORK_TRAFFIC_GENERIC_DEST_IP, networkTrafficGenericDestIp);
	}

	public void setNetworkTrafficGenericDestIpv6(String networkTrafficGenericDestIpv6) {
		addPair(NETWORK_TRAFFIC_GENERIC_DEST_IPV6, networkTrafficGenericDestIpv6);
	}

	public void setNetworkTrafficGenericDestLat(int networkTrafficGenericDestLat) {
		addPair(NETWORK_TRAFFIC_GENERIC_DEST_LAT, networkTrafficGenericDestLat);
	}

	public void setNetworkTrafficGenericDestLong(int networkTrafficGenericDestLong) {
		addPair(NETWORK_TRAFFIC_GENERIC_DEST_LONG, networkTrafficGenericDestLong);
	}

	public void setNetworkTrafficGenericDestMac(String networkTrafficGenericDestMac) {
		addPair(NETWORK_TRAFFIC_GENERIC_DEST_MAC, networkTrafficGenericDestMac);
	}

	public void setNetworkTrafficGenericDestNtDomain(String networkTrafficGenericDestNtDomain) {
		addPair(NETWORK_TRAFFIC_GENERIC_DEST_NT_DOMAIN, networkTrafficGenericDestNtDomain);
	}

	public void setNetworkTrafficGenericDestNtHost(String networkTrafficGenericDestNtHost) {
		addPair(NETWORK_TRAFFIC_GENERIC_DEST_NT_HOST, networkTrafficGenericDestNtHost);
	}

	public void setNetworkTrafficGenericDestPort(int networkTrafficGenericDestPort) {
		addPair(NETWORK_TRAFFIC_GENERIC_DEST_PORT, networkTrafficGenericDestPort);
	}

	public void setNetworkTrafficGenericDestTranslatedIp(String networkTrafficGenericDestTranslatedIp) {
		addPair(NETWORK_TRAFFIC_GENERIC_DEST_TRANSLATED_IP, networkTrafficGenericDestTranslatedIp);
	}

	public void setNetworkTrafficGenericDestTranslatedPort(int networkTrafficGenericDestTranslatedPort) {
		addPair(NETWORK_TRAFFIC_GENERIC_DEST_TRANSLATED_PORT, networkTrafficGenericDestTranslatedPort);
	}

	public void setNetworkTrafficGenericIpVersion(int networkTrafficGenericIpVersion) {
		addPair(NETWORK_TRAFFIC_GENERIC_IP_VERSION, networkTrafficGenericIpVersion);
	}

	public void setNetworkTrafficGenericOutboundInterface(String networkTrafficGenericOutboundInterface) {
		addPair(NETWORK_TRAFFIC_GENERIC_OUTBOUND_INTERFACE, networkTrafficGenericOutboundInterface);
	}

	public void setNetworkTrafficGenericPacketsIn(long networkTrafficGenericPacketsIn) {
		addPair(NETWORK_TRAFFIC_GENERIC_PACKETS_IN, networkTrafficGenericPacketsIn);
	}

	public void setNetworkTrafficGenericPacketsOut(long networkTrafficGenericPacketsOut) {
		addPair(NETWORK_TRAFFIC_GENERIC_PACKETS_OUT, networkTrafficGenericPacketsOut);
	}

	public void setNetworkTrafficGenericProto(String networkTrafficGenericProto) {
		addPair(NETWORK_TRAFFIC_GENERIC_PROTO, networkTrafficGenericProto);
	}

	public void setNetworkTrafficGenericSessionId(String networkTrafficGenericSessionId) {
		addPair(NETWORK_TRAFFIC_GENERIC_SESSION_ID, networkTrafficGenericSessionId);
	}

	public void setNetworkTrafficGenericSsid(String networkTrafficGenericSsid) {
		addPair(NETWORK_TRAFFIC_GENERIC_SSID, networkTrafficGenericSsid);
	}

	public void setNetworkTrafficGenericSrcCountry(String networkTrafficGenericSrcCountry) {
		addPair(NETWORK_TRAFFIC_GENERIC_SRC_COUNTRY, networkTrafficGenericSrcCountry);
	}

	public void setNetworkTrafficGenericSrcHost(String networkTrafficGenericSrcHost) {
		addPair(NETWORK_TRAFFIC_GENERIC_SRC_HOST, networkTrafficGenericSrcHost);
	}

	public void setNetworkTrafficGenericSrcInt(String networkTrafficGenericSrcInt) {
		addPair(NETWORK_TRAFFIC_GENERIC_SRC_INT, networkTrafficGenericSrcInt);
	}

	public void setNetworkTrafficGenericSrcIp(String networkTrafficGenericSrcIp) {
		addPair(NETWORK_TRAFFIC_GENERIC_SRC_IP, networkTrafficGenericSrcIp);
	}

	public void setNetworkTrafficGenericSrcIpv6(String networkTrafficGenericSrcIpv6) {
		addPair(NETWORK_TRAFFIC_GENERIC_SRC_IPV6, networkTrafficGenericSrcIpv6);
	}

	public void setNetworkTrafficGenericSrcLat(int networkTrafficGenericSrcLat) {
		addPair(NETWORK_TRAFFIC_GENERIC_SRC_LAT, networkTrafficGenericSrcLat);
	}

	public void setNetworkTrafficGenericSrcLong(int networkTrafficGenericSrcLong) {
		addPair(NETWORK_TRAFFIC_GENERIC_SRC_LONG, networkTrafficGenericSrcLong);
	}

	public void setNetworkTrafficGenericSrcMac(String networkTrafficGenericSrcMac) {
		addPair(NETWORK_TRAFFIC_GENERIC_SRC_MAC, networkTrafficGenericSrcMac);
	}

	public void setNetworkTrafficGenericSrcNtDomain(String networkTrafficGenericSrcNtDomain) {
		addPair(NETWORK_TRAFFIC_GENERIC_SRC_NT_DOMAIN, networkTrafficGenericSrcNtDomain);
	}

	public void setNetworkTrafficGenericSrcNtHost(String networkTrafficGenericSrcNtHost) {
		addPair(NETWORK_TRAFFIC_GENERIC_SRC_NT_HOST, networkTrafficGenericSrcNtHost);
	}

	public void setNetworkTrafficGenericSrcPort(int networkTrafficGenericSrcPort) {
		addPair(NETWORK_TRAFFIC_GENERIC_SRC_PORT, networkTrafficGenericSrcPort);
	}

	public void setNetworkTrafficGenericSrcTranslatedIp(String networkTrafficGenericSrcTranslatedIp) {
		addPair(NETWORK_TRAFFIC_GENERIC_SRC_TRANSLATED_IP, networkTrafficGenericSrcTranslatedIp);
	}

	public void setNetworkTrafficGenericSrcTranslatedPort(int networkTrafficGenericSrcTranslatedPort) {
		addPair(NETWORK_TRAFFIC_GENERIC_SRC_TRANSLATED_PORT, networkTrafficGenericSrcTranslatedPort);
	}

	public void setNetworkTrafficGenericSyslogId(String networkTrafficGenericSyslogId) {
		addPair(NETWORK_TRAFFIC_GENERIC_SYSLOG_ID, networkTrafficGenericSyslogId);
	}

	public void setNetworkTrafficGenericSyslogPriority(String networkTrafficGenericSyslogPriority) {
		addPair(NETWORK_TRAFFIC_GENERIC_SYSLOG_PRIORITY, networkTrafficGenericSyslogPriority);
	}

	public void setNetworkTrafficGenericTcpFlag(String networkTrafficGenericTcpFlag) {
		addPair(NETWORK_TRAFFIC_GENERIC_TCP_FLAG, networkTrafficGenericTcpFlag);
	}

	public void setNetworkTrafficGenericTos(String networkTrafficGenericTos) {
		addPair(NETWORK_TRAFFIC_GENERIC_TOS, networkTrafficGenericTos);
	}

	public void setNetworkTrafficGenericTransport(String networkTrafficGenericTransport) {
		addPair(NETWORK_TRAFFIC_GENERIC_TRANSPORT, networkTrafficGenericTransport);
	}

	public void setNetworkTrafficGenericTtl(int networkTrafficGenericTtl) {
		addPair(NETWORK_TRAFFIC_GENERIC_TTL, networkTrafficGenericTtl);
	}

	public void setNetworkTrafficGenericVlanId(long networkTrafficGenericVlanId) {
		addPair(NETWORK_TRAFFIC_GENERIC_VLAN_ID, networkTrafficGenericVlanId);
	}

	public void setNetworkTrafficGenericVlanName(String networkTrafficGenericVlanName) {
		addPair(NETWORK_TRAFFIC_GENERIC_VLAN_NAME, networkTrafficGenericVlanName);
	}

	public void setPacketFilteringAction(String packetFilteringAction) {
		addPair(PACKET_FILTERING_ACTION, packetFilteringAction);
	}

	public void setPacketFilteringDestPort(int packetFilteringDestPort) {
		addPair(PACKET_FILTERING_DEST_PORT, packetFilteringDestPort);
	}

	public void setPacketFilteringDirection(String packetFilteringDirection) {
		addPair(PACKET_FILTERING_DIRECTION, packetFilteringDirection);
	}

	public void setPacketFilteringDvc(String packetFilteringDvc) {
		addPair(PACKET_FILTERING_DVC, packetFilteringDvc);
	}

	public void setPacketFilteringRule(String packetFilteringRule) {
		addPair(PACKET_FILTERING_RULE, packetFilteringRule);
	}

	public void setPacketFilteringSvcPort(int packetFilteringSvcPort) {
		addPair(PACKET_FILTERING_SVC_PORT, packetFilteringSvcPort);
	}

	public void setProxyAction(String proxyAction) {
		addPair(PROXY_ACTION, proxyAction);
	}

	public void setProxyDest(String proxyDest) {
		addPair(PROXY_DEST, proxyDest);
	}

	public void setProxyHttpContentType(String proxyHttpContentType) {
		addPair(PROXY_HTTP_CONTENT_TYPE, proxyHttpContentType);
	}

	public void setProxyHttpMethod(String proxyHttpMethod) {
		addPair(PROXY_HTTP_METHOD, proxyHttpMethod);
	}

	public void setProxyHttpRefer(String proxyHttpRefer) {
		addPair(PROXY_HTTP_REFER, proxyHttpRefer);
	}

	public void setProxyHttpResponse(int proxyHttpResponse) {
		addPair(PROXY_HTTP_RESPONSE, proxyHttpResponse);
	}

	public void setProxyHttpUserAgent(String proxyHttpUserAgent) {
		addPair(PROXY_HTTP_USER_AGENT, proxyHttpUserAgent);
	}

	public void setProxyProduct(String proxyProduct) {
		addPair(PROXY_PRODUCT, proxyProduct);
	}

	public void setProxySrc(String proxySrc) {
		addPair(PROXY_SRC, proxySrc);
	}

	public void setProxyStatus(int proxyStatus) {
		addPair(PROXY_STATUS, proxyStatus);
	}

	public void setProxyUser(String proxyUser) {
		addPair(PROXY_USER, proxyUser);
	}

	public void setProxyUrl(String proxyUrl) {
		addPair(PROXY_URL, proxyUrl);
	}

	public void setProxyVendor(String proxyVendor) {
		addPair(PROXY_VENDOR, proxyVendor);
	}

	public void setSystemCenterApp(String systemCenterApp) {
		addPair(SYSTEM_CENTER_APP, systemCenterApp);
	}

	public void setSystemCenterFreembytes(long systemCenterFreembytes) {
		addPair(SYSTEM_CENTER_FREEMBYTES, systemCenterFreembytes);
	}

	public void setSystemCenterKernelRelease(String systemCenterKernelRelease) {
		addPair(SYSTEM_CENTER_KERNEL_RELEASE, systemCenterKernelRelease);
	}

	public void setSystemCenterLabel(String systemCenterLabel) {
		addPair(SYSTEM_CENTER_LABEL, systemCenterLabel);
	}

	public void setSystemCenterMount(String systemCenterMount) {
		addPair(SYSTEM_CENTER_MOUNT, systemCenterMount);
	}

	public void setSystemCenterOs(String systemCenterOs) {
		addPair(SYSTEM_CENTER_OS, systemCenterOs);
	}

	public void setSystemCenterPercentprocessortime(int systemCenterPercentprocessortime) {
		addPair(SYSTEM_CENTER_PERCENTPROCESSORTIME, systemCenterPercentprocessortime);
	}

	public void setSystemCenterSetlocaldefs(int systemCenterSetlocaldefs) {
		addPair(SYSTEM_CENTER_SETLOCALDEFS, systemCenterSetlocaldefs);
	}

	public void setSystemCenterSelinux(String systemCenterSelinux) {
		addPair(SYSTEM_CENTER_SELINUX, systemCenterSelinux);
	}

	public void setSystemCenterSelinuxtype(String systemCenterSelinuxtype) {
		addPair(SYSTEM_CENTER_SELINUXTYPE, systemCenterSelinuxtype);
	}

	public void setSystemCenterShell(String systemCenterShell) {
		addPair(SYSTEM_CENTER_SHELL, systemCenterShell);
	}

	public void setSystemCenterSrcPort(int systemCenterSrcPort) {
		addPair(SYSTEM_CENTER_SRC_PORT, systemCenterSrcPort);
	}

	public void setSystemCenterSshdProtocol(String systemCenterSshdProtocol) {
		addPair(SYSTEM_CENTER_SSHD_PROTOCOL, systemCenterSshdProtocol);
	}

	public void setSystemCenterStartmode(String systemCenterStartmode) {
		addPair(SYSTEM_CENTER_STARTMODE, systemCenterStartmode);
	}

	public void setSystemCenterSystemuptime(long systemCenterSystemuptime) {
		addPair(SYSTEM_CENTER_SYSTEMUPTIME, systemCenterSystemuptime);
	}

	public void setSystemCenterTotalmbytes(long systemCenterTotalmbytes) {
		addPair(SYSTEM_CENTER_TOTALMBYTES, systemCenterTotalmbytes);
	}

	public void setSystemCenterUsedmbytes(long systemCenterUsedmbytes) {
		addPair(SYSTEM_CENTER_USEDMBYTES, systemCenterUsedmbytes);
	}

	public void setSystemCenterUser(String systemCenterUser) {
		addPair(SYSTEM_CENTER_USER, systemCenterUser);
	}

	public void setSystemCenterUpdates(long systemCenterUpdates) {
		addPair(SYSTEM_CENTER_UPDATES, systemCenterUpdates);
	}

	public void setTrafficDest(String trafficDest) {
		addPair(TRAFFIC_DEST, trafficDest);
	}

	public void setTrafficDvc(String trafficDvc) {
		addPair(TRAFFIC_DVC, trafficDvc);
	}

	public void setTrafficSrc(String trafficSrc) {
		addPair(TRAFFIC_SRC, trafficSrc);
	}

	public void setUpdatePackage(String updatePackage) {
		addPair(UPDATE_PACKAGE, updatePackage);
	}

	public void setUserInfoUpdatesAffectedUser(String userInfoUpdatesAffectedUser) {
		addPair(USER_INFO_UPDATES_AFFECTED_USER, userInfoUpdatesAffectedUser);
	}

	public void setUserInfoUpdatesAffectedUserGroup(String userInfoUpdatesAffectedUserGroup) {
		addPair(USER_INFO_UPDATES_AFFECTED_USER_GROUP, userInfoUpdatesAffectedUserGroup);
	}

	public void setUserInfoUpdatesAffectedUserGroupId(int userInfoUpdatesAffectedUserGroupId) {
		addPair(USER_INFO_UPDATES_AFFECTED_USER_GROUP_ID, userInfoUpdatesAffectedUserGroupId);
	}

	public void setUserInfoUpdatesAffectedUserId(int userInfoUpdatesAffectedUserId) {
		addPair(USER_INFO_UPDATES_AFFECTED_USER_ID, userInfoUpdatesAffectedUserId);
	}

	public void setUserInfoUpdatesAffectedUserPrivilege(String userInfoUpdatesAffectedUserPrivilege) {
		addPair(USER_INFO_UPDATES_AFFECTED_USER_PRIVILEGE, userInfoUpdatesAffectedUserPrivilege);
	}

	public void setUserInfoUpdatesUser(String userInfoUpdatesUser) {
		addPair(USER_INFO_UPDATES_USER, userInfoUpdatesUser);
	}

	public void setUserInfoUpdatesUserGroup(String userInfoUpdatesUserGroup) {
		addPair(USER_INFO_UPDATES_USER_GROUP, userInfoUpdatesUserGroup);
	}

	public void setUserInfoUpdatesUserGroupId(int userInfoUpdatesUserGroupId) {
		addPair(USER_INFO_UPDATES_USER_GROUP_ID, userInfoUpdatesUserGroupId);
	}

	public void setUserInfoUpdatesUserId(int userInfoUpdatesUserId) {
		addPair(USER_INFO_UPDATES_USER_ID, userInfoUpdatesUserId);
	}

	public void setUserInfoUpdatesUserPrivilege(String userInfoUpdatesUserPrivilege) {
		addPair(USER_INFO_UPDATES_USER_PRIVILEGE, userInfoUpdatesUserPrivilege);
	}

	public void setUserInfoUpdatesUserSubject(String userInfoUpdatesUserSubject) {
		addPair(USER_INFO_UPDATES_USER_SUBJECT, userInfoUpdatesUserSubject);
	}

	public void setUserInfoUpdatesUserSubjectId(int userInfoUpdatesUserSubjectId) {
		addPair(USER_INFO_UPDATES_USER_SUBJECT_ID, userInfoUpdatesUserSubjectId);
	}

	public void setUserInfoUpdatesUserSubjectPrivilege(String userInfoUpdatesUserSubjectPrivilege) {
		addPair(USER_INFO_UPDATES_USER_SUBJECT_PRIVILEGE, userInfoUpdatesUserSubjectPrivilege);
	}

	public void setVulnerabilityCategory(String vulnerabilityCategory) {
		addPair(VULNERABILITY_CATEGORY, vulnerabilityCategory);
	}

	public void setVulnerabilityDest(String vulnerabilityDest) {
		addPair(VULNERABILITY_DEST, vulnerabilityDest);
	}

	public void setVulnerabilityOs(String vulnerabilityOs) {
		addPair(VULNERABILITY_OS, vulnerabilityOs);
	}

	public void setVulnerabilitySeverity(String vulnerabilitySeverity) {
		addPair(VULNERABILITY_SEVERITY, vulnerabilitySeverity);
	}

	public void setVulnerabilitySignature(String vulnerabilitySignature) {
		addPair(VULNERABILITY_SIGNATURE, vulnerabilitySignature);
	}

	public void setWindowsAdminObjectName(String windowsAdminObjectName) {
		addPair(WINDOWS_ADMIN_OBJECT_NAME, windowsAdminObjectName);
	}

	public void setWindowsAdminObjectType(String windowsAdminObjectType) {
		addPair(WINDOWS_ADMIN_OBJECT_TYPE, windowsAdminObjectType);
	}

	public void setWindowsAdminObjectHandle(String windowsAdminObjectHandle) {
		addPair(WINDOWS_ADMIN_OBJECT_HANDLE, windowsAdminObjectHandle);
	}


}
