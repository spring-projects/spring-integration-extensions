/* Copyright 2002-2013 the original author or authors.
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
package org.springframework.integration.smpp;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.integration.smpp.core.SmppConstants;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.atomic.AtomicInteger;
/**
 * Simple test, more of the SMPP API than anything, at the moment.
 * <p/>
 * Demonstrates that the {@link org.springframework.integration.smpp.session.SmppSessionFactoryBean} works, too.
 *
 * @author Josh Long
 * @since 1.0
 */
@ContextConfiguration("classpath:TestSmppConnection-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class TestSmppConnection {

	@Value("${smpp.systemId}")
	private String destination;

	@Autowired
	private SubscribableChannel inboundChannel;

	@Autowired
	private MessageChannel outboundChannel;

	@Autowired
	private SubscribableChannel receiptChannel;

	private Message<String> messageOut;

	private AtomicInteger count = new AtomicInteger();

	@Before
	public void setUp() {
		messageOut = MessageBuilder.withPayload("This is the message")
				.setHeader(SmppConstants.DST_ADDR, destination)
				.setHeader(SmppConstants.SRC_ADDR, destination)
				.build();
	}

	@Test
	public void testSmppConnection() throws Throwable {
		MessageHandler standardInboundHandler = new MessageHandler() {
			@Override
			public void handleMessage(Message<?> message) throws MessagingException {
				System.out.println("Standard Inbound channel receive: " + message);
				count.incrementAndGet();
			}
		};
		MessageHandler receiptHandler = new MessageHandler() {
			@Override
			public void handleMessage(Message<?> message) throws MessagingException {
				String received = message.getPayload().toString();
				System.out.println("Outbound channel output receive receipt: " + received);
			}
		};
		inboundChannel.subscribe(standardInboundHandler);
		receiptChannel.subscribe(receiptHandler);

		outboundChannel.send(messageOut);

		Thread.sleep(5000);
	}

}
