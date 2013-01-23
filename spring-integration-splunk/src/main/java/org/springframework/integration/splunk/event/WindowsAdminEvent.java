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
public class WindowsAdminEvent extends SplunkEvent {
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
