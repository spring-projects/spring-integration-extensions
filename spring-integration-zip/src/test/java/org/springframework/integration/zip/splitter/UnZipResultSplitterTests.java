package org.springframework.integration.zip.splitter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Andriy Kryvtsun
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class UnZipResultSplitterTests {

	private static int TIMEOUT = 10000;

	@Autowired
	private MessageChannel input;

	@Autowired
	private QueueChannel output;

	@Test
	public void splitUnZippedDataWithPreservingHeaderValues() {

		final String headerName = "headerName";
		final String headerValue = "headerValue";

		Map<String, Object> payload = new HashMap<String, Object>();
		payload.put("file1", "data1");
		payload.put("file2", "data2");

		Message<?> inMessage = MessageBuilder.withPayload(payload)
				.setHeader(headerName, headerValue)
				.build();

		input.send(inMessage);

		checkMessage(output.receive(TIMEOUT), headerName, headerValue);
		checkMessage(output.receive(TIMEOUT), headerName, headerValue);
	}

	private static void checkMessage(Message<?> message, String headerName, String headerValue) {
		assertNotNull(message);
		assertEquals(headerValue, message.getHeaders().get(headerName));
	}
}
