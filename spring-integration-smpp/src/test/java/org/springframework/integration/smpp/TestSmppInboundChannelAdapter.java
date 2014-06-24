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

import org.jsmpp.bean.SMSCDeliveryReceipt;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.integration.smpp.core.SmppConstants;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration("classpath:TestSmppInboundChannelAdapter-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class TestSmppInboundChannelAdapter {

	private String smsMessageToSend = "test SMPP message being sent from this time:"+ System.currentTimeMillis()+".";

	@Value("${test.dst.number}")
	private String number;

	private AtomicInteger atomicInteger = new AtomicInteger();

	@Value("#{outbound}")
	private SubscribableChannel out;

	@Value("#{inbound}")
	private SubscribableChannel in;

	@Autowired
	private ApplicationContext context;

	@Before
	public void before () throws Throwable {
		Assert.assertNotNull(this.number);
	}

	private String lastReceivedSms = null ;

	@Test
	public void testReceiving() throws Throwable {


		in.subscribe(new MessageHandler() {
			public void handleMessage(Message<?> message) throws MessagingException {
				lastReceivedSms =(message.getPayload().toString());
				atomicInteger.incrementAndGet();
			}
		});

		// lets send something
		Message<String> smsMsg = MessageBuilder.withPayload(this.smsMessageToSend)
					.setHeader(SmppConstants.SRC_ADDR, this.number)
					.setHeader(SmppConstants.DST_ADDR, this.number)
					.setHeader(SmppConstants.REGISTERED_DELIVERY_MODE, SMSCDeliveryReceipt.SUCCESS)
					.build();
		out.send(smsMsg);

		Thread.sleep(1000 * 10);

		Assert.assertTrue(atomicInteger.intValue()>0);
		Assert.assertEquals(atomicInteger.intValue() ,1);
		Assert.assertEquals(this.smsMessageToSend, lastReceivedSms);
	}
}
