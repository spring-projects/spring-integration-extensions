/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.integration.cassandra.config.xml;

import java.util.List;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.config.ExpressionFactoryBean;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * @author Filippo Balicchia
 *
 */
public class CassandraParserUtils {

	public static void processOutboundTypeAttributes(Element element, ParserContext parserContext,
			BeanDefinitionBuilder builder) {

		String cassandraTemplate = element.getAttribute("cassandra-template");
		String operationType = element.getAttribute("type");
		String writeOptionsInstance = element.getAttribute("write-options");
		String ingestQuery = element.getAttribute("cql-ingest");
		String query = element.getAttribute("query");

		if (StringUtils.isEmpty(cassandraTemplate)) {
			parserContext.getReaderContext().error("cassandra-template is empty", element);
		}

		builder.addConstructorArgValue(new RuntimeBeanReference(cassandraTemplate));
		if (!StringUtils.isEmpty(operationType)) {
			builder.addConstructorArgValue(operationType);
		}

		if (!StringUtils.isEmpty(writeOptionsInstance)) {
			builder.addPropertyReference("writeOptions", writeOptionsInstance);
		}

		if (!StringUtils.isEmpty(ingestQuery) && !"INSERT".equalsIgnoreCase(operationType)) {
			parserContext.getReaderContext().error("Ingest cql query can be apply only with insert operation", element);
		}
		else if (!StringUtils.isEmpty(ingestQuery)) {
			builder.addPropertyValue("ingestQuery", ingestQuery);
		}

		if (!StringUtils.isEmpty(query)) {
			builder.addPropertyValue("query", query);
		}

		BeanDefinition statementExpressionDef = IntegrationNamespaceUtils
				.createExpressionDefIfAttributeDefined("statement-expression", element);
		if (statementExpressionDef != null) {
			builder.addPropertyValue("statementExpression", statementExpressionDef);
		}

		List<Element> parameterExpression = DomUtils.getChildElementsByTagName(element, "parameter-expression");
		if (!CollectionUtils.isEmpty(parameterExpression)) {
			ManagedMap<String, Object> parameterExpressionsMap = new ManagedMap<String, Object>();
			for (Element parameterExpressionElement : parameterExpression) {
				String name = parameterExpressionElement.getAttribute("name");
				String expression = parameterExpressionElement.getAttribute("expression");
				BeanDefinitionBuilder factoryBeanBuilder = BeanDefinitionBuilder
						.genericBeanDefinition(ExpressionFactoryBean.class);
				factoryBeanBuilder.addConstructorArgValue(expression);
				parameterExpressionsMap.put(name, factoryBeanBuilder.getBeanDefinition());
			}
			builder.addPropertyValue("parameterExpressions", parameterExpressionsMap);
		}

	}
}
