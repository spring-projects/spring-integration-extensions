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

package org.springframework.integration.dsl.sftp;

import java.io.File;
import java.util.Comparator;

import com.jcraft.jsch.ChannelSftp;

import org.springframework.integration.dsl.file.RemoteFileInboundChannelAdapterSpec;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.sftp.filters.SftpRegexPatternFileListFilter;
import org.springframework.integration.sftp.filters.SftpSimplePatternFileListFilter;
import org.springframework.integration.sftp.inbound.SftpInboundFileSynchronizer;
import org.springframework.integration.sftp.inbound.SftpInboundFileSynchronizingMessageSource;

/**
 * @author Artem Bilan
 */
public class SftpInboundChannelAdapterSpec
		extends RemoteFileInboundChannelAdapterSpec<ChannelSftp.LsEntry, SftpInboundChannelAdapterSpec,
				SftpInboundFileSynchronizingMessageSource> {

	SftpInboundChannelAdapterSpec(SessionFactory<ChannelSftp.LsEntry> sessionFactory, Comparator<File> comparator) {
		super(new SftpInboundFileSynchronizer(sessionFactory));
		this.target = new SftpInboundFileSynchronizingMessageSource(this.synchronizer, comparator);
	}

	@Override
	public SftpInboundChannelAdapterSpec patternFilter(String pattern) {
		return filter(new SftpSimplePatternFileListFilter(pattern));
	}

	@Override
	public SftpInboundChannelAdapterSpec regexFilter(String regex) {
		return filter(new SftpRegexPatternFileListFilter(regex));
	}

}
