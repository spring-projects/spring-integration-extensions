/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.integration.samples.zip;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.file.FileHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

/**
 * This Spring Integration transformation handler takes the input file, converts
 * the file into a string, converts the file contents into an upper-case string
 * and then sets a few Spring Integration message headers.
 *
 * @author Gunnar Hillert
 * @since 1.0
 */
public class TransformationHandler {

	/**
	 * Actual Spring Integration transformation handler.
	 *
	 * @param inputMessage Spring Integration input message
	 * @return New Spring Integration message with updated headers
	 */
	@Transformer
	public Message<String> handleFile(final Message<File> inputMessage) {

		final File inputFile = inputMessage.getPayload();
		final String filename = inputFile.getName();
		final String fileExtension = FilenameUtils.getExtension(filename);

		final String inputAsString;

		try {
			inputAsString = FileUtils.readFileToString(inputFile);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}

		final Message<String> message = MessageBuilder.withPayload(inputAsString.toUpperCase(Locale.ENGLISH))
					.setHeader(FileHeaders.FILENAME,      filename)
					.setHeader(FileHeaders.ORIGINAL_FILE, inputFile)
					.setHeader("file_size", inputFile.length())
					.setHeader("file_extension", fileExtension)
					.build();

		return message;
	}
}
