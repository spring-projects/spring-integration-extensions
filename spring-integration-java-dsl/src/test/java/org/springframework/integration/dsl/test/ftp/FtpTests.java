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

package org.springframework.integration.dsl.test.ftp;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.isOneOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.commons.net.ftp.FTPFile;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.ConfigFileApplicationContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.integration.dsl.core.Pollers;
import org.springframework.integration.dsl.ftp.Ftp;
import org.springframework.integration.dsl.sftp.Sftp;
import org.springframework.integration.dsl.test.sftp.TestSftpServer;
import org.springframework.integration.file.FileHeaders;
import org.springframework.integration.file.remote.RemoteFileTemplate;
import org.springframework.integration.file.remote.gateway.AbstractRemoteFileOutboundGateway;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.PollableChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Artem Bilan
 */
@ContextConfiguration(initializers = ConfigFileApplicationContextInitializer.class)
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
public class FtpTests {

	@Autowired
	private ControlBusGateway controlBus;

	@Autowired
	private MBeanServer mBeanServer;

	@Autowired
	private TestFtpServer ftpServer;

	@Autowired
	private DefaultFtpSessionFactory ftpSessionFactory;

	@Autowired
	@Qualifier("ftpInboundResultChannel")
	private PollableChannel ftpInboundResultChannel;

	@Autowired
	@Qualifier("toFtpChannel")
	private MessageChannel toFtpChannel;

	@Autowired
	@Qualifier("ftpMgetInputChannel")
	private MessageChannel ftpMgetInputChannel;

	@Autowired
	@Qualifier("remoteFileOutputChannel")
	private PollableChannel remoteFileOutputChannel;

	@Before
	@After
	public void setupRemoteFileServers() {
		this.ftpServer.recursiveDelete(this.ftpServer.getTargetLocalDirectory());
		this.ftpServer.recursiveDelete(this.ftpServer.getTargetFtpDirectory());
	}

	@Test
	public void testFtpInboundFlow() {
		this.controlBus.send("@ftpInboundAdapter.start()");

		Message<?> message = this.ftpInboundResultChannel.receive(1000);
		assertNotNull(message);
		Object payload = message.getPayload();
		assertThat(payload, instanceOf(File.class));
		File file = (File) payload;
		assertThat(file.getName(), isOneOf("FTPSOURCE1.TXT.a", "FTPSOURCE2.TXT.a"));
		assertThat(file.getAbsolutePath(), containsString("ftpTest"));

		message = this.ftpInboundResultChannel.receive(1000);
		assertNotNull(message);
		file = (File) message.getPayload();
		assertThat(file.getName(), isOneOf("FTPSOURCE1.TXT.a", "FTPSOURCE2.TXT.a"));
		assertThat(file.getAbsolutePath(), containsString("ftpTest"));

		this.controlBus.send("@ftpInboundAdapter.stop()");
	}

	@Test
	public void testFtpOutboundFlow() {
		String fileName = "foo.file";
		this.toFtpChannel.send(MessageBuilder.withPayload("foo")
				.setHeader(FileHeaders.FILENAME, fileName)
				.build());

		RemoteFileTemplate<FTPFile> template = new RemoteFileTemplate<>(this.ftpSessionFactory);
		FTPFile[] files = template.execute(session ->
				session.list(this.ftpServer.getTargetFtpDirectory().getName() + "/" + fileName));
		assertEquals(1, files.length);
		assertEquals(3, files[0].getSize());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testFtpMgetFlow() {
		String dir = "ftpSource/";
		this.ftpMgetInputChannel.send(new GenericMessage<Object>(dir + "*"));
		Message<?> result = this.remoteFileOutputChannel.receive(1000);
		assertNotNull(result);
		List<File> localFiles = (List<File>) result.getPayload();
		// should have filtered ftpSource2.txt
		assertEquals(2, localFiles.size());

		for (File file : localFiles) {
			assertThat(file.getPath().replaceAll(Matcher.quoteReplacement(File.separator), "/"),
					Matchers.containsString(dir));
		}
		assertThat(localFiles.get(1).getPath().replaceAll(Matcher.quoteReplacement(File.separator), "/"),
				Matchers.containsString(dir + "subFtpSource"));
	}

	@Test
	public void testMBeansForDSL() throws MalformedObjectNameException {
		assertFalse(this.mBeanServer.queryMBeans(ObjectName.getInstance("org.springframework.integration:" +
				"type=MessageHandler,name=ftpMgetInputChannel,bean=anonymous"), null).isEmpty());
	}


	@MessagingGateway(defaultRequestChannel = "controlBus.input")
	private static interface ControlBusGateway {

		void send(String command);

	}

	@Configuration
	@Import(TestFtpServer.class)
	@EnableAutoConfiguration
	@IntegrationComponentScan
	public static class ContextConfiguration {

		@Autowired
		private TestFtpServer ftpServer;

		@Autowired
		private DefaultFtpSessionFactory ftpSessionFactory;

		@Bean(name = PollerMetadata.DEFAULT_POLLER)
		public PollerMetadata poller() {
			return Pollers.fixedRate(500).maxMessagesPerPoll(1).get();
		}

		@Bean
		public IntegrationFlow controlBus() {
			return f -> f.controlBus();
		}

		@Bean
		public IntegrationFlow ftpInboundFlow() {
			return IntegrationFlows
					.from(s -> s.ftp(this.ftpSessionFactory)
									.preserveTimestamp(true)
									.remoteDirectory("ftpSource")
									.regexFilter(".*\\.txt$")
									.localFilename(f -> f.toUpperCase() + ".a")
									.localDirectory(this.ftpServer.getTargetLocalDirectory()),
							e -> e.id("ftpInboundAdapter").autoStartup(false))
					.channel(MessageChannels.queue("ftpInboundResultChannel"))
					.get();
		}

		@Bean
		public IntegrationFlow ftpOutboundFlow() {
			return IntegrationFlows.from("toFtpChannel")
					.handle(Ftp.outboundAdapter(this.ftpSessionFactory)
									.useTemporaryFileName(false)
									.fileNameExpression("headers['" + FileHeaders.FILENAME + "']")
									.remoteDirectory(this.ftpServer.getTargetFtpDirectory().getName())
					).get();
		}

		@Bean
		public PollableChannel remoteFileOutputChannel() {
			return new QueueChannel();
		}

		@Bean
		public MessageHandler ftpOutboundGateway() {
			return Ftp.outboundGateway(this.ftpSessionFactory, AbstractRemoteFileOutboundGateway.Command.MGET,
					"payload")
					.options(AbstractRemoteFileOutboundGateway.Option.RECURSIVE)
					.regexFileNameFilter("(subFtpSource|.*1.txt)")
					.localDirectoryExpression("@ftpServer.targetLocalDirectoryName + #remoteDirectory")
					.localFilenameExpression("#remoteFileName.replaceFirst('ftpSource', 'localTarget')")
					.get();
		}

		@Bean
		public IntegrationFlow ftpMGetFlow() {
			return IntegrationFlows.from("ftpMgetInputChannel")
					.handle(ftpOutboundGateway())
					.channel(remoteFileOutputChannel())
					.get();
		}

	}

}
