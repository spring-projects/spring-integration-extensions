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
package org.springframework.integration.xquery.support;

import java.util.List;

import javax.xml.xquery.XQResultSequence;

/**
 * The strategy interface that will be used to map the {@link XQResultSequence}
 * returned by XQuery execution to a List of the specified type
 *
 * @author Amol Nayak
 *
 * @since 1.0
 *
 */
public interface XQueryResultMapper<T> {

	/**
	 * Maps the results from the {@link XQResultSequence} to a {@link List} of a specific type
	 * @param result
	 * @return
	 */
	List<T> mapResults(XQResultSequence result);
}
