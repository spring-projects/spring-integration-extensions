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

package org.springframework.integration.zip.transformer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author Gunnar Hillert
 * @author Artem Bilan
 * @since 1.0
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class UnZipTransformerTests {

	@Rule
	public TemporaryFolder testFolder = new TemporaryFolder();

	@Autowired
	private ResourceLoader resourceLoader;

	private File workDir;

	@Before
	public void setup() throws IOException {
		this.workDir = testFolder.newFolder();
	}

	/**
	 * UnCompress a ZIP archive containing a single file only. The result will be
	 * a byte array.
	 *
	 * @throws IOException
	 */
	@Test
	public void unzipSingleFileAsInputStreamToByteArray() throws IOException {

		final Resource resource = this.resourceLoader.getResource("classpath:testzipdata/single.zip");
		final InputStream is = resource.getInputStream();

		final Message<InputStream> message = MessageBuilder.withPayload(is).build();

		final UnZipTransformer unZipTransformer = new UnZipTransformer();
		unZipTransformer.setZipResultType(ZipResultType.BYTE_ARRAY);
		unZipTransformer.afterPropertiesSet();

		final Message<?> resultMessage = unZipTransformer.transform(message);

		Assert.assertNotNull(resultMessage);

		@SuppressWarnings("unchecked")
		Map<String, byte[]> unzippedData = (Map<String, byte[]>) resultMessage.getPayload();

		Assert.assertNotNull(unzippedData);
		Assert.assertTrue(unzippedData.size() == 1);
		Assert.assertEquals("Spring Integration Rocks!", new String(unzippedData.values().iterator().next()));

	}

	/**
	 *
	 *
	 * @throws IOException
	 */
	@Test
	public void unzipSingleFileToByteArray() throws IOException {

		final Resource resource = this.resourceLoader.getResource("classpath:testzipdata/single.zip");
		final InputStream is = resource.getInputStream();

		final File inputFile = new File(this.workDir, "unzipSingleFileToByteArray");

		IOUtils.copy(is, new FileOutputStream(inputFile));

		final Message<File> message = MessageBuilder.withPayload(inputFile).build();

		final UnZipTransformer unZipTransformer = new UnZipTransformer();
		unZipTransformer.setZipResultType(ZipResultType.BYTE_ARRAY);
		unZipTransformer.afterPropertiesSet();

		final Message<?> resultMessage = unZipTransformer.transform(message);

		Assert.assertNotNull(resultMessage);

		@SuppressWarnings("unchecked")
		Map<String, byte[]> unzippedData = (Map<String, byte[]>) resultMessage.getPayload();

		Assert.assertNotNull(unzippedData);
		Assert.assertTrue(unzippedData.size() == 1);
		Assert.assertTrue(inputFile.exists());
		Assert.assertEquals("Spring Integration Rocks!", new String(unzippedData.values().iterator().next()));

	}

	/**
	 *
	 *
	 * @throws IOException
	 */
	@Test
	public void unzipSingleFileToByteArrayWithDeleteFilesTrue() throws IOException {

		final Resource resource = this.resourceLoader.getResource("classpath:testzipdata/single.zip");
		final InputStream is = resource.getInputStream();

		final File inputFile = new File(this.workDir, "unzipSingleFileToByteArray");

		FileOutputStream output = new FileOutputStream(inputFile);
		IOUtils.copy(is, output);
		output.close();

		final Message<File> message = MessageBuilder.withPayload(inputFile).build();

		final UnZipTransformer unZipTransformer = new UnZipTransformer();
		unZipTransformer.setZipResultType(ZipResultType.BYTE_ARRAY);
		unZipTransformer.setDeleteFiles(true);
		unZipTransformer.afterPropertiesSet();

		final Message<?> resultMessage = unZipTransformer.transform(message);

		Assert.assertNotNull(resultMessage);

		@SuppressWarnings("unchecked")
		Map<String, byte[]> unzippedData = (Map<String, byte[]>) resultMessage.getPayload();

		Assert.assertNotNull(unzippedData);
		Assert.assertTrue(unzippedData.size() == 1);
		Assert.assertFalse(inputFile.exists());
		Assert.assertEquals("Spring Integration Rocks!", new String(unzippedData.values().iterator().next()));

	}

	/**
	 * UnCompress a ZIP archive containing multiple files. The result will be
	 * a collection of files.
	 *
	 * @throws IOException
	 */
	@Test
	public void unzipMultipleFilesAsInputStreamToByteArray() throws IOException {

		final Resource resource = this.resourceLoader.getResource("classpath:testzipdata/countries.zip");
		final InputStream is = resource.getInputStream();

		final Message<InputStream> message = MessageBuilder.withPayload(is).build();

		final UnZipTransformer unZipTransformer = new UnZipTransformer();
		unZipTransformer.setZipResultType(ZipResultType.BYTE_ARRAY);
		unZipTransformer.afterPropertiesSet();

		final Message<?> resultMessage = unZipTransformer.transform(message);

		Assert.assertNotNull(resultMessage);

		@SuppressWarnings("unchecked")
		Map<String, byte[]> unzippedData = (Map<String, byte[]>) resultMessage.getPayload();

		Assert.assertNotNull(unzippedData);
		Assert.assertTrue(unzippedData.size() == 5);

	}

	/**
	 * UnCompress a ZIP archive containing multiple files. The result will be
	 * a collection of files.
	 *
	 * @throws IOException
	 */
	@Test
	public void unzipMultipleFilesAsInputStreamWithExpectSingleResultTrue() throws IOException {

		final Resource resource = this.resourceLoader.getResource("classpath:testzipdata/countries.zip");
		final InputStream is = resource.getInputStream();

		final Message<InputStream> message = MessageBuilder.withPayload(is).build();

		final UnZipTransformer unZipTransformer = new UnZipTransformer();
		unZipTransformer.setZipResultType(ZipResultType.BYTE_ARRAY);
		unZipTransformer.setExpectSingleResult(true);
		unZipTransformer.afterPropertiesSet();

		try {
			unZipTransformer.transform(message);
		}
		catch (MessagingException e) {
			Assert.assertTrue(e.getMessage().contains("The UnZip operation extracted "
					+ "5 result objects but expectSingleResult was 'true'."));
			return;
		}

		Assert.fail("Expected a MessagingException to be thrown.");

	}

	@Test
	public void unzipInvalidZipFile() throws IOException, InterruptedException {

		File fileToUnzip = this.testFolder.newFile();
		FileUtils.writeStringToFile(fileToUnzip, "hello world");

		UnZipTransformer unZipTransformer = new UnZipTransformer();
		unZipTransformer.setZipResultType(ZipResultType.BYTE_ARRAY);
		unZipTransformer.setExpectSingleResult(true);
		unZipTransformer.afterPropertiesSet();

		Message<File> message = MessageBuilder.withPayload(fileToUnzip).build();

		try {
			unZipTransformer.transform(message);
			Assert.fail("Expected a MessagingException to be thrown.");
		}
		catch (MessagingException e) {
			Assert.assertTrue(e.getMessage().contains(String.format("Not a zip file: '%s'.",
					fileToUnzip.getAbsolutePath())));
		}
	}

}
