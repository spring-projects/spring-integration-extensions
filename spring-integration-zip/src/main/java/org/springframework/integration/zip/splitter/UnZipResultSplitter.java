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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;

import org.springframework.integration.file.FileHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.zip.ZipHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

/**
 *
 * @author Gunnar Hillert
 * @author Andriy Kryvtsun
 * @since 1.0
 *
 */
public class UnZipResultSplitter {

	public List<Message<Object>> splitUnzippedMap(Message<Map<String, Object>> message) {
		final MessageHeaders headers = message.getHeaders();
		final Map<String, Object> unzippedEntries = message.getPayload();

		final List<Message<Object>> messages = new ArrayList<Message<Object>>(unzippedEntries.size());

		for (Map.Entry<String, Object> entry : unzippedEntries.entrySet()) {
			final String path = FilenameUtils.getPath(entry.getKey());
			final String filename = FilenameUtils.getName(entry.getKey());
			final Message<Object> splitMessage = MessageBuilder.withPayload(entry.getValue())
					.setHeader(FileHeaders.FILENAME, filename)
					.setHeader(ZipHeaders.ZIP_ENTRY_PATH, path)
					.copyHeaders(headers)
					.build();
			messages.add(splitMessage);
		}
		return messages;
	}

}
