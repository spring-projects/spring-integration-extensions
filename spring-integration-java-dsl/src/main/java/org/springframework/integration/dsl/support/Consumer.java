/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.integration.dsl.support;

/**
 * Implementations accept a given value and perform work on the argument.
 *
 * <p>This is a copy of Java 8 {@code Consumer} interface.
 *
 * @param <T> the type of values to accept
 *
 * @author Jon Brisbin
 * @author Stephane Maldini
 */
public interface Consumer<T> {

	/**
	 * Execute the logic of the action, accepting the given parameter.
	 * @param t The parameter to pass to the consumer.
	 */
	void accept(T t);

}
