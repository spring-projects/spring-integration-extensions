/*
 * Copyright 2002-2012 the original author or authors.
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
package org.springframework.integration.splunk.config.xml;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.splunk.support.SplunkExecutor;
import org.springframework.util.Assert;
import org.w3c.dom.Element;

/**
 * Contains various utility methods for parsing Splunk Adapter
 * specific namesspace elements as well as for the generation of the
 * respective {@link BeanDefinition}s.
 *
 * @author Jarred Li
 * @since 1.0
 *
 */
public final class SplunkParserUtils {

	/** Prevent instantiation. */
	private SplunkParserUtils() {
		throw new AssertionError();
	}

	/**
	 * Create a new {@link BeanDefinitionBuilder} for the class {@link SplunkExecutor}.
	 * Initialize the wrapped {@link SplunkExecutor} with common properties.
	 *
	 * @param element Must not be null
	 * @param parserContext Must not be null
	 * @return The BeanDefinitionBuilder for the SplunkExecutor
	 */
	public static BeanDefinitionBuilder getSplunkExecutorBuilder(final Element element, final ParserContext parserContext) {

		Assert.notNull(element, "The provided element must not be null.");
		Assert.notNull(parserContext, "The provided parserContext must not be null.");

		final BeanDefinitionBuilder splunkExecutorBuilder = BeanDefinitionBuilder.genericBeanDefinition(SplunkExecutor.class);

		return splunkExecutorBuilder;

	}

}
