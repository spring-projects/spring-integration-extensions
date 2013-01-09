package org.springframework.integration.smpp;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.MessagingException;
import org.springframework.integration.core.MessageHandler;
import org.springframework.integration.core.SubscribableChannel;
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
 * @since 2.1
 */
@ContextConfiguration("classpath:TestSmppConnection-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class TestSmppConnection {

    @Value("${smpp.systemId}")
    String destination;

    @Autowired SubscribableChannel inboundChannel;
    @Autowired MessageChannel outboundChannel;
    @Autowired SubscribableChannel receiptChannel;

    Message<String> messageOut;

    AtomicInteger count = new AtomicInteger();

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
