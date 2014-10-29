/*
 * Copyright 2014 the original author or authors.
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

package org.springframework.integration.dsl.support.tuple;

/**
 * @author Artem Bilan
 */
public abstract class Tuples {
	/**
	 * Create a {@link Tuple1} with the given object.
	 *
	 * @param t1   The first value in the tuple.
	 * @param <T1> The type of the first value.
	 * @return The new {@link Tuple1}.
	 */
	public static <T1> Tuple1<T1> of(T1 t1) {
		return new Tuple1<T1>(t1);
	}

	/**
	 * Create a {@link Tuple2} with the given objects.
	 *
	 * @param t1   The first value in the tuple.
	 * @param t2   The second value in the tuple.
	 * @param <T1> The type of the first value.
	 * @param <T2> The type of the second value.
	 * @return The new {@link Tuple2}.
	 */
	public static <T1, T2> Tuple2<T1, T2> of(T1 t1, T2 t2) {
		return new Tuple2<T1, T2>(t1, t2);
	}

}
