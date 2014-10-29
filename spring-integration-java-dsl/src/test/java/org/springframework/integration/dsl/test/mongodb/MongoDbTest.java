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

package org.springframework.integration.dsl.test.mongodb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.Channels;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.integration.dsl.core.Pollers;
import org.springframework.integration.mongodb.store.MongoDbChannelMessageStore;
import org.springframework.integration.store.PriorityCapableChannelMessageStore;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.PollableChannel;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.mongodb.MongoClient;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;

/**
 * @author Artem Bilan
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
public class MongoDbTest {

	private static int mongoPort;

	private static MongodExecutable mongodExe;

	@BeforeClass
	public static void setup() throws IOException {
		mongoPort = Network.getFreeServerPort();
		mongodExe = MongodStarter.getDefaultInstance()
				.prepare(new MongodConfigBuilder()
						.version(Version.Main.PRODUCTION)
						.net(new Net(mongoPort, Network.localhostIsIPv6()))
						.build());
		mongodExe.start();
	}

	@AfterClass
	public static void tearDown() {
		mongodExe.stop();
	}

	@Autowired
	private ControlBusGateway controlBus;

	@Autowired
	@Qualifier("priorityChannel")
	private MessageChannel priorityChannel;

	@Autowired
	@Qualifier("priorityReplyChannel")
	private PollableChannel priorityReplyChannel;


	@Test
	public void testPriority() throws InterruptedException {
		Message<String> message = MessageBuilder.withPayload("1").setPriority(1).build();
		this.priorityChannel.send(message);

		message = MessageBuilder.withPayload("-1").setPriority(-1).build();
		this.priorityChannel.send(message);

		message = MessageBuilder.withPayload("3").setPriority(3).build();
		this.priorityChannel.send(message);

		message = MessageBuilder.withPayload("0").setPriority(0).build();
		this.priorityChannel.send(message);

		message = MessageBuilder.withPayload("2").setPriority(2).build();
		this.priorityChannel.send(message);

		message = MessageBuilder.withPayload("none").build();
		this.priorityChannel.send(message);

		message = MessageBuilder.withPayload("31").setPriority(3).build();
		this.priorityChannel.send(message);

		this.controlBus.send("@priorityChannelBridge.start()");

		Message<?> receive = this.priorityReplyChannel.receive(2000);
		assertNotNull(receive);
		assertEquals("3", receive.getPayload());

		receive = this.priorityReplyChannel.receive(2000);
		assertNotNull(receive);
		assertEquals("31", receive.getPayload());

		receive = this.priorityReplyChannel.receive(2000);
		assertNotNull(receive);
		assertEquals("2", receive.getPayload());

		receive = this.priorityReplyChannel.receive(2000);
		assertNotNull(receive);
		assertEquals("1", receive.getPayload());

		receive = this.priorityReplyChannel.receive(2000);
		assertNotNull(receive);
		assertEquals("0", receive.getPayload());

		receive = this.priorityReplyChannel.receive(2000);
		assertNotNull(receive);
		assertEquals("-1", receive.getPayload());

		receive = this.priorityReplyChannel.receive(2000);
		assertNotNull(receive);
		assertEquals("none", receive.getPayload());

		this.controlBus.send("@priorityChannelBridge.stop()");
	}

	@MessagingGateway(defaultRequestChannel = "controlBus.input")
	private static interface ControlBusGateway {

		void send(String command);
	}


	@Configuration
	@EnableIntegration
	@IntegrationComponentScan
	public static class ContextConfiguration {

		@Bean
		public IntegrationFlow controlBus() {
			return f -> f.controlBus();
		}

		@Bean
		public MongoDbFactory mongoDbFactory() throws Exception {
			return new SimpleMongoDbFactory(new MongoClient("localhost", mongoPort), "local");
		}

		@Bean
		public MongoDbChannelMessageStore mongoDbChannelMessageStore(MongoDbFactory mongoDbFactory) {
			MongoDbChannelMessageStore mongoDbChannelMessageStore = new MongoDbChannelMessageStore(mongoDbFactory);
			mongoDbChannelMessageStore.setPriorityEnabled(true);
			return mongoDbChannelMessageStore;
		}

		@Bean
		public IntegrationFlow priorityFlow(PriorityCapableChannelMessageStore mongoDbChannelMessageStore) {
			return IntegrationFlows.from((Channels c) ->
					c.priority("priorityChannel", mongoDbChannelMessageStore, "priorityGroup"))
					.bridge(s -> s.poller(Pollers.fixedDelay(100))
							.autoStartup(false)
							.id("priorityChannelBridge"))
					.channel(MessageChannels.queue("priorityReplyChannel"))
					.get();
		}

	}

}
