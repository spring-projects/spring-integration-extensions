/*
 * Copyright 2002-2019 the original author or authors.
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
package org.springframework.integration.xquery.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQResultSequence;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Node;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.test.util.TestUtils;
import org.springframework.integration.xquery.support.AbstractXQueryResultMapper;
import org.springframework.integration.xquery.support.BooleanResultMapper;
import org.springframework.integration.xquery.support.NodeResultMapper;
import org.springframework.integration.xquery.support.NumberResultMapper;
import org.springframework.integration.xquery.support.StringResultMapper;
import org.springframework.integration.xquery.support.XQueryParameter;
import org.springframework.integration.xquery.support.XQueryResultMapper;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;

/**
 * The base test class for the XQueryExecutor tests
 *
 * @author Amol Nayak
 * @author Gary Russell
 *
 * @since 1.0
 *
 */
public abstract class AbstractXQueryExecutorTests {


	private final static String EXT_VARIABLE_XQUERY =
		"declare variable $name as xs:string external;" +
		"declare variable $class as xs:int external;" +
		" for 	$student in /mappings/students/student," +
		"		$subject in /mappings/subjects/subject " +
		"where	$student/@id = $subject/students/studentId " +
		"and	$student/name = $name " +
		"and	$student/class = $class " +
		"return $subject/name/text()";

	private final String xmlString = "<person active=\"true\">" +
											"<name>Mike</name>" +
											"<age>29</age>" +
										"</person>";


	/**
	 * Test if default created result mappers are appropriately instantiated
	 */
	@Test
	public void initializeDefaultMappers() {
		XQueryExecutor executor = getExecutor();
		executor.setXQuery("'Hello World'");
		executor.afterPropertiesSet();
		@SuppressWarnings("rawtypes")
		Map executorMap = TestUtils.getPropertyValue(executor, "resultMappers", Map.class);
		Assert.assertNotNull(executorMap);
		Assert.assertEquals(4, executorMap.size());
		Assert.assertEquals(StringResultMapper.class, executorMap.get(String.class).getClass());
		Assert.assertEquals(BooleanResultMapper.class, executorMap.get(Boolean.class).getClass());
		Assert.assertEquals(NumberResultMapper.class, executorMap.get(Number.class).getClass());
		Assert.assertEquals(NodeResultMapper.class, executorMap.get(Node.class).getClass());
	}

	/**
	 * Tests of the registered mappers take priority over the defaults
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void registerCustomMappers() {
		XQueryExecutor executor = getExecutor();
		executor.setXQuery("'Hello'");
		Map map = new HashMap();
		map.put(MyCustomClass.class, new XQueryResultMapper<MyCustomClass>() {
			@Override
			public List<MyCustomClass> mapResults(XQResultSequence result) {
				// TODO Auto-generated method stub
				return null;
			}
		});
		map.put(Node.class, new CustomNodeMapper());
		executor.setResultMappers(map);
		executor.afterPropertiesSet();
		Map executorMap = TestUtils.getPropertyValue(executor, "resultMappers", Map.class);
		Assert.assertNotNull(executorMap);
		Assert.assertEquals(5, executorMap.size());
		Assert.assertEquals(StringResultMapper.class, executorMap.get(String.class).getClass());
		Assert.assertEquals(BooleanResultMapper.class, executorMap.get(Boolean.class).getClass());
		Assert.assertEquals(NumberResultMapper.class, executorMap.get(Number.class).getClass());
		Assert.assertEquals(CustomNodeMapper.class, executorMap.get(Node.class).getClass());
	}


	/**
	 * Tries to execute the result passing an unmapped class
	 */
	@Test
	public void executeForUnknownClassType() {
		XQueryExecutor executor = getExecutor();
		executor.setXQuery("'Hello World'");
		executor.afterPropertiesSet();
		try {
			executor.execute(MessageBuilder.withPayload("").build(), MyCustomClass.class);
		} catch (IllegalArgumentException e) {
			Assert.assertEquals("No Result mapper found for the type org.springframework.integration.xquery.core.AbstractXQueryExecutorTests$MyCustomClass",
					e.getMessage());
		}
	}

	/**
	 * @return
	 */
	protected abstract XQueryExecutor getExecutor();

	/**
	 * Test with null message
	 */
	@Test
	public void withNullMessage() {
		XQueryExecutor executor = getExecutor();
		executor.setXQuery("'Hello World'");
		executor.afterPropertiesSet();
		try {
			executor.execute(null, String.class);
		} catch (IllegalArgumentException e) {
			Assert.assertEquals("Non null message expected", e.getMessage());
		}

	}

