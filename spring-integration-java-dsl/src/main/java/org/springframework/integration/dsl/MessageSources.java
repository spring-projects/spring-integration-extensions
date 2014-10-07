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
import java.util.Comparator;

import javax.jms.ConnectionFactory;

import org.apache.commons.net.ftp.FTPFile;

import org.springframework.integration.dsl.file.FileInboundChannelAdapterSpec;
import org.springframework.integration.dsl.file.Files;
import org.springframework.integration.dsl.ftp.Ftp;
import org.springframework.integration.dsl.ftp.FtpInboundChannelAdapterSpec;
import org.springframework.integration.dsl.jms.Jms;
import org.springframework.integration.dsl.jms.JmsInboundChannelAdapterSpec;
import org.springframework.integration.dsl.mail.ImapMailInboundChannelAdapterSpec;
import org.springframework.integration.dsl.mail.Mail;
import org.springframework.integration.dsl.mail.Pop3MailInboundChannelAdapterSpec;
import org.springframework.integration.dsl.sftp.Sftp;
import org.springframework.integration.dsl.sftp.SftpInboundChannelAdapterSpec;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.jms.core.JmsTemplate;

import com.jcraft.jsch.ChannelSftp;

/**
 * @author Artem Bilan
 */
public class MessageSources {

	public FileInboundChannelAdapterSpec file(File directory) {
		return file(directory, null);
	}

	public FileInboundChannelAdapterSpec file(File directory, Comparator<File> receptionOrderComparator) {
		return Files.inboundAdapter(directory, receptionOrderComparator);
	}

	public FtpInboundChannelAdapterSpec ftp(SessionFactory<FTPFile> sessionFactory) {
		return ftp(sessionFactory, null);
	}

	public FtpInboundChannelAdapterSpec ftp(SessionFactory<FTPFile> sessionFactory,
			Comparator<File> receptionOrderComparator) {
		return Ftp.inboundAdapter(sessionFactory, receptionOrderComparator);
	}

	public SftpInboundChannelAdapterSpec sftp(SessionFactory<ChannelSftp.LsEntry> sessionFactory) {
		return sftp(sessionFactory, null);
	}

	public SftpInboundChannelAdapterSpec sftp(SessionFactory<ChannelSftp.LsEntry> sessionFactory,
			Comparator<File> receptionOrderComparator) {
		return Sftp.inboundAdapter(sessionFactory, receptionOrderComparator);
	}

	public JmsInboundChannelAdapterSpec<? extends JmsInboundChannelAdapterSpec<?>> jms(JmsTemplate jmsTemplate) {
		return Jms.inboundAdapter(jmsTemplate);
	}

	public JmsInboundChannelAdapterSpec.JmsInboundChannelSpecTemplateAware jms(ConnectionFactory connectionFactory) {
		return Jms.inboundAdapter(connectionFactory);
	}

	public ImapMailInboundChannelAdapterSpec imap() {
		return Mail.imapInboundAdapter();
	}

	public ImapMailInboundChannelAdapterSpec imap(String url) {
		return Mail.imapInboundAdapter(url);
	}

	public Pop3MailInboundChannelAdapterSpec pop3() {
		return Mail.pop3InboundAdapter();
	}

	public Pop3MailInboundChannelAdapterSpec pop3(String url) {
		return Mail.pop3InboundAdapter(url);
	}

	public Pop3MailInboundChannelAdapterSpec pop3(String host, String username, String password) {
		return pop3(host, -1, username, password);
	}

	public Pop3MailInboundChannelAdapterSpec pop3(String host, int port, String username, String password) {
		return Mail.pop3InboundAdapter(host, port, username, password);
	}

	MessageSources() {
	}

}
