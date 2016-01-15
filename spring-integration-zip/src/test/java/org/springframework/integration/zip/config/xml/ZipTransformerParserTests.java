/*
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.springframework.integration.zip.config.xml;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.nio.charset.Charset;
import java.util.zip.Deflater;

import org.junit.After;
import org.junit.Test;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.integration.endpoint.EventDrivenConsumer;
import org.springframework.integration.file.DefaultFileNameGenerator;
import org.springframework.integration.file.FileNameGenerator;
import org.springframework.integration.test.util.TestUtils;
import org.springframework.integration.transformer.MessageTransformingHandler;
import org.springframework.integration.zip.transformer.ZipResultType;
import org.springframework.integration.zip.transformer.ZipTransformer;
import org.springframework.util.Assert;

/**
 * @author Gunnar Hillert
 * @since 1.0
 */
public class ZipTransformerParserTests {

	private ConfigurableApplicationContext context;

	@Test
	public void testZipTransformerParserWithDefaults() {

		setUp("ZipTransformerParserTests.xml", getClass());

		EventDrivenConsumer consumer = this.context.getBean("zipTransformerWithDefaults", EventDrivenConsumer.class);

		final AbstractMessageChannel inputChannel = TestUtils.getPropertyValue(consumer, "inputChannel", AbstractMessageChannel.class);
		assertEquals("input", inputChannel.getComponentName());

		final MessageTransformingHandler handler = TestUtils.getPropertyValue(consumer, "handler", MessageTransformingHandler.class);

		final AbstractMessageChannel outputChannel = TestUtils.getPropertyValue(handler, "outputChannel", AbstractMessageChannel.class);
		assertEquals("output", outputChannel.getComponentName());

		final ZipTransformer zipTransformer = TestUtils.getPropertyValue(handler, "transformer", ZipTransformer.class);

		final Charset charset = TestUtils.getPropertyValue(zipTransformer, "charset", Charset.class);
		final FileNameGenerator fileNameGenerator = TestUtils.getPropertyValue(zipTransformer, "fileNameGenerator", FileNameGenerator.class);
		final ZipResultType zipResultType = TestUtils.getPropertyValue(zipTransformer, "zipResultType", ZipResultType.class);
		final File workDirectory = TestUtils.getPropertyValue(zipTransformer, "workDirectory", File.class);
		final Integer compressionLevel = TestUtils.getPropertyValue(zipTransformer, "compressionLevel", Integer.class);
		final Boolean deleteFiles = TestUtils.getPropertyValue(zipTransformer, "deleteFiles", Boolean.class);

		assertNotNull(charset);
		assertNotNull(fileNameGenerator);
		assertNotNull(zipResultType);
		assertNotNull(workDirectory);
		assertNotNull(compressionLevel);
		assertNotNull(deleteFiles);

		assertEquals(Charset.defaultCharset(), charset);
		Assert.isInstanceOf(DefaultFileNameGenerator.class, fileNameGenerator);
		assertEquals(ZipResultType.FILE, zipResultType);
		assertEquals(new File(System.getProperty("java.io.tmpdir") + File.separator + "ziptransformer"), workDirectory);
		assertTrue("WorkDirectory should exist.", workDirectory.exists());
		assertTrue("WorkDirectory should be a directory.", workDirectory.isDirectory());
		assertEquals(Integer.valueOf(Deflater.DEFAULT_COMPRESSION), Integer.valueOf(compressionLevel));
		assertFalse("By default the 'deleteFiles' property should be false.", deleteFiles);
	}

	@Test
	public void testZipTransformerParserWithExplicitSettings() {

		setUp("ZipTransformerParserTests.xml", getClass());

		EventDrivenConsumer consumer = this.context.getBean("zipTransformer", EventDrivenConsumer.class);

		final AbstractMessageChannel inputChannel = TestUtils.getPropertyValue(consumer, "inputChannel", AbstractMessageChannel.class);
		assertEquals("input", inputChannel.getComponentName());

		final MessageTransformingHandler handler = TestUtils.getPropertyValue(consumer, "handler", MessageTransformingHandler.class);

		final AbstractMessageChannel outputChannel = TestUtils.getPropertyValue(handler, "outputChannel", AbstractMessageChannel.class);
		assertEquals("output", outputChannel.getComponentName());

		final ZipTransformer zipTransformer = TestUtils.getPropertyValue(handler, "transformer", ZipTransformer.class);

		final Charset charset = TestUtils.getPropertyValue(zipTransformer, "charset", Charset.class);
		final FileNameGenerator fileNameGenerator = TestUtils.getPropertyValue(zipTransformer, "fileNameGenerator", FileNameGenerator.class);
		final ZipResultType zipResultType = TestUtils.getPropertyValue(zipTransformer, "zipResultType", ZipResultType.class);
		final File workDirectory = TestUtils.getPropertyValue(zipTransformer, "workDirectory", File.class);
		final Integer compressionLevel = TestUtils.getPropertyValue(zipTransformer, "compressionLevel", Integer.class);
		final Boolean deleteFiles = TestUtils.getPropertyValue(zipTransformer, "deleteFiles", Boolean.class);

		assertNotNull(charset);
		assertNotNull(fileNameGenerator);
		assertNotNull(zipResultType);
		assertNotNull(workDirectory);
		assertNotNull(compressionLevel);
		assertNotNull(deleteFiles);

		assertEquals(Charset.defaultCharset(), charset);
		Assert.isInstanceOf(DefaultFileNameGenerator.class, fileNameGenerator);
		assertEquals(ZipResultType.BYTE_ARRAY, zipResultType);
		assertEquals(new File(System.getProperty("java.io.tmpdir") + File.separator + "ziptransformer"), workDirectory);
		assertTrue("WorkDirectory should exist.", workDirectory.exists());
		assertTrue("WorkDirectory should be a directory.", workDirectory.isDirectory());
		assertEquals(Integer.valueOf(2), Integer.valueOf(compressionLevel));
		assertTrue("The 'deleteFiles' property should be true.", deleteFiles);
	}

	@Test
	public void testZipTransformerParserWithIncorrectResultType() {

		try {
			setUp("ZipTransformerParserTestsWithIncorrectResultType.xml", getClass());
			fail("Expected a BeanDefinitionParsingException to be thrown.");
		}
		catch (BeanCreationException e) {
			assertThat(e.getMessage(), containsString("Failed to convert property value of type [java.lang.String] " +
					"to required type [org.springframework.integration.zip.transformer.ZipResultType] "));
		}
	}

	@After
	public void tearDown() {
		if (context != null) {
			context.close();
		}
	}

	public void setUp(String name, Class<?> cls) {
		context = new ClassPathXmlApplicationContext(name, cls);
	}

}
