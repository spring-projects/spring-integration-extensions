/*
 * Copyright 2002-2019 the original author or authors.
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
package org.springframework.integration.xquery.config.xml;
import java.util.Map;

import javax.xml.xquery.XQDataSource;

import org.junit.Assert;
import org.junit.Test;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.integration.endpoint.EventDrivenConsumer;
import org.springframework.integration.test.util.TestUtils;
import org.springframework.integration.xml.XmlPayloadConverter;
import org.springframework.integration.xquery.DummyXmlPayloadConverter;
import org.springframework.integration.xquery.router.XQueryRouter;
import org.springframework.integration.xquery.support.XQueryParameter;

import net.sf.saxon.xqj.SaxonXQDataSource;


/**
 * The test case for the XQuery router parser
 *
 * @author Amol Nayak
 * @author Gary Russell
 *
 * @since 1.0
 *
 */
public class XQueryRouterParserTests {

	private ClassPathXmlApplicationContext ctx;
	private EventDrivenConsumer consumer;


	@Test
	public void routerOne() {
		setUp("xqueryRouterOne");
		XQueryRouter router = TestUtils.getPropertyValue(consumer, "handler", XQueryRouter.class);
		Assert.assertNotNull(TestUtils.getPropertyValue(router, "executor.xQuery", String.class));
		Assert.assertEquals(SaxonXQDataSource.class,
				TestUtils.getPropertyValue(router, "executor.xqDataSource", XQDataSource.class).getClass());
		@SuppressWarnings("unchecked")
		Map<String, XQueryParameter> params =
			TestUtils.getPropertyValue(router, "executor.xQueryParameterMap",Map.class);
		Assert.assertEquals(2, params.size());
		Assert.assertTrue(params.containsKey("name"));
		Assert.assertTrue(params.containsKey("class"));
		XQueryParameter param = params.get("name");
		Assert.assertEquals("name", param.getParameterName());
		Assert.assertEquals("headers['name']",
				TestUtils.getPropertyValue(param, "messageProcessor.expression.expression",String.class));

		destroy();
	}

	@Test
	public void routerTwo() {
		setUp("xqueryRouterTwo");
		XQueryRouter router = TestUtils.getPropertyValue(consumer, "handler", XQueryRouter.class);
		Assert.assertNotNull(TestUtils.getPropertyValue(router, "executor.xQuery", String.class));
		Assert.assertEquals(DummyXQDataSource.class,
				TestUtils.getPropertyValue(router, "executor.xqDataSource", XQDataSource.class).getClass());
		Assert.assertEquals(DummyXmlPayloadConverter.class,
				TestUtils.getPropertyValue(router,"executor.converter",XmlPayloadConverter.class).getClass());
		@SuppressWarnings("unchecked")
		Map<String, XQueryParameter> params =
			TestUtils.getPropertyValue(router, "executor.xQueryParameterMap",Map.class);
		Assert.assertEquals(1, params.size());
		Assert.assertTrue(params.containsKey("name"));
		XQueryParameter param = params.get("name");
		Assert.assertEquals("name", param.getParameterName());
		Assert.assertEquals("name",
				TestUtils.getPropertyValue(param, "parameterValue",String.class));
		destroy();
	}

	@Test
	public void routerThree() {
		setUp("xqueryRouterThree");
		XQueryRouter router = TestUtils.getPropertyValue(consumer, "handler", XQueryRouter.class);
		Assert.assertNotNull(TestUtils.getPropertyValue(router, "executor.xQuery", String.class));
		Assert.assertEquals(ClassPathResource.class, TestUtils.getPropertyValue(router, "executor.xQueryFileResource",Resource.class).getClass());
		destroy();
	}

	private void setUp(String beanName) {
		ctx = new ClassPathXmlApplicationContext("XQueryRouterParserTests-context.xml",XQueryRouterParserTests.class);
		consumer = ctx.getBean(beanName, EventDrivenConsumer.class);
	}


	public void destroy() {
		ctx.close();
	}

	public static class DummyXQDataSource extends SaxonXQDataSource {

	}
}
