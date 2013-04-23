package org.springframework.integration.kafka.config.xml;

import junit.framework.Assert;
import kafka.javaapi.producer.Producer;
import kafka.serializer.Encoder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.kafka.support.KafkaProducerContext;
import org.springframework.integration.kafka.support.TopicConfiguration;
import org.springframework.integration.kafka.support.TopicMetadata;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class KafkaProducerContextParserTests {

    @Autowired
    private ApplicationContext appContext;

    @Test
    @SuppressWarnings("unchecked")
    public void testProducerContextConfiguration(){
        final KafkaProducerContext producerContext = appContext.getBean("producerContext", KafkaProducerContext.class);
        Assert.assertNotNull(producerContext);

        final Map<String, TopicConfiguration> topicConfigurations = producerContext.getTopicsConfiguration();
        Assert.assertEquals(topicConfigurations.size(), 2);

        final TopicConfiguration topicConfigurationTest1 = topicConfigurations.get("topicConfiguration_test1");
        Assert.assertNotNull(topicConfigurationTest1);
        final TopicMetadata topicMetadataTest1 = topicConfigurationTest1.getTopicMetadata();
        Assert.assertEquals(topicMetadataTest1.getTopic(), "test1");
        Assert.assertEquals(topicMetadataTest1.getCompressionCodec(), "0");
        Assert.assertEquals(topicMetadataTest1.getKeyClassType(), java.lang.String.class);
        Assert.assertEquals(topicMetadataTest1.getValueClassType(), java.lang.String.class);

        final Encoder valueEncoder = appContext.getBean("valueEncoder", Encoder.class);
        Assert.assertEquals(topicMetadataTest1.getValueEncoder(), valueEncoder);
        Assert.assertEquals(topicMetadataTest1.getKeyEncoder(), valueEncoder);

        final Producer producerTest1 = appContext.getBean("prodFactory_test1", Producer.class);
        Assert.assertEquals(topicConfigurationTest1, new TopicConfiguration(topicMetadataTest1, producerTest1));

        final TopicConfiguration topicConfigurationTest2 = topicConfigurations.get("topicConfiguration_" + "test2");
        Assert.assertNotNull(topicConfigurationTest2);
        final TopicMetadata topicMetadataTest2 = topicConfigurationTest2.getTopicMetadata();
        Assert.assertEquals(topicMetadataTest2.getTopic(), "test2");
        Assert.assertEquals(topicMetadataTest2.getCompressionCodec(), "0");

        final Producer producerTest2 = appContext.getBean("prodFactory_test2", Producer.class);
        Assert.assertEquals(topicConfigurationTest2, new TopicConfiguration(topicMetadataTest2, producerTest2));
    }
}
