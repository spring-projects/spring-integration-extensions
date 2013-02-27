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

import static org.springframework.integration.aws.s3.core.ObjectPermissions.READ;
import static org.springframework.integration.aws.s3.core.ObjectPermissions.READ_ACP;
import static org.springframework.integration.aws.s3.core.ObjectPermissions.WRITE_ACP;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.integration.aws.s3.core.AmazonS3Object;
import org.springframework.integration.aws.s3.core.AmazonS3ObjectACL;
import org.springframework.integration.aws.s3.core.Grantee;
import org.springframework.integration.aws.s3.core.GranteeType;
import org.springframework.integration.aws.s3.core.ObjectGrant;
import org.springframework.integration.aws.s3.core.ObjectPermissions;
import org.springframework.integration.test.util.TestUtils;

/**
 * The test case for the class {@link AmazonS3ObjectBuilder}
 *
 * @author Amol Nayak
 *
 * @since 0.5
 *
 */
public class AmazonS3ObjectBuilderTests {

	@Rule
	public static TemporaryFolder temp = new TemporaryFolder();

	private static final String GROUP_GRANTEE = "http://acs.amazonaws.com/groups/global/AllUsers";
	private static final String EMAIL_GRANTEE = "test@test.com";
	private static final String CANONICAL_GRANTEE = "12345678900987654321abcdefabcdeab12345678900987654321abcdefabcde";

	/**
	 * Tries to construct the object with a null file instance
	 */
	@Test(expected=IllegalArgumentException.class)
	public void withNullFile() {
		AmazonS3ObjectBuilder.getInstance().fromFile(null);
	}

	/**
	 * Tries to construct the file from a non existent file
	 */
	@Test(expected=IllegalArgumentException.class)
	public void withNonExistentFile() {
		AmazonS3ObjectBuilder.getInstance().fromFile(new File("somejunkfile"));
	}

	/**
	 * Tries to construct the file which is a directory
	 */
	@Test(expected=IllegalArgumentException.class)
	public void withADirectory() {
		File dir = temp.newFolder("tempdir");
		AmazonS3ObjectBuilder.getInstance().fromFile(dir);
		dir.delete();
	}

	/**
	 * Tries to construct the {@link AmazonS3Object} from a null location
	 */
	@Test(expected=IllegalArgumentException.class)
	public void withNullPathString() {
		AmazonS3ObjectBuilder.getInstance().fromLocation(null);
	}

	/**
	 * Tries to construct the {@link AmazonS3Object} from a valid file
	 */
	@Test
	public void withValidFile() throws Exception {
		File file = temp.newFile("temp.txt");
		String pathname = file.getAbsolutePath();
		AmazonS3ObjectBuilder builder =
			AmazonS3ObjectBuilder.getInstance().fromLocation(pathname);
		file = TestUtils.getPropertyValue(builder, "file", File.class);
		Assert.assertNotNull(file);
		Assert.assertEquals(pathname, file.getAbsolutePath());
	}

	/**
	 * Tries to construct the {@link AmazonS3Object} from an {@link InputStream}
	 */
	@Test
	public void withInputStream()throws Exception  {
		File tempFile = temp.newFile("Temp.txt");
		InputStream in = new FileInputStream(tempFile);
		AmazonS3ObjectBuilder builder =
			AmazonS3ObjectBuilder.getInstance().fromInputStream(in);
		Assert.assertNotNull(TestUtils.getPropertyValue(builder, "in", InputStream.class));
		in.close();
	}

	/**
	 * Tries to construct the {@link AmazonS3Object} from an {@link InputStream} and a {@link File}
	 * instance
	 */
	@Test(expected=IllegalArgumentException.class)
	public void withBothinputStreamAndFile() throws Exception {
		File tempFile = temp.newFile("Temp.txt");
		FileInputStream in = new FileInputStream(tempFile);
		AmazonS3ObjectBuilder
			.getInstance()
			.fromInputStream(in)
			.fromFile(tempFile);
		in.close();
	}

	/**
	 * Constructs the {@link AmazonS3Object} with some user metadata
	 */
	@Test
	public void withUserMetadata() throws Exception {
		File tempFile = temp.newFile("Temp.txt");
		AmazonS3ObjectBuilder builder = AmazonS3ObjectBuilder
										.getInstance()
										.fromFile(tempFile)
										.withUserMetaData(Collections.singletonMap("Key", "Value"));
		Assert.assertNotNull(TestUtils.getPropertyValue(builder, "userMetaData", Map.class));
	}

