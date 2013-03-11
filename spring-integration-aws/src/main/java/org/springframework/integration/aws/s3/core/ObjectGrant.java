/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.integration.aws.s3.core;

/**
 * Represent one Grant for a Grantee and the associated permissions
 *
 * @author Amol Nayak
 *
 * @since 0.5
 *
 */
public class ObjectGrant {

	private final Grantee grantee;
	private final ObjectPermissions permission;


	/**
	 * Instantiate an Object grant for the given grantee and with given permissions
	 * @param grantee
	 * @param permission
	 */
	public ObjectGrant(Grantee grantee, ObjectPermissions permission) {
		super();
		this.grantee = grantee;
		this.permission = permission;
	}

	/**
	 * Gets the grantee for this particular object permission
	 */
	public Grantee getGrantee() {
		return grantee;
	}

	/**
	 * Gets the Corresponding object permission
	 */
	public ObjectPermissions getPermission() {
		return permission;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((grantee == null) ? 0 : grantee.hashCode());
		result = prime * result
				+ ((permission == null) ? 0 : permission.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ObjectGrant other = (ObjectGrant) obj;
		if (grantee == null) {
			if (other.grantee != null)
				return false;
		} else if (!grantee.equals(other.grantee))
			return false;
		if (permission != other.permission)
			return false;
		return true;
	}
}
