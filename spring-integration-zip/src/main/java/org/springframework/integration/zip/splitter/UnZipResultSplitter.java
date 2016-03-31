/*
 * Copyright 2015 the original author or authors.
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

import org.apache.commons.io.FilenameUtils;
import org.springframework.integration.file.FileHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.zip.ZipHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Gunnar Hillert
 * @since 1.0
 *
 */
public class UnZipResultSplitter {

	public List<Message<Object>> splitUnzippedMap(Message<Map<String, Object>> message) {
		return createMessages(message.getPayload(), message.getHeaders());
	}

	private List<Message<Object>> createMessages(Map<String, Object> unzippedEntries, MessageHeaders headers) {
		List<Message<Object>> messages = createList(unzippedEntries.size());
		for (Map.Entry<String, Object> entry : unzippedEntries.entrySet()) {
			messages.add(createMessage(entry, headers));
		}
		return messages;
	}

	private static List<Message<Object>> createList(int initialCapacity) {
		return new ArrayList<Message<Object>>(initialCapacity);
	}

	private static Message<Object> createMessage(Map.Entry<String, Object> entry, Map<String, ?> headers) {
		return MessageBuilder.withPayload(entry.getValue())
                .setHeader(FileHeaders.FILENAME, getName(entry))
                .setHeader(ZipHeaders.ZIP_ENTRY_PATH, getPath(entry))
				.copyHeaders(headers)
                .build();
	}

	private static String getName(Map.Entry<String, Object> entry) {
		return FilenameUtils.getName(entry.getKey());
	}

	private static String getPath(Map.Entry<String, Object> entry) {
		return FilenameUtils.getPath(entry.getKey());
	}
}
