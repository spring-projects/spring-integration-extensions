/*
 * Copyright 2012-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.integration.smb.session;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import jcifs.smb.SmbFile;

/**
 *
 * @author Gunnar Hillert
 *
 */
public class SmbSessionTests {

	@Test
	public void testCreateSmbFileObjectWithBackSlash1() throws IOException {

		System.setProperty("file.separator", "\\");
		SmbShare smbShare = new SmbShare("smb://myshare/shared/");
		SmbSession smbSession = new SmbSession(smbShare);
		SmbFile smbFile = smbSession.createSmbFileObject("smb://myshare\\blubba\\");
		assertEquals("smb://myshare/blubba/", smbFile.getCanonicalPath());
	}

	@Test
	public void testCreateSmbFileObjectWithBackSlash2() throws IOException {

		System.setProperty("file.separator", "\\");
		SmbShare smbShare = new SmbShare("smb://myshare\\shared\\");
		SmbSession smbSession = new SmbSession(smbShare);
		SmbFile smbFile = smbSession.createSmbFileObject("smb://myshare\\blubba\\");
		assertEquals("smb://myshare/blubba/", smbFile.getCanonicalPath());
	}

	@Test
	public void testCreateSmbFileObjectWithBackSlash3() throws IOException {

		System.setProperty("file.separator", "\\");
		SmbShare smbShare = new SmbShare("smb://myshare\\shared\\");
		SmbSession smbSession = new SmbSession(smbShare);
		SmbFile smbFile = smbSession.createSmbFileObject("..\\another");
		assertEquals("smb://myshare/another/", smbFile.getCanonicalPath());
	}

	@Test
	public void testCreateSmbFileObjectWithBackSlash4() throws IOException {

		System.setProperty("file.separator", "/");
		SmbShare smbShare = new SmbShare("smb://myshare/shared/");
		SmbSession smbSession = new SmbSession(smbShare);
		SmbFile smbFile = smbSession.createSmbFileObject("smb://myshare\\blubba\\");
		assertEquals("smb://myshare/blubba/", smbFile.getCanonicalPath());
	}


	@Test
	public void testCreateSmbFileObjectwithMissingTrailingSlash1() throws IOException {

		SmbShare smbShare = new SmbShare("smb://myshare/shared");
		SmbSession smbSession = new SmbSession(smbShare);

		SmbFile smbFile = smbSession.createSmbFileObject("smb://myshare\\blubba");
		assertEquals("smb://myshare/blubba/", smbFile.getCanonicalPath());

	}

	@Test
	public void testCreateSmbFileObjectwithMissingTrailingSlash2() throws IOException {

		SmbShare smbShare = new SmbShare("smb://myshare/shared/");
		SmbSession smbSession = new SmbSession(smbShare);

		SmbFile smbFile = smbSession.createSmbFileObject(".");
		assertEquals("smb://myshare/shared/", smbFile.getCanonicalPath());

	}

	@Test
	public void testCreateSmbFileObjectwithMissingTrailingSlash3() throws IOException {

		SmbShare smbShare = new SmbShare("smb://myshare/shared/");
		SmbSession smbSession = new SmbSession(smbShare);

		SmbFile smbFile = smbSession.createSmbFileObject("../anotherShare");
		assertEquals("smb://myshare/anotherShare/", smbFile.getCanonicalPath());

	}
}
