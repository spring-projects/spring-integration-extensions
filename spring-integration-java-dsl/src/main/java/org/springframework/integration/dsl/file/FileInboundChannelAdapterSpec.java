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

package org.springframework.integration.dsl.file;

import java.io.File;
import java.util.Comparator;

import org.springframework.integration.dsl.core.MessageSourceSpec;
import org.springframework.integration.file.DirectoryScanner;
import org.springframework.integration.file.FileLocker;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.filters.AcceptAllFileListFilter;
import org.springframework.integration.file.filters.AcceptOnceFileListFilter;
import org.springframework.integration.file.filters.CompositeFileListFilter;
import org.springframework.integration.file.filters.FileListFilter;
import org.springframework.integration.file.filters.RegexPatternFileListFilter;
import org.springframework.integration.file.filters.SimplePatternFileListFilter;
import org.springframework.integration.file.locking.NioFileLocker;
import org.springframework.util.Assert;

/**
 * @author Artem Bilan
 */
public class FileInboundChannelAdapterSpec
		extends MessageSourceSpec<FileInboundChannelAdapterSpec, FileReadingMessageSource> {

	private FileListFilter<File> filter;

	private FileLocker locker;

	FileInboundChannelAdapterSpec() {
		this.target = new FileReadingMessageSource();
	}

	FileInboundChannelAdapterSpec(Comparator<File> receptionOrderComparator) {
		this.target = new FileReadingMessageSource(receptionOrderComparator);
	}

	FileInboundChannelAdapterSpec directory(File directory) {
		this.target.setDirectory(directory);
		return _this();
	}

	public FileInboundChannelAdapterSpec scanner(DirectoryScanner scanner) {
		this.target.setScanner(scanner);
		return _this();
	}

	public FileInboundChannelAdapterSpec autoCreateDirectory(boolean autoCreateDirectory) {
		this.target.setAutoCreateDirectory(autoCreateDirectory);
		return _this();
	}

	public FileInboundChannelAdapterSpec filter(FileListFilter<File> filter) {
		return filter(filter, false);
	}

	public FileInboundChannelAdapterSpec filter(FileListFilter<File> filter, boolean preventDuplicates) {
		Assert.isNull(this.filter,
				"The 'filter' (" + this.filter + ") is already configured for the FileReadingMessageSource");
		FileListFilter<File> targetFilter = filter;
		if (preventDuplicates) {
			targetFilter = createCompositeWithAcceptOnceFilter(filter);
		}
		this.filter = targetFilter;
		this.target.setFilter(targetFilter);
		return _this();
	}

	public FileInboundChannelAdapterSpec preventDuplicatesFilter(boolean preventDuplicates) {
		if (preventDuplicates) {
			return filter(new AcceptOnceFileListFilter<File>(), false);
		}
		else {
			return filter(new AcceptAllFileListFilter<File>(), false);
		}
	}

	public FileInboundChannelAdapterSpec patternFilter(String pattern) {
		return patternFilter(pattern, true);
	}

	public FileInboundChannelAdapterSpec patternFilter(String pattern, boolean preventDuplicates) {
		return filter(new SimplePatternFileListFilter(pattern), preventDuplicates);
	}

	public FileInboundChannelAdapterSpec regexFilter(String regex) {
		return regexFilter(regex, true);
	}

	public FileInboundChannelAdapterSpec regexFilter(String regex, boolean preventDuplicates) {
		return filter(new RegexPatternFileListFilter(regex), preventDuplicates);
	}

	private CompositeFileListFilter<File> createCompositeWithAcceptOnceFilter(FileListFilter<File> otherFilter) {
		CompositeFileListFilter<File> compositeFilter = new CompositeFileListFilter<File>();
		compositeFilter.addFilter(new AcceptOnceFileListFilter<File>());
		compositeFilter.addFilter(otherFilter);
		return compositeFilter;
	}

	public FileInboundChannelAdapterSpec locker(FileLocker locker) {
		Assert.isNull(this.locker,
				"The 'locker' (" + this.locker + ") is already configured for the FileReadingMessageSource");
		this.locker = locker;
		this.target.setLocker(locker);
		return _this();
	}

	public FileInboundChannelAdapterSpec nioLocker() {
		return locker(new NioFileLocker());
	}

	public FileInboundChannelAdapterSpec scanEachPoll(boolean scanEachPoll) {
		this.target.setScanEachPoll(scanEachPoll);
		return _this();
	}

	@Override
	protected FileReadingMessageSource doGet() {
		throw new UnsupportedOperationException();
	}

}
