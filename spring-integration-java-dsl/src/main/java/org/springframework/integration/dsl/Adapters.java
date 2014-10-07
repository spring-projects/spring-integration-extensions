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

package org.springframework.integration.dsl;

import java.io.File;

import javax.jms.ConnectionFactory;

import org.apache.commons.net.ftp.FTPFile;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.integration.dsl.amqp.Amqp;
import org.springframework.integration.dsl.amqp.AmqpOutboundEndpointSpec;
import org.springframework.integration.dsl.file.FileWritingMessageHandlerSpec;
import org.springframework.integration.dsl.file.Files;
import org.springframework.integration.dsl.ftp.Ftp;
import org.springframework.integration.dsl.ftp.FtpMessageHandlerSpec;
import org.springframework.integration.dsl.ftp.FtpOutboundGatewaySpec;
import org.springframework.integration.dsl.jms.Jms;
import org.springframework.integration.dsl.jms.JmsOutboundChannelAdapterSpec;
import org.springframework.integration.dsl.jms.JmsOutboundGatewaySpec;
import org.springframework.integration.dsl.mail.Mail;
import org.springframework.integration.dsl.mail.MailSendingMessageHandlerSpec;
import org.springframework.integration.dsl.sftp.Sftp;
import org.springframework.integration.dsl.sftp.SftpMessageHandlerSpec;
import org.springframework.integration.dsl.sftp.SftpOutboundGatewaySpec;
import org.springframework.integration.dsl.support.Function;
import org.springframework.integration.file.remote.RemoteFileTemplate;
import org.springframework.integration.file.remote.gateway.AbstractRemoteFileOutboundGateway;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.file.support.FileExistsMode;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;

import com.jcraft.jsch.ChannelSftp;

/**
 * @author Artem Bilan
 */
public class Adapters {

	public AmqpOutboundEndpointSpec amqp(AmqpTemplate amqpTemplate) {
		return Amqp.outboundAdapter(amqpTemplate);
	}

	public AmqpOutboundEndpointSpec amqpGateway(AmqpTemplate amqpTemplate) {
		return Amqp.outboundGateway(amqpTemplate);
	}

	public FileWritingMessageHandlerSpec file(File destinationDirectory) {
		return Files.outboundAdapter(destinationDirectory);
	}

	public FileWritingMessageHandlerSpec file(String directoryExpression) {
		return Files.outboundAdapter(directoryExpression);
	}

	public <P> FileWritingMessageHandlerSpec file(Function<Message<P>, ?> directoryFunction) {
		return Files.outboundAdapter(directoryFunction);
	}

	public FileWritingMessageHandlerSpec fileGateway(File destinationDirectory) {
		return Files.outboundGateway(destinationDirectory);
	}

	public FileWritingMessageHandlerSpec fileGateway(String directoryExpression) {
		return Files.outboundGateway(directoryExpression);
	}

	public <P> FileWritingMessageHandlerSpec fileGateway(Function<Message<P>, ?> directoryFunction) {
		return Files.outboundGateway(directoryFunction);
	}

	public FtpMessageHandlerSpec ftp(SessionFactory<FTPFile> sessionFactory) {
		return Ftp.outboundAdapter(sessionFactory);
	}

	public FtpMessageHandlerSpec ftp(SessionFactory<FTPFile> sessionFactory, FileExistsMode fileExistsMode) {
		return Ftp.outboundAdapter(sessionFactory, fileExistsMode);
	}

	public FtpMessageHandlerSpec ftp(RemoteFileTemplate<FTPFile> remoteFileTemplate) {
		return Ftp.outboundAdapter(remoteFileTemplate);
	}

	public FtpMessageHandlerSpec ftp(RemoteFileTemplate<FTPFile> remoteFileTemplate, FileExistsMode fileExistsMode) {
		return Ftp.outboundAdapter(remoteFileTemplate, fileExistsMode);
	}

	public FtpOutboundGatewaySpec ftpGateway(SessionFactory<FTPFile> sessionFactory,
			AbstractRemoteFileOutboundGateway.Command command, String expression) {
		return Ftp.outboundGateway(sessionFactory, command, expression);
	}

	public FtpOutboundGatewaySpec ftpGateway(SessionFactory<FTPFile> sessionFactory, String command,
			String expression) {
		return Ftp.outboundGateway(sessionFactory, command, expression);
	}

	public SftpMessageHandlerSpec ftps(SessionFactory<ChannelSftp.LsEntry> sessionFactory) {
		return Sftp.outboundAdapter(sessionFactory);
	}

	public SftpMessageHandlerSpec sftp(SessionFactory<ChannelSftp.LsEntry> sessionFactory,
			FileExistsMode fileExistsMode) {
		return Sftp.outboundAdapter(sessionFactory, fileExistsMode);
	}

	public SftpMessageHandlerSpec sftp(RemoteFileTemplate<ChannelSftp.LsEntry> remoteFileTemplate) {
		return Sftp.outboundAdapter(remoteFileTemplate);
	}

	public SftpMessageHandlerSpec sftp(RemoteFileTemplate<ChannelSftp.LsEntry> remoteFileTemplate,
			FileExistsMode fileExistsMode) {
		return Sftp.outboundAdapter(remoteFileTemplate, fileExistsMode);
	}

	public SftpOutboundGatewaySpec sftpGateway(SessionFactory<ChannelSftp.LsEntry> sessionFactory,
			AbstractRemoteFileOutboundGateway.Command command, String expression) {
		return Sftp.outboundGateway(sessionFactory, command, expression);
	}

	public SftpOutboundGatewaySpec sftpGateway(SessionFactory<ChannelSftp.LsEntry> sessionFactory, String command,
			String expression) {
		return Sftp.outboundGateway(sessionFactory, command, expression);
	}

	public JmsOutboundChannelAdapterSpec.JmsOutboundChannelSpecTemplateAware jms(ConnectionFactory connectionFactory) {
		return Jms.outboundAdapter(connectionFactory);
	}

	public JmsOutboundChannelAdapterSpec<? extends JmsOutboundChannelAdapterSpec<?>> jms(JmsTemplate jmsTemplate) {
		return Jms.outboundAdapter(jmsTemplate);
	}

	public JmsOutboundGatewaySpec jmsGateway(ConnectionFactory connectionFactory) {
		return Jms.outboundGateway(connectionFactory);
	}

	public MailSendingMessageHandlerSpec mail(String host) {
		return Mail.outboundAdapter(host);
	}

	Adapters() {
	}

}
