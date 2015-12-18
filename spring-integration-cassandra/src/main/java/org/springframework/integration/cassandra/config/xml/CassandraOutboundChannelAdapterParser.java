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

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.cassandra.outbound.CassandraMessageHandler;
import org.springframework.integration.cassandra.outbound.CassandraMessageHandler.OperationType;
import org.springframework.integration.config.xml.AbstractOutboundChannelAdapterParser;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.w3c.dom.Element;

/**
 * @author Filippo Balicchia
 *
 */
public class CassandraOutboundChannelAdapterParser extends AbstractOutboundChannelAdapterParser
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected AbstractBeanDefinition parseConsumer(Element element, ParserContext parserContext)
    {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(CassandraMessageHandler.class);
        
        
        String cassandraTemplate = element.getAttribute("cassandra-template");
        String operationType = element.getAttribute("operation-type");
        String writeOptionsInstance = element.getAttribute("write-options");
        String ingestQuery = element.getAttribute("cql-ingest");

		if (StringUtils.isEmpty(cassandraTemplate)){
			parserContext.getReaderContext().error("cassandra-template is empty", element);
		}

		if (StringUtils.isEmpty(operationType)) {
			parserContext.getReaderContext().error("operation-type need to be specified", element);
		}
		
		OperationType queryType = CassandraMessageHandler.OperationType.toType(operationType);
		builder.addConstructorArgValue(
				new RuntimeBeanReference(cassandraTemplate));
		builder.addPropertyValue("queryType", queryType);
		
		if(StringUtils.isNotEmpty(writeOptionsInstance)){
			builder.addPropertyValue("writeOptions", new RuntimeBeanReference(writeOptionsInstance));
		}
		
		if (StringUtils.isNotEmpty(ingestQuery) && !"INSERT".equalsIgnoreCase(operationType)) {
			parserContext.getReaderContext().error( "Ingest cql query can be apply only with insert operation",element);
		}
		else if (StringUtils.isNotEmpty(ingestQuery)) {
			builder.addPropertyValue("ingestQuery", ingestQuery);
		}
		
		
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "ingestQuery");
		
		return builder.getBeanDefinition();
    }
}