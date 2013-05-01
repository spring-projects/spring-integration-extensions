package org.springframework.integration.kafka.config.xml;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.kafka.support.ConsumerMetadata;
import org.springframework.integration.kafka.support.KafkaConsumerContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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

        final ConsumerMetadata cm = appContext.getBean("consumerMetadata_default1", ConsumerMetadata.class);
        Assert.assertNotNull(cm);
    }
}
