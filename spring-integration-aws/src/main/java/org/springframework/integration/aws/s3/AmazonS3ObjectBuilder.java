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
package org.springframework.integration.aws.s3;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.integration.aws.s3.core.AmazonS3Object;
import org.springframework.integration.aws.s3.core.AmazonS3ObjectACL;
import org.springframework.integration.aws.s3.core.Grantee;
import org.springframework.integration.aws.s3.core.GranteeType;
import org.springframework.integration.aws.s3.core.ObjectGrant;
import org.springframework.integration.aws.s3.core.ObjectPermissions;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * The convenience builder class for building {@link AmazonS3Object}
 *
 * @author Amol Nayak
 *
 * @since 0.5
 *
 */
public class AmazonS3ObjectBuilder {

	private final Log logger = LogFactory.getLog(getClass());

	private File file;
	private InputStream in;
	private Map<String, Object> metaData;
	private Map<String, String> userMetaData;
	private AmazonS3ObjectACL objectACL;

	/**
	 * Gets a new instance of the builder
	 * @return
	 */
	public static AmazonS3ObjectBuilder getInstance() {
		return new AmazonS3ObjectBuilder();
	}

	/**
	 * Sets the file which is to be read for uploading into S3
	 * @param file
	 * @return
	 */
	public AmazonS3ObjectBuilder fromFile(File file) {
		Assert.notNull(file,"null 'file' object provided");
		Assert.isNull(in, "Cannot instantiate using both the InputStream and File");
		Assert.isTrue(file.exists(), "Provided File location \""+ file.getAbsolutePath()
				+ "\" is invalid");
		Assert.isTrue(!file.isDirectory(), "Provided File location \""+ file.getAbsolutePath()
				+ "\" is a directory path, please provide a valid file location");
		this.file = file;
		return this;
	}

	/**
	 * Convenience method for setting the File object from the String path
	 * @param fileLocation
	 * @return
	 */
	public AmazonS3ObjectBuilder fromLocation(String fileLocation) {
		Assert.hasText(fileLocation, "'fileLocation should be non null, non empty string");
		return fromFile(new File(fileLocation));
	}

	/**
	 * Sets an InputStream from which the data to be uploaded to S3 will be read
	 * @param in
	 * @return
	 */
	public AmazonS3ObjectBuilder fromInputStream(InputStream in) {
		Assert.notNull(in, "The Stream object provided is null");
		Assert.isNull(file, "Cannot instantiate using both the InputStream and File");
		this.in = in;
		return this;
	}

	/**
	 * Use the given user meta data for the file to be uploaded
	 * @param userMetaData
	 * @return
	 */
	public AmazonS3ObjectBuilder withUserMetaData(Map<String, String> userMetaData) {
		this.userMetaData = userMetaData;
		return this;
	}

	/**
	 * uses the given metadata for the S3 object to be uploaded
	 * @param metaData
	 * @return
	 */
	public AmazonS3ObjectBuilder withMetaData(Map<String, Object> metaData) {
		this.metaData = metaData;
		return this;
	}

	/**
	 * Sets the S3 Object ACL
	 * @param objectACL
	 * @return
	 */
	public AmazonS3ObjectBuilder withObjectACL(Map<String, Collection<String>> objectACL) {
		//The key can be of three types, the email id, the canonical id of the user or the Group identifier
		if(objectACL != null && !objectACL.isEmpty()) {
			this.objectACL = new AmazonS3ObjectACL();
			for(String key:objectACL.keySet()) {
				if(isCanonicalId(key)) {
					addPermissions(key, GranteeType.CANONICAL_GRANTEE_TYPE, objectACL.get(key));
				}
				else if(isGroupIdentifier(key)) {
					addPermissions(key, GranteeType.GROUP_GRANTEE_TYPE, objectACL.get(key));
				}
				else {
					//assuming thats an email identifier, not using regex to validate the email
					//id. We can add email validation if needed and throw an eexception
					//if some unexpected value comes up.
					addPermissions(key, GranteeType.EMAIL_GRANTEE_TYPE, objectACL.get(key));
				}
			}
		}
		return this;
	}

	/**
	 * Internal helper method for adding the Grants to the object ACL
	 * @param grantee
	 * @param type
	 * @param permissions
	 */
	private void addPermissions(String grantee,GranteeType type,Collection<String> permissions) {
		for(String permission:permissions) {
			ObjectPermissions objectPermission = getObjectPermission(permission);
			this.objectACL.addGrant(new ObjectGrant
					(new Grantee(grantee,type), objectPermission));
		}
	}

	/**
	 * Gets the Appropriate {@link ObjectPermissions} based on the String passed
	 * @param permission
	 * @return
	 */
	private ObjectPermissions getObjectPermission(String permission) {
		//Types of permission are READ, READ_ACP, WRITE_ACP
		if(StringUtils.hasText(permission)) {
			String permissionString = permission.trim().replaceAll("[ ]+", "_").toUpperCase();
			try {
				return ObjectPermissions.valueOf(permissionString);
			} catch (IllegalArgumentException e) {
				logger.error("Requested Enum not found, see underlying exception for more details", e);
				throw e;
			}
		}
		else {
			throw new IllegalArgumentException("Empty or null string found for object permission");
		}
	}

	/**
	 * Method checks if the provided String is a canonical id of the AWS account
	 * @param identifier
	 * @return
	 */
	private boolean isCanonicalId(String identifier) {
		//Note: we do not check if the given id is a valid canonical id with AWS, we just check if it is in the right format
		if(StringUtils.hasText(identifier)) {
			if(identifier.length() == 64) {
				String replacedString = identifier.trim().replaceAll("[a-fA-F0-9]+","");
				return replacedString.length() == 0;
			}
		}
		return false;
	}

	/**
	 * Checks if the given identifier corresponds to a group id
	 * @param identifier
	 * @return
	 */
	private boolean isGroupIdentifier(String identifier){
		if(StringUtils.hasText(identifier)) {
			String trimmedIdentifier = identifier.trim();
			return trimmedIdentifier.equals("http://acs.amazonaws.com/groups/global/AllUsers")
					|| trimmedIdentifier.equals("http://acs.amazonaws.com/groups/global/AuthenticatedUsers");
		}
		return false;
	}

	/**
	 * Builds the {@link AmazonS3Object} from the provided meta data, file/input stream and object ACLs
	 * @return
	 */
	public AmazonS3Object build() {
		Assert.isTrue(!(in == null && file == null),"One of File object or InputStream is required");
		return new AmazonS3Object(userMetaData, metaData, in, file,objectACL);
	}
}
