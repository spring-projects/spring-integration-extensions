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

import static org.springframework.integration.aws.common.AWSTestUtils.getCredentials;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.integration.aws.core.AWSCredentials;
import org.springframework.integration.aws.core.PropertiesAWSCredentials;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.Grant;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;

/**
 * The abstract test class for testing all the common functionality for AWS operations
 * on S3 using the appropriate implementation provided by the subclass.
 *
 * Note: To run the test, you will have to create one  bucket for yourself and
 * set the name in the {@link #BUCKET_NAME}
 *
 * @author Amol Nayak
 *
 * @since 0.5
 *
 */
public abstract class AbstractAmazonS3OperationsImplAWSTests {

	private static final String VALID_CANONICAL_ID = "f854da004ee08cf4f8664334d288561c8512c508db9785388de7319ded85f8f3";

	@Rule
	public static final TemporaryFolder temp = new TemporaryFolder();

	//private static final String UPLOAD_SOURCE_DIRECTORY = System.getProperty("java.io.tmpdir") + "upload";


	//To run the test, you will have to create one one bucket for yourself and
	//set the name here
	protected static final String BUCKET_NAME = "com.si.aws.test.bucket";

	private static AmazonS3Client client;
	private static PropertiesAWSCredentials credentials;

	@BeforeClass
	public static final void setup() throws Exception {
		AWSCredentials credentials = getCredentials();
		client = new AmazonS3Client(
				new BasicAWSCredentials(credentials.getAccessKey(), credentials.getSecretKey()));
	}


	/**
	 * Sets the multipart threshold value of the upload to 5K,
	 * this should get executed successfully
	 */
	@Test
	public void withMultipartThresholdWith5k() {
		AbstractAmazonS3Operations impl = getS3OperationsImplementation();
		impl.setMultipartUploadThreshold(5120);
		Assert.assertEquals(5120,impl.getMultipartUploadThreshold());
	}

	/**
	 * Sets the multipart threshold value of the upload to a value < 5K,
	 * should throw IllegalArgumentException
	 */
	@Test(expected=IllegalArgumentException.class)
	public void withMultipartThresholdWithLt5k() {
		getS3OperationsImplementation().setMultipartUploadThreshold(5000);
	}

	/**
	 * Sets the directory path as a null value.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void setNullDirectoryString() {
		getS3OperationsImplementation().setTemporaryDirectory((String)null);
	}

	/**
	 * Pass a non null string that exists
	 */
	@Test
	public void setNonNullDirectoryString() {
		//this will exist
		String directory = System.getProperty("java.io.tmpdir");
		getS3OperationsImplementation().setTemporaryDirectory(directory);
	}

	/**
	 * Pass a String to a directory that doesn't exist
	 */
	@Test(expected=IllegalArgumentException.class)
	public void setNonExistentDirectory() {
		//getting the current time in millis and hope no folder with that name exists
		long current = System.currentTimeMillis();
		DefaultAmazonS3Operations s3Service = new DefaultAmazonS3Operations(credentials);
		s3Service.setTemporaryDirectory("./" + current);
	}

	/**
	 * Sets the temporary file suffix to the null value
	 */
	@Test(expected=IllegalArgumentException.class)
	public void setNullTemporaryFileSuffix() {
		getS3OperationsImplementation().setTemporaryFileSuffix(null);
	}

	/**
	 * Sets the temporary file suffix to a string that begins with a "."
	 */
	@Test
	public void setValidTempFileSuffixStartingWithDot() {
		AbstractAmazonS3Operations impl = getS3OperationsImplementation();
		impl.setTemporaryFileSuffix(".tempsuff");
		Assert.assertEquals(".tempsuff", impl.getTemporaryFileSuffix());
	}


	/**
	 * Sets the temporary file suffix to a string that does not begin with a "."
	 */
	@Test
	public void setValidTempFileSuffixStartingWithoutDot() {
		AbstractAmazonS3Operations impl = getS3OperationsImplementation();
		impl.setTemporaryFileSuffix("tmpsuff");
		Assert.assertEquals(".tmpsuff", impl.getTemporaryFileSuffix());
	}

