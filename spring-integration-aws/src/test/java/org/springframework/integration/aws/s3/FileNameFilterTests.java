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

import org.junit.Assert;

import org.junit.Test;

/**
 *
 * The test cases for various {@link FileNameFilter} implementations.
 *
 * @author Amol Nayak
 *
 * @since 0.5
 *
 */
public class FileNameFilterTests {

	/**
	 * The case sets the folder of the  filter to null so that it accepts all the files in the same
	 * folder, the {@link AbstractFileNameFilter#isAcceptSubFolders() returns false.
	 *
	 */
	@Test
	public void acceptAllFilesWithoutSubfolders() {
		AbstractFileNameFilter filter = new AlwaysTrueFileNamefilter();
		filter.setAcceptSubFolders(false);
		Assert.assertTrue(filter.accept("SomeFile.txt"));
		Assert.assertFalse(filter.accept("somessubfolder/SomeFile.txt"));
	}


	/**
	 * The case sets the folder of the  filter to null so that it accepts all the files in the same
	 * folder and the sub folders, the {@link AbstractFileNameFilter#isAcceptSubFolders() returns true.
	 *
	 */
	@Test
	public void acceptAllFilesWithSubfolders() {
		AbstractFileNameFilter filter = new AlwaysTrueFileNamefilter();
		filter.setAcceptSubFolders(true);
		Assert.assertTrue(filter.accept("SomeFile.txt"));
		Assert.assertTrue(filter.accept("somessubfolder/SomeFile.txt"));
	}

	/**
	 * The test case sets a sub folder for the search and sets the
	 * {@link AbstractFileNameFilter#setAcceptSubFolders(boolean) to false
	 */
	@Test
	public void acceptInSubfolderWithoutSubfolder() {
		AbstractFileNameFilter filter = new AlwaysTrueFileNamefilter();
		filter.setFolderName("/subfolder");
		filter.setAcceptSubFolders(false);
		Assert.assertFalse(filter.accept("FileName.txt"));
		Assert.assertTrue(filter.accept("subfolder/FileName.txt"));
		Assert.assertFalse(filter.accept("subfolder/anothersf/FileName.txt"));
	}

	/**
	 * The test case sets a sub folder for the search and sets the
	 * {@link AbstractFileNameFilter#setAcceptSubFolders(boolean) to true
	 */
	@Test
	public void acceptInSubfolderWithSubfolder() {
		AbstractFileNameFilter filter = new AlwaysTrueFileNamefilter();
		filter.setFolderName("/subfolder");
		filter.setAcceptSubFolders(true);
		Assert.assertFalse(filter.accept("FileName.txt"));
		Assert.assertTrue(filter.accept("subfolder/FileName.txt"));
		Assert.assertTrue(filter.accept("subfolder/anothersf/FileName.txt"));
	}

	/**
	 * Tests the regex file filter
	 */
	@Test
	public void regexTest() {
		//accept only file names with name in lower case and ends with .txt
		AbstractFileNameFilter filter = new RegexFileNameFilter("[a-z]+\\.txt");
		filter.setAcceptSubFolders(true);
		Assert.assertTrue(filter.accept("test.txt"));
		Assert.assertFalse(filter.accept("Test.txt"));
		Assert.assertFalse(filter.accept("test123.txt"));
		Assert.assertFalse(filter.accept("test.tx"));
		Assert.assertFalse(filter.accept("test"));
		Assert.assertTrue(filter.accept("test/test.txt"));
		Assert.assertTrue(filter.accept("test/Test/12/test.txt"));
		Assert.assertFalse(filter.accept("test/Test/12/test.tx"));
		Assert.assertFalse(filter.accept("test/Test/12/Test.txt"));
	}

	/**
	 * Tests the wildcard file filter
	 */
	@Test
	public void wildCardTest() {
		//accept only file names with name in lower case and ends with .txt
		AbstractFileNameFilter filter = new WildcardFileNameFilter("*.txt");
		filter.setAcceptSubFolders(true);
		Assert.assertTrue(filter.accept("test.txt"));
		Assert.assertTrue(filter.accept("Test.txt"));
		Assert.assertTrue(filter.accept("test123.txt"));
		Assert.assertFalse(filter.accept("test.tx"));
		Assert.assertFalse(filter.accept("test"));
		Assert.assertTrue(filter.accept("test/test.txt"));
		Assert.assertTrue(filter.accept("test/Test/12/test.txt"));
		Assert.assertTrue(filter.accept("test/Test/12/Test.txt"));
		Assert.assertFalse(filter.accept("test/Test/12/Test.ext"));
	}
}
