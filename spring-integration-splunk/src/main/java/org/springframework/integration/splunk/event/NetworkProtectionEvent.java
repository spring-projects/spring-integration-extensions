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
public class NetworkProtectionEvent extends SplunkEvent {
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
}
