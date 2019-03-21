/*
 * Copyright 2015-2016 the original author or authors.
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

package org.springframework.integration.zip.transformer;

import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.zeroturnaround.zip.ZipUtil;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.zip.ZipHeaders;
import org.springframework.messaging.Message;

/**
 *
 * @author Gunnar Hillert
 * @author Artem Bilan
 * @since 1.0
 *
 */
public class ZipTransformerTests {

	@Rule
	public TemporaryFolder testFolder = new TemporaryFolder();

	@Test
	public void zipString() throws IOException {
		final ZipTransformer zipTransformer = new ZipTransformer();
		zipTransformer.setBeanFactory(mock(BeanFactory.class));
		zipTransformer.setZipResultType(ZipResultType.BYTE_ARRAY);
		zipTransformer.afterPropertiesSet();

		final String stringToCompress = "Hello World";

		final Date fileDate = new Date();

		final Message<String> message = MessageBuilder.withPayload(stringToCompress)
			.setHeader(ZipHeaders.ZIP_ENTRY_FILE_NAME, "test.txt")
			.setHeader(ZipHeaders.ZIP_ENTRY_LAST_MODIFIED_DATE, fileDate)
			.build();

		final Message<?> result = zipTransformer.transform(message);

		Object resultPayload = result.getPayload();

		Assert.assertTrue("Expected payload to be an instance of byte but was "
				+ resultPayload.getClass().getName(), resultPayload instanceof byte[]);

		final File temporaryTestDirectory = testFolder.newFolder();

		ZipUtil.unpack(new ByteArrayInputStream((byte[]) resultPayload), temporaryTestDirectory);

		final File unzippedEntry = new File(temporaryTestDirectory, "test.txt");
		Assert.assertTrue(unzippedEntry.exists());
		Assert.assertTrue(unzippedEntry.isFile());

		//See http://stackoverflow.com/questions/3725662/what-is-the-earliest-timestamp-value-that-is-supported-in-zip-file-format
		Assert.assertTrue((fileDate.getTime() - 3000) < unzippedEntry.lastModified());
		Assert.assertTrue((fileDate.getTime() + 3000) > unzippedEntry.lastModified());
	}

	@Test
	public void zipStringCollection() throws IOException {
		final ZipTransformer zipTransformer = new ZipTransformer();
		zipTransformer.setBeanFactory(mock(BeanFactory.class));
		zipTransformer.setZipResultType(ZipResultType.BYTE_ARRAY);
		zipTransformer.afterPropertiesSet();

		final String string1ToCompress = "Cartman";
		final String string2ToCompress = "Kenny";
		final String string3ToCompress = "Butters";

		final List<String> strings = new ArrayList<String>(3);

		strings.add(string1ToCompress);
		strings.add(string2ToCompress);
		strings.add(string3ToCompress);

		final Date fileDate = new Date();

		final Message<List<String>> message = MessageBuilder.withPayload(strings)
			.setHeader(ZipHeaders.ZIP_ENTRY_FILE_NAME, "test.txt")
			.setHeader(ZipHeaders.ZIP_ENTRY_LAST_MODIFIED_DATE, fileDate)
			.build();

		final Message<?> result = zipTransformer.transform(message);

		Object resultPayload = result.getPayload();

		Assert.assertTrue("Expected payload to be an instance of byte but was "
				+ resultPayload.getClass().getName(), resultPayload instanceof byte[]);

		final File temporaryTestDirectory = testFolder.newFolder();

		ZipUtil.unpack(new ByteArrayInputStream((byte[]) resultPayload), temporaryTestDirectory);

		File[] files = temporaryTestDirectory.listFiles();

		Assert.assertTrue(files.length >= 3);

		final Set<String> expectedFileNames = new HashSet<String>();

		expectedFileNames.add("test_1.txt");
		expectedFileNames.add("test_2.txt");
		expectedFileNames.add("test_3.txt");

		for (File file : files) {

			if (file.getName().startsWith("test")) {
				Assert.assertTrue(file.exists());
				Assert.assertTrue(file.isFile());

				//See http://stackoverflow.com/questions/3725662/what-is-the-earliest-timestamp-value-that-is-supported-in-zip-file-format
				Assert.assertTrue(String.format("%s : %s", fileDate.getTime() - 4000, file.lastModified()),
						(fileDate.getTime() - 4000) < file.lastModified());
				Assert.assertTrue((fileDate.getTime() + 4000) > file.lastModified());

				Assert.assertTrue(
					String.format("File '%s' did not end with '.txt'.", file.getName()),
						file.getName().endsWith(".txt"));

				Assert.assertTrue(expectedFileNames.contains(file.getName()));
			}

		}

	}

