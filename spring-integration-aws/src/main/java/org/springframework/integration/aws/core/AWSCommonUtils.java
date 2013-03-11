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
package org.springframework.integration.aws.core;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The common utility methods for the
 *
 * @author Amol Nayak
 *
 * @since 0.5
 *
 */
public class AWSCommonUtils {

	private static final Log logger = LogFactory.getLog(AWSCommonUtils.class);

	/**
	 * Generates the MD5 hash of the file provided
	 * @param file
	 */
	public static byte[] getContentsMD5AsBytes(File file) {

		DigestInputStream din = null;
		final byte[] digestToReturn;

		try {
			BufferedInputStream bin = new BufferedInputStream(new FileInputStream(file),32768);
			din = new DigestInputStream(bin, MessageDigest.getInstance("MD5"));
			//Just to update the digest
			byte[] dummy = new byte[4096];
			for (int i = 1; i > 0; i = din.read(dummy));
			digestToReturn = din.getMessageDigest().digest();
		}
		catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("Caught Exception while generating a MessageDigest instance", e);
		}
		catch (FileNotFoundException e) {
			throw new IllegalStateException("File " + file.getName() + " not found", e);
		}
		catch(IOException e) {
			throw new IllegalStateException("Caught exception while reading from file", e);
		}
		finally {
			IOUtils.closeQuietly(din);
		}
		return digestToReturn;
	}

	/**
	 * Compute the MD5 hash of the provided String
	 * @param contents The String whose MD5 sun is to be computed
	 */
	public static byte[] getContentsMD5AsBytes(String contents) {
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			return digest.digest(contents.getBytes());
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(String.format("Unable to digest the input String '%s' using MD5.", contents), e);
		}
	}

	/**
	 * Encodes the given raw bytes into hex
	 * @param rawBytes
	 */
	public static String encodeHex(byte[] rawBytes) throws UnsupportedEncodingException {
		return new String(Hex.encodeHex(rawBytes));
	}

	/**
	 * Decodes the given base 64 raw bytes
	 *
	 * @param rawBytes
	 */
	public static byte[] decodeBase64(byte[] rawBytes) throws UnsupportedEncodingException {
		return Base64.decodeBase64(rawBytes);
	}
}