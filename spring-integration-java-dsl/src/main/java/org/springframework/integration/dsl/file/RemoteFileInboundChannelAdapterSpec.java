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
import java.util.Collection;
import java.util.Collections;

import org.springframework.integration.dsl.core.ComponentsRegistration;
import org.springframework.integration.dsl.core.MessageSourceSpec;
import org.springframework.integration.dsl.support.Function;
import org.springframework.integration.dsl.support.FunctionExpression;
import org.springframework.integration.file.filters.FileListFilter;
import org.springframework.integration.file.remote.synchronizer.AbstractInboundFileSynchronizer;
import org.springframework.integration.file.remote.synchronizer.AbstractInboundFileSynchronizingMessageSource;
import org.springframework.util.Assert;

/**
 * @author Artem Bilan
 */
public abstract class RemoteFileInboundChannelAdapterSpec<F, S extends RemoteFileInboundChannelAdapterSpec<F, S, MS>,
		MS extends AbstractInboundFileSynchronizingMessageSource<F>>
		extends MessageSourceSpec<S, MS> implements ComponentsRegistration {

	protected final AbstractInboundFileSynchronizer<F> synchronizer;

	private FileListFilter<F> filter;

	protected RemoteFileInboundChannelAdapterSpec(AbstractInboundFileSynchronizer<F> synchronizer) {
		this.synchronizer = synchronizer;
	}

	public S autoCreateLocalDirectory(boolean autoCreateLocalDirectory) {
		this.target.setAutoCreateLocalDirectory(autoCreateLocalDirectory);
		return _this();
	}

	public S localDirectory(File localDirectory) {
		this.target.setLocalDirectory(localDirectory);
		return _this();
	}

	public S localFilter(FileListFilter<File> localFileListFilter) {
		this.target.setLocalFilter(localFileListFilter);
		return _this();
	}

	public S remoteFileSeparator(String remoteFileSeparator) {
		this.synchronizer.setRemoteFileSeparator(remoteFileSeparator);
		return _this();
	}

	public S localFilenameExpression(String localFilenameExpression) {
		this.synchronizer.setLocalFilenameGeneratorExpression(PARSER.parseExpression(localFilenameExpression));
		return _this();
	}

	public S localFilename(Function<String, String> localFilenameFunction) {
		this.synchronizer.setLocalFilenameGeneratorExpression(new FunctionExpression<String>(localFilenameFunction));
		return _this();
	}

	public S temporaryFileSuffix(String temporaryFileSuffix) {
		this.synchronizer.setTemporaryFileSuffix(temporaryFileSuffix);
		return _this();
	}

	public S remoteDirectory(String remoteDirectory) {
		this.synchronizer.setRemoteDirectory(remoteDirectory);
		return _this();
	}

	public S filter(FileListFilter<F> filter) {
		Assert.isNull(this.filter,
				"The 'filter' (" + this.filter + ") is already configured for the: " + this);
		this.filter = filter;
		this.synchronizer.setFilter(filter);
		return _this();
	}

	public abstract S patternFilter(String pattern);

	public abstract S regexFilter(String regex);

	public S deleteRemoteFiles(boolean deleteRemoteFiles) {
		this.synchronizer.setDeleteRemoteFiles(deleteRemoteFiles);
		return _this();
	}

	public S preserveTimestamp(boolean preserveTimestamp) {
		this.synchronizer.setPreserveTimestamp(preserveTimestamp);
		return _this();
	}

	@Override
	public Collection<Object> getComponentsToRegister() {
		return Collections.<Object>singletonList(this.synchronizer);
	}

	@Override
	protected MS doGet() {
		throw new UnsupportedOperationException();
	}

}