	/**
	 * Constructs the {@link AmazonS3Object} with some metadata
	 */
	@Test
	public void withMetadata() throws Exception {
		File tempFile = temp.newFile("Temp.txt");
		AmazonS3ObjectBuilder builder = AmazonS3ObjectBuilder
										.getInstance()
										.fromFile(tempFile)
										.withMetaData(Collections.singletonMap("Key", (Object)"Value"));
		Assert.assertNotNull(TestUtils.getPropertyValue(builder, "metaData", Map.class));
	}

	/**
	 * Constructs the {@link AmazonS3Object} with an invalid ACL identifier
	 */
	@Test
	public void withValidACL() throws Exception {
		File tempFile = temp.newFile("Temp.txt");
		Map<String, Collection<String>> acls = generateObjectACLS();
		AmazonS3ObjectBuilder builder = AmazonS3ObjectBuilder
											.getInstance()
											.fromFile(tempFile)
											.withObjectACL(acls);
		AmazonS3ObjectACL acl = TestUtils.getPropertyValue(builder, "objectACL", AmazonS3ObjectACL.class);
		assertGrants(acl);
	}

	/**
	 * Builds a complete object with all the possible attributes and checks
	 * if those are properly populated in the constructed {@link AmazonS3Object}
	 */
	@Test
	public void withCompleteObjectFromFile() throws Exception {
		File tempFile = temp.newFile("Temp.txt");
		Map<String, Collection<String>> acls = generateObjectACLS();
		Map<String, String> userMetadata = Collections.singletonMap("Key", "Value");
		Map<String, Object> metadata = Collections.singletonMap("Key", (Object)"Value");
		AmazonS3ObjectBuilder builder = AmazonS3ObjectBuilder
											.getInstance()
											.fromFile(tempFile)
											.withObjectACL(acls)
											.withMetaData(metadata)
											.withUserMetaData(userMetadata);
		AmazonS3Object object = builder.build();
		assertGrants(object.getObjectACL());
		Assert.assertEquals(userMetadata, object.getUserMetaData());
		Assert.assertEquals(metadata, object.getMetaData());
		Assert.assertEquals(tempFile, object.getFileSource());
		Assert.assertNull(object.getInputStream());
	}

	/**
	 * Builds an object with an {@link InputStream}
	 */
	@Test
	public void wothObjectFromStream() throws Exception {
		File tempFile = temp.newFile("Temp.txt");
		InputStream in = new FileInputStream(tempFile);
		AmazonS3ObjectBuilder builder = AmazonS3ObjectBuilder
											.getInstance()
											.fromInputStream(in);
		AmazonS3Object object = builder.build();
		Assert.assertNull(object.getFileSource());
		Assert.assertEquals(in,object.getInputStream());
		in.close();
	}



	/**
	 * Generate the object test ACLS
	 * @return
	 */
	private Map<String, Collection<String>> generateObjectACLS() {
		Map<String, Collection<String>> acls = new HashMap<String, Collection<String>>();
		acls.put(CANONICAL_GRANTEE,
				Arrays.asList("write acp"));
		acls.put(EMAIL_GRANTEE,
				Arrays.asList("read acp", "write acp"));
		acls.put(GROUP_GRANTEE,
				Arrays.asList("read"));
		return acls;
	}

	/**
	 * Checks for the grants in the object
	 *
	 */
	private void assertGrants(AmazonS3ObjectACL acl) {
		Set<ObjectGrant> grants = acl.getGrants();
		Assert.assertEquals(4, grants.size());
		for(ObjectGrant grant:grants) {
			Grantee grantee = grant.getGrantee();
			GranteeType type = grantee.getGranteeType();
			ObjectPermissions permission = grant.getPermission();
			if(type == GranteeType.CANONICAL_GRANTEE_TYPE) {
				Assert.assertEquals(CANONICAL_GRANTEE, grantee.getIdentifier());
				Assert.assertEquals(WRITE_ACP,permission);
			}
			else if(type == GranteeType.EMAIL_GRANTEE_TYPE){
				Assert.assertEquals(EMAIL_GRANTEE, grantee.getIdentifier());
				Assert.assertTrue(permission == WRITE_ACP || permission == READ_ACP);
			}
			else if(type == GranteeType.GROUP_GRANTEE_TYPE) {
				Assert.assertEquals(GROUP_GRANTEE, grantee.getIdentifier());
				Assert.assertEquals(READ,permission);
			}
		}
	}



}
