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

package org.springframework.integration.zip.config.xml;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.springframework.integration.zip.transformer.UnZipTransformer;
import org.springframework.integration.zip.transformer.ZipResultType;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Parser for the 'unzip-transformer' element.
 *
 * @author Gunnar Hillert
 * @since 1.0
 */
public class UnZipTransformerParser extends AbstractZipTransformerParser {

	@Override
	protected String getTransformerClassName() {
		return UnZipTransformer.class.getName();
	}

	@Override
	protected void postProcessTransformer(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
		Object source = parserContext.extractSource(element);

		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "charset");
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "expect-single-result");

		final String resultType     = element.getAttribute("result-type");

		if (StringUtils.hasText(resultType)) {

			final ZipResultType zipResultType = ZipResultType.convertToZipResultType(resultType);

			if (zipResultType != null) {
				builder.addPropertyValue("zipResultType", zipResultType);
			}
			else {
				parserContext.getReaderContext().error(
					String.format("Unable to convert the provided result-type '%s' " +
							"to the respective ZipResultType enum.", resultType), source);
			}

		}
	}

}
