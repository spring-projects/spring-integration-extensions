package org.springframework.integration.kafka.support;

import junit.framework.Assert;
import kafka.javaapi.producer.Producer;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Soby Chacko
 */
public class ProducerFactoryBeanTests {

    @Test
    public void createProducerWithDefaultMetadata() throws Exception {
        final TopicMetadata topicMetadata = new TopicMetadata("test");
        final TopicMetadata tm = Mockito.spy(topicMetadata);
        final ProducerFactoryBean producerFactoryBean = new ProducerFactoryBean(tm, "localhost:9092");
        final Producer producer = producerFactoryBean.getObject();

        Assert.assertTrue(producer != null);

        Mockito.verify(tm, Mockito.times(1)).getPartitioner();
        Mockito.verify(tm, Mockito.times(1)).getCompressionCodec();
        Mockito.verify(tm, Mockito.times(1)).getKafkaEncoder();
        Mockito.verify(tm, Mockito.times(1)).getKafkaKeyEncoder();
    }
}