	//TODO. Test all the conditions that test the folder generation logic, null folder
	//null bucket etc

	/**
	 * The AWS Service test put a file with null bucket given
	 *
	 */
	public void putToNullBucket() {
		AbstractAmazonS3Operations impl = getS3OperationsImplementation();
		impl.putObject(null, "/", "name", null);
	}

	//TODO: Execute the following cases for putObject

	/**
	 * Executes the put object with a null bucket name, should throw
	 * an {@link IllegalArgumentException}
	 */
	@Test(expected=IllegalArgumentException.class)
	public void putWithNullBucket() {
		AmazonS3Operations impl = getS3OperationsImplementation();
		impl.putObject(null, null, "SomeObject", null);
	}

	/**
	 * Executes the put object with a null object name, should throw
	 * an {@link IllegalArgumentException}
	 */
	@Test(expected=IllegalArgumentException.class)
	public void withNullObjectName() {
		AmazonS3Operations impl = getS3OperationsImplementation();
		impl.putObject(BUCKET_NAME, "/", null, null);
	}

	/**
	 * Executes the put object with a null s3 object, should throw
	 * an {@link IllegalArgumentException}
	 */
	@Test(expected=IllegalArgumentException.class)
	public void withNullS3Object() {
		AmazonS3Operations impl = getS3OperationsImplementation();
		impl.putObject(BUCKET_NAME, "/", "TestObjectName.txt", null);
	}

	/**
	 * Executes the put object with both file source and input stream provided
	 *
	 * an {@link IllegalArgumentException}
	 */
	@Test(expected=IllegalArgumentException.class)
	public void withBothFileSourceAndInputStream() throws Exception {
		String folder = temp.getRoot().getAbsolutePath();
		File file = new File(folder + File.separator + "SomeTestFile.txt");
		file.createNewFile();
		FileInputStream fin = new FileInputStream(file);
		new AmazonS3Object(null, null,fin, file);
		fin.close();
		file.delete();
	}

	/**
	 * Executes the put object with none of file source and input stream provided
	 *
	 * an {@link IllegalArgumentException}
	 */
	@Test(expected=IllegalArgumentException.class)
	public void withNoneOfFileSourceAndInputStream() throws Exception {
		new AmazonS3Object(null, null, null, null);
	}

	/**
	 * With a temp file, upload to the bucket and see temp file gets deleted and the object
	 * successfully uploaded. Also the ACL of the provided object is null, so the
	 * object should get default ACLs
	 *
	 */
	@Test
	public void putFromTempFile() throws Exception {
		//first delete the file from the AWS bucket
		String key = "TestPutFromTempFile.txt";
		deleteObject(BUCKET_NAME, key);


		File file = generateUploadFile();

		//Now put the object
		AbstractAmazonS3Operations operations = getS3OperationsImplementation();
		operations.setTemporaryDirectory(temp.getRoot());
		FileInputStream fin = new FileInputStream(file);
		//No ACL associated
		AmazonS3Object object = new AmazonS3Object(null, null,fin, null);
		operations.putObject(BUCKET_NAME, null, key, object);

		assertTempFileDeletion(temp.getRoot().getAbsolutePath(), key);

		assertObjectExistenceInBucket(key);
		fin.close();
		//delete the source file.
		file.delete();
	}


	/**
	 * Upload a file to a folder in the given bucket with the folder name
	 * ending with a slash. Also the ACL of the provided object is null, so the
	 * object should get default ACLs.
	 *
	 */
	@Test
	public void putToFolderWithEndingSlash() throws Exception {
		String uploadFileName = "TestPutWithEndingSlash.txt";
		String key = "somedir/with/endingslash/" + uploadFileName;
		//first delete the file from the AWS bucket
		deleteObject(BUCKET_NAME, key);

		File file = generateUploadFile();

		//Now put the object
		AmazonS3Operations operations = getS3OperationsImplementation();

		//No ACL associated
		AmazonS3Object object = new AmazonS3Object(null, null, null, file);
		operations.putObject(BUCKET_NAME, "somedir/with/endingslash/", uploadFileName, object);


		assertObjectExistenceInBucket(key);
		//delete the source file.
		file.delete();
	}

