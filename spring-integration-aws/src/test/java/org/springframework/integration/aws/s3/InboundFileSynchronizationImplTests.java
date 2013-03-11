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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.integration.aws.common.AWSTestUtils.assertFileContent;
import static org.springframework.integration.aws.common.AWSTestUtils.getContentsRecursively;
import static org.springframework.integration.aws.common.AWSTestUtils.md5Hash;
import static org.springframework.integration.aws.common.AWSTestUtils.writeToFile;
import static org.springframework.integration.aws.s3.AmazonS3OperationsMockingUtil.BUCKET;
import static org.springframework.integration.aws.s3.AmazonS3OperationsMockingUtil.mockAmazonS3Operations;
import static org.springframework.integration.aws.s3.AmazonS3OperationsMockingUtil.mockS3Operations;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.integration.aws.s3.core.AmazonS3Operations;
import org.springframework.integration.test.util.TestUtils;

/**
 * Test class for {@link InboundFileSynchronizationImpl}
 *
 * @author Amol Nayak
 *
 * @since 0.5
 *
 */
public class InboundFileSynchronizationImplTests {

	private static AmazonS3Operations operations;

	@Rule
	public final TemporaryFolder tempFolder = new TemporaryFolder();

	@BeforeClass
	public static void setup() {
		operations = mockS3Operations();
	}

	private InboundFileSynchronizationImpl getInboundFileSynchronizationImpl() {
		//uncomment the below code if you want to execute against the actual bucket
		//but before that you need to do the following
		//create a bucket and set that in the constant BUCKET in the class
		//1. add a file test.txt with content test.txt to the root.
		//2. create a folder sub1 and add a file test.txt to it with content sub1/test.txt.
		//3. create a folder sub1/sub11 and add a file test.txt to it with content sub1/sub11/test.txt.
		//4. create a folder sub2 and add a file test.txt to it with content sub/test.txt.

//		DefaultAmazonS3Operations operations = new DefaultAmazonS3Operations(AmazonWSTestUtils.getCredentials());
//		try {
//			operations.afterPropertiesSet();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

		InboundFileSynchronizationImpl sync = new InboundFileSynchronizationImpl(operations,
				new InboundLocalFileOperationsImpl());
		return sync;
	}


	/**
	 * Tests with {@link AmazonS3Operations} instance as null
	 */
	@Test(expected=IllegalArgumentException.class)
	public void withS3OperationsAsNull() {
		new InboundFileSynchronizationImpl(null, new InboundLocalFileOperationsImpl());
	}

	/**
	 * Tests with {@link InboundLocalFileOperations} instance as null
	 */
	@Test(expected=IllegalArgumentException.class)
	public void withLocalFileOperationsAsNull() {
		new InboundFileSynchronizationImpl(operations, null);
	}

	/**
	 * Tests after setting both the wildcard and filename regex
	 */
	@Test(expected=IllegalArgumentException.class)
	public void withBothWildCardAndRegex() throws Exception {
		InboundFileSynchronizationImpl sync = getInboundFileSynchronizationImpl();
		sync.setFileWildcard("*.txt");
		sync.setFileNamePattern("[a-z]+\\.txt");
		sync.afterPropertiesSet();
	}

	/**
	 * Sets non of regex and wildcard
	 */
	@Test
	public void withNoneOfRegexAndWildcard() throws Exception {
		InboundFileSynchronizationImpl impl =  getInboundFileSynchronizationImpl();
		impl.afterPropertiesSet();
		assertEquals(AlwaysTrueFileNamefilter.class,
				TestUtils.getPropertyValue(impl, "filter", FileNameFilter.class).getClass());
	}

	/**
	 * Tests after setting filename regex only
	 */
	@Test
	public void withRegexOnly() throws Exception {
		InboundFileSynchronizationImpl sync = getInboundFileSynchronizationImpl();
		sync.setFileNamePattern("[a-z]+\\.txt");
		sync.afterPropertiesSet();
		FileNameFilter filter = TestUtils.getPropertyValue(sync, "filter",FileNameFilter.class);
		assertNotNull(filter);
		assertEquals(RegexFileNameFilter.class, filter.getClass());
		assertEquals("[a-z]+\\.txt",
				TestUtils.getPropertyValue(filter, "filter.pattern.pattern", String.class));

	}

	/**
	 * Tests after setting filename wildcard only
	 */
	@Test
	public void withWildcardOnly() throws Exception {
		InboundFileSynchronizationImpl sync = getInboundFileSynchronizationImpl();
		sync.setFileWildcard("*.txt");
		sync.afterPropertiesSet();
		FileNameFilter filter = TestUtils.getPropertyValue(sync, "filter",FileNameFilter.class);
		assertNotNull(filter);
		assertEquals(WildcardFileNameFilter.class, filter.getClass());
		assertEquals("*.txt",
				TestUtils.getPropertyValue(filter, "filter.wildcards", String[].class)[0]);
	}


