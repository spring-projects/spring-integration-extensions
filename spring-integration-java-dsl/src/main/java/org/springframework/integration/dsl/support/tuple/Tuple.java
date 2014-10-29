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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.springframework.util.Assert;

/**
 * A {@literal Tuple} is an immutable {@link java.util.Collection} of objects,
 * each of which can be of an arbitrary type.
 *
 * @author Jon Brisbin
 * @author Stephane Maldini
 */
@SuppressWarnings({"rawtypes"})
public class Tuple implements Iterable, Serializable {

	private static final long serialVersionUID = 8777121214502020842L;

	protected final Object[] entries;

	protected final int size;

	/**
	 * Creates a new {@code Tuple} that holds the given {@code values}.
	 *
	 * @param values The values to hold
	 */
	public Tuple(Collection<Object> values) {
		Assert.notEmpty(values);
		this.entries = values.toArray();
		this.size = entries.length;
	}

	/**
	 * Creates a new {@code Tuple} that holds the given {@code values}.
	 *
	 * @param values The values to hold
	 */
	public Tuple(Object... values) {
		this.entries = Arrays.copyOf(values, values.length);
		this.size = values.length;
	}


	/**
	 * Get the object at the given index.
	 *
	 * @param index The index of the object to retrieve. Starts at 0.
	 * @return The object. Might be {@literal null}.
	 */
	public Object get(int index) {
		return (size > 0 && size > index ? entries[index] : null);
	}

	/**
	 * Turn this {@literal Tuple} into a plain Object array.
	 *
	 * @return A new Object array.
	 */
	public Object[] toArray() {
		return entries;
	}

	/**
	 * Return the number of elements in this {@literal Tuple}.
	 *
	 * @return The size of this {@literal Tuple}.
	 */
	public int size() {
		return size;
	}

	@Override
	public Iterator<?> iterator() {
		return Arrays.asList(entries).iterator();
	}


	@Override
	public int hashCode() {
		if (this.size == 0) {
			return 0;
		}
		else if (this.size == 1) {
			return this.entries[0].hashCode();
		}
		else {
			int hashCode = 1;
			for (Object entry : this.entries) {
				hashCode = hashCode ^ entry.hashCode();
			}
			return hashCode;
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) return false;

		if (!(o instanceof Tuple)) return false;

		Tuple cast = (Tuple) o;

		if (this.size != cast.size) return false;

		for (int i = 0; i < this.size; i++) {
			if (!this.entries[i].equals(cast.entries[i])) {
				return false;
			}
		}
		return true;
	}

}
