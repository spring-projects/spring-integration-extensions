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

import java.lang.reflect.Constructor;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;

import org.springframework.expression.common.LiteralExpression;
import org.springframework.integration.dsl.core.ComponentsRegistration;
import org.springframework.integration.dsl.core.MessageHandlerSpec;
import org.springframework.integration.dsl.support.Function;
import org.springframework.integration.dsl.support.FunctionExpression;
import org.springframework.integration.file.DefaultFileNameGenerator;
import org.springframework.integration.file.FileNameGenerator;
import org.springframework.integration.file.remote.RemoteFileTemplate;
import org.springframework.integration.file.remote.handler.FileTransferringMessageHandler;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.file.support.FileExistsMode;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * @author Artem Bilan
 */
public abstract class FileTransferringMessageHandlerSpec<F, S extends FileTransferringMessageHandlerSpec<F, S>>
		extends MessageHandlerSpec<S, FileTransferringMessageHandler<F>>
		implements ComponentsRegistration {

	private FileNameGenerator fileNameGenerator;

	private DefaultFileNameGenerator defaultFileNameGenerator;

	protected FileTransferringMessageHandlerSpec(SessionFactory<F> sessionFactory) {
		this.target = new FileTransferringMessageHandler<F>(sessionFactory);
	}

	protected FileTransferringMessageHandlerSpec(RemoteFileTemplate<F> remoteFileTemplate) {
		this.target = new FileTransferringMessageHandler<F>(remoteFileTemplate);
	}

	@SuppressWarnings("unchecked")
	protected FileTransferringMessageHandlerSpec(RemoteFileTemplate<F> remoteFileTemplate,
			FileExistsMode fileExistsMode) {
		Constructor<?> fileExistsModeConstructor =
				ClassUtils.getConstructorIfAvailable(FileTransferringMessageHandler.class, RemoteFileTemplate.class,
						FileExistsMode.class);
		if (fileExistsModeConstructor == null) {
			logger.warn("The 'FileExistsMode' constructor argument for the 'FileTransferringMessageHandler' is " +
					"available since Spring Integration 4.1. Will be ignored for previous versions.");
			this.target = new FileTransferringMessageHandler<F>(remoteFileTemplate);
		}
		else {
			try {
				this.target =
						(FileTransferringMessageHandler<F>) fileExistsModeConstructor.newInstance(remoteFileTemplate,
								fileExistsMode);
			}
			catch (Exception e) {
				throw new IllegalStateException(e);
			}
		}
	}

	public S autoCreateDirectory(boolean autoCreateDirectory) {
		this.target.setAutoCreateDirectory(autoCreateDirectory);
		return _this();
	}

	public S remoteFileSeparator(String remoteFileSeparator) {
		this.target.setRemoteFileSeparator(remoteFileSeparator);
		return _this();
	}

	public S remoteDirectory(String remoteDirectory) {
		this.target.setRemoteDirectoryExpression(new LiteralExpression(remoteDirectory));
		return _this();
	}

	public S remoteDirectoryExpression(String remoteDirectoryExpression) {
		this.target.setRemoteDirectoryExpression(PARSER.parseExpression(remoteDirectoryExpression));
		return _this();
	}

	public <P> S remoteDirectory(Function<Message<P>, String> remoteDirectoryFunction) {
		this.target.setRemoteDirectoryExpression(new FunctionExpression<Message<P>>(remoteDirectoryFunction));
		return _this();
	}

	public S temporaryRemoteDirectory(String temporaryRemoteDirectory) {
		this.target.setTemporaryRemoteDirectoryExpression(new LiteralExpression(temporaryRemoteDirectory));
		return _this();
	}

	public S temporaryRemoteDirectoryExpression(String temporaryRemoteDirectoryExpression) {
		this.target.setTemporaryRemoteDirectoryExpression(PARSER.parseExpression(temporaryRemoteDirectoryExpression));
		return _this();
	}

	public <P> S temporaryRemoteDirectory(Function<Message<P>, String> temporaryRemoteDirectoryFunction) {
		this.target.setTemporaryRemoteDirectoryExpression(
				new FunctionExpression<Message<P>>(temporaryRemoteDirectoryFunction));
		return _this();
	}

	public S useTemporaryFileName(boolean useTemporaryFileName) {
		this.target.setUseTemporaryFileName(useTemporaryFileName);
		return _this();
	}

	public S fileNameGenerator(FileNameGenerator fileNameGenerator) {
		this.fileNameGenerator = fileNameGenerator;
		this.target.setFileNameGenerator(fileNameGenerator);
		return _this();
	}

	public S fileNameExpression(String fileNameGeneratorExpression) {
		Assert.isNull(this.fileNameGenerator,
				"'fileNameGenerator' and 'fileNameGeneratorExpression' are mutually exclusive.");
		this.defaultFileNameGenerator = new DefaultFileNameGenerator();
		this.defaultFileNameGenerator.setExpression(fileNameGeneratorExpression);
		return fileNameGenerator(this.defaultFileNameGenerator);
	}

	public S charset(String charset) {
		this.target.setCharset(charset);
		return _this();
	}

	public S charset(Charset charset) {
		this.target.setCharset(charset.name());
		return _this();
	}

	public S temporaryFileSuffix(String temporaryFileSuffix) {
		this.target.setTemporaryFileSuffix(temporaryFileSuffix);
		return _this();
	}

	@Override
	public Collection<Object> getComponentsToRegister() {
		if (this.defaultFileNameGenerator != null) {
			return Collections.<Object>singletonList(this.defaultFileNameGenerator);
		}
		return null;
	}

	@Override
	protected FileTransferringMessageHandler<F> doGet() {
		throw new UnsupportedOperationException();
	}

}
