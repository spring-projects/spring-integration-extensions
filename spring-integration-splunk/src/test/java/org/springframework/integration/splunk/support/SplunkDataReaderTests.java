/*
 * Copyright 2011-2012 the original author or authors.
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
package org.springframework.integration.splunk.support;

import static org.mockito.Mockito.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import com.splunk.Job;
import com.splunk.JobCollection;
import com.splunk.Service;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.springframework.core.io.ClassPathResource;
import org.springframework.integration.splunk.core.ServiceFactory;
import org.springframework.integration.splunk.event.SplunkEvent;

/**
 * @author Jarred Li
 * @since 1.0
 *
 */
public class SplunkDataReaderTests {

	private SplunkDataReader reader;

	@Before
	public void before() {
		ServiceFactory serviceFactory =serviceFactory();
		reader = new SplunkDataReader(serviceFactory);
	}

	/**
	 * Test method for {@link org.springframework.integration.splunk.support.SplunkDataReader#search()}.
	 * @throws Exception
	 */
	@Test
	public void testBlockingSearch() throws Exception {
		reader.setMode(SearchMode.BLOCKING);
		reader.setSearch("search spring:example");
		List<SplunkEvent> data = reader.read();
		Assert.assertNotNull(data);
		Assert.assertEquals(5, data.size());
	}

	@Test
	public void testNonBlockingSearch() throws Exception {
		reader.setMode(SearchMode.NORMAL);
		reader.setSearch("search spring:example");
		List<SplunkEvent> data = reader.read();
		Assert.assertNotNull(data);
		Assert.assertEquals(5, data.size());
	}


	@Ignore
	@Test
	public void testRealtimeSearch() throws Exception {
		reader.setMode(SearchMode.REALTIME);
		reader.setSearch("search spring:example");
		List<SplunkEvent> data = reader.read();
		Assert.assertNotNull(data);
		Assert.assertEquals(5, data.size());
	}

	private ServiceFactory serviceFactory() {
		InputStream is = null;

		try {
			is = new ClassPathResource("splunk-data.xml").getInputStream();
		} catch (FileNotFoundException e) {
			Assert.fail("can not read splunk data file");
		} catch (IOException e) {
			Assert.fail("can not read splunk data file");
		}

		Service service = mock(Service.class);
		service.setToken("token");
		JobCollection jobCollection = mock(JobCollection.class);
		Job blockingJob = mock(Job.class);
		when(blockingJob.isDone()).thenReturn(true);
		when(blockingJob.getResultCount()).thenReturn(5);
		when(blockingJob.getResults(any(Map.class))).thenReturn(is);
		when(jobCollection.create(any(String.class), any(Map.class))).thenReturn(blockingJob);
		when(service.getJobs()).thenReturn(jobCollection);

		ServiceFactory serviceFactory = mock(ServiceFactory.class);
		when(serviceFactory.getService()).thenReturn(service);
		return serviceFactory;
	}
}
