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

import static org.springframework.integration.aws.common.AWSTestUtils.assertFileContent;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Collections;

import junit.framework.Assert;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.integration.test.util.TestUtils;

/**
 * The test class for {@link InboundLocalFileOperationsImpl} that is used to perform
 * operations on local file system
 *
 * @author Amol Nayak
 *
 * @since 0.5
 *
 */
public class InboundLocalFileOperationsImplTests {

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	/**
	 * Tries registering a null listener with the class
	 */
	@Test(expected=IllegalArgumentException.class)
	public void withNullListener() {
		InboundLocalFileOperations operations = new InboundLocalFileOperationsImpl();
		operations.addEventListener(null);
	}

	/**
	 * Tries setting the listeners which is an empty list
	 */
	@SuppressWarnings("unchecked")
	@Test(expected=IllegalArgumentException.class)
	public void setEmptyListeners() {
		InboundLocalFileOperations operations = new InboundLocalFileOperationsImpl();
		operations.setEventListeners(Collections.EMPTY_LIST);
	}

	/**
	 * Test case for setting a temporary suffix that begins with a .
	 */
	@Test
	public void setTempSuffixBeginningWithDot() {
		InboundLocalFileOperations operations = new InboundLocalFileOperationsImpl();
		operations.setTemporaryFileSuffix(".write");
		Assert.assertEquals(".write", TestUtils.getPropertyValue(operations, "tempFileSuffix"));
	}

	/**
	 * Test case for setting a temporary suffix that does not begins with a .
	 */
	@Test
	public void setTempSuffixNotBeginningWithDot() {
		InboundLocalFileOperations operations = new InboundLocalFileOperationsImpl();
		operations.setTemporaryFileSuffix("write");
		Assert.assertEquals(".write", TestUtils.getPropertyValue(operations, "tempFileSuffix"));
	}


	/**
	 *Since the provided directory is null, we expect an {@link IllegalArgumentException}
	 */
	@Test(expected=IllegalArgumentException.class)
	public void writeWithNullDirectory() throws Exception {
		InboundLocalFileOperations operations = new InboundLocalFileOperationsImpl();
		operations.writeToFile(null, null, null);
	}

	/**
	 *Since the provided file name is null, we expect an {@link IllegalArgumentException}
	 */
	@Test(expected=IllegalArgumentException.class)
	public void writeWithNullFileName() throws Exception {
		InboundLocalFileOperations operations = new InboundLocalFileOperationsImpl();
		operations.writeToFile(tempFolder.newFolder("Test"), null, null);
	}

	/**
	 *Since the provided stream as null, we expect an {@link IllegalArgumentException}
	 */
	@Test(expected=IllegalArgumentException.class)
	public void writeWithNullStream() throws Exception {
		InboundLocalFileOperations operations = new InboundLocalFileOperationsImpl();
		operations.writeToFile(tempFolder.newFolder("Test"), "TestFile.txt", null);
	}

	/**
	 *Provided {@link File} for directory exists and is not a directory
	 */
	@Test(expected=IllegalArgumentException.class)
	public void writeWithExistantNonDirectory() throws Exception {
		InboundLocalFileOperations operations = new InboundLocalFileOperationsImpl();
		operations.writeToFile(tempFolder.newFile("Test"), "TestFile.txt", new ByteArrayInputStream(new byte[]{}));
	}

	/**
	 *Provided {@link File} for directory does not exist exists and the create flag is false
	 */
	@Test(expected=IllegalArgumentException.class)
	public void writeWithNonExistentDirectory() throws Exception {
		InboundLocalFileOperations operations = new InboundLocalFileOperationsImpl();
		operations.writeToFile(new File(tempFolder.getRoot() + "SomeDir"), "TestFile.txt", new ByteArrayInputStream(new byte[]{}));
	}

	/**
	 * Writes some test content to the file
	 */
	@Test
	public void writeTestContentToFile() throws Exception {
		InboundLocalFileOperations operations = new InboundLocalFileOperationsImpl();
		operations.setCreateDirectoriesIfRequired(true);
		File directory = new File(tempFolder.getRoot() + File.separator + "someNestedDir");
		File tempFile = new File(directory.getAbsolutePath() + File.separator + "SomeFileName.txt.writing");
		File permFile = new File(directory.getAbsolutePath() + File.separator + "SomeFileName.txt");
		Assert.assertFalse(tempFile.exists());
		Assert.assertFalse(permFile.exists());
		operations.writeToFile(directory, "SomeFileName.txt", new ByteArrayInputStream("Some Test Content".getBytes()));
		Assert.assertFalse(tempFile.exists());
		Assert.assertTrue(permFile.exists());
		//Check the content
		assertFileContent(permFile, "Some Test Content");
		//TODO: Test FileEventHandlers
	}

	/**
	 * Writes some test content to the file with teh given target file existent
	 */
	@Test
	public void writeTestContentWithTargetExistent() throws Exception {
		InboundLocalFileOperations operations = new InboundLocalFileOperationsImpl();
		operations.setCreateDirectoriesIfRequired(true);
		File directory = tempFolder.newFolder("someNestedDir");
		File tempFile = new File(directory.getAbsolutePath() + File.separator + "SomeFileName.txt.writing");
		File permFile = new File(directory.getAbsolutePath() + File.separator + "SomeFileName.txt");
		permFile.createNewFile();
		//Write Some content
		FileOutputStream fos = new FileOutputStream(permFile);
		fos.write("Some Old Contents".getBytes());
		fos.close();
		assertFileContent(permFile, "Some Old Contents");
		Assert.assertFalse(tempFile.exists());
		Assert.assertTrue(permFile.exists());
		operations.writeToFile(directory, "SomeFileName.txt", new ByteArrayInputStream("Some Test Content".getBytes()));
		Assert.assertFalse(tempFile.exists());
		Assert.assertTrue(permFile.exists());
		//Check the content
		assertFileContent(permFile, "Some Test Content");
	}


}
