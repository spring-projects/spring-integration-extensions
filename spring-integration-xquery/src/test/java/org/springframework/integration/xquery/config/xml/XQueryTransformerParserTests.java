/*
 * Copyright 2002-2012 the original author or authors.
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

import java.util.List;
import java.util.Map;

import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQResultSequence;

import junit.framework.Assert;
import net.sf.saxon.xqj.SaxonXQDataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.integration.endpoint.EventDrivenConsumer;
import org.springframework.integration.test.util.TestUtils;
import org.springframework.integration.transformer.Transformer;
import org.springframework.integration.xml.XmlPayloadConverter;
import org.springframework.integration.xquery.DummyXmlPayloadConverter;
import org.springframework.integration.xquery.support.XQueryParameter;
import org.springframework.integration.xquery.support.XQueryResultMapper;
import org.springframework.integration.xquery.transformer.XQueryTransformer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * The test class for XQuery Transformer parser
 *
 * @author Amol Nayak
 *
 * @since 1.0
 *
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class XQueryTransformerParserTests {

	private ClassPathXmlApplicationContext ctx;
	private EventDrivenConsumer consumer;

	@Test
	public void transformerOne() {
		setUp("xqueryTransformerOne");
		Transformer transformer = TestUtils.getPropertyValue(consumer, "handler.transformer", Transformer.class);
		Assert.assertEquals(XQueryTransformer.class, transformer.getClass());
		Assert.assertEquals(String.class, TestUtils.getPropertyValue(transformer, "resultType", Class.class));
		Assert.assertNotNull(TestUtils.getPropertyValue(transformer, "executor.xQuery", String.class));
		Assert.assertEquals(SaxonXQDataSource.class,
				TestUtils.getPropertyValue(transformer, "executor.xqDataSource", XQDataSource.class).getClass());
		@SuppressWarnings("unchecked")
		Map<String, XQueryParameter> params =
			TestUtils.getPropertyValue(transformer, "executor.xQueryParameterMap",Map.class);
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
	public void transformerTwo() {
		setUp("xqueryTransformerTwo");
		XQueryTransformer transformer = TestUtils.getPropertyValue(consumer, "handler.transformer", XQueryTransformer.class);
		Assert.assertNotNull(TestUtils.getPropertyValue(transformer, "executor.xQuery", String.class));
		Assert.assertEquals(DummyXQDataSource.class,
				TestUtils.getPropertyValue(transformer, "executor.xqDataSource", XQDataSource.class).getClass());
		Assert.assertEquals(DummyXmlPayloadConverter.class,
				TestUtils.getPropertyValue(transformer,"executor.converter",XmlPayloadConverter.class).getClass());
		Assert.assertEquals(true, TestUtils.getPropertyValue(transformer, "executor.formatOutput",Boolean.class).booleanValue());
		destroy();
	}

	@Test
	public void transformerThree() {
		setUp("xqueryTransformerThree");
		XQueryTransformer transformer = TestUtils.getPropertyValue(consumer, "handler.transformer", XQueryTransformer.class);
		Assert.assertNotNull(TestUtils.getPropertyValue(transformer, "executor.xQuery", String.class));
		Assert.assertEquals(ClassPathResource.class, TestUtils.getPropertyValue(transformer, "executor.xQueryFileResource",Resource.class).getClass());
		destroy();
	}

	@Test
	public void booleanTypeResult() {
		setUp("booleanResultTypeTransformer");
		XQueryTransformer transformer = TestUtils.getPropertyValue(consumer, "handler.transformer", XQueryTransformer.class);
		Assert.assertEquals(Boolean.class, TestUtils.getPropertyValue(transformer, "resultType", Class.class));
	}

	@Test
	public void stringTypeResult() {
		setUp("stringResultTypeTransformer");
		XQueryTransformer transformer = TestUtils.getPropertyValue(consumer, "handler.transformer", XQueryTransformer.class);
		Assert.assertEquals(String.class, TestUtils.getPropertyValue(transformer, "resultType", Class.class));
	}

	@Test
	public void resultMapperProvided() {
		setUp("customXQueryResultMapper");
		XQueryTransformer transformer = TestUtils.getPropertyValue(consumer, "handler.transformer", XQueryTransformer.class);
		Assert.assertEquals(DummyXQueryResultMapper.class, TestUtils.getPropertyValue(transformer, "resultMapper", XQueryResultMapper.class).getClass());
	}


	private void setUp(String beanName) {
		ctx = new ClassPathXmlApplicationContext("XQueryTransformerParserTests-context.xml",XQueryTransformerParserTests.class);
		consumer = ctx.getBean(beanName, EventDrivenConsumer.class);
	}

	public void destroy() {
		ctx.close();
	}

	public static class DummyXQDataSource extends SaxonXQDataSource {

	}

	public static class DummyXQueryResultMapper implements XQueryResultMapper<String> {

		public List<String> mapResults(XQResultSequence result) {
			return null;
		}
	}
}
