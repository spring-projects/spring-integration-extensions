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

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.integration.file.remote.session.SessionFactory;

import jcifs.smb.SmbFile;

/**
 * The SMB session factory.
 *
 * @author Markus Spann
 * @since 1.0
 */
public class SmbSessionFactory extends SmbConfig implements SessionFactory<SmbFile> {

	private static Log logger = LogFactory.getLog(SmbSessionFactory.class);

	public SmbSessionFactory() {
		logger.debug("New " + getClass().getName() + " created.");
	}

	public final SmbSession getSession() {
		try {
			return createSession();
		}
		catch (Exception _ex) {
			throw new IllegalStateException("Failed to create session.", _ex);
		}
	}

	protected SmbSession createSession() throws IOException {
		SmbShare smbShare = new SmbShare(this);
		smbShare.setReplaceFile(isReplaceFile());
		smbShare.setUseTempFile(isUseTempFile());

		if (logger.isInfoEnabled()) {
			logger.info(String.format("SMB share init: %s/%s", getHostPort(), getShareAndDir()));
		}

		smbShare.init();
		logger.debug("SMB share initialized.");

		return new SmbSession(smbShare);
	}

}