	/**
	 * Sets the {@link InboundFileSynchronizationImpl#setAcceptSubFolders(boolean) as true
	 */
	@Test
	public void withAcceptSubfolderAsTrue() throws Exception {
		InboundFileSynchronizationImpl impl = getInboundFileSynchronizationImpl();
		impl.setAcceptSubFolders(true);
		impl.afterPropertiesSet();
		assertTrue(TestUtils.getPropertyValue(impl, "filter.acceptSubFolders",Boolean.class).booleanValue());
		assertTrue(TestUtils.getPropertyValue(impl, "fileOperations.createDirectoriesIfRequired",
								Boolean.class).booleanValue());
	}


	/**
	 * Invokes with remote directory as / and create directory set to true
	 */
	@Test
	public void withRemoteAsRootAndCreateDirectoryToTrue() throws Exception {
		setupMock();
		String rootDirectoryPath = tempFolder.getRoot().getAbsolutePath();
		String path = String.format("%s%s%s",
				rootDirectoryPath,File.separator,"test.txt");
		File fileOne = new File(path);
		path = String.format("%s%s%s%s%s",
				rootDirectoryPath,File.separator,"sub1",File.separator,"test.txt");
		File fileTwo = new File(path);
		path = String.format("%s%s%s%s%s%s%s",
				rootDirectoryPath,File.separator,"sub1",File.separator,"sub11",File.separator,"test.txt");
		File fileThree = new File(path);;
		path = String.format("%s%s%s%s%s",
				rootDirectoryPath,File.separator,"sub2",File.separator,"test.txt");
		File fileFour = new File(path);
		assertFalse(fileOne.exists());
		assertFalse(fileTwo.exists());
		assertFalse(fileThree.exists());
		assertFalse(fileFour.exists());
		InboundFileSynchronizationImpl impl = getInboundFileSynchronizationImpl();
		impl.setAcceptSubFolders(true);
		impl.afterPropertiesSet();
		impl.synchronizeToLocalDirectory(tempFolder.getRoot(), BUCKET, "/");
		assertTrue(fileOne.exists());
		assertFileContent(fileOne, "test.txt");
		assertTrue(fileTwo.exists());
		assertFileContent(fileTwo, "sub1/test.txt");
		assertTrue(fileThree.exists());
		assertFileContent(fileThree, "sub1/sub11/test.txt");
		assertTrue(fileFour.exists());
		assertFileContent(fileFour, "sub2/test.txt");
		assertEquals(4, getContentsRecursively(tempFolder.getRoot()).size());
	}


	/**
	 * Invokes with remote directory as / and create directory set to false
	 */
	@Test
	public void withRemoteAsRootAndCreateDirectoryToFalse() throws Exception {
		setupMock();
		String rootDirectoryPath = tempFolder.getRoot().getAbsolutePath();
		String path = String.format("%s%s%s",
				rootDirectoryPath,File.separator,"test.txt");
		File fileOne = new File(path);
		assertFalse(fileOne.exists());
		InboundFileSynchronizationImpl impl = getInboundFileSynchronizationImpl();
		impl.setAcceptSubFolders(false);
		impl.afterPropertiesSet();
		impl.synchronizeToLocalDirectory(tempFolder.getRoot(), BUCKET, "/");
		assertTrue(fileOne.exists());
		assertFileContent(fileOne, "test.txt");
		assertEquals(1, getContentsRecursively(tempFolder.getRoot()).size());
	}


	/**
	 * Invokes with remote directory as /sub1 and create directory set to true
	 */
	@Test
	public void withRemoteAssub1AndCreateDirectoryToTrue() throws Exception {
		setupMock();
		String rootDirectoryPath = tempFolder.getRoot().getAbsolutePath();
		String path = String.format("%s%s%s%s%s",
				rootDirectoryPath,File.separator,"sub1",File.separator,"test.txt");
		File fileOne = new File(path);
		path = String.format("%s%s%s%s%s%s%s",
				rootDirectoryPath,File.separator,"sub1",File.separator,"sub11",File.separator,"test.txt");
		File fileTwo = new File(path);
		assertFalse(fileOne.exists());
		assertFalse(fileTwo.exists());
		InboundFileSynchronizationImpl impl = getInboundFileSynchronizationImpl();
		impl.setAcceptSubFolders(true);
		impl.afterPropertiesSet();
		impl.synchronizeToLocalDirectory(tempFolder.getRoot(), BUCKET, "/sub1");
		assertTrue(fileOne.exists());
		assertFileContent(fileOne, "sub1/test.txt");
		assertTrue(fileTwo.exists());
		assertFileContent(fileTwo, "sub1/sub11/test.txt");
		assertEquals(2, getContentsRecursively(tempFolder.getRoot()).size());
	}

