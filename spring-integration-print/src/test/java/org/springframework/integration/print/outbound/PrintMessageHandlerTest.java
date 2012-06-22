package org.springframework.integration.print.outbound;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.integration.support.MessageBuilder;

public class PrintMessageHandlerTest {

	//@Test
	//@Ignore
	public void test() {
		PrintMessageHandler handler = new PrintMessageHandler();

		handler.handleMessage(MessageBuilder.withPayload("This is a test".getBytes()).build());
	}

}
