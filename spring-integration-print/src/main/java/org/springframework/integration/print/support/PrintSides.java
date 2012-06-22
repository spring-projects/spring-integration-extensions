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
