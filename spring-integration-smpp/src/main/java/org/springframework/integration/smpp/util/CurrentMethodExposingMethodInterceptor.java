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
 * @since 2.1
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