	@Test
	public void zipStringToFile() throws IOException {
		final ZipTransformer zipTransformer = new ZipTransformer();
		zipTransformer.setBeanFactory(mock(BeanFactory.class));
		zipTransformer.afterPropertiesSet();

		final String stringToCompress = "Hello World";

		final String zipEntryFileName = "test.txt";
		final Message<String> message = MessageBuilder.withPayload(stringToCompress)
			.setHeader(ZipHeaders.ZIP_ENTRY_FILE_NAME, zipEntryFileName)
			.build();

		final Message<?> result = zipTransformer.transform(message);

		Assert.assertTrue("Expected payload to be an instance of file but was "
				+ result.getPayload().getClass().getName(), result.getPayload() instanceof File);

		final File payload = (File) result.getPayload();

		System.out.println(payload.getAbsolutePath());

		Assert.assertEquals(message.getHeaders().getId().toString() + ".msg.zip", payload.getName());
		Assert.assertTrue(SpringZipUtils.isValid(payload));

		final byte[] zipEntryData = ZipUtil.unpackEntry(payload, "test.txt");

		Assert.assertNotNull("Entry '" + zipEntryFileName + "' was not found.", zipEntryData);
		Assert.assertTrue("Hello World".equals(new String(zipEntryData)));

	}

	@Test
	public void zipFile() throws IOException {

		ZipTransformer zipTransformer = new ZipTransformer();
		zipTransformer.setBeanFactory(mock(BeanFactory.class));
		zipTransformer.setDeleteFiles(true);
		zipTransformer.afterPropertiesSet();

		final File testFile = createTestFile(10);

		Assert.assertTrue(testFile.exists());

		final Message<File> message = MessageBuilder.withPayload(testFile).build();

		final Message<?> result = zipTransformer.transform(message);

		Assert.assertTrue(result.getPayload() instanceof File);

		final File payload = (File) result.getPayload();

		Assert.assertEquals(testFile.getName() + ".zip", payload.getName());
		Assert.assertTrue(SpringZipUtils.isValid(payload));
	}

	@Test
	public void zipCollection() throws IOException {

		final File testFile1 = createTestFile(1);
		final File testFile2 = createTestFile(2);
		final File testFile3 = createTestFile(3);
		final File testFile4 = createTestFile(4);

		Assert.assertTrue(testFile1.exists());
		Assert.assertTrue(testFile2.exists());
		Assert.assertTrue(testFile3.exists());
		Assert.assertTrue(testFile4.exists());

		final Collection<File> files = new ArrayList<File>();

		files.add(testFile1);
		files.add(testFile2);
		files.add(testFile3);
		files.add(testFile4);

		final ZipTransformer zipTransformer = new ZipTransformer();
		zipTransformer.setBeanFactory(mock(BeanFactory.class));
		zipTransformer.afterPropertiesSet();

		final Message<Collection<File>> message = MessageBuilder.withPayload(files).build();

		final Message<?> result = zipTransformer.transform(message);

		Assert.assertTrue(result.getPayload() instanceof File);

		final File outputZipFile = (File) result.getPayload();

		Assert.assertTrue(outputZipFile.exists());
		Assert.assertTrue(outputZipFile.isFile());
		Assert.assertTrue(outputZipFile.getName().endsWith("zip"));
		Assert.assertTrue(SpringZipUtils.isValid(outputZipFile));

	}

	private File createTestFile(int size) throws IOException {

		final File temporaryTestDirectory = this.testFolder.newFolder();

		final File testFile = new File(temporaryTestDirectory, "testdata" + UUID.randomUUID().toString() + ".data");

		RandomAccessFile f = null;
		try {
			f = new RandomAccessFile(testFile, "rw");
			f.setLength(size * 1024 * 1024);
		}
		catch (Exception e) {
			System.err.println(e);
		}
		finally {
			try {
				if (f != null) {
					f.close();
				}
			} catch (IOException e) {}
		}
		return testFile;

	}

}
