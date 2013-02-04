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
public class TrafficEvent extends SplunkEvent {

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

	public void setTrafficDest(String trafficDest) {
		addPair(TRAFFIC_DEST, trafficDest);
	}

	public void setTrafficDvc(String trafficDvc) {
		addPair(TRAFFIC_DVC, trafficDvc);
	}

	public void setTrafficSrc(String trafficSrc) {
		addPair(TRAFFIC_SRC, trafficSrc);
	}
}
