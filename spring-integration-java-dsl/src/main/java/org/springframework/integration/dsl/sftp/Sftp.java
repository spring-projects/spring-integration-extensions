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

import org.springframework.integration.file.remote.RemoteFileTemplate;
import org.springframework.integration.file.remote.gateway.AbstractRemoteFileOutboundGateway;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.file.support.FileExistsMode;
import org.springframework.integration.sftp.gateway.SftpOutboundGateway;

import com.jcraft.jsch.ChannelSftp;

/**
 * @author Artem Bilan
 */
public abstract class Sftp {

	public static SftpInboundChannelAdapterSpec inboundAdapter(SessionFactory<ChannelSftp.LsEntry> sessionFactory) {
		return inboundAdapter(sessionFactory, null);
	}

	public static SftpInboundChannelAdapterSpec inboundAdapter(SessionFactory<ChannelSftp.LsEntry> sessionFactory,
			Comparator<File> receptionOrderComparator) {
		return new SftpInboundChannelAdapterSpec(sessionFactory, receptionOrderComparator);
	}

	public static SftpMessageHandlerSpec outboundAdapter(SessionFactory<ChannelSftp.LsEntry> sessionFactory) {
		return new SftpMessageHandlerSpec(sessionFactory);
	}

	public static SftpMessageHandlerSpec outboundAdapter(SessionFactory<ChannelSftp.LsEntry> sessionFactory,
			FileExistsMode fileExistsMode) {
		return outboundAdapter(new RemoteFileTemplate<ChannelSftp.LsEntry>(sessionFactory), fileExistsMode);
	}

	public static SftpMessageHandlerSpec outboundAdapter(RemoteFileTemplate<ChannelSftp.LsEntry> remoteFileTemplate) {
		return new SftpMessageHandlerSpec(remoteFileTemplate);
	}

	public static SftpMessageHandlerSpec outboundAdapter(RemoteFileTemplate<ChannelSftp.LsEntry> remoteFileTemplate,
			FileExistsMode fileExistsMode) {
		return new SftpMessageHandlerSpec(remoteFileTemplate, fileExistsMode);
	}

	public static SftpOutboundGatewaySpec outboundGateway(SessionFactory<ChannelSftp.LsEntry> sessionFactory,
			AbstractRemoteFileOutboundGateway.Command command, String expression) {
		return outboundGateway(sessionFactory, command.getCommand(), expression);
	}

	public static SftpOutboundGatewaySpec outboundGateway(SessionFactory<ChannelSftp.LsEntry> sessionFactory,
			String command, String expression) {
		return new SftpOutboundGatewaySpec(new SftpOutboundGateway(sessionFactory, command, expression));
	}

}