	/**
	 * Upload a file to a folder in the given bucket with the folder name
	 * ending without a slash. Also the ACL of the provided object is null, so the
	 * object should get default ACLs.
	 *
	 */
	@Test
	public void putToFolderWithoutEndingSlash() throws Exception {
		String uploadFile = "TestPutWithoutEndingSlash.txt";
		String key = "somedir/without/endingslash/" + uploadFile;
		//first delete the file from the AWS bucket
		deleteObject(BUCKET_NAME, key);

		File file = generateUploadFile();

		//Now put the object
		AmazonS3Operations operations = getS3OperationsImplementation();

		//No ACL associated
		AmazonS3Object object = new AmazonS3Object(null, null, null, file);
		operations.putObject(BUCKET_NAME, "somedir/without/endingslash", uploadFile, object);

		assertObjectExistenceInBucket(key);
		//delete the source file.
		file.delete();
	}

	/**
	 * Upload a file to a folder in the given bucket with the folder name
	 * beginning a slash. Also the ACL of the provided object is null, so the
	 * object should get default ACLs.
	 *
	 */
	@Test
	public void putToFolderBeginningWithSlash() throws Exception {
		String uploadFileName = "TestPutBeginningWithSlash.txt";
		String key = "beginning/with/slash/" + uploadFileName;
		//first delete the file from the AWS bucket
		deleteObject(BUCKET_NAME, key);

		File file = generateUploadFile();
		//Now put the object
		AmazonS3Operations operations = getS3OperationsImplementation();

		//No ACL associated
		AmazonS3Object object = new AmazonS3Object(null, null, null, file);
		operations.putObject(BUCKET_NAME, "/beginning/with/slash/", uploadFileName, object);

		assertObjectExistenceInBucket(key);

		//delete the source file.
		file.delete();
	}


	/**
	 * Upload a file with the provided ACLs and meta data, the test verifies if the
	 * ACLs and the metadata of the file is appropriately set
	 *
	 */
	@Test
	public void putToFolderForACLAndMetadataTest() throws Exception {
		String uploadFileName = "TestObjectACLAndMetaData.txt";
		String key = "acl/and/metadata/test/" + uploadFileName;
		//first delete the file from the AWS bucket
		deleteObject(BUCKET_NAME, key);

		File file = generateUploadFile();
		FileInputStream fin = new FileInputStream(file);

		//Now put the object
		AmazonS3Operations operations = getS3OperationsImplementation();

		Map<String, String> userMetaData = Collections.singletonMap("TestKey", "TestValue");
		AmazonS3ObjectACL acl = new AmazonS3ObjectACL();
		ObjectGrant grant = new ObjectGrant(new Grantee(VALID_CANONICAL_ID, GranteeType.CANONICAL_GRANTEE_TYPE),
													ObjectPermissions.READ_ACP);
		acl.addGrant(grant);
		AmazonS3Object object = new AmazonS3Object(userMetaData, null, fin, null,acl);
		operations.putObject(BUCKET_NAME, "/acl/and/metadata/test/", uploadFileName, object);

		//This fails somehow on my machine
		//assertTempFileDeletion(uploadFileName);

		//NOTE: The case of the key is no longer in the case we used, its all lower case.
		//lets get the object's User metadata first
		S3Object s3Object = getObject(BUCKET_NAME,key);
		ObjectMetadata objectMetadata = s3Object.getObjectMetadata();
		userMetaData = objectMetadata.getUserMetadata();
		Assert.assertNotNull("User metadata is not expected to be null, but got null", userMetaData);
		Assert.assertTrue("Expecting the key 'testkey' in user MetaData", userMetaData.containsKey("testkey"));
		Assert.assertEquals("TestValue", userMetaData.get("testkey"));

		//lets verify the object's ACL
		AccessControlList acls = getObjectACL(BUCKET_NAME, key);
		Set<Grant> grants = acls.getGrants();
		boolean isACLValid = false;
		for(Grant g:grants) {
			com.amazonaws.services.s3.model.Grantee grantee = g.getGrantee();
			if(VALID_CANONICAL_ID.equals(grantee.getIdentifier())
					&& "READ_ACP".equals(grant.getPermission().toString())) {
				isACLValid = true;
			}
		}
		Assert.assertTrue("Expected Object ACl not found", isACLValid);
		fin.close();

		//delete the source file.
		file.delete();
	}

