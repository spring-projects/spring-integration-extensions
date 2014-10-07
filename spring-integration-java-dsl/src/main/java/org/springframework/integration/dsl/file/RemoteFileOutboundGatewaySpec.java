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

import org.springframework.integration.dsl.core.MessageHandlerSpec;
import org.springframework.integration.dsl.support.Function;
import org.springframework.integration.dsl.support.FunctionExpression;
import org.springframework.integration.file.filters.FileListFilter;
import org.springframework.integration.file.filters.RegexPatternFileListFilter;
import org.springframework.integration.file.filters.SimplePatternFileListFilter;
import org.springframework.integration.file.remote.gateway.AbstractRemoteFileOutboundGateway;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;

/**
 * @author Artem Bilan
 */
public abstract class RemoteFileOutboundGatewaySpec<F, S extends RemoteFileOutboundGatewaySpec<F, S>>
		extends MessageHandlerSpec<S, AbstractRemoteFileOutboundGateway<F>> {

	private FileListFilter<F> filter;

	private FileListFilter<File> mputFilter;

	protected RemoteFileOutboundGatewaySpec(AbstractRemoteFileOutboundGateway<F> outboundGateway) {
		this.target = outboundGateway;
	}

	public S options(String options) {
		this.target.setOptions(options);
		return _this();
	}

	public S options(AbstractRemoteFileOutboundGateway.Option... options) {
		Assert.noNullElements(options);
		StringBuilder optionsString = new StringBuilder();
		for (AbstractRemoteFileOutboundGateway.Option option : options) {
			optionsString.append(option.getOption()).append(" ");
		}
		this.target.setOptions(optionsString.toString());
		return _this();
	}

	public S remoteFileSeparator(String remoteFileSeparator) {
		this.target.setRemoteFileSeparator(remoteFileSeparator);
		return _this();
	}

	public S localDirectory(File localDirectory) {
		this.target.setLocalDirectory(localDirectory);
		return _this();
	}

	public S localDirectoryExpression(String localDirectoryExpression) {
		this.target.setLocalDirectoryExpression(PARSER.parseExpression(localDirectoryExpression));
		return _this();
	}

	public <P> S localDirectory(Function<Message<P>, String> localDirectoryFunction) {
		this.target.setLocalDirectoryExpression(new FunctionExpression<Message<P>>(localDirectoryFunction));
		return _this();
	}

	public S autoCreateLocalDirectory(boolean autoCreateLocalDirectory) {
		this.target.setAutoCreateLocalDirectory(autoCreateLocalDirectory);
		return _this();
	}

	public S temporaryFileSuffix(String temporaryFileSuffix) {
		this.target.setTemporaryFileSuffix(temporaryFileSuffix);
		return _this();
	}

	public S filter(FileListFilter<F> filter) {
		Assert.isNull(this.filter,
				"The 'filter' (" + this.filter + ") is already configured for the: " + this);
		this.filter = filter;
		this.target.setFilter(filter);
		return _this();
	}

	public abstract S patternFileNameFilter(String pattern);

	public abstract S regexFileNameFilter(String regex);

	public S mputFilter(FileListFilter<File> filter) {
		Assert.isNull(this.mputFilter,
				"The 'filter' (" + this.mputFilter + ") is already configured for the: " + this);
		this.mputFilter = filter;
		this.target.setMputFilter(filter);
		return _this();
	}


	public S patternMputFilter(String pattern) {
		return mputFilter(new SimplePatternFileListFilter(pattern));
	}

	public S regexMpuFilter(String regex) {
		return mputFilter(new RegexPatternFileListFilter(regex));
	}


	public S renameExpression(String expression) {
		this.target.setRenameExpression(expression);
		return _this();
	}

	public S localFilenameExpression(String localFilenameExpression) {
		this.target.setLocalFilenameGeneratorExpression(PARSER.parseExpression(localFilenameExpression));
		return _this();
	}

	public <P> S localFilename(Function<Message<P>, String> localFilenameFunction) {
		this.target.setLocalFilenameGeneratorExpression(new FunctionExpression<Message<P>>(localFilenameFunction));
		return _this();
	}

	@Override
	protected AbstractRemoteFileOutboundGateway<F> doGet() {
		throw new UnsupportedOperationException();
	}

}
