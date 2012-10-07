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

import junit.framework.Assert;

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
public class SplunkInboundChannelAdapterParserSavedSearchTests {

	@Autowired
	private ApplicationContext appContext;

	/**
	 * Test method for {@link org.springframework.integration.splunk.config.xml.SplunkInboundChannelAdapterParser#parseSource(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)}.
	 */
	@Test
	public void testParseSourceElementParserContext() {
		SourcePollingChannelAdapter adapter = appContext.getBean("splunkInboundChannelAdapter",
				SourcePollingChannelAdapter.class);
		Assert.assertNotNull(adapter);

		SplunkDataReader reader = appContext.getBean("splunkInboundChannelAdapter.splunkExecutor.reader",
				SplunkDataReader.class);
		Assert.assertNotNull(reader);

		SearchMode mode = SearchMode.SAVEDSEARCH;
		Assert.assertEquals(mode, reader.getMode());

		String savedSearch = "savedSearch";
		Assert.assertEquals(savedSearch, reader.getSavedSearch());

		String owner = "admin";
		Assert.assertEquals(owner, reader.getOwner());

		String app = "search";
		Assert.assertEquals(app, reader.getApp());
	}

}
