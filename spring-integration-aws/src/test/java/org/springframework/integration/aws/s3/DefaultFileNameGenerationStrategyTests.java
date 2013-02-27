/*
 * Copyright 2002-2013 the original author or authors.
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

package org.springframework.integration.aws.s3;

import java.io.File;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;


/**
 * The test class for {@link DefaultFileNameGenerationStrategy}
 *
 * @author Amol Nayak
 *
 * @since 0.5
 *
 */
public class DefaultFileNameGenerationStrategyTests {

	/**
	 * Tests with the file name present in the predetermined header "file_name" of the message
	 */
	@Test
	public void withNameInHeader() {
		Message<String> message = MessageBuilder.withPayload("SomeString")
							.setHeader(AmazonS3MessageHeaders.FILE_NAME, "FileName.txt")
							.build();
		DefaultFileNameGenerationStrategy strategy = new DefaultFileNameGenerationStrategy();
		Assert.assertEquals("FileName.txt", strategy.generateFileName(message));
	}


	/**
	 * Tests with a payload as a temp file payload
	 */
	@Test
	public void withATempFile() {
		File file = new File(System.getProperty("java.io.tmpdir") + "TempFile.txt.writing");
		Message<File> message = MessageBuilder.withPayload(file)
								.build();
		DefaultFileNameGenerationStrategy strategy = new DefaultFileNameGenerationStrategy();
		Assert.assertEquals("TempFile.txt", strategy.generateFileName(message));
		file.delete();
	}

	/**
	 * Tests with a payload as a temp file payload
	 */
	@Test
	public void withANonTempFile() {
		File file = new File(System.getProperty("java.io.tmpdir") + "TempFile.txt");
		Message<File> message = MessageBuilder.withPayload(file)
								.build();
		DefaultFileNameGenerationStrategy strategy = new DefaultFileNameGenerationStrategy();
		Assert.assertEquals("TempFile.txt", strategy.generateFileName(message));
		file.delete();
	}

	/**
	 * Tests with a payload as a temp file payload
	 */
	@Test
	public void withMessageIdName() {
		Message<String> message = MessageBuilder.withPayload("String")
								.build();
		DefaultFileNameGenerationStrategy strategy = new DefaultFileNameGenerationStrategy();
		UUID uid = message.getHeaders().getId();
		Assert.assertEquals(uid.toString() + ".ext", strategy.generateFileName(message));
	}

	/**
	 * Tests with the file name generation expression as a null value
	 */
	@Test(expected=IllegalArgumentException.class)
	public void withNullExprssion() {
		DefaultFileNameGenerationStrategy strategy = new DefaultFileNameGenerationStrategy();
		strategy.setFileNameExpression(null);
	}

	/**
	 * Tests with a null value for temporary suffix
	 */
	@Test(expected=IllegalArgumentException.class)
	public void withNullTemporarySuffix() {
		DefaultFileNameGenerationStrategy strategy = new DefaultFileNameGenerationStrategy();
		strategy.setTemporarySuffix(null);
	}
}