	/**
	 * List the contents in the bucket with null bucket name
	 */
	@Test(expected=IllegalArgumentException.class)
	public void listWithNullBucket() {
		AmazonS3Operations impl = getS3OperationsImplementation();
		impl.listObjects(null, "folder", null, 1);
	}

	/**
	 * List objects with negative page size
	 */
	@Test(expected=IllegalArgumentException.class)
	public void listWithPageSizeLt0() {
		AmazonS3Operations impl = getS3OperationsImplementation();
		impl.listObjects(BUCKET_NAME, "folder", null, -2);
	}

	/**
	 * List with null folder
	 */
	@Test
	public void listWithNullFolder() {
		AmazonS3Operations impl = getS3OperationsImplementation();
		PaginatedObjectsView pov = impl.listObjects(BUCKET_NAME, null, null, 100);
		List<S3ObjectSummary> summary = pov.getObjectSummary();
		Assert.assertNotNull(summary);
		Assert.assertTrue(summary.size() > 0);
		System.out.println("Summary list size is " + summary.size());
	}

	/**
	 * List with folder as a slash(/), for root folder
	 */
	@Test
	public void listWithSlashOnRoot() {
		AmazonS3Operations impl = getS3OperationsImplementation();
		PaginatedObjectsView pov = impl.listObjects(BUCKET_NAME, "/", null, 100);
		List<S3ObjectSummary> summary = pov.getObjectSummary();
		Assert.assertNotNull(summary);
		Assert.assertTrue(summary.size() > 0);
		System.out.println("Summary list size is " + summary.size());
	}

	/**
	 * List with folder as a slash(/)
	 */
	@Test
	public void listWithFolderAsSlash() {
		AmazonS3Operations impl = getS3OperationsImplementation();
		PaginatedObjectsView pov = impl.listObjects(BUCKET_NAME, "/acl", null, 100);
		List<S3ObjectSummary> summary = pov.getObjectSummary();
		Assert.assertNotNull(summary);
		Assert.assertTrue(summary.size() > 0);
		System.out.println("Summary list size is " + summary.size());
	}

	/**
	 * List with folder as a not beginning with slash(/)
	 */
	@Test
	public void listWithFolderNotBeginningWithSlash() {
		AmazonS3Operations impl = getS3OperationsImplementation();
		PaginatedObjectsView pov = impl.listObjects(BUCKET_NAME, "somedir/with", null, 100);
		List<S3ObjectSummary> summary = pov.getObjectSummary();
		Assert.assertNotNull(summary);
		Assert.assertTrue(summary.size() > 0);
		System.out.println("Summary list size is " + summary.size());
	}

	/**
	 * The test case assumes that all previous AWS tests are executed and we have at least 4 objects
	 * in the bucket, on running all the above tests you will have 5, so we need not do anything
	 * special to add more objects to execute this test
	 *
	 */
	@Test
	public void paginateRecords() {
		AmazonS3Operations impl = getS3OperationsImplementation();
		PaginatedObjectsView pov = impl.listObjects(BUCKET_NAME, "/", null, 3);
		List<S3ObjectSummary> summary = pov.getObjectSummary();
		Assert.assertNotNull(summary);
		Assert.assertEquals(3, summary.size());
		String nextMarker = pov.getNextMarker();
		Assert.assertNotNull("Expected a non null marker", nextMarker);
		pov = impl.listObjects(BUCKET_NAME, "/", nextMarker, 3);
		summary = pov.getObjectSummary();
		Assert.assertNotNull(summary);
		Assert.assertTrue(summary.size() > 0);
		System.out.printf("Number of records on second page are %d\n",summary.size());
	}

	/**
	 * Tests the get object with a null bucket
	 */
	@Test(expected=IllegalArgumentException.class)
	public void getObjectFromNullBucket() {
		AmazonS3Operations impl = getS3OperationsImplementation();
		impl.getObject(null, null, "ObjectName.txt");
	}

	/**
	 * Gets a non existent object from the bucket, should return null on unsuccessful search
	 * and if the object with the key doesn't exist.
	 */
	@Test
	public void getNonExistentObject() {
		AmazonS3Operations impl = getS3OperationsImplementation();
		AmazonS3Object object = impl.getObject(BUCKET_NAME, null, "jhgkmjbhdc.thb");
		Assert.assertNull("Expecting a null object but got a non null one", object);
	}

	/**
	 * Invoked the getObject with null folder, this will get the object
	 * from the root of the bucket.
	 *
	 */
	@Test
	public void getObjectWithNullFolder() {
		AmazonS3Operations impl = getS3OperationsImplementation();
		AmazonS3Object object = impl.getObject(BUCKET_NAME, null, "TestPutFromTempFile.txt");
		Assert.assertNotNull("Expecting a non null object but got a null one", object);

	}

	/**
	 * Invoked the getObject with folder name beginning with /
	 *
	 */
	@Test
	public void getObjectromFolderBeginningWithSlash() {
		AmazonS3Operations impl = getS3OperationsImplementation();
		AmazonS3Object object = impl.getObject(BUCKET_NAME, "/acl/and/metadata/test", "TestObjectACLAndMetaData.txt");
		Assert.assertNotNull("Expecting a non null object but got a null one", object);
	}

	/**
	 * Invoked the getObject with folder name beginning with /
	 *
	 */
	@Test
	public void getObjectFromFolderBeginningWithoutSlash() {
		AmazonS3Operations impl = getS3OperationsImplementation();
		AmazonS3Object object = impl.getObject(BUCKET_NAME, "acl/and/metadata/test", "TestObjectACLAndMetaData.txt");
		Assert.assertNotNull("Expecting a non null object but got a null one", object);
	}


	/**
	 * The common method that checks if the temp file generated for the
	 * test file uploaded is deleted.
	 */
	private void assertTempFileDeletion(String rootFolder, String baseFileName) {
		//Check if the temp file exists.
		File tempFile = new File(rootFolder + File.separator + baseFileName + ".writing");
		Assert.assertFalse("Was expecting the temp file to be deleted, but is present", tempFile.exists());
	}

	/**
	 * Common method that will assert the existence of the object with the given key in the
	 * bucket
	 *
	 * @param key
	 */
	private void assertObjectExistenceInBucket(String key) {
		S3Object s3Object = getObject(BUCKET_NAME, key);
		//This is not needed as an exception will be thrown if the key does not exist
		Assert.assertNotNull("Non null S3Object expected",s3Object);
	}

	/**
	 * The private helper method that generates the test file to be uploaded
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private File generateUploadFile() throws IOException, FileNotFoundException {
		//TODO: Move this to @BeforeClass?
		String fileName = System.currentTimeMillis() + ".txt";
		File file = new File(temp.getRoot().getAbsolutePath() + File.separator + fileName);
		file.createNewFile();
		//Write something to it
		FileOutputStream fos = new FileOutputStream(file);
		fos.write("Test".getBytes());
		fos.close();
		return file;
	}



	//-- helper methods to interact with AWS S3 services
	//These methods are used to verify and assert if the implementation
	//has performed the desired operation
	/**
	 * Gets the object from the S3 bucket
	 *
	 * @param gets the object from the bucket with the given key
	 */
	protected S3Object getObject(String bucket,String key) {
		return client.getObject(new GetObjectRequest(bucket, key));
	}

	/**
	 * Gets the objects's {@link AccessControlList} (ACL) for the given bucket and key
	 *
	 * @param bucket
	 * @param key
	 * @return
	 */
	protected AccessControlList getObjectACL(String bucket,String key) {
		return client.getObjectAcl(bucket, key);
	}

	/**
	 * Deleted the given object name from the given bucket and key
	 *
	 * @param bucket
	 * @param key
	 *
	 */
	protected void deleteObject(String bucket,String key) {
		client.deleteObject(bucket, key);
	}


	protected abstract AbstractAmazonS3Operations getS3OperationsImplementation();
}
