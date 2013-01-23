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
public class EndpointProtectionEvent extends SplunkEvent {

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


}
