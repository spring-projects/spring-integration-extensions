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
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.NestedIOException;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

/**
 * @author Markus Spann
 * @since 1.0
 */
public class SmbShare extends SmbFile {

	private static final Log logger = LogFactory.getLog(SmbShare.class);

	private final AtomicBoolean open = new AtomicBoolean(false);

	private final AtomicBoolean replaceFile = new AtomicBoolean(false);

	private final AtomicBoolean useTempFile = new AtomicBoolean(false);

	public SmbShare(String url) throws IOException {
		super(StringUtils.cleanPath(url));
	}

	public SmbShare(SmbConfig _smbConfig) throws IOException {
		this(_smbConfig.validate().getUrl());
	}

	public void init() throws NestedIOException {
		boolean canRead = false;
		try {
			if (!exists()) {
				logger.info("SMB root directory does not exist. Creating it.");
				mkdirs();
			}
			canRead = canRead();
		}
		catch (SmbException _ex) {
			throw new NestedIOException("Unable to initialize share: " + this, _ex);
		}
		Assert.isTrue(canRead, "Share is not accessible " + this);
		this.open.set(true);
	}

	public boolean isReplaceFile() {
		return this.replaceFile.get();
	}

	public void setReplaceFile(boolean _replace) {
		this.replaceFile.set(_replace);
	}

	public boolean isUseTempFile() {
		return this.useTempFile.get();
	}

	public void setUseTempFile(boolean _useTempFile) {
		this.useTempFile.set(_useTempFile);
	}

	/**
	 * Checks whether the share is accessible.
	 * Note: jcifs.smb.SmbFile defines a package-protected method isOpen().
	 * @return true if open
	 */
	boolean isOpened() {
		return this.open.get();
	}

	/**
	 * Set the open state to closed.
	 * Note: jcifs.smb.SmbFile defines a package-protected method close().
	 */
	void doClose() {
		this.open.set(false);
	}

	public String newTempFileSuffix() {
		return "-" + Long.toHexString(Double.doubleToLongBits(Math.random())) + ".tmp";
	}

}
