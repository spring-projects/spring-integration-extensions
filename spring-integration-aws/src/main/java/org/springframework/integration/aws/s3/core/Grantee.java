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
 * Indicates the Grantee who is being given the Access to a particular resource on S3
 *
 * @author Amol Nayak
 *
 * @since 0.5
 *
 */
public class Grantee {

	private String identifier;
	private GranteeType granteeType;

	public Grantee() {

	}

	public Grantee(String identifier, GranteeType granteeType) {
		this.identifier = identifier;
		this.granteeType = granteeType;
	}

	/**
	 * The identifier of a particular type identifying the grantee
	 * @param id
	 */
	void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * Gets the particular identifier representing the grantee
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Gets the Particular grantee Type
	 */
	public GranteeType getGranteeType() {
		return granteeType;
	}

	/**
	 * Sets the Type of the grantee see {@link GranteeType} for more information
	 * @param granteeType
	 */
	public void setGranteeType(GranteeType granteeType) {
		this.granteeType = granteeType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((granteeType == null) ? 0 : granteeType.hashCode());
		result = prime * result
				+ ((identifier == null) ? 0 : identifier.hashCode());
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
		Grantee other = (Grantee) obj;
		if (granteeType != other.granteeType)
			return false;
		if (identifier == null) {
			if (other.identifier != null)
				return false;
		} else if (!identifier.equals(other.identifier))
			return false;
		return true;
	}




}
