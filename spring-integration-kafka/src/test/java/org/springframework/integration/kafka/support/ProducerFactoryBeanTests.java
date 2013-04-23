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
        final TopicMetadata<byte[], byte[]> topicMetadata = new TopicMetadata<byte[], byte[]>("test");
        final TopicMetadata<byte[], byte[]> tm = Mockito.spy(topicMetadata);
        final ProducerFactoryBean<byte[], byte[]> producerFactoryBean = new ProducerFactoryBean<byte[], byte[]>(tm, "localhost:9092");
        final Producer producer = producerFactoryBean.getObject();

        Assert.assertTrue(producer != null);

        Mockito.verify(tm, Mockito.times(1)).getPartitioner();
        Mockito.verify(tm, Mockito.times(1)).getCompressionCodec();
        Mockito.verify(tm, Mockito.times(1)).getValueEncoder();
        Mockito.verify(tm, Mockito.times(1)).getKeyEncoder();
    }
}
