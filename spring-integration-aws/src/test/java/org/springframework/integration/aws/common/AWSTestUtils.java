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
package org.springframework.integration.aws.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.codec.binary.Base64;
import org.springframework.integration.aws.core.AWSCredentials;
import org.springframework.integration.aws.core.PropertiesAWSCredentials;

/**
 * Common test class utility to be used by AmazonWS test cases
 *
 * @author Amol Nayak
 *
 * @since 0.5
 *
 */
public final class AWSTestUtils {

	private static PropertiesAWSCredentials credentials;

	private AWSTestUtils() {
		throw new AssertionError("Cannot instantiate a utility class");
	}

	/**
	 * Method that will be used to test the contents of the file to assert we are getting the
	 * the right value
	 *
	 * @param permFile
	 * @param expectedContent
	 * @throws IOException
	 */
	public static final void assertFileContent(File permFile, String expectedContent)
			throws IOException {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(permFile)));
			Assert.assertEquals(expectedContent, reader.readLine());
		} finally {
			if(reader != null) {
				reader.close();
			}
		}
	}

	/**
	 * Writes the content to the file at given location.
	 *
	 * @param path
	 * @param content
	 */
	public static final void writeToFile(String path, String content) throws IOException {
		//create the required directories if needed
		if(path.contains(File.separator)) {
			int index = path.lastIndexOf(File.separatorChar);
			if(index != 0) {
				new File(path.substring(0, index)).mkdirs();
			}
		}
		File file = new File(path);
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(content.getBytes());
		fos.close();
	}

	/**
	 * The static helper method that would be used by other AWS tests to
	 * get the implementation of the {@link AmazonWSCredentials} instance
	 * @return
	 * @throws Exception
	 */
	public static AWSCredentials getCredentials() {
		if(credentials == null) {
			credentials =
				new PropertiesAWSCredentials("classpath:awscredentials.properties");
			try {
				credentials.afterPropertiesSet();
			} catch (Exception e) {
				return null;
			}
		}
		return credentials;
	}

	/**
	 * When passed a {@link File} instance for the root directory, the contents are recursively
	 * checked and the listing of the directories is retrieved.
	 *
	 * @param rootDirectory
	 * @return
	 */
	public static List<File> getContentsRecursively(File rootDirectory) {
		if(!rootDirectory.isDirectory()) {
			return null;
		}
		else {
			List<File> files = new ArrayList<File>();
			getContentsRecursively(rootDirectory, files);
			return files;
		}
	}

	/**
	 * Recursively gets all the files present under the provided directory
	 * @param file
	 * @param files
	 */
	private static void getContentsRecursively(File file, List<File> files) {
		if(file.isFile()) {
			files.add(file);
		}
		else if(file.isDirectory()){
			File[] children = file.listFiles();
			if(children != null && children.length != 0) {
				for(File child:children) {
					getContentsRecursively(child, files);
				}
			}
		}
	}

	/**
	 * Helper method that will be used to generate the base64 encoded MD5 hash of the string
	 *
	 * @param input
	 * @return
	 */
	public static String md5Hash(String input) {
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			byte[] digestedBytes = digest.digest(input.getBytes("UTF-8"));
			return new String(Base64.encodeBase64(digestedBytes),"UTF-8");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		} catch(UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}
}