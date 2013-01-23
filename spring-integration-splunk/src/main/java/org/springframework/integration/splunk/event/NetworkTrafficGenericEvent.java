/*
 * Copyright 2002-2013 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.springframework.integration.splunk.event;

/**
 * @author David Turanski
 *
 */
@SuppressWarnings("serial")
public class NetworkTrafficGenericEvent extends SplunkEvent {
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

}
