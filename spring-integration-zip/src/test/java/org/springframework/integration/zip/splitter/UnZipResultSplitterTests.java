/*
 * Copyright 2015-2016 the original author or authors.
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

package org.springframework.integration.zip.splitter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.file.FileHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.zip.ZipHeaders;
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

	private static final String DIR_1 = "dir1/";
	private static final String DIR_2 = "dir2/";

	private static final String FILE_1 = "file1";
	private static final String FILE_2 = "file2";

	private static final String DATA_1 = "data1";
	private static final String DATA_2 = "data2";

	private static int TIMEOUT = 10000;

	@Autowired
	private MessageChannel input;

	@Autowired
	private QueueChannel output;

	@Test
	public void splitPreservingSourceMessageHeaderValues() {

		final String headerName = "headerName";
		final String headerValue = "headerValue";

		Message<?> inMessage = MessageBuilder.withPayload(createPayload())
				.setHeader(headerName, headerValue)
				.build();

		input.send(inMessage);

		Message<?> message1 = output.receive(TIMEOUT);
		checkMessage(message1, headerName, headerValue, DATA_1);

		Message<?> message2 = output.receive(TIMEOUT);
		checkMessage(message2, headerName, headerValue, DATA_2);
	}

	private static void checkMessage(Message<?> message, String headerName, String headerValue, String payload) {
		assertNotNull(message);
		checkHeaderValue(message, headerName, headerValue);
		checkPayload(message, payload);
	}

	@Test
	public void splitPreservingServiceHeaderValues() {

		Message<?> inMessage = MessageBuilder.withPayload(createPayload())
				.setHeader(ZipHeaders.ZIP_ENTRY_PATH, "dir")
				.setHeader(FileHeaders.FILENAME, "filename")
				.build();

		input.send(inMessage);

		Message<?> message1 = output.receive(TIMEOUT);
		checkMessage(message1, DIR_1, FILE_1, DATA_1, true);

		Message<?> message2 = output.receive(TIMEOUT);
		checkMessage(message2, DIR_2, FILE_2, DATA_2, true);
	}

	private static void checkMessage(Message<?> message, String path, String filename, String payload, boolean flag) {
		assertNotNull(message);
		checkHeaderValue(message, ZipHeaders.ZIP_ENTRY_PATH, path);
		checkHeaderValue(message, FileHeaders.FILENAME, filename);
		checkPayload(message, payload);
	}

	private static Map<String, Object> createPayload() {
		Map<String, Object> payload = new LinkedHashMap<String, Object>();
		payload.put(DIR_1 + FILE_1, DATA_1);
		payload.put(DIR_2 + FILE_2, DATA_2);
		return payload;
	}

	private static void checkPayload(Message<?> message, String payload) {
		assertEquals(payload, message.getPayload());
	}

	private static void checkHeaderValue(Message<?> message, String headerName, String headerValue) {
		assertEquals(headerValue, message.getHeaders().get(headerName));
	}
}
