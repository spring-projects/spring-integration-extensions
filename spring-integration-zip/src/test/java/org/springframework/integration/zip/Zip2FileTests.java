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

package org.springframework.integration.zip;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.integration.file.FileHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

/**
 *
 * @author Gunnar Hillert
 * @since 1.0
 *
 */
public class Zip2FileTests {

	private AnnotationConfigApplicationContext context;
	private MessageChannel input;

	private static final Properties properties = new Properties();

	@Rule
	public TemporaryFolder testFolder = new TemporaryFolder();

	private File workDir;

	@Before
	public void setup() throws IOException {
		this.workDir = testFolder.newFolder();
		properties.put("workDir", workDir);
		System.out.print(this.workDir.getAbsolutePath());

		context = new AnnotationConfigApplicationContext();
		context.register(ContextConfiguration.class);
		context.refresh();
		input = context.getBean("input", MessageChannel.class);
	}

	@After
	public void cleanup() {
		if (context != null) {
			context.close();
		}
	}

	@Test
	public void zipStringWithDefaultFileName() throws FileNotFoundException, IOException, InterruptedException {

		final Message<String> message = MessageBuilder.withPayload("Zip me up.").build();

		input.send(message);

		Assert.assertTrue(this.workDir.list().length == 1);

		File fileInWorkDir = this.workDir.listFiles()[0];

		Assert.assertTrue(fileInWorkDir.isFile());
		Assert.assertTrue(fileInWorkDir.getName().contains(message.getHeaders().getId().toString()));
		Assert.assertTrue("The created file should have a 'zip' file extension.", fileInWorkDir.getName().endsWith(".zip"));
	}

	@Test
	public void zipStringWithExplicitFileName() throws FileNotFoundException, IOException, InterruptedException {
		input.send(MessageBuilder.withPayload("Zip me up.").setHeader(FileHeaders.FILENAME, "zipString.zip").build());

		Assert.assertTrue(this.workDir.list().length == 1);
		Assert.assertEquals("zipString.zip", this.workDir.listFiles()[0].getName());
	}

	@Test
	public void zipBytesWithExplicitFileName() throws FileNotFoundException, IOException, InterruptedException {

		input.send(MessageBuilder.withPayload("Zip me up.".getBytes()).setHeader(FileHeaders.FILENAME, "zipString.zip").build());

		Assert.assertTrue(this.workDir.list().length == 1);
		Assert.assertEquals("zipString.zip", this.workDir.listFiles()[0].getName());
	}

	@Test
	public void zipFile() throws FileNotFoundException, IOException, InterruptedException {

		final File fileToCompress = testFolder.newFile();
		FileUtils.writeStringToFile(fileToCompress, "hello world");

		input.send(MessageBuilder.withPayload(fileToCompress).build());

		Assert.assertTrue(this.workDir.list().length == 1);
		Assert.assertEquals(fileToCompress.getName() + ".zip", this.workDir.listFiles()[0].getName());
	}

	@Test
	public void zipIterableWithMultipleStrings() throws FileNotFoundException, IOException, InterruptedException {

		String stringToCompress1 = "String1";
		String stringToCompress2 = "String2";
		String stringToCompress3 = "String3";
		String stringToCompress4 = "String4";

		final List<String> stringsToCompress = new ArrayList<String>(4);

		stringsToCompress.add(stringToCompress1);
		stringsToCompress.add(stringToCompress2);
		stringsToCompress.add(stringToCompress3);
		stringsToCompress.add(stringToCompress4);

		input.send(MessageBuilder.withPayload(stringsToCompress).setHeader(FileHeaders.FILENAME, "zipWith4Strings.zip").build());

		Assert.assertTrue(this.workDir.list().length == 1);
		Assert.assertEquals("zipWith4Strings.zip", this.workDir.listFiles()[0].getName());
	}

	@Test
	public void zipIterableWithDifferentTypes() throws FileNotFoundException, IOException, InterruptedException {

		String stringToCompress = "String1";
		byte[] bytesToCompress  = "String2".getBytes();
		final File fileToCompress = testFolder.newFile();
		FileUtils.writeStringToFile(fileToCompress, "hello world");

		final List<Object> objectsToCompress = new ArrayList<Object>(3);

		objectsToCompress.add(stringToCompress);
		objectsToCompress.add(bytesToCompress);
		objectsToCompress.add(fileToCompress);

		input.send(MessageBuilder.withPayload(objectsToCompress).setHeader(FileHeaders.FILENAME, "objects-to-compress.zip").build());

		Assert.assertTrue(this.workDir.list().length == 1);
		Assert.assertEquals("objects-to-compress.zip", this.workDir.listFiles()[0].getName());
	}

	@Configuration
	@ImportResource("classpath:org/springframework/integration/zip/Zip2FileTests-context.xml")
	public static class ContextConfiguration {

		@Bean
		Properties properties() throws IOException {
			return properties;
		}

	}
}
