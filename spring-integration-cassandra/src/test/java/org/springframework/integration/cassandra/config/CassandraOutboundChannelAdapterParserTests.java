package org.springframework.integration.cassandra.config;

import static org.junit.Assert.*;

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
//    @Test
//    public void minimalConfig(){
//		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
//				"outbound-adapter-parser-config.xml", this.getClass());
//        
//		CassandraMessageHandler<?> handler = TestUtils.getPropertyValue(
//				context.getBean("outbound1.adapter"), "handler",
//				CassandraMessageHandler.class);
//        
//		assertEquals("outbound1.adapter", TestUtils.getPropertyValue(handler, "componentName"));
//		
//		assertEquals(CassandraMessageHandler.OperationType.INSERT, TestUtils.getPropertyValue(handler, "queryType"));
//		assertEquals(context.getBean("cassandraTemplate"), TestUtils.getPropertyValue(handler, "cassandraTemplate"));
//		assertEquals(context.getBean("writeOptions"), TestUtils.getPropertyValue(handler, "writeOptions"));
//        
//    }
//    
//    @Test(expected=BeanDefinitionParsingException.class)
//    public void ingestConfigFail(){
//		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
//				"outbound-adapter-parser-config.xml", this.getClass());
//        
//		CassandraMessageHandler<?> handler = TestUtils.getPropertyValue(
//				context.getBean("outbound2.adapter"), "handler",
//				CassandraMessageHandler.class);
//    }
    
    
    @Test
    public void ingestConfig(){
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				"outbound-adapter-parser-config.xml", this.getClass());
        
		CassandraMessageHandler<?> handler = TestUtils.getPropertyValue(
				context.getBean("outbound3.adapter"), "handler",
				CassandraMessageHandler.class);
		
		
		assertEquals(context.getBean("ingestQuery"), TestUtils.getPropertyValue(handler, "ingestQuery"));
		
		//cql-ingest="insert into book (isbn, title, author, pages, saleDate, isInStock) values (?, ?, ?, ?, ?, ?)"
		
		
		
    }
    
    
    
    

}
