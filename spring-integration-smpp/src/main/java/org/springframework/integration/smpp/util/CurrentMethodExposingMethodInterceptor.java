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

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * Simple method interceptor that does nothing but store the currently executing method and make it available for the duration
 * of the invoked method, so that any class may introspect the currently running method
 * without setting up a custom {@link MethodInterceptor} like this one.
 * <p/>
 * In a sense, this is like JavaScript's method arity feature.
 *
 * @author Josh Long
 * @since 1.0
 */
public class CurrentMethodExposingMethodInterceptor implements MethodInterceptor {

	@Override
	public Object invoke(MethodInvocation methodInvocation) throws Throwable {
		try {
			CurrentExecutingMethodHolder.setCurrentlyExecutingMethod(methodInvocation.getMethod());
			return methodInvocation.proceed();
		} finally {
			CurrentExecutingMethodHolder.removeMethod();
		}
	}
}
