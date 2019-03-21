/*
 * Copyright 2002-2012 the original author or authors.
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
package org.springframework.integration.print.support;

import javax.print.attribute.standard.Sides;

import org.springframework.util.Assert;

public enum PrintSides {

	DUPLEX("DUPLEX", Sides.DUPLEX),
	ONE_SIDED("ONE_SIDED", Sides.ONE_SIDED),
	TUMBLE("TUMBLE", Sides.TUMBLE),
	TWO_SIDED_LONG_EDGE("TWO_SIDED_LONG_EDGE", Sides.TWO_SIDED_LONG_EDGE),
	TWO_SIDED_SHORT_EDGE("TWO_SIDED_SHORT_EDGE", Sides.TWO_SIDED_SHORT_EDGE);

	private String id;
	private Sides sides;

	PrintSides(String id, Sides sides) {
		this.id = id;
		this.sides = sides;
	}

	public Sides getSides() {
		return sides;
	}

	public static PrintSides fromString(String id) {

		Assert.hasText(id, "'Enumeration id must not be null nor empty'");

		for (PrintSides printSide : PrintSides.class.getEnumConstants()) {
			if (printSide.id.equalsIgnoreCase(id)) {
				return printSide;
			}
		}

		throw new IllegalArgumentException(String.format("No enumeration found for id '%s'", id));
	}

}
