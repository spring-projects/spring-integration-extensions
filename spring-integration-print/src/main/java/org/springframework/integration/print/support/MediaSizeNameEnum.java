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

import javax.print.attribute.standard.MediaSizeName;

import org.springframework.util.Assert;

/**
 * @author Gunnar Hillert
 * @since 1.0
 */
public enum MediaSizeNameEnum {
	ISO_A0(MediaSizeName.ISO_A0),
	ISO_A1(MediaSizeName.ISO_A1),
	ISO_A2(MediaSizeName.ISO_A2),
	ISO_A3(MediaSizeName.ISO_A3),
	ISO_A4(MediaSizeName.ISO_A4),
	ISO_A5(MediaSizeName.ISO_A5),
	ISO_A6(MediaSizeName.ISO_A6),
	ISO_A7(MediaSizeName.ISO_A7),
	ISO_A8(MediaSizeName.ISO_A8),
	ISO_A9(MediaSizeName.ISO_A9),
	ISO_A10(MediaSizeName.ISO_A10),
	ISO_B0(MediaSizeName.ISO_B0),
	ISO_B1(MediaSizeName.ISO_B1),
	ISO_B2(MediaSizeName.ISO_B2),
	ISO_B3(MediaSizeName.ISO_B3),
	ISO_B4(MediaSizeName.ISO_B4),
	ISO_B5(MediaSizeName.ISO_B5),
	ISO_B6(MediaSizeName.ISO_B6),
	ISO_B7(MediaSizeName.ISO_B7),
	ISO_B8(MediaSizeName.ISO_B8),
	ISO_B9(MediaSizeName.ISO_B9),
	ISO_B10(MediaSizeName.ISO_B10),
	JIS_B0(MediaSizeName.JIS_B0),
	JIS_B1(MediaSizeName.JIS_B1),
	JIS_B2(MediaSizeName.JIS_B2),
	JIS_B3(MediaSizeName.JIS_B3),
	JIS_B4(MediaSizeName.JIS_B4),
	JIS_B5(MediaSizeName.JIS_B5),
	JIS_B6(MediaSizeName.JIS_B6),
	JIS_B7(MediaSizeName.JIS_B7),
	JIS_B8(MediaSizeName.JIS_B8),
	JIS_B9(MediaSizeName.JIS_B9),
	JIS_B10(MediaSizeName.JIS_B10),
	ISO_C0(MediaSizeName.ISO_C0),
	ISO_C1(MediaSizeName.ISO_C1),
	ISO_C2(MediaSizeName.ISO_C2),
	ISO_C3(MediaSizeName.ISO_C3),
	ISO_C4(MediaSizeName.ISO_C4),
	ISO_C5(MediaSizeName.ISO_C5),
	ISO_C6(MediaSizeName.ISO_C6),
	NA_LETTER(MediaSizeName.NA_LETTER),
	NA_LEGAL(MediaSizeName.NA_LEGAL),
	LEDGER(MediaSizeName.LEDGER),
	TABLOID(MediaSizeName.TABLOID),
	INVOICE(MediaSizeName.INVOICE),
	FOLIO(MediaSizeName.FOLIO),
	QUARTO(MediaSizeName.QUARTO),
	JAPANESE_POSTCARD(MediaSizeName.JAPANESE_POSTCARD),
	JAPANESE_DOUBLE_POSTCARD(MediaSizeName.JAPANESE_DOUBLE_POSTCARD),
	A(MediaSizeName.A),
	B(MediaSizeName.B),
	C(MediaSizeName.C),
	D(MediaSizeName.D),
	E(MediaSizeName.E),
	ISO_DESIGNATED_LONG(MediaSizeName.ISO_DESIGNATED_LONG),
	ITALY_ENVELOPE(MediaSizeName.ITALY_ENVELOPE),
	MONARCH_ENVELOPE(MediaSizeName.MONARCH_ENVELOPE),
	PERSONAL_ENVELOPE(MediaSizeName.PERSONAL_ENVELOPE),
	NA_NUMBER_9_ENVELOPE(MediaSizeName.NA_NUMBER_9_ENVELOPE),
	NA_NUMBER_10_ENVELOPE(MediaSizeName.NA_NUMBER_10_ENVELOPE),
	NA_NUMBER_11_ENVELOPE(MediaSizeName.NA_NUMBER_11_ENVELOPE),
	NA_NUMBER_12_ENVELOPE(MediaSizeName.NA_NUMBER_12_ENVELOPE),
	NA_NUMBER_14_ENVELOPE(MediaSizeName.NA_NUMBER_14_ENVELOPE),
	NA_6X9_ENVELOPE(MediaSizeName.NA_6X9_ENVELOPE),
	NA_7X9_ENVELOPE(MediaSizeName.NA_7X9_ENVELOPE),
	NA_9X11_ENVELOPE(MediaSizeName.NA_9X11_ENVELOPE),
	NA_9X12_ENVELOPE(MediaSizeName.NA_9X12_ENVELOPE),
	NA_10X13_ENVELOPE(MediaSizeName.NA_10X13_ENVELOPE),
	NA_10X14_ENVELOPE(MediaSizeName.NA_10X14_ENVELOPE),
	NA_10X15_ENVELOPE(MediaSizeName.NA_10X15_ENVELOPE),
	NA_5X7(MediaSizeName.NA_5X7),
	NA_8X10(MediaSizeName.NA_8X10);

	private MediaSizeName mediaSizeName;

	private MediaSizeNameEnum(MediaSizeName mediaSizeName) {
		this.mediaSizeName = mediaSizeName;
	}

	public MediaSizeName getMediaSizeName() {
		return mediaSizeName;
	}

	public static MediaSizeName getForString(String mediaSizeNameAsString) {

		Assert.hasText(mediaSizeNameAsString, "'mediaSizeNameAsString' must neither be null nor empty.");

		final MediaSizeNameEnum[] mediaSizeNameEnumValues = MediaSizeNameEnum.values();

		for (MediaSizeNameEnum mediaSizeNameEnum : mediaSizeNameEnumValues) {
			if (mediaSizeNameEnum.name().equalsIgnoreCase(mediaSizeNameAsString)) {
				return mediaSizeNameEnum.mediaSizeName;
			}
		}

		throw new IllegalArgumentException("Invalid mediaSizeName '" + mediaSizeNameAsString
				+ "' provided.");

	}
}
