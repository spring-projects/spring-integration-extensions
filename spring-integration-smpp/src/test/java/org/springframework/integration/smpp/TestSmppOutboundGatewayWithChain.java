/* Copyright 2002-2014 the original author or authors.
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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.smpp.core.SmppConstants;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

/**
 * Simple tests to make sure that gateway will perform retry and route MessagingException
 * to exception channel when advice chain is defined with a retry advice and error callback.
 *
 * @author Johanes Soetanto
 * @author Edge Dalmacio
 * @since 1.0
 */
@ContextConfiguration("classpath:TestSmppOutboundGatewayWithChain-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class TestSmppOutboundGatewayWithChain {

	@Autowired
	private MessagingTemplate messagingTemplate;

	@Autowired
	private MessageChannel outChannel;

	private String smsMessageToSend = "jSMPP is truly a convenient, and powerful API for SMPP " +
			"on the Java and Spring Integration platforms (sent " + System.currentTimeMillis() + ")";

	@Test
	public void testSendingGoesToExceptionChannel() throws Throwable {
		Message<String> smsMsg = MessageBuilder.withPayload(this.smsMessageToSend)
				.setHeader(SmppConstants.SRC_ADDR, "1616")
				.setHeader(SmppConstants.DST_ADDR, "NoRouteDestination")
				.build();
		outChannel.send(smsMsg);

		Thread.sleep(500);
		Message<?> exception = messagingTemplate.receive("exceptionChannel");
		Assert.assertNotNull(exception);
		Assert.assertTrue(exception.getPayload() instanceof MessagingException);
	}

	@Test
	public void testSendingGoesToReplyChannel() throws Throwable {
		Message<String> smsMsg = MessageBuilder.withPayload(this.smsMessageToSend)
				.setHeader(SmppConstants.SRC_ADDR, "1616")
				.setHeader(SmppConstants.DST_ADDR, "1616")
				.build();
		outChannel.send(smsMsg);

		Thread.sleep(500);
		Message<?> exception = messagingTemplate.receive("replyChannel");
		Assert.assertNotNull(exception);
		Assert.assertTrue(exception.getPayload() instanceof List);
	}
}
