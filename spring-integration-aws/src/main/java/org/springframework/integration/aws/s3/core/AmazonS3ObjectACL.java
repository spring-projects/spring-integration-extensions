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

import java.util.HashSet;
import java.util.Set;

import org.springframework.util.Assert;

/**
 * The object containing the Amazon S3 Object's ACL. Access is used to control the access to
 * the resource in S3 bucket
 *
 * @author Amol Nayak
 *
 * @since 0.5
 *
 */
public class AmazonS3ObjectACL {

	private Set<ObjectGrant> grants = new HashSet<ObjectGrant>();

	/**
	 * Gets all the grants on the object in the bucket
	 */
	public Set<ObjectGrant> getGrants() {
		return grants;
	}

	/**
	 * Sets the provided grants on the S3 object
	 *
	 * @param grants
	 */
	public void setGrants(Set<ObjectGrant> grants) {
		Assert.notNull(grants, "Provide non null 'grants'");
		this.grants = grants;
	}

	/**
	 * A convenience method that will be used to add grants to the objects.
	 *
	 * @param grant
	 */
	public void addGrant(ObjectGrant grant) {
		Assert.notNull(grant,"Provide non null object grant");
		grants.add(grant);
	}
}
