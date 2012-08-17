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
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.springframework.integration.xquery.core.XQueryExecutor;
import org.springframework.integration.xquery.support.XQueryParameter;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The common utility class for the XQuery components that will be performing the
 * common operations used in the parsers like, creating the XQueryExecutor instance
 * etc.
 *
 * @author Amol Nayak
 * @since 2.2
 *
 */
public class XQueryParserUtils {

	/**
	 * Create the instance of the {@link XQueryExecutor}
	 * @param element
	 * @return
	 */
	public static final AbstractBeanDefinition getXQueryExecutor(Element element) {
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(XQueryExecutor.class);
		IntegrationNamespaceUtils.setReferenceIfAttributeDefined(builder, element, "converter");
		IntegrationNamespaceUtils.setReferenceIfAttributeDefined(builder, element, "xq-datasource","xQDataSource");
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "format-output");
		NodeList list = element.getElementsByTagNameNS(element.getNamespaceURI(), "xquery");
		Attr xQueryAttribute = element.getAttributeNode("xquery");
		Attr xQueryResource = element.getAttributeNode("xquery-file-resource");

		Assert.isTrue(!(xQueryAttribute != null && xQueryResource != null),
				"Only one of xquery or xquery-file-resource may be specified");

		Assert.isTrue(!(xQueryAttribute != null && list != null && list.getLength() > 0),
				"At most one of the xquery attribute " +
				"or the xquery child element should to be provided");

		Assert.isTrue(!(xQueryResource != null && list != null && list.getLength() > 0),
				"At most one of the xquery-file-resource attribute " +
				"or the xquery child element should to be provided");

		Assert.isTrue(xQueryResource != null || xQueryAttribute != null || (list != null && list.getLength() > 0),
				"At least one of xquery, xquery-file-resource attributes or the xquery child element needs to be provided");

		if(xQueryResource != null) {
			//resource specified
			String textContent = xQueryResource.getTextContent();
			Assert.isTrue(StringUtils.hasText(textContent),"Non empty, non null resource path should be provided");
			Resource resource;
			if(textContent.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX)) {
				resource = new ClassPathResource(textContent.substring(ResourceUtils.CLASSPATH_URL_PREFIX.length()));
			}
			else if(textContent.startsWith(ResourceUtils.FILE_URL_PREFIX)) {
				resource = new FileSystemResource(textContent.substring(ResourceUtils.FILE_URL_PREFIX.length()));
			}
			else {
				//assuming its a classpath resource
				resource = new ClassPathResource(textContent);
			}
			builder.addPropertyValue("xQueryFileResource", resource);
		}
		else {
			//child element or attribute defined
			String textContent;
			if(xQueryAttribute == null) {
				//child might be element specified
				Assert.isTrue(list != null && list.getLength() == 1,"Maximum one xquery child node may be specified");
				textContent = list.item(0).getTextContent();
			}
			else {
				//xquery specified in the attribute
				textContent = xQueryAttribute.getTextContent();
			}
			if(StringUtils.hasText(textContent)) {
				builder.addPropertyValue("xQuery", textContent.trim());
			}
		}

		//lets get the parameter nodes
		NodeList parameters = element.getElementsByTagNameNS(element.getNamespaceURI(), "xquery-parameter");
		if(parameters != null && parameters.getLength() > 0) {
			ManagedList<AbstractBeanDefinition> params = new ManagedList<AbstractBeanDefinition>();
			for(int i = 0;i < parameters.getLength();i++) {
				Node node = parameters.item(i);
				NamedNodeMap attrs = node.getAttributes();
				Assert.isTrue(attrs.getLength() > 1,
						"One of ref, value or expression should be present with the name attribute");
				Attr nameAttr = (Attr)attrs.getNamedItem("name");

				//TODO No check for the mutually exclusivity of these attributes, needed?

				//create a new XQueryParameter instance
				BeanDefinitionBuilder paramBuilder =
					BeanDefinitionBuilder.genericBeanDefinition(XQueryParameter.class);
				paramBuilder.addConstructorArgValue(nameAttr.getTextContent());
				Attr attr;
				//add the value if present
				if(attrs.getNamedItem("value") != null) {
					attr = (Attr)attrs.getNamedItem("value");
					paramBuilder.addPropertyValue("parameterValue",attr.getTextContent());
				}
				else if(attrs.getNamedItem("ref") != null) {
					attr = (Attr)attrs.getNamedItem("ref");
					paramBuilder.addPropertyReference("parameterValue", attr.getTextContent());
				}
				else if(attrs.getNamedItem("expression") != null) {
					attr = (Attr)attrs.getNamedItem("expression");
					paramBuilder.addPropertyValue("expression", attr.getTextContent());
				}
				params.add(paramBuilder.getBeanDefinition());
			}
			builder.addPropertyValue("xQueryParameters", params);
		}


		return builder.getBeanDefinition();
	}
}
