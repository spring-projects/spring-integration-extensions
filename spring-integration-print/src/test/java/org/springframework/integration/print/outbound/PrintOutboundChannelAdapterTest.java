package org.springframework.integration.print.outbound;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class PrintOutboundChannelAdapterTest {

	@Autowired
	private DirectChannel channel;

	@Test
	//@Ignore
	public void test() {
		final Message<String> message = MessageBuilder.withPayload("This is a test.").build();
		channel.send(message);
	}

}
