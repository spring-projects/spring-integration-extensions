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
public class NetworkTrafficEssEvent extends SplunkEvent {
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

}
