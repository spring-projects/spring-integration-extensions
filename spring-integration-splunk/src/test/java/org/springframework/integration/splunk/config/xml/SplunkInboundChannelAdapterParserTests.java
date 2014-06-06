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
package org.springframework.integration.splunk.config.xml;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.endpoint.SourcePollingChannelAdapter;
import org.springframework.integration.splunk.support.SearchMode;
import org.springframework.integration.splunk.support.SplunkDataReader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Jarred Li
 * @since 1.0
 *
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class SplunkInboundChannelAdapterParserTests {

	@Autowired
	private ApplicationContext appContext;

	/**
	 * Test method for {@link org.springframework.integration.splunk.config.xml.SplunkInboundChannelAdapterParser#parseSource(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)}.
	 */
	@Test
	//@Ignore
	public void testParseSourceElementParserContext() {
		SourcePollingChannelAdapter adapter = appContext.getBean("splunkInboundChannelAdapter",
				SourcePollingChannelAdapter.class);
		Assert.assertNotNull(adapter);
		Assert.assertFalse(adapter.isAutoStartup());

		SplunkDataReader reader = appContext.getBean("splunkInboundChannelAdapter.splunkExecutor.reader",
				SplunkDataReader.class);
		Assert.assertNotNull(reader);

		String searchString = "search spring:example";
		Assert.assertEquals(searchString, reader.getSearch());

		SearchMode mode = SearchMode.BLOCKING;
		Assert.assertEquals(mode, reader.getMode());

		String earliestTime = "-1d";
		Assert.assertEquals(earliestTime, reader.getEarliestTime());

		String latestTime = "now";
		Assert.assertEquals(latestTime, reader.getLatestTime());

		String initEarliestTime = "-1d";
		Assert.assertEquals(initEarliestTime, reader.getInitEarliestTime());

		String fieldList = "field1, field2";
		Assert.assertEquals(fieldList, reader.getFieldList());

	}

}
