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

package org.springframework.integration.zip;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.zeroturnaround.zip.ZipException;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.transformer.MessageTransformationException;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandlingException;

/**
 *
 * @author Gunnar Hillert
 * @since 1.0
 *
 */
public class UnZip2FileTests {

	private AnnotationConfigApplicationContext context;
	private ResourceLoader resourceLoader;
	private MessageChannel input;

	private static final Properties properties = new Properties();

	@Rule
	public TemporaryFolder testFolder = new TemporaryFolder();

	private File workDir;

	@Before
	public void setup() throws IOException {
		this.workDir = this.testFolder.newFolder();
		properties.put("workDir", this.workDir);
		System.out.print(this.workDir.getAbsolutePath());

		this.context = new AnnotationConfigApplicationContext();
		this.context.register(ContextConfiguration.class);
		this.context.refresh();
		this.input = this.context.getBean("input", MessageChannel.class);
		this.resourceLoader = this.context;
	}

	@After
	public void cleanup() {
		if (this.context != null) {
			this.context.close();
		}
	}

	@Test
	public void unZipWithOneEntry() throws Exception {

		final Resource resource = this.resourceLoader.getResource("classpath:testzipdata/single.zip");
		final InputStream is = resource.getInputStream();

		byte[] zipdata = IOUtils.toByteArray(is);

		final Message<byte[]> message = MessageBuilder.withPayload(zipdata).build();

		input.send(message);

		Assert.assertTrue(this.workDir.list().length == 1);

		File fileInWorkDir = this.workDir.listFiles()[0];

		Assert.assertTrue(fileInWorkDir.isFile());
		Assert.assertEquals("single.txt", fileInWorkDir.getName());
	}

	@Test
	public void unZipWithMultipleEntries() throws Exception {

		final Resource resource = resourceLoader.getResource("classpath:testzipdata/countries.zip");
		final InputStream is = resource.getInputStream();

		byte[] zipdata = IOUtils.toByteArray(is);

		final Message<byte[]> message = MessageBuilder.withPayload(zipdata).build();

		input.send(message);

		Assert.assertTrue(this.workDir.list().length == 4);

		File[] files = this.workDir.listFiles();

		boolean continents = false;
		boolean de = false;
		boolean fr = false;
		boolean pl = false;

		for (File file : files) {
			if (file.getName().equals("continents")) {
				continents = true;
				Assert.assertTrue(file.isDirectory());
				Assert.assertTrue(file.list().length == 2);
			}
			if (file.getName().equals("de.txt")) {
				de = true;
				Assert.assertTrue(file.isFile());
			}
			if (file.getName().equals("fr.txt")) {
				fr = true;
				Assert.assertTrue(file.isFile());
			}
			if (file.getName().equals("pl.txt")) {
				pl = true;
				Assert.assertTrue(file.isFile());
			}
		}

		Assert.assertTrue(continents);
		Assert.assertTrue(de);
		Assert.assertTrue(fr);
		Assert.assertTrue(pl);

	}

	@Test
	public void unZipTraversal() throws Exception {
		final Resource resource = this.resourceLoader.getResource("classpath:testzipdata/zip-malicious-traversal.zip");
		final InputStream is = resource.getInputStream();
		byte[] zipdata = IOUtils.toByteArray(is);
		final Message<byte[]> message = MessageBuilder.withPayload(zipdata).build();
		try {
			input.send(message);
			fail("Expected Exception");
		}
		catch (Exception e) {
			Assert.assertThat(e, instanceOf(MessageTransformationException.class));
			Assert.assertThat(e.getCause(), instanceOf(MessageHandlingException.class));
			Assert.assertThat(e.getCause().getCause(), instanceOf(ZipException.class));
			Assert.assertThat(e.getCause().getCause().getMessage(),
					containsString("is trying to leave the target output directory"));
		}
	}

	@Configuration
	@ImportResource("classpath:org/springframework/integration/zip/UnZip2FileTests-context.xml")
	public static class ContextConfiguration {

		@Bean
		Properties properties() throws IOException {
			return properties;
		}

	}

}