	@Test
	public void executeWithStringForNonLeaf() {
		XQueryExecutor executor = getExecutor();
		executor.setXQuery(".//person");
		executor.afterPropertiesSet();
		List<String> names = executor.executeForString(MessageBuilder.withPayload(xmlString).build());
		Assert.assertNotNull(names);
		Assert.assertEquals(1, names.size());
	}


	@Test
	public void executeWithBooleanForNonLeaf() {
		XQueryExecutor executor = getExecutor();
		executor.setXQuery("//person");
		executor.afterPropertiesSet();
		List<Boolean> names = executor.executeForBoolean(MessageBuilder.withPayload(xmlString).build());
		Assert.assertNotNull(names);
		Assert.assertEquals(1, names.size());
		Assert.assertTrue(names.contains(Boolean.FALSE));
	}


	@Test
	public void executeWithNumberForNonLeaf() {
		XQueryExecutor executor = getExecutor();
		executor.setXQuery("//person");
		executor.afterPropertiesSet();
		@SuppressWarnings("unused")
		List<Number> names;
		try {
			names = executor.executeForNumber(MessageBuilder.withPayload(xmlString).build());
		} catch (MessagingException e) {
			Assert.assertEquals(NumberFormatException.class,e.getCause().getClass());
		}
	}


	/**
	 * Test case to execute the XQuery and return a {@link List} of {@link String}
	 */
	@Test
	public void executeWithString() {
		XQueryExecutor executor = getExecutor();
		executor.setXQuery(".//person/name/text()");
		executor.afterPropertiesSet();
		List<String> names = executor.executeForString(MessageBuilder.withPayload(xmlString).build());
		Assert.assertNotNull(names);
		Assert.assertEquals(1, names.size());
		Assert.assertTrue(names.contains("Mike"));
	}


	/**
	 * Test case to execute the XQuery and return a {@link List} of {@link Boolean}
	 */
	@Test
	public void executeWithBoolean() {
		XQueryExecutor executor = getExecutor();
		executor.setXQuery("//person/@active");
		executor.afterPropertiesSet();
		List<Boolean> names = executor.executeForBoolean(MessageBuilder.withPayload(xmlString).build());
		Assert.assertNotNull(names);
		Assert.assertEquals(1, names.size());
		Assert.assertTrue(names.contains(Boolean.TRUE));
	}


	/**
	 * Test case to execute the XQuery and return a {@link List} of {@link Number}
	 */
	@Test
	public void executeWithNumber() {
		XQueryExecutor executor = getExecutor();
		executor.setXQuery("//person/age/text()");
		executor.afterPropertiesSet();
		List<Number> names = executor.executeForNumber(MessageBuilder.withPayload(xmlString).build());
		Assert.assertNotNull(names);
		Assert.assertEquals(1, names.size());
		Assert.assertTrue(names.contains(Long.valueOf(29)));
	}


	/**
	 * Test case to execute the XQuery and return a {@link List} of {@link Node}
	 */
	@Test
	public void executeWithNode() {
		XQueryExecutor executor = getExecutor();
		executor.setXQuery("//person");
		executor.afterPropertiesSet();
		List<Node> names = executor.executeForNode(MessageBuilder.withPayload(xmlString).build());
		Assert.assertNotNull(names);
		Assert.assertEquals(1, names.size());
		Assert.assertEquals("person",names.get(0).getLocalName());
	}


	/**
	 * Test with null message
	 */
	@Test
	public void withNullType() {
		XQueryExecutor executor = getExecutor();
		executor.setXQuery("'Hello World'");
		executor.afterPropertiesSet();
		try {
			executor.execute(MessageBuilder.withPayload("").build(),(Class<?>) null);
		} catch (IllegalArgumentException e) {
			Assert.assertEquals("Non null type expected", e.getMessage());
		}

	}


	/**
	 * Executes the case where the router has no parameters but the query expects parameters
	 */
	@Test
	public void noParametersProvided() {
		XQueryExecutor executor = getExecutor();
		executor.setXQuery(EXT_VARIABLE_XQUERY);
		try {
			executor.afterPropertiesSet();
		} catch (MessagingException e) {
			Assert.assertEquals("Expecting 2 parameters in the xquery, but none provided to the router", e.getMessage());
		}
	}

