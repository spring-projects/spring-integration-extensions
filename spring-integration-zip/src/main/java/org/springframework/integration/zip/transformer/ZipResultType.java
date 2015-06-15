/*
 * Copyright 2015 the original author or authors.
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
package org.springframework.integration.zip.transformer;

import org.springframework.util.Assert;

/**
 *
 * @author Gunnar Hillert
 * @since 1.0
 */
public enum ZipResultType {

	FILE("FILE"),
	BYTE_ARRAY("BYTE_ARRAY");

	private String id;

	private ZipResultType(String id) {
		this.id = id;
	}

	/**
	 * Retrieves the matching enum constant for a provided String representation
	 * of the {@link ZipResultType}. The provided name must match exactly the identifier as
	 * used to declare the enum constant.
	 *
	 * @param zipResultTypeAsString Name of the enum to convert. Must be not null and not empty.
	 * @return The enumeration that matches. Returns Null of no match was found.
	 *
	 */
	public static ZipResultType convertToZipResultType(String zipResultTypeAsString) {

		Assert.hasText(zipResultTypeAsString, "Parameter zipResultTypeAsString must not be null or empty");

		for (ZipResultType zipResultType : ZipResultType.values()) {
			if (zipResultType.name().equalsIgnoreCase(zipResultTypeAsString)) {
				return zipResultType;
			}
		}

		return null;
	}

	public String getId() {
		return id;
	}

}
