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

import org.springframework.integration.file.remote.RemoteFileTemplate;
import org.springframework.integration.file.remote.gateway.AbstractRemoteFileOutboundGateway;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.file.support.FileExistsMode;
import org.springframework.integration.ftp.gateway.FtpOutboundGateway;

/**
 * @author Artem Bilan
 */
public abstract class Ftp {

	public static FtpInboundChannelAdapterSpec inboundAdapter(SessionFactory<FTPFile> sessionFactory) {
		return inboundAdapter(sessionFactory, null);
	}

	public static FtpInboundChannelAdapterSpec inboundAdapter(SessionFactory<FTPFile> sessionFactory,
			Comparator<File> receptionOrderComparator) {
		return new FtpInboundChannelAdapterSpec(sessionFactory, receptionOrderComparator);
	}

	public static FtpMessageHandlerSpec outboundAdapter(SessionFactory<FTPFile> sessionFactory) {
		return new FtpMessageHandlerSpec(sessionFactory);
	}

	public static FtpMessageHandlerSpec outboundAdapter(SessionFactory<FTPFile> sessionFactory,
			FileExistsMode fileExistsMode) {
		return outboundAdapter(new RemoteFileTemplate<FTPFile>(sessionFactory), fileExistsMode);
	}

	public static FtpMessageHandlerSpec outboundAdapter(RemoteFileTemplate<FTPFile> remoteFileTemplate) {
		return new FtpMessageHandlerSpec(remoteFileTemplate);
	}

	public static FtpMessageHandlerSpec outboundAdapter(RemoteFileTemplate<FTPFile> remoteFileTemplate,
			FileExistsMode fileExistsMode) {
		return new FtpMessageHandlerSpec(remoteFileTemplate, fileExistsMode);
	}

	public static FtpOutboundGatewaySpec outboundGateway(SessionFactory<FTPFile> sessionFactory,
			AbstractRemoteFileOutboundGateway.Command command, String expression) {
		return outboundGateway(sessionFactory, command.getCommand(), expression);
	}

	public static FtpOutboundGatewaySpec outboundGateway(SessionFactory<FTPFile> sessionFactory,
			String command, String expression) {
		return new FtpOutboundGatewaySpec(new FtpOutboundGateway(sessionFactory, command, expression));
	}

}
