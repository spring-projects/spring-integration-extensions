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

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.channel.NullChannel;
import org.springframework.integration.dsl.core.MessageProducerSpec;
import org.springframework.integration.file.config.FileTailInboundChannelAdapterFactoryBean;
import org.springframework.integration.file.tail.FileTailingMessageProducerSupport;
import org.springframework.messaging.MessageChannel;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.util.Assert;

/**
 * @author Artem Bilan
 */
public class TailAdapterSpec extends MessageProducerSpec<TailAdapterSpec, FileTailingMessageProducerSupport> {

	private final FileTailInboundChannelAdapterFactoryBean factoryBean = new FileTailInboundChannelAdapterFactoryBean();

	private MessageChannel outputChannel;

	private MessageChannel errorChannel;

	TailAdapterSpec() {
		super(null);
		this.factoryBean.setBeanFactory(new DefaultListableBeanFactory());
	}

	TailAdapterSpec file(File file) {
		Assert.notNull(file);
		this.factoryBean.setFile(file);
		return _this();
	}

	public TailAdapterSpec nativeOptions(String nativeOptions) {
		this.factoryBean.setNativeOptions(nativeOptions);
		return _this();
	}

	public TailAdapterSpec taskExecutor(TaskExecutor taskExecutor) {
		this.factoryBean.setTaskExecutor(taskExecutor);
		return _this();
	}

	public TailAdapterSpec taskScheduler(TaskScheduler taskScheduler) {
		this.factoryBean.setTaskScheduler(taskScheduler);
		return _this();
	}

	public TailAdapterSpec delay(long delay) {
		this.factoryBean.setDelay(delay);
		return _this();
	}

	public TailAdapterSpec fileDelay(long fileDelay) {
		this.factoryBean.setFileDelay(fileDelay);
		return _this();
	}

	public TailAdapterSpec end(boolean end) {
		this.factoryBean.setEnd(end);
		return _this();
	}

	public TailAdapterSpec reopen(boolean reopen) {
		this.factoryBean.setReopen(reopen);
		return _this();
	}

	@Override
	public TailAdapterSpec id(String id) {
		this.factoryBean.setBeanName(id);
		return _this();
	}

	@Override
	public TailAdapterSpec phase(int phase) {
		this.factoryBean.setPhase(phase);
		return _this();
	}

	@Override
	public TailAdapterSpec autoStartup(boolean autoStartup) {
		this.factoryBean.setAutoStartup(autoStartup);
		return _this();
	}

	@Override
	public TailAdapterSpec outputChannel(MessageChannel outputChannel) {
		this.outputChannel = outputChannel;
		return _this();
	}

	@Override
	public TailAdapterSpec errorChannel(MessageChannel errorChannel) {
		this.errorChannel = errorChannel;
		return _this();
	}

	@Override
	protected FileTailingMessageProducerSupport doGet() {
		if (this.outputChannel == null) {
			this.factoryBean.setOutputChannel(new NullChannel());
		}
		FileTailingMessageProducerSupport tailingMessageProducerSupport = null;
		try {
			this.factoryBean.afterPropertiesSet();
			tailingMessageProducerSupport = this.factoryBean.getObject();
		}
		catch (Exception e) {
			throw new IllegalStateException(e);
		}
		if (this.errorChannel != null) {
			tailingMessageProducerSupport.setErrorChannel(this.errorChannel);
		}
		tailingMessageProducerSupport.setOutputChannel(this.outputChannel);
		return tailingMessageProducerSupport;
	}

}
