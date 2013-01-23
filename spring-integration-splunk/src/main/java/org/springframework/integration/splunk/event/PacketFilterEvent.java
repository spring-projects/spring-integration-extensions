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
public class PacketFilterEvent extends SplunkEvent {

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
}
