/*
 * Copyright 2014 the original author or authors.
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

package org.springframework.integration.dsl.ftp;

import java.io.File;
import java.util.Comparator;

import org.apache.commons.net.ftp.FTPFile;

import org.springframework.integration.dsl.file.RemoteFileInboundChannelAdapterSpec;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.ftp.filters.FtpRegexPatternFileListFilter;
import org.springframework.integration.ftp.filters.FtpSimplePatternFileListFilter;
import org.springframework.integration.ftp.inbound.FtpInboundFileSynchronizer;
import org.springframework.integration.ftp.inbound.FtpInboundFileSynchronizingMessageSource;

/**
 * @author Artem Bilan
 */
public class FtpInboundChannelAdapterSpec
		extends RemoteFileInboundChannelAdapterSpec<FTPFile, FtpInboundChannelAdapterSpec,
				FtpInboundFileSynchronizingMessageSource> {

	FtpInboundChannelAdapterSpec(SessionFactory<FTPFile> sessionFactory, Comparator<File> comparator) {
		super(new FtpInboundFileSynchronizer(sessionFactory));
		this.target = new FtpInboundFileSynchronizingMessageSource(this.synchronizer, comparator);
	}

	@Override
	public FtpInboundChannelAdapterSpec patternFilter(String pattern) {
		return filter(new FtpSimplePatternFileListFilter(pattern));
	}

	@Override
	public FtpInboundChannelAdapterSpec regexFilter(String regex) {
		return filter(new FtpRegexPatternFileListFilter(regex));
	}

}
