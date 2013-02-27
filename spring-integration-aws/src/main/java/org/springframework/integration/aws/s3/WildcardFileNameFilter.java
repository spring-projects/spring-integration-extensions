/*
 * Copyright 2002-2013 the original author or authors.
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
package org.springframework.integration.aws.s3;

import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.springframework.util.Assert;

/**
 * Performs wildcard filename filtering based on the wildcard String passed.
 * Used Apache Commons IO {@link WildcardFileFilter} to perform the filtering
 *
 * @author Amol Nayak
 *
 * @since 0.5
 *
 */
public class WildcardFileNameFilter extends AbstractFileNameFilter {

	private final IOFileFilter filter;

	/**
	 * Default construtor accepting the wildcard string
	 *
	 * @param wildcardString
	 */
	public WildcardFileNameFilter(String wildcardString) {
		Assert.hasText(wildcardString, "Wildcard string should be non null, non empty String");
		filter = new WildcardFileFilter(wildcardString,IOCase.INSENSITIVE);
		//Our checks will be case insensitive
	}

	/* (non-Javadoc)
	 * @see org.springframework.integration.aws.s3.FileNameFilter#accept(java.lang.String)
	 */

	@Override
	public boolean isFileNameAccepted(String fileName) {
		return filter.accept(null, fileName);
	}
}
