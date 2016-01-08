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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.Charset;

import org.junit.After;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.integration.endpoint.EventDrivenConsumer;
import org.springframework.integration.file.DefaultFileNameGenerator;
import org.springframework.integration.file.FileNameGenerator;
import org.springframework.integration.test.util.TestUtils;
import org.springframework.integration.transformer.MessageTransformingHandler;
import org.springframework.integration.zip.transformer.UnZipTransformer;
import org.springframework.integration.zip.transformer.ZipResultType;
import org.springframework.util.Assert;

/**
 *
 * @author Gunnar Hillert
 * @since 1.0
 *
 */
public class UnZipTransformerParserTests {

	private ConfigurableApplicationContext context;

	@Test
	public void testUnZipTransformerParserWithDefaults() {

		setUp("UnZipTransformerParserTests.xml", getClass());

		EventDrivenConsumer consumer   = this.context.getBean("unzipTransformerWithDefaults", EventDrivenConsumer.class);

		final AbstractMessageChannel inputChannel = TestUtils.getPropertyValue(consumer, "inputChannel", AbstractMessageChannel.class);
		assertEquals("input", inputChannel.getComponentName());

		final MessageTransformingHandler handler = TestUtils.getPropertyValue(consumer, "handler", MessageTransformingHandler.class);

		final AbstractMessageChannel outputChannel = TestUtils.getPropertyValue(handler, "outputChannel", AbstractMessageChannel.class);
		assertEquals("output", outputChannel.getComponentName());

		final UnZipTransformer unZipTransformer = TestUtils.getPropertyValue(handler, "transformer", UnZipTransformer.class);

		final Charset charset = TestUtils.getPropertyValue(unZipTransformer, "charset", Charset.class);
		final FileNameGenerator fileNameGenerator = TestUtils.getPropertyValue(unZipTransformer, "fileNameGenerator", FileNameGenerator.class);
		final ZipResultType zipResultType = TestUtils.getPropertyValue(unZipTransformer, "zipResultType", ZipResultType.class);
		final File workDirectory = TestUtils.getPropertyValue(unZipTransformer, "workDirectory", File.class);
		final Boolean deleteFiles = TestUtils.getPropertyValue(unZipTransformer, "deleteFiles", Boolean.class);
		final Boolean expectSingleResult = TestUtils.getPropertyValue(unZipTransformer, "expectSingleResult", Boolean.class);

		assertNotNull(charset);
		assertNotNull(fileNameGenerator);
		assertNotNull(zipResultType);
		assertNotNull(workDirectory);
		assertNotNull(deleteFiles);
		assertNotNull(expectSingleResult);

		assertEquals(Charset.defaultCharset(), charset);
		Assert.isInstanceOf(DefaultFileNameGenerator.class, fileNameGenerator);
		assertEquals(ZipResultType.FILE, zipResultType);
		assertEquals(new File(System.getProperty("java.io.tmpdir") + File.separator + "ziptransformer"), workDirectory);
		assertTrue("WorkDirectory should exist.", workDirectory.exists());
		assertTrue("WorkDirectory should be a directory.", workDirectory.isDirectory());
		assertFalse("By default the 'deleteFiles' property should be false.", deleteFiles);
		assertFalse("The 'expectSingleResult' property should be false.", expectSingleResult);
	}

	@Test
	public void testUnZipTransformerParserWithExplicitSettings() {

		setUp("UnZipTransformerParserTests.xml", getClass());

		EventDrivenConsumer consumer   = this.context.getBean("unzipTransformer", EventDrivenConsumer.class);

		final AbstractMessageChannel inputChannel = TestUtils.getPropertyValue(consumer, "inputChannel", AbstractMessageChannel.class);
		assertEquals("input", inputChannel.getComponentName());

		final MessageTransformingHandler handler = TestUtils.getPropertyValue(consumer, "handler", MessageTransformingHandler.class);

		final AbstractMessageChannel outputChannel = TestUtils.getPropertyValue(handler, "outputChannel", AbstractMessageChannel.class);
		assertEquals("output", outputChannel.getComponentName());

		final UnZipTransformer unZipTransformer = TestUtils.getPropertyValue(handler, "transformer", UnZipTransformer.class);

		final Charset charset = TestUtils.getPropertyValue(unZipTransformer, "charset", Charset.class);
		final FileNameGenerator fileNameGenerator = TestUtils.getPropertyValue(unZipTransformer, "fileNameGenerator", FileNameGenerator.class);
		final ZipResultType zipResultType = TestUtils.getPropertyValue(unZipTransformer, "zipResultType", ZipResultType.class);
		final File workDirectory = TestUtils.getPropertyValue(unZipTransformer, "workDirectory", File.class);
		final Boolean deleteFiles = TestUtils.getPropertyValue(unZipTransformer, "deleteFiles", Boolean.class);
		final Boolean expectSingleResult = TestUtils.getPropertyValue(unZipTransformer, "expectSingleResult", Boolean.class);

		assertNotNull(charset);
		assertNotNull(fileNameGenerator);
		assertNotNull(zipResultType);
		assertNotNull(workDirectory);
		assertNotNull(deleteFiles);
		assertNotNull(expectSingleResult);

		assertEquals(Charset.defaultCharset(), charset);
		Assert.isInstanceOf(DefaultFileNameGenerator.class, fileNameGenerator);
		assertEquals(ZipResultType.FILE, zipResultType);
		assertEquals(new File(System.getProperty("java.io.tmpdir") + File.separator + "ziptransformer"), workDirectory);
		assertTrue("WorkDirectory should exist.", workDirectory.exists());
		assertTrue("WorkDirectory should be a directory.", workDirectory.isDirectory());
		assertTrue("The 'deleteFiles' property should be true.", deleteFiles);
		assertTrue("The 'expectSingleResult' property should be true.", expectSingleResult);
	}

	@After
	public void tearDown(){
		if(context != null){
			context.close();
		}
	}

	public void setUp(String name, Class<?> cls){
		context    = new ClassPathXmlApplicationContext(name, cls);
	}

}
