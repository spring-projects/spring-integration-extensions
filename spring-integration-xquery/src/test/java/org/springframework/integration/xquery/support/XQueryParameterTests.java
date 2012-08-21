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
package org.springframework.integration.xquery.support;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.test.util.TestUtils;

/**
 * The test class for {@link XQueryParameter} class
 *
 * @author Amol Nayak
 *
 * @since 1.0
 *
 */
public class XQueryParameterTests {

	/**
	 * Sets a null parameter name, should throw an exception
	 */
	@Test
	public void setNullParameterName() {
		try {
			@SuppressWarnings("unused")
			XQueryParameter param = new XQueryParameter(null);
		} catch (IllegalArgumentException e) {
			Assert.assertEquals("Non null non empty String value expected for parameter name", e.getMessage());
		}
	}

	/**
	 * Sets a null parameter name, should throw an exception
	 */
	@Test
	public void setEmptyParameterName() {
		try {
			@SuppressWarnings("unused")
			XQueryParameter param = new XQueryParameter("");
		} catch (IllegalArgumentException e) {
			Assert.assertEquals("Non null non empty String value expected for parameter name", e.getMessage());
		}
	}

	/**
	 * Sets the valid parameter name
	 */
	@Test
	public void setValidParameterName() {
		XQueryParameter param = new XQueryParameter("testName");
		Assert.assertEquals("testName", param.getParameterName());
	}


	/**
	 * Sets both the expression then the parameter value, should throw an exception
	 */
	@Test
	public void setParameterExpressionThenValue() {
		XQueryParameter param = new XQueryParameter("testParameter");
		param.setExpression("headers['someHeader']");
		try {
			param.setParameterValue("Some Value");
		} catch (IllegalArgumentException e) {
			Assert.assertEquals("The parameter value and expression are mutually " +
				"exclusive, parameter expression already set", e.getMessage());
		}
		Assert.assertEquals(false, TestUtils.getPropertyValue(param,"isValueSet", Boolean.class).booleanValue());
		Assert.assertEquals("headers['someHeader']",
				TestUtils.getPropertyValue(param, "messageProcessor.expression.expression",String.class));
	}

	/**
	 * Sets both the value then the parameter expression, should throw an exception
	 */
	@Test
	public void setParameterValueThenExpression() {
		XQueryParameter param = new XQueryParameter("testParameter");
		param.setParameterValue("Some Value");
		try {
			param.setExpression("headers['someHeader']");
		} catch (IllegalArgumentException e) {
			Assert.assertEquals("The parameter value and expression are mutually exclusive" +
				", parameter value already set", e.getMessage());
		}
		Assert.assertEquals(true, TestUtils.getPropertyValue(param,"isValueSet", Boolean.class).booleanValue());
	}

	/**
	 * Sets the parameter name and its value from the constructor
	 */
	@Test
	public void setParameterValueInConstructor() {
		XQueryParameter param = new XQueryParameter("testName", (Object)"testValue");
		Assert.assertEquals(true, TestUtils.getPropertyValue(param, "isValueSet",Boolean.class).booleanValue());
		Assert.assertEquals("testName", param.getParameterName());
		Assert.assertEquals("testValue", TestUtils.getPropertyValue(param, "parameterValue", String.class));
	}

	/**
	 * Sets the parameter value from the constructor
	 */
	@Test
	public void setParameterExpressionInConstructor() {
		XQueryParameter param = new XQueryParameter("testName", "headers['someHeader']");
		Assert.assertEquals(false, TestUtils.getPropertyValue(param, "isValueSet",Boolean.class).booleanValue());
		Assert.assertEquals("testName", param.getParameterName());
		Assert.assertEquals("headers['someHeader']", TestUtils.getPropertyValue(param, "expression", String.class));
		Assert.assertEquals("headers['someHeader']",
				TestUtils.getPropertyValue(param, "messageProcessor.expression.expression",String.class));
	}

	/**
	 * Sets the expression string twice, should throw an exception
	 */
	@Test
	public void setExpressionTwice() {
		XQueryParameter param = new XQueryParameter("paramName");
		param.setExpression("headers['headerValue']");
		try {
			param.setExpression("headers['headerV']");
		} catch (IllegalArgumentException e) {
			Assert.assertEquals("Expression string is already set once, cannot reset it", e.getMessage());
		}
	}

	/**
	 * Evaluate the parameter to get the static value set
	 */
	@Test
	public void executeForStaticValue() {
		XQueryParameter param = new XQueryParameter("paramName", Integer.valueOf(10));
		Object value = param.evaluate(null);
		Assert.assertEquals(value, Integer.valueOf(10));
	}

	/**
	 * Execute an expression to get the return values
	 */
	@Test
	public void executeExpressionValue() {
		XQueryParameter param = new XQueryParameter("paramName","headers['numbers'].?[#this > 5]");
		List<Integer> numbers = Arrays.asList(1,3,4,2,5,6,7);
		Message<String> message = MessageBuilder.withPayload("")
									.setHeader("numbers", numbers)
									.build();
		@SuppressWarnings("unchecked")
		Collection<Integer> coll = (Collection<Integer>)param.evaluate(message);
		Assert.assertEquals(2, coll.size());
	}
}
