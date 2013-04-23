package org.springframework.integration.kafka.config.xml;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.endpoint.PollingConsumer;
import org.springframework.integration.kafka.outbound.KafkaProducerMessageHandler;
import org.springframework.integration.kafka.support.KafkaProducerContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class KafkaOutboundAdapterParserTests {

    @Autowired
    private ApplicationContext appContext;

    @Test
    public void testOutboundAdapterConfiguration(){
       final PollingConsumer pollingConsumer = appContext.getBean("kafkaOutboundChannelAdapter", PollingConsumer.class);
       final KafkaProducerMessageHandler messageHandler = appContext.getBean(KafkaProducerMessageHandler.class);
       Assert.assertNotNull(pollingConsumer);
       Assert.assertNotNull(messageHandler);
       final KafkaProducerContext producerContext = messageHandler.getKafkaProducerContext();
       Assert.assertNotNull(producerContext);
       Assert.assertEquals(producerContext.getTopicsConfiguration().size(), 2);
    }
}
