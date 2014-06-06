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
import org.springframework.integration.splunk.support.SplunkServer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Jarred Li
 * @since 1.0
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class SplunkServerParserTests {

	@Autowired
	private ApplicationContext appContext;

	/**
	 * Test method for {@link org.springframework.integration.splunk.config.xml.SplunkServerParser#doParse(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext, org.springframework.beans.factory.support.BeanDefinitionBuilder)}.
	 */
	@Test
	public void testDoParseElementParserContextBeanDefinitionBuilder() {
		SplunkServer server = appContext.getBean("splunkServer", SplunkServer.class);

		Assert.assertEquals("test.splunk.server", server.getHost());
		Assert.assertEquals(8089, server.getPort());
		Assert.assertEquals("admin", server.getUsername());
		Assert.assertEquals("password", server.getPassword());
		Assert.assertEquals("https", server.getScheme());
		Assert.assertEquals("admin", server.getOwner());
		Assert.assertEquals("search", server.getApp());
	}

}
