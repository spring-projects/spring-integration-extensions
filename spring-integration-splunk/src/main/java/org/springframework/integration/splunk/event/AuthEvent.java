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
public class AuthEvent extends SplunkEvent {
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
	
	public void setAuthAction(String authAction) {
		addPair(AUTH_ACTION, authAction);
	}
}
