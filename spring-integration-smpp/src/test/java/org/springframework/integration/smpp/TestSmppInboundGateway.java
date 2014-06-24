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

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.handler.AbstractReplyProducingMessageHandler;
import org.springframework.integration.smpp.core.SmesMessageSpecification;
import org.springframework.integration.smpp.session.ExtendedSmppSession;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration("classpath:TestSmppInboundGateway-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class TestSmppInboundGateway {

	private final Logger logger = LoggerFactory.getLogger(TestSmppInboundGateway.class);

	@Value("#{out1}") SubscribableChannel out1;
	@Value("#{out2}") SubscribableChannel out2;
	@Value("#{in1}") SubscribableChannel in1;
	@Value("#{in2}") SubscribableChannel in2;

	// test data
	String toPhone = "33333";
	String fromPhone = "1111";
	long now = System.currentTimeMillis();
	String smsRequest = "this is a request created at " + now;
	String smsResponse = "this is a response created at " + now;

	@Value("#{outboundSession}")
	ExtendedSmppSession outSession;

	@Before public void before(){
		outSession.start();
	}

	AtomicInteger count = new AtomicInteger();

	@Test
	public void testSendingAndReceivingAnSms() throws Throwable {

		// the gateway *receives* SMS messages, and then expects a reply.
		// So we need to both *send* an SMS for the gateway to receive, and then *receive* the reply
		// from the gateway to confirm it was sent...

		// two sends, two receives, one pair taken care of the by the gateway
		// it would be ideal if we could in essence wrap this inbound gateway with an outbound gateway so that..

		// outbound-gw: send
		//// inbound-gw: receive, produces reply
		//// inbound-gw: send
		// outbound-gw: receive
		// however atm thats not supported by the outbound-gw, it only 'replies' with the message ID of the outbound send

		// anyway....
		//1) lets send an outbound message so that our gateway has something to listen for


		MessageHandler inboundMessageHandler = new AbstractReplyProducingMessageHandler() {
			@Override
			protected Object handleRequestMessage(Message<?> requestMessage) {
				 logger.debug("Processing incoming message for inbound-gw and produce a reply");
				Assert.assertEquals(requestMessage.getPayload(), smsRequest);
				count.incrementAndGet();
				return MessageBuilder.withPayload(
						smsResponse).copyHeadersIfAbsent( requestMessage.getHeaders()).build();

			}
		};
		this.in1.subscribe(inboundMessageHandler);

		// this is handler for reply from inbound-gateway
		MessageHandler replyHandler = new MessageHandler() {
			@Override
			public void handleMessage(Message<?> message) throws MessagingException {
				logger.debug("Reply handler receives: "+message.getPayload());
				Assert.assertEquals(message.getPayload(), smsResponse);
			}
		};
		this.in2.subscribe(replyHandler);

		// outbound gateway send
		logger.debug("Sending message from: {} to: {} message: '{}'", fromPhone, toPhone, smsRequest);
		SmesMessageSpecification.newSmesMessageSpecification(outSession, this.fromPhone, this.toPhone, this.smsRequest).send();
		/*Message<String> smsMsg = MessageBuilder.withPayload(smsRequest)
				.setHeader(SmppConstants.SRC_ADDR, fromPhone)
				.setHeader(SmppConstants.DST_ADDR, toPhone)
				.setHeader(SmppConstants.REGISTERED_DELIVERY_MODE, SMSCDeliveryReceipt.SUCCESS)
				.build();
		out2.send(smsMsg);*/

		Thread.sleep(1000 * 10);

		Assert.assertEquals(this.count.intValue(),1);


	}
}