	/**
	 * Invokes with remote directory as /sub1 and create directory set to false
	 */
	@Test
	public void withRemoteAssub1AndCreateDirectoryToFalse() throws Exception {
		setupMock();
		String rootDirectoryPath = tempFolder.getRoot().getAbsolutePath();
		String path = String.format("%s%s%s%s%s",
				rootDirectoryPath,File.separator,"sub1",File.separator,"test.txt");
		File file = new File(path);
		assertFalse(file.exists());
		InboundFileSynchronizationImpl impl = getInboundFileSynchronizationImpl();
		impl.setAcceptSubFolders(false);
		impl.afterPropertiesSet();
		impl.synchronizeToLocalDirectory(tempFolder.getRoot(), BUCKET, "/sub1");
		assertTrue(file.exists());
		assertFileContent(file, "sub1/test.txt");
		assertEquals(1, getContentsRecursively(tempFolder.getRoot()).size());
	}

	/**
	 * Invokes with remote directory as / and create directory set to false
	 * The two files test.txt and sub1/test.txt would already be present on
	 * the file system. Both test.txt and sub/test.txt will have content different that the remote one.
	 * test.txt will be replaced and sub/test.txt will not be replaced.
	 *
	 */
	@Test
	public void withRemoteAsRootAndCreateDirectoryToFalse2() throws Exception {
		setupMock();
		String rootDirectoryPath = tempFolder.getRoot().getAbsolutePath();
		//create test.txt and sub1/test.txt

		String path = String.format("%s%s%s",
									rootDirectoryPath,File.separator,"test.txt");
		writeToFile(path, "OldContents");
		File fileOne = new File(path);
		assertTrue(fileOne.exists());
		assertFileContent(fileOne, "OldContents");

		path = String.format("%s%s%s%s%s",
				rootDirectoryPath,File.separator,"sub1",File.separator,"test.txt");
		writeToFile(path, "OldContents");
		File fileTwo = new File(path);
		assertTrue(fileTwo.exists());
		assertFileContent(fileTwo, "OldContents");

		InboundFileSynchronizationImpl impl = getInboundFileSynchronizationImpl();
		impl.setAcceptSubFolders(false);
		impl.afterPropertiesSet();
		impl.synchronizeToLocalDirectory(tempFolder.getRoot(), BUCKET, "/");
		assertTrue(fileOne.exists());
		assertFileContent(fileOne, "test.txt");

		assertTrue(fileTwo.exists());
		assertFileContent(fileTwo, "OldContents");

		assertEquals(2, getContentsRecursively(tempFolder.getRoot()).size());
	}

	/**
	 * Invokes with remote directory as / and create directory set to true
	 * The two files test.txt and sub1/test.txt would already be present on
	 * the file system. Both test.txt and sub/test.txt will have content different that the remote one.
	 * test.txt and sub/test.txt both will be replaced.
	 *
	 */
	@Test
	public void withRemoteAsRootAndCreateDirectoryToTrue2() throws Exception {
		setupMock();
		String rootDirectoryPath = tempFolder.getRoot().getAbsolutePath();
		//create test.txt and sub1/test.txt

		String path = String.format("%s%s%s",
									rootDirectoryPath,File.separator,"test.txt");
		writeToFile(path, "OldContents");
		File fileOne = new File(path);
		assertTrue(fileOne.exists());
		assertFileContent(fileOne, "OldContents");

		path = String.format("%s%s%s%s%s",
				rootDirectoryPath,File.separator,"sub1",File.separator,"test.txt");
		writeToFile(path, "OldContents");
		File fileTwo = new File(path);
		assertTrue(fileTwo.exists());
		assertFileContent(fileTwo, "OldContents");

		InboundFileSynchronizationImpl impl = getInboundFileSynchronizationImpl();
		impl.setAcceptSubFolders(true);
		impl.afterPropertiesSet();
		impl.synchronizeToLocalDirectory(tempFolder.getRoot(), BUCKET, "/");
		assertTrue(fileOne.exists());
		assertFileContent(fileOne, "test.txt");

		assertTrue(fileTwo.exists());
		assertFileContent(fileTwo, "sub1/test.txt");

		assertEquals(4, getContentsRecursively(tempFolder.getRoot()).size());
	}

