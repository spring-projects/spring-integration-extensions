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
package org.springframework.integration.xquery.router;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.test.util.TestUtils;
import org.springframework.integration.xquery.core.XQueryExecutor;


/**
 * The Test class for the XQuery router
 *
 * @author Amol Nayak
 *
 * @since 1.0
 *
 */
public class XQueryRouterTests {

	private static final String STUDENT_XML =	"<students>" +
											"<student>" +
												"<name>Name1</name>" +
												"<age>21</age>" +
											"</student>" +
											"<student>" +
												"<name>Name2</name>" +
												"<age>22</age>" +
											"</student>" +
										"</students>";

	/**
	 * Tests the executor without seting the XQueryExecutor's instance
	 */
	@Test
	public void withoutExecutor() {
		XQueryRouter router = new XQueryRouter();
		try {
			router.afterPropertiesSet();
		} catch (IllegalArgumentException e) {
			Assert.assertEquals("No XQueryExecutor instance provided", e.getMessage());
		}
	}


	/**
	 * Tries setting the executor instance of the {@link XQueryRouter} to null.
	 */
	@Test
	public void setNullExecutor() {
		XQueryRouter router = new XQueryRouter();
		try {
			router.setExecutor(null);
		} catch (IllegalArgumentException e) {
			Assert.assertEquals("Provide a non null implementation of the executor", e.getMessage());
		}
	}

	/**
	 * Gets the channel keys for the given XQuery expression
	 */
	@Test
	public void getChannelKeys() {
		XQueryRouter router = new XQueryRouter();
		router.setExecutor(getExecutor("//test/text()"));
		router.afterPropertiesSet();
		List<Object> keys=
				router.getChannelKeys(MessageBuilder.withPayload("<test>Test</test>").build());
		Assert.assertNotNull(keys);
		Assert.assertEquals(1, keys.size());
		Assert.assertTrue(keys.contains("Test"));
	}

	@Test
	public void testDefaultResultType() {
		XQueryRouter router =  new XQueryRouter();
		router.setExecutor(getExecutor("'Hello'"));
		router.afterPropertiesSet();
		Assert.assertEquals(String.class,
				TestUtils.getPropertyValue(router, "resultType", Class.class));

	}

	@Test
	public void getChannelKeysAsStrings() {
		XQueryRouter router = new XQueryRouter();
		router.setExecutor(getExecutor("/students/student/name/text()"));
		router.afterPropertiesSet();
		List<Object> keys = router.getChannelKeys(MessageBuilder.withPayload(STUDENT_XML).build());
		Assert.assertEquals(2, keys.size());
		Assert.assertTrue(keys.contains("Name1"));
		Assert.assertTrue(keys.contains("Name2"));
	}

	/**
	 * The method demonstrates the conversion of a Numeric type to a String with get
	 * channel keys method.
	 */
	@Test
	public void getChannelKeysAsStrings2() {
		XQueryRouter router = new XQueryRouter();
		router.setExecutor(getExecutor("/students/student/age/text()"));
		router.afterPropertiesSet();
		List<Object> keys = router.getChannelKeys(MessageBuilder.withPayload(STUDENT_XML).build());
		Assert.assertEquals(2, keys.size());
		Assert.assertTrue(keys.contains("21"));
		Assert.assertTrue(keys.contains("22"));
	}

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
