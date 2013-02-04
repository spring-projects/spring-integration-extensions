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
public class UserInfoUpdateEvent extends SplunkEvent {
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

}
