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
public class IntrusionDetectionEvent extends SplunkEvent {
	
	
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

}
