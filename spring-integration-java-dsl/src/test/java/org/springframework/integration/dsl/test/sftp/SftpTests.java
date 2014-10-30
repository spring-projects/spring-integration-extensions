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

package org.springframework.integration.dsl.test.sftp;

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

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

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
import org.springframework.integration.dsl.sftp.Sftp;
import org.springframework.integration.file.FileHeaders;
import org.springframework.integration.file.remote.RemoteFileTemplate;
import org.springframework.integration.file.remote.gateway.AbstractRemoteFileOutboundGateway;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.PollableChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.jcraft.jsch.ChannelSftp;

/**
 * @author Artem Bilan
 */
@ContextConfiguration(initializers = ConfigFileApplicationContextInitializer.class)
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
public class SftpTests {

	@Autowired
	private ControlBusGateway controlBus;

	@Autowired
	private MBeanServer mBeanServer;

	@Autowired
	private TestSftpServer sftpServer;

	@Autowired
	private DefaultSftpSessionFactory sftpSessionFactory;

	@Autowired
	@Qualifier("sftpInboundResultChannel")
	private PollableChannel sftpInboundResultChannel;

	@Autowired
	@Qualifier("toSftpChannel")
	private MessageChannel toSftpChannel;

	@Autowired
	@Qualifier("remoteFileOutputChannel")
	private PollableChannel remoteFileOutputChannel;


	@Autowired
	@Qualifier("sftpMgetInputChannel")
	private MessageChannel sftpMgetInputChannel;

	@Before
	@After
	public void setupRemoteFileServers() {
		this.sftpServer.recursiveDelete(this.sftpServer.getTargetLocalDirectory());
		this.sftpServer.recursiveDelete(this.sftpServer.getTargetSftpDirectory());
	}

	@Test
	public void testSftpInboundFlow() {
		this.controlBus.send("@sftpInboundAdapter.start()");

		Message<?> message = this.sftpInboundResultChannel.receive(1000);
		assertNotNull(message);
		Object payload = message.getPayload();
		assertThat(payload, instanceOf(File.class));
		File file = (File) payload;
		assertThat(file.getName(), isOneOf("SFTPSOURCE1.TXT.a", "SFTPSOURCE2.TXT.a"));
		assertThat(file.getAbsolutePath(), containsString("sftpTest"));

		message = this.sftpInboundResultChannel.receive(1000);
		assertNotNull(message);
		file = (File) message.getPayload();
		assertThat(file.getName(), isOneOf("SFTPSOURCE1.TXT.a", "SFTPSOURCE2.TXT.a"));
		assertThat(file.getAbsolutePath(), containsString("sftpTest"));

		this.controlBus.send("@sftpInboundAdapter.stop()");
	}

	@Test
	public void testSftpOutboundFlow() {
		String fileName = "foo.file";
		this.toSftpChannel.send(MessageBuilder.withPayload("foo")
				.setHeader(FileHeaders.FILENAME, fileName)
				.build());

		RemoteFileTemplate<ChannelSftp.LsEntry> template = new RemoteFileTemplate<>(this.sftpSessionFactory);
		ChannelSftp.LsEntry[] files = template.execute(session ->
				session.list(this.sftpServer.getTargetSftpDirectory().getName() + "/" + fileName));
		assertEquals(1, files.length);
		assertEquals(3, files[0].getAttrs().getSize());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSftpMgetFlow() {
		String dir = "sftpSource/";
		this.sftpMgetInputChannel.send(new GenericMessage<Object>(dir + "*"));
		Message<?> result = this.remoteFileOutputChannel.receive(1000);
		assertNotNull(result);
		List<File> localFiles = (List<File>) result.getPayload();
		// should have filtered sftpSource2.txt
		assertEquals(2, localFiles.size());

		for (File file : localFiles) {
			assertThat(file.getPath().replaceAll(Matcher.quoteReplacement(File.separator), "/"),
					Matchers.containsString(dir));
		}
		assertThat(localFiles.get(1).getPath().replaceAll(Matcher.quoteReplacement(File.separator), "/"),
				Matchers.containsString(dir + "subSftpSource"));
	}

	@Test
	public void testMBeansForDSL() throws MalformedObjectNameException {
		assertFalse(this.mBeanServer.queryMBeans(ObjectName.getInstance("org.springframework.integration:" +
				"bean=anonymous,name=sftpMgetInputChannel,type=MessageHandler"), null).isEmpty());
	}


	@MessagingGateway(defaultRequestChannel = "controlBus.input")
	private static interface ControlBusGateway {

		void send(String command);

	}

	@Configuration
	@Import(TestSftpServer.class)
	@EnableAutoConfiguration
	@IntegrationComponentScan
	public static class ContextConfiguration {

		@Autowired
		private TestSftpServer sftpServer;

		@Autowired
		private DefaultSftpSessionFactory sftpSessionFactory;

		@Bean(name = PollerMetadata.DEFAULT_POLLER)
		public PollerMetadata poller() {
			return Pollers.fixedRate(500).maxMessagesPerPoll(1).get();
		}

		@Bean
		public IntegrationFlow controlBus() {
			return f -> f.controlBus();
		}

		@Bean
		public IntegrationFlow sftpInboundFlow() {
			return IntegrationFlows
					.from(s -> s.sftp(this.sftpSessionFactory)
									.preserveTimestamp(true)
									.remoteDirectory("sftpSource")
									.regexFilter(".*\\.txt$")
									.localFilenameExpression("#this.toUpperCase() + '.a'")
									.localDirectory(this.sftpServer.getTargetLocalDirectory()),
							e -> e.id("sftpInboundAdapter").autoStartup(false))
					.channel(MessageChannels.queue("sftpInboundResultChannel"))
					.get();
		}

		@Bean
		public IntegrationFlow sftpOutboundFlow() {
			return IntegrationFlows.from("toSftpChannel")
					.handle(Sftp.outboundAdapter(this.sftpSessionFactory)
									.useTemporaryFileName(false)
									.remoteDirectory(this.sftpServer.getTargetSftpDirectory().getName())
					).get();
		}

		@Bean
		public PollableChannel remoteFileOutputChannel() {
			return new QueueChannel();
		}

		@Bean
		public IntegrationFlow sftpMGetFlow() {
			return IntegrationFlows.from("sftpMgetInputChannel")
					.handleWithAdapter(h ->
							h.sftpGateway(this.sftpSessionFactory, AbstractRemoteFileOutboundGateway.Command.MGET,
									"payload")
									.options(AbstractRemoteFileOutboundGateway.Option.RECURSIVE)
									.regexFileNameFilter("(subSftpSource|.*1.txt)")
									.localDirectoryExpression("@sftpServer.targetLocalDirectoryName + #remoteDirectory")
									.localFilenameExpression("#remoteFileName.replaceFirst('sftpSource', 'localTarget')"))
					.channel(remoteFileOutputChannel())
					.get();
		}

	}

}
