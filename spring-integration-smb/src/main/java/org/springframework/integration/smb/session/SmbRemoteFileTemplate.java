/*
 * Copyright 2017-2022 the original author or authors.
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

import org.springframework.integration.file.remote.RemoteFileTemplate;
import org.springframework.integration.file.remote.session.SessionFactory;

import jcifs.smb.SmbFile;

/**
 * The SMB-specific {@link RemoteFileTemplate} implementation.
 *
 * @author Artem Bilan
 * @author Gregory Bragg
 */
public class SmbRemoteFileTemplate extends RemoteFileTemplate<SmbFile> {

	private final SmbSessionFactory smbSessionFactory;

	/**
	 * Construct a {@link SmbRemoteFileTemplate} with the supplied session factory.
	 * @param sessionFactory the session factory.
	 */
	public SmbRemoteFileTemplate(SessionFactory<SmbFile> sessionFactory) {
		super(sessionFactory);
		this.smbSessionFactory = (SmbSessionFactory) sessionFactory;
	}

	public boolean isReplaceFile() {
		return this.smbSessionFactory.isReplaceFile();
	}

	public void setReplaceFile(boolean replaceFile) {
		this.smbSessionFactory.setReplaceFile(replaceFile);
	}

	public boolean isUseTempFile() {
		return this.smbSessionFactory.isUseTempFile();
	}

	public void setUseTempFile(boolean useTempFile) {
		this.smbSessionFactory.setUseTempFile(useTempFile);
	}

	@Override
	public boolean isUseTemporaryFileName() {
		return this.smbSessionFactory.isUseTempFile();
	}

	@Override
	public void setUseTemporaryFileName(boolean useTemporaryFileName) {
		this.smbSessionFactory.setUseTempFile(useTemporaryFileName);
	}

}
