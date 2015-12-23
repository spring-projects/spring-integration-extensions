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

package org.springframework.integration.cassandra.config;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.beans.factory.parsing.BeanDefinitionParsingException;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.cassandra.outbound.CassandraMessageHandler;
import org.springframework.integration.test.util.TestUtils;

/**
 * @author fbalicchia
 * @version $Id: $
 */
public class CassandraOutboundChannelAdapterParserTests
{
    @Test
    public void minimalConfig(){
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				"outbound-adapter-parser.xml", this.getClass());
        
		CassandraMessageHandler<?> handler = TestUtils.getPropertyValue(
				context.getBean("outbound1.adapter"), "handler",
				CassandraMessageHandler.class);
        
		assertEquals("outbound1.adapter", TestUtils.getPropertyValue(handler, "componentName"));
		
		assertEquals(CassandraMessageHandler.OperationType.INSERT, TestUtils.getPropertyValue(handler, "queryType"));
		assertEquals(context.getBean("cassandraTemplate"), TestUtils.getPropertyValue(handler, "cassandraTemplate"));
		assertEquals(context.getBean("writeOptions"), TestUtils.getPropertyValue(handler, "writeOptions"));
        
    }
    
    @Test(expected=BeanDefinitionParsingException.class)
    public void ingestConfigFail(){
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				"outbound-adapter-parser-fail.xml", this.getClass());
        
    }

    @Test 
    public void ingestConfig(){
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				"outbound-adapter-parser.xml", this.getClass());
		
		CassandraMessageHandler<?> handler = TestUtils.getPropertyValue(
				context.getBean("outbound2"), "handler",
				CassandraMessageHandler.class);
		
		assertEquals("insert into book (isbn, title, author, pages, saleDate, isInStock) values (?, ?, ?, ?, ?, ?)", TestUtils.getPropertyValue(handler, "ingestQuery"));
		
    }
    
    @Test 
    public void fullConfig(){
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				"outbound-adapter-parser.xml", this.getClass());
		
		CassandraMessageHandler<?> handler = TestUtils.getPropertyValue(
				context.getBean("outbound3.adapter"), "handler",
				CassandraMessageHandler.class);
		
		assertEquals("SELECT * FROM book limit :size", TestUtils.getPropertyValue(handler, "query"));
		assertEquals(Boolean.TRUE, TestUtils.getPropertyValue(handler, "producesReply"));
		assertEquals(CassandraMessageHandler.OperationType.STATEMENT, TestUtils.getPropertyValue(handler, "queryType"));
		assertEquals(context.getBean("writeOptions"), TestUtils.getPropertyValue(handler, "writeOptions"));
		
    }
    
    
    
    
    

}
