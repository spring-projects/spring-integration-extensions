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

import java.util.Comparator;

import javax.print.DocFlavor;

/**
 * @author Gunnar Hillert
 * @since 1.0
 */
public class DocFlavorComparator implements Comparator<DocFlavor> {

	public int compare(DocFlavor docFlavor1, DocFlavor docFlavor2) {

		int comparison = docFlavor1.getMimeType().compareTo(docFlavor2.getMimeType());

		if (comparison == 0) {
			return docFlavor1.getMediaSubtype().compareTo(docFlavor2.getMediaSubtype());
		}

		return comparison;
	}

}