	/**
	 * Execute the case where parameters are provided, but some or all of the parameters are missing
	 */
	@Test
	public void fewParametersProvided() {
		XQueryExecutor executor = getExecutor();
		executor.setXQuery(EXT_VARIABLE_XQUERY);
		executor.addXQueryParameter(new XQueryParameter("class",Integer.valueOf(3)));
		try {
			executor.afterPropertiesSet();
		} catch (MessagingException e) {
			Assert.assertEquals("Missing parameter(s) [$name]", e.getMessage());
		}
	}

	/**
	 * Sets null XQuery, should get an exception
	 */
	@Test
	public void setNullXQuery() {
		XQueryExecutor executor = getExecutor();
		try {
			executor.setXQuery(null);
		} catch (IllegalArgumentException e) {
			Assert.assertEquals("Provide a non null XQuery", e.getMessage());
		}
	}


	/**
	 * Sets the XQuery file resource and then the xquery
	 */
	@Test
	public void setResourceAndXQuery() {
		XQueryExecutor executor = getExecutor();
		Resource res = new ClassPathResource("/org/springframework/integration/xquery/XQuery.xq");
		Assert.assertTrue(res.exists());
		executor.setXQueryFileResource(res);
		try {
			executor.setXQuery("'Hello World'");
		} catch (IllegalArgumentException e) {
			Assert.assertEquals("Only one of XQuery resource file or XQuery may be specified", e.getMessage());
		}
		Assert.assertNull(TestUtils.getPropertyValue(executor, "xQuery"));
		executor.addXQueryParameter(new XQueryParameter("name", "headers['studentName']"));
		executor.addXQueryParameter(new XQueryParameter("class", Integer.valueOf(1)));
		executor.afterPropertiesSet();
		Assert.assertNotNull(TestUtils.getPropertyValue(executor, "xQuery",String.class));
	}

	/**
	 * Initialize the router without setting the xquery
	 */
	@Test
	public void withoutXQuery() {
		XQueryExecutor executor = getExecutor();
		try {
			executor.afterPropertiesSet();
		} catch (IllegalArgumentException e) {
			Assert.assertEquals("One of XQuery or the XQuery resource is mandatory", e.getMessage());
		}
	}

	/**
	 *Sets the XQuery and then the resource
	 */
	@Test
	public void setXQueryAndResource() {
		XQueryExecutor executor = getExecutor();
		Resource res = new ClassPathResource("org/springframework/integration/xquery/XQuery.xq");
		Assert.assertTrue(res.exists());
		executor.setXQuery("'Hello World'");
		try {
			executor.setXQueryFileResource(res);
		} catch (IllegalArgumentException e) {
			Assert.assertEquals("Only one of XQuery resource file or XQuery may be specified", e.getMessage());
		}
	}

	/**
	 *Sets a null {@link XQDataSource} instance
	 */
	@Test
	public void setNullXQataSource() {
		XQueryExecutor executor = getExecutor();
		try {
			executor.setXQDataSource(null);
		} catch (IllegalArgumentException e) {
			Assert.assertEquals("Provide a non null instance of the XQDatasource", e.getMessage());
		}
	}


	/**
	 * Tests the router with valid xml payload. The values returned will be the
	 * text content of the node, if the node is a text or attribute type
	 */
	@Test
	public void withValidPayload() {
		withValidPayloadInternal(true);
	}

	/**
	 * Tests the router with valid xml payload. The values returned will be the
	 * {@link Node}, irrespective of the type of the node
	 */
	@Test
	public void withValidPayload2() {
		withValidPayloadInternal(false);
	}

	/**
	 *
	 */
	private void withValidPayloadInternal(boolean extractNodeText) {
		File file = new File("./src/test/resources/org/springframework/integration/xquery/SubjectMapping.xml");
		Message<File> message = MessageBuilder.withPayload(file)
									.setHeader("studentName", "Jughead")
									.build();
		XQueryExecutor executor = getExecutor();
		executor.setXQuery(EXT_VARIABLE_XQUERY);
		executor.addXQueryParameter(new XQueryParameter("name", "headers['studentName']"));
		executor.addXQueryParameter(new XQueryParameter("class", Integer.valueOf(1)));
		executor.afterPropertiesSet();
		List<Object> subjects;
		if(extractNodeText) {
			subjects = new ArrayList<Object>(executor.executeForString(message));
		}
		else {
			subjects = new ArrayList<Object>(executor.executeForNode(message));
		}
		Assert.assertNotNull(subjects);
		Assert.assertEquals(2, subjects.size());
		if(extractNodeText) {
			Assert.assertEquals(String.class, subjects.get(0).getClass());
			Assert.assertTrue(subjects.contains("Cooking"));
			Assert.assertTrue(subjects.contains("Math"));
		}
		else {
			Assert.assertTrue(Node.class.isAssignableFrom(subjects.get(0).getClass()));
		}
	}

