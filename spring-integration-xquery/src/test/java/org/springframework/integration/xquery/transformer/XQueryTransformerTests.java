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
package org.springframework.integration.xquery.transformer;

import java.math.BigInteger;
import java.util.List;

import javax.xml.xquery.XQResultSequence;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Node;

import org.springframework.core.io.ClassPathResource;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.test.util.TestUtils;
import org.springframework.integration.xquery.core.XQueryExecutor;
import org.springframework.integration.xquery.support.XQueryResultMapper;

/**
 * The Test class for the {@link XQueryTransformer} class
 *
 * @author Amol Nayak
 * @author Gary Russell
 *
 * @since 1.0
 *
 */
public class XQueryTransformerTests {

	@Test
	public void withNoExecutor() {
		try {
			XQueryTransformer transformer = new XQueryTransformer();
			transformer.afterPropertiesSet();
		} catch (IllegalArgumentException e) {
			Assert.assertEquals("No XQueryExecutor instance provided", e.getMessage());
		}
	}


	@Test
	public void withNullExecutor() {
		try {
			XQueryTransformer transformer = new XQueryTransformer();
			transformer.setExecutor(null);
		} catch (IllegalArgumentException e) {
			Assert.assertEquals("Provide a non null XQueryExecutor instance", e.getMessage());
		}
	}

	@Test
	public void withNoMapperAndResultType() {
		XQueryTransformer transformer = new XQueryTransformer();
		transformer.setExecutor(getExecutor("//hello"));
		transformer.afterPropertiesSet();
		Assert.assertEquals(String.class, TestUtils.getPropertyValue(transformer, "resultType",Class.class));
	}

	@Test
	public void withStringResultType() {
		XQueryTransformer transformer = new XQueryTransformer();
		transformer.setExecutor(getExecutor("//hello"));
		transformer.setResultType(String.class);
		transformer.afterPropertiesSet();
		Assert.assertEquals(String.class, TestUtils.getPropertyValue(transformer, "resultType",Class.class));
	}


	@Test
	public void withNumberResultType() {
		XQueryTransformer transformer = new XQueryTransformer();
		transformer.setExecutor(getExecutor("//hello"));
		transformer.setResultType(Number.class);
		transformer.afterPropertiesSet();
		Assert.assertEquals(Number.class, TestUtils.getPropertyValue(transformer, "resultType",Class.class));
	}



	@Test
	public void withBooleanResultType() {
		XQueryTransformer transformer = new XQueryTransformer();
		transformer.setExecutor(getExecutor("//hello"));
		transformer.setResultType(Boolean.class);
		transformer.afterPropertiesSet();
		Assert.assertEquals(Boolean.class, TestUtils.getPropertyValue(transformer, "resultType",Class.class));
	}


	@Test
	public void withNodeResultType() {
		XQueryTransformer transformer = new XQueryTransformer();
		transformer.setExecutor(getExecutor("//hello"));
		transformer.setResultType(Node.class);
		transformer.afterPropertiesSet();
		Assert.assertEquals(Node.class, TestUtils.getPropertyValue(transformer, "resultType",Class.class));
	}

	@Test
	public void withUnknownResultType() {
		try {
			XQueryTransformer transformer = new XQueryTransformer();
			transformer.setExecutor(getExecutor("//hello"));
			transformer.setResultType(getClass());
		} catch (IllegalArgumentException e) {
			Assert.assertEquals("Valid values for the result type class is String, Boolean, Number or Node, " +
					"for any other type, provide a custom implementation of XQueryResultMapper", e.getMessage());
		}
	}

	@Test
	public void setFirstTypeThenMapper() {
		try {
			XQueryTransformer transformer = new XQueryTransformer();
			transformer.setResultType(String.class);
			transformer.setResultMapper(new DummyXQueryResultMapper());
		} catch (IllegalArgumentException e) {
			Assert.assertEquals("Only one of the result mapper of the resultType can be set", e.getMessage());
		}
	}

	@Test
	public void setFirstMapperThenType() {
		try {
			XQueryTransformer transformer = new XQueryTransformer();
			transformer.setResultMapper(new DummyXQueryResultMapper());
			transformer.setResultType(String.class);
		} catch (IllegalArgumentException e) {
			Assert.assertEquals("Only one of the result mapper of the resultType can be set", e.getMessage());
		}
	}

	@Test
	public void setNullMapper() {
		try {
			XQueryTransformer transformer = new XQueryTransformer();
			transformer.setResultMapper(null);
		} catch (IllegalArgumentException e) {
			Assert.assertEquals("Provide a non null value for the result mapper", e.getMessage());
		}
	}

	@Test
	public void setNullType() {
		try {
			XQueryTransformer transformer = new XQueryTransformer();
			transformer.setResultType(null);
		} catch (IllegalArgumentException e) {
			Assert.assertEquals("Provide a non null value for the result type", e.getMessage());
		}
	}

	@Test
	public void executeForString() {
		XQueryTransformer transformer = new XQueryTransformer();
		transformer.setExecutor(getExecutor("'Hello World!'"));
		transformer.afterPropertiesSet();
		try {
			Object result = transformer.doTransform(MessageBuilder.withPayload("<test/>").build());
			Assert.assertNotNull(result);
			Assert.assertEquals("Hello World!", result);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}

	@Test
	public void executeForInteger() {
		XQueryTransformer transformer = new XQueryTransformer();
		transformer.setExecutor(getExecutor("10 + 12"));
		transformer.setResultType(Number.class);
		transformer.afterPropertiesSet();
		try {
			Object result = transformer.doTransform(MessageBuilder.withPayload("<test/>").build());
			Assert.assertNotNull(result);
			Assert.assertEquals(BigInteger.valueOf(22), result);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}

	@Test
	public void personTransformer() {
		String inputXml = 	"<employees>" +
								"<employee id=\"1\" name=\"Emp1\" dept=\"Dep1\"/>" +
								"<employee id=\"2\" name=\"Emp2\" dept=\"Dep2\"/>" +
							"</employees>";

		XQueryTransformer transformer = new XQueryTransformer();
		XQueryExecutor executor = new XQueryExecutor();
		executor.setXQueryFileResource(
				new ClassPathResource("org/springframework/integration/xquery/XQueryTransform.xq"));
		executor.setFormatOutput(true);
		executor.afterPropertiesSet();
		transformer.setExecutor(executor);
		transformer.afterPropertiesSet();
		try {
			Object transformed = transformer.doTransform(MessageBuilder.withPayload(inputXml).build());
			Assert.assertNotNull(transformed);
			System.out.println(transformed);
		} catch (Exception e) {
			Assert.assertTrue("Caught Exception while transformation, " + e.getMessage(),false);
		}
	}

	private class DummyXQueryResultMapper implements XQueryResultMapper<Integer> {

		@Override
		public List<Integer> mapResults(XQResultSequence result) {
			// TODO Auto-generated method stub
			return null;
		}
	}


	//TODO: Refactor, Can have a common superclass for all XQuery components
	/**
	 * @return
	 */
	private XQueryExecutor getExecutor(String xQuery) {
		XQueryExecutor executor = new XQueryExecutor();
		executor.setXQuery(xQuery);
		executor.afterPropertiesSet();
		return executor;
	}
}
