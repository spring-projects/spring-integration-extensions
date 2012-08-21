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
package org.springframework.integration.xquery.config.xml;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.config.xml.AbstractTransformerParser;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * The parser for the XQuery transformer component
 *
 * @author Amol Nayak
 *
 * @since 1.0
 *
 */
public class XQueryTransformerParser extends AbstractTransformerParser {

	/* (non-Javadoc)
	 * @see org.springframework.integration.config.xml.AbstractTransformerParser#getTransformerClassName()
	 */
	@Override
	protected String getTransformerClassName() {
		return "org.springframework.integration.xquery.transformer.XQueryTransformer";
	}

	/* (non-Javadoc)
	 * @see org.springframework.integration.config.xml.AbstractTransformerParser#parseTransformer(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext, org.springframework.beans.factory.support.BeanDefinitionBuilder)
	 */
	@Override
	protected void parseTransformer(Element element,
			ParserContext parserContext, BeanDefinitionBuilder builder) {
		AbstractBeanDefinition executor = XQueryParserUtils.getXQueryExecutor(element);
		builder.addPropertyValue("executor", executor);
		//Add the result type and the result class attributes
		String resultType = element.getAttribute("result-type");
		boolean hasResultType = StringUtils.hasText(resultType);
		String xqueryResultMapper = element.getAttribute("xquery-result-mapper");
		boolean hasResultMapper = StringUtils.hasText(xqueryResultMapper);
		Assert.isTrue(!(hasResultType && hasResultMapper),
				"Only one of result-type or xquery-result-mapper may be specified");
		if(hasResultType) {
			Class<?> type = null;
			if("string".equalsIgnoreCase(resultType)) {
				type = String.class;
			}
			else if("boolean".equalsIgnoreCase(resultType)) {
				type = Boolean.class;
			}
			else if("number".equalsIgnoreCase(resultType)) {
				type = Number.class;
			}
			else if("node".equalsIgnoreCase(resultType)) {
				type = Node.class;
			}
			else {
				try {
					type = Class.forName(resultType);
				} catch (ClassNotFoundException e) {
					throw new IllegalArgumentException("Class " + resultType + " specified in result-type not found, " +
							"have you provided the fully qualified name?",e);
				}
			}
			builder.addPropertyValue("resultType", type);
		}
		else if(hasResultMapper) {
			builder.addPropertyReference("resultMapper", xqueryResultMapper);
		}
	}
}
