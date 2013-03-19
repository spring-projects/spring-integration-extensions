package org.springframework.integration.kafka.config.xml;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.kafka.support.KafkaServer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Soby Chacko
 * @since 1.0
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class KafkaServerParserTests {

	@Autowired
	private ApplicationContext appContext;

	/**
	 * Test method for {@link org.springframework.integration.kafka.config.xml.KafkaServerParser#doParse(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext, org.springframework.beans.factory.support.BeanDefinitionBuilder)}.
	 */
	@Test
	public void testDoParseElementParserContextBeanDefinitionBuilder() {
		KafkaServer server = appContext.getBean("kafkaServer", KafkaServer.class);

		Assert.assertEquals("localhost:2181", server.getZkConnect());
        Assert.assertEquals("10000", server.getZkConnectionTimeout());
	}

}
