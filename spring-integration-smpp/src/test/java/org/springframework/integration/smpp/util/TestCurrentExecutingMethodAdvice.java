/* Copyright 2002-2013 the original author or authors.
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
package org.springframework.integration.smpp.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.aop.framework.ProxyFactoryBean;

/**
 * @author Josh Long
 * @since 1.0
 */
public class TestCurrentExecutingMethodAdvice {
	static String currentMethodName() {
		return CurrentExecutingMethodHolder.getCurrentlyExecutingMethod().getName();
	}

	static public class TestClassWithAMethod {
		public TestClassWithAMethod() {
		}

		private Log log = LogFactory.getLog(getClass());

		public void testMe() throws Throwable {
			Assert.assertEquals("testMe", currentMethodName());
			log.debug("The currently executing method name is " + currentMethodName());
		}
	}

	@Test
	public void testLoggingTheCurrentlyExecutingMethodName() throws Throwable {
		ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();
		proxyFactoryBean.setProxyTargetClass(true);
		proxyFactoryBean.addAdvice(new CurrentMethodExposingMethodInterceptor());
		proxyFactoryBean.setTarget(new TestClassWithAMethod());

		TestClassWithAMethod testClassWithAMethod = (TestClassWithAMethod) proxyFactoryBean.getObject();
		testClassWithAMethod.testMe();
	}
}