	/**
	 * Tests with a query that will generate a numeric value and we get the result as {@link Number}
	 */
	@Test
	public void withNumericExpression() {
		XQueryExecutor executor = getExecutor();
		executor.setXQuery("2 + 2");
		executor.afterPropertiesSet();
		List<Number> result = executor.executeForNumber(MessageBuilder.withPayload("<test/>").build());
		Assert.assertEquals(1, result.size());
		Assert.assertTrue(result.get(0).intValue() == 4);

	}

	/**
	 *Tests a query that will generate a decimal value and we get the result as a {@link Number}
	 */
	@Test
	public void withDecimalExpression() {
		XQueryExecutor executor = getExecutor();
		executor.setXQuery("2.0 + 2.1");
		executor.afterPropertiesSet();
		List<Number> result = executor.executeForNumber(MessageBuilder.withPayload("<test/>").build());
		Assert.assertEquals(1, result.size());
		Assert.assertTrue(result.contains(Double.valueOf(4.1)));

	}

	/**
	 * Execute the XQuery that gives a boolean value and get the value as a Boolean
	 */
	@Test
	public void withBooleanResult() {
		XQueryExecutor executor = getExecutor();
		executor.setXQuery("1 = 1");
		executor.afterPropertiesSet();
		List<Boolean> result = executor.executeForBoolean(MessageBuilder.withPayload("<test/>").build());
		Assert.assertEquals(1, result.size());
		Assert.assertTrue(result.contains(Boolean.TRUE));
	}

	/**
	 *The XQuery gives a string value and we want the result to be a string value
	 */
	@Test
	public void getTextString() {
		XQueryExecutor executor = getExecutor();
		executor.setXQuery("'Hello World'");
		executor.afterPropertiesSet();
		List<String> result = executor.executeForString(MessageBuilder.withPayload("<test/>").build());
		Assert.assertEquals(1, result.size());
		Assert.assertTrue(result.contains("Hello World"));
	}

	/**
	 *The XQuery gives a boolean value and we want the result to be a string value
	 */
	@Test
	public void getBooleanAsTextString() {
		XQueryExecutor executor = getExecutor();
		executor.setXQuery("1 = 2");
		executor.afterPropertiesSet();
		List<String> result = executor.executeForString(MessageBuilder.withPayload("<test/>").build());
		Assert.assertEquals(1, result.size());
		Assert.assertTrue(result.contains("false"));
	}

	/**
	 *The XQuery gives a text value in the node and we want the result to be a string value
	 */
	@Test
	public void getTextNodeAsString() {
		XQueryExecutor executor = getExecutor();
		executor.setXQuery("//test/text()");
		executor.afterPropertiesSet();
		List<String> result = executor.executeForString(MessageBuilder.withPayload("<test>Some Value</test>").build());
		Assert.assertEquals(1, result.size());
		Assert.assertTrue(result.contains("Some Value"));
	}

	/**
	 *The XQuery gives a text value in the node and we want the result to be a Boolean
	 */
	@Test
	public void getTextNodeAsBoolean() {
		XQueryExecutor executor = getExecutor();
		executor.setXQuery("//test/text()");
		executor.afterPropertiesSet();
		List<Boolean> result = executor.executeForBoolean(MessageBuilder.withPayload("<test>Some Value</test>").build());
		Assert.assertEquals(1, result.size());
		Assert.assertTrue(result.contains(Boolean.FALSE));
	}

	/**
	 *The XQuery gives a text value in the node and we want the result to be a Long
	 */
	@Test
	public void getTextNodeAsLong() {
		XQueryExecutor executor = getExecutor();
		executor.setXQuery("//number/text()");
		executor.afterPropertiesSet();
		List<Number> result = executor.executeForNumber(MessageBuilder.withPayload("<number>1</number>").build());
		Assert.assertEquals(1, result.size());
		Assert.assertTrue(result.contains(Long.valueOf(1)));
	}