	/**
	 * The case is slightly different then previous ones.
	 * We list from the /sub1 with /sub1/test.txt and /sub1/sub11/test.txt
	 * having different contents, however the etag of
	 * /sub1/sub11/test.txt is same as remote one hence the local one should not get replaced
	 * where as /sub1/test.txt should.
	 *
	 */
	@Test
	public void withRemoteAsRootAndCreateDirectoryToTrue3() throws Exception {
		mockAmazonS3Operations(Arrays.asList(
				new String[]{"test.txt","test.txt",md5Hash("test.txt"),null},
				new String[]{"sub1/test.txt","sub1/test.txt",md5Hash("sub1/test.txt"),null},
				new String[]{"sub1/sub11/test.txt","sub1/sub11/test.txt",md5Hash("OldContents"),null},
				new String[]{"sub2/test.txt","sub2/test.txt",md5Hash("sub2/test.txt"),null}
			));

		withinSub1FolderTests(true);

	}

	/**
	 * The scenario tests by listing the directory /sub1 which has two files
	 * /sub1/test.txt and /sub1/sub11/test.txt. The MD5 of the file will be absent and the etag is
	 * for MultiUpload. This should force replace the file irrespective of the content.
	 */
	@Test
	public void withMultipartUploadForceReplace() throws Exception {
		mockAmazonS3Operations(Arrays.asList(
				new String[]{"sub1/test.txt","sub1/test.txt",null,
						new String(Hex.encodeHex(Base64.decodeBase64(md5Hash("SomeContentSub1").getBytes()))) + "-1"},
				new String[]{"sub1/sub11/test.txt","sub1/sub11/test.txt",null,
						new String(Hex.encodeHex(Base64.decodeBase64(md5Hash("SomeContentSub1/Sub11").getBytes()))) + "-1"}
			));
		withinSub1FolderTests(false);

	}

	/**
	 * The scenario will test with two files present in /sub1 directory, /sub1/test.txt and
	 * /sub1/sub11/test.txt. Now both these files have multipart upload etag but both have
	 * MD5 hash in the user's metadata. The contents of both the files is different than the one
	 * on remote but /sub/test.txt has MD5 sum same as remote, so this should not get replaced
	 *
	 * @throws Exception
	 */
	@Test
	public void withMultipartUploadWithMD5Metadata() throws Exception {
		mockAmazonS3Operations(Arrays.asList(
				new String[]{"sub1/test.txt","sub1/test.txt",md5Hash("sub1/test.txt"),
						new String(Hex.encodeHex(Base64.decodeBase64(md5Hash("sub1/test.txt").getBytes()))) + "-1"},
				new String[]{"sub1/sub11/test.txt","sub1/sub11/test.txt",md5Hash("OldContents"),
						new String(Hex.encodeHex(Base64.decodeBase64(md5Hash("OldContents").getBytes()))) + "-1"}
			));

		withinSub1FolderTests(true);
	}

	/**
	 * Private method that extracts the common assertion logic for the files in sub1 folder
	 * @throws Exception
	 */
	private void withinSub1FolderTests(boolean acceptSubfolder) throws Exception {
		String rootDirectoryPath = tempFolder.getRoot().getAbsolutePath();

		//create sub1/sub11/test.txt and sub1/test.txt
		String path = String.format("%s%s%s%s%s%s%s",
				rootDirectoryPath,File.separator,"sub1",File.separator,"sub11",File.separator,"test.txt");
		writeToFile(path, "OldContents");
		File fileOne = new File(path);
		assertTrue(fileOne.exists());
		assertFileContent(fileOne, "OldContents");

		path = String.format("%s%s%s%s%s",
				rootDirectoryPath,File.separator,"sub1",File.separator,"test.txt");
		writeToFile(path, "OldContents");
		File fileTwo = new File(path);
		assertTrue(fileTwo.exists());
		assertFileContent(fileTwo, "OldContents");

		InboundFileSynchronizationImpl impl = getInboundFileSynchronizationImpl();
		impl.setAcceptSubFolders(acceptSubfolder);
		impl.afterPropertiesSet();
		impl.synchronizeToLocalDirectory(tempFolder.getRoot(), BUCKET, "/sub1");

		assertTrue(fileOne.exists());
		assertFileContent(fileOne, "OldContents");

		assertTrue(fileTwo.exists());
		assertFileContent(fileTwo, "sub1/test.txt");

		assertEquals(2, getContentsRecursively(tempFolder.getRoot()).size());
	}


	/**
	 * Private helper method that will be setup mock s3 operations to give an illusion
	 * that it has 4 objects in the remote bucket
	 *
	 */
	private void setupMock() {
		mockAmazonS3Operations(Arrays.asList(
			new String[]{"test.txt","test.txt",md5Hash("test.txt"),null},
			new String[]{"sub1/test.txt","sub1/test.txt",md5Hash("sub1/test.txt"),null},
			new String[]{"sub1/sub11/test.txt","sub1/sub11/test.txt",md5Hash("sub1/sub11/test.txt"),null},
			new String[]{"sub2/test.txt","sub2/test.txt",md5Hash("sub2/test.txt"),null}
		));
	}
}
