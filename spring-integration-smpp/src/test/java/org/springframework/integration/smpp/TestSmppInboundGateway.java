package org.springframework.integration.smpp;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.Message;
import org.springframework.integration.core.MessageHandler;
import org.springframework.integration.core.SubscribableChannel;
import org.springframework.integration.handler.AbstractReplyProducingMessageHandler;
import org.springframework.integration.smpp.core.SmesMessageSpecification;
import org.springframework.integration.smpp.session.ExtendedSmppSession;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.atomic.AtomicInteger;

@ContextConfiguration("classpath:TestSmppInboundGateway-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class TestSmppInboundGateway {

	@Value("#{out1}") SubscribableChannel out1;
	@Value("#{out2}") SubscribableChannel out2;
	@Value("#{in1}") SubscribableChannel in1;
	@Value("#{in2}") SubscribableChannel in2;

	// test data
	String toPhone = "33333";   // todo make sure the gateway automatically 'flips' with the to/from on the reply SMS
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

		SmesMessageSpecification.newSmesMessageSpecification(outSession, this.fromPhone, this.toPhone, this.smsRequest).send();

		MessageHandler inboundMessageHandler = new AbstractReplyProducingMessageHandler() {
			 @Override
			 protected Object handleRequestMessage(Message<?> requestMessage) {
				 Assert.assertEquals(requestMessage.getPayload(), smsRequest);
				 count.incrementAndGet();
				 return MessageBuilder.withPayload(
						 smsResponse).copyHeadersIfAbsent( requestMessage.getHeaders()).build();

			 }
		 };
		this.in1.subscribe(inboundMessageHandler);

		// launch the whole thing



	 	Thread.sleep(1000 * 10);

		Assert.assertEquals(this.count.intValue(),1);


	}
}