	/**
	 *The XQuery gives a text value in the node and we want the result to be a Long
	 */
	@Test
	public void getTextNodeAsDouble() {
		XQueryExecutor executor = getExecutor();
		executor.setXQuery("//number/text()");
		executor.afterPropertiesSet();
		List<Number> result = executor.executeForNumber(MessageBuilder.withPayload("<number>1.1</number>").build());
		Assert.assertEquals(1, result.size());
		Assert.assertTrue(result.contains(Double.valueOf(1.1)));
	}



	/**
	 * Tests by using a String that is a non decimal number and we get the value as a {@link Number}
	 */
	@Test
	public void withNonDecimalStringExpression() {
		XQueryExecutor executor = getExecutor();
		executor.setResultMappers(Collections.singletonMap(Number.class,
				(XQueryResultMapper<Number>)new NumberResultMapper()));
		executor.setXQuery("'4'");
		executor.afterPropertiesSet();
		List<Number> result = executor.executeForNumber(MessageBuilder.withPayload("<test/>").build());
		Assert.assertEquals(1, result.size());
		Assert.assertTrue(result.contains(Long.valueOf(4)));
	}

	/**
	 * Tests by using a String that is a decimal number and we get the value as a {@link Number}
	 */
	@Test
	public void withDecimalStringExpression() {
		XQueryExecutor executor = getExecutor();
		executor.setResultMappers(Collections.singletonMap(Number.class,
				(XQueryResultMapper<Number>)new NumberResultMapper()));
		executor.setXQuery("'4.1'");
		executor.afterPropertiesSet();
		List<Number> result = executor.executeForNumber(MessageBuilder.withPayload("<test/>").build());
		Assert.assertEquals(1, result.size());
		Assert.assertTrue(result.contains(Double.valueOf(4.1)));
	}

	/**
	 * Tests by getting the Integer value and getting the result as {@link String}
	 */
	@Test
	public void getIntegerAsString() {
		XQueryExecutor executor = getExecutor();
		executor.setXQuery("4");
		executor.afterPropertiesSet();
		List<String> result = executor.executeForString(MessageBuilder.withPayload("<test/>").build());
		Assert.assertEquals(1, result.size());
		Assert.assertTrue(result.contains("4"));
	}



	/**
	 * Uses a decimal value and gets the result as {@link String}
	 */
	@Test
	public void getDecimalAsString() {
		XQueryExecutor executor = getExecutor();
		executor.setXQuery("4.1");
		executor.afterPropertiesSet();
		List<String> result = executor.executeForString(MessageBuilder.withPayload("<test/>").build());
		Assert.assertEquals(1, result.size());
		Assert.assertTrue(result.contains("4.1"));
	}

//	@Test
//	public void getCurrentDate() {
//		XQueryExecutor executor = new XQueryExecutor();
//		executor.setXQuery("current-date()");
//		executor.afterPropertiesSet();
//		executor.executeForString(MessageBuilder.withPayload("<test/>").build());
//	}


	@Test
	public void withStringExpression2() {
		XQueryExecutor executor = getExecutor();
		executor.setResultMappers(Collections.singletonMap(Number.class,
				(XQueryResultMapper<Number>)new NumberResultMapper()));
		executor.setXQuery("/person/age/text()");
		executor.afterPropertiesSet();
		List<Number> result = executor.executeForNumber(MessageBuilder.withPayload(xmlString).build());
		Assert.assertEquals(1, result.size());
		Assert.assertTrue(result.contains(Long.valueOf(29)));
	}

	@Test
	public void withFormatingFlag() {
		XQueryExecutor executor = getExecutor();
		executor.setFormatOutput(true);
		executor.setXQuery("'Hello World'");
		executor.afterPropertiesSet();
		//should not fail
		executor.executeForString(MessageBuilder.withPayload("<dummy/>").build());
		@SuppressWarnings("unchecked")
		Map<Class<Object>, XQueryResultMapper<Object>>  mappers = TestUtils.getPropertyValue(executor, "resultMappers", Map.class);
		for(Object val:mappers.values()) {
			if(val instanceof AbstractXQueryResultMapper) {
				Assert.assertEquals(true,
						TestUtils.getPropertyValue(val, "formatOutput", Boolean.class).booleanValue());
			}
		}

	}



	private class MyCustomClass {}
	private class CustomNodeMapper implements XQueryResultMapper<Node> {
		@Override
		public List<Node> mapResults(XQResultSequence result) {
			return null;
		}

	}
}
