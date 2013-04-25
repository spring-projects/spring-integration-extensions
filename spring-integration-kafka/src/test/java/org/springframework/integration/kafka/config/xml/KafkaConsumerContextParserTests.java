package org.springframework.integration.kafka.config.xml;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.kafka.support.ConsumerConfiguration;
import org.springframework.integration.kafka.support.KafkaConsumerContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collection;
import java.util.Map;

/**
 * @author Soby Chacko
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class KafkaConsumerContextParserTests {

    @Autowired
    private ApplicationContext appContext;

    @Test
    @SuppressWarnings("unchecked")
    public void testConsumerContextConfiguration() {
        final KafkaConsumerContext consumerContext = appContext.getBean("consumerContext", KafkaConsumerContext.class);
        Assert.assertNotNull(consumerContext);

        final Collection<ConsumerConfiguration> consumerConfigurations = consumerContext.getConsumerConfigurations();
        Assert.assertEquals(consumerConfigurations.size(), 1);

        final ConsumerConfiguration consumerConfiguration = consumerConfigurations.iterator().next();
        final Map<String, Integer> topicStreamMap = consumerConfiguration.getTopicStreamMap();
        Assert.assertEquals(topicStreamMap.size(), 2);
        Assert.assertEquals(topicStreamMap.get("test1"), (Integer)4);
        Assert.assertEquals(topicStreamMap.get("test2"), (Integer)4);
    }
}
