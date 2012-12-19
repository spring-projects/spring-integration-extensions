package org.springframework.integration.smpp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsmpp.bean.SMSCDeliveryReceipt;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.Message;
import org.springframework.integration.MessagingException;
import org.springframework.integration.core.MessageHandler;
import org.springframework.integration.core.SubscribableChannel;
import org.springframework.integration.smpp.core.SmppConstants;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.atomic.AtomicInteger;

@ContextConfiguration("classpath:TestSmppInboundChannelAdapter-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class TestSmppInboundChannelAdapter {

	private String smsMessageToSend = "test SMPP message being sent from this time:"+ System.currentTimeMillis()+".";

	@Value("${test.dst.number}") String number;

	private Log log = LogFactory.getLog(getClass());

	private AtomicInteger atomicInteger = new AtomicInteger();

	@Value("#{outbound}") SubscribableChannel out;

	@Value("#{inbound}") SubscribableChannel in;

	@Autowired
	private ApplicationContext context;

	@Before
	public void before () throws Throwable {
		Assert.assertNotNull(this.number);
	}

	 String lastReceivedSms = null ;

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
