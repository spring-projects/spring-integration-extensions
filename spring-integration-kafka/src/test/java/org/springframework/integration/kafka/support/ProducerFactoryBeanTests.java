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
        final ProducerMetadata<byte[], byte[]> producerMetadata = new ProducerMetadata<byte[], byte[]>("test");
        final ProducerMetadata<byte[], byte[]> tm = Mockito.spy(producerMetadata);
        final ProducerFactoryBean<byte[], byte[]> producerFactoryBean = new ProducerFactoryBean<byte[], byte[]>(tm, "localhost:9092");
        final Producer producer = producerFactoryBean.getObject();

        Assert.assertTrue(producer != null);

        Mockito.verify(tm, Mockito.times(1)).getPartitioner();
        Mockito.verify(tm, Mockito.times(1)).getCompressionCodec();
        Mockito.verify(tm, Mockito.times(1)).getValueEncoder();
        Mockito.verify(tm, Mockito.times(1)).getKeyEncoder();
        Mockito.verify(tm, Mockito.times(1)).isAsync();
        Mockito.verify(tm, Mockito.times(0)).getBatchNumMessages();
    }

    @Test
    public void createProducerWithAsyncFeatures() throws Exception {
        final ProducerMetadata<byte[], byte[]> producerMetadata = new ProducerMetadata<byte[], byte[]>("test");
        producerMetadata.setAsync(true);
        producerMetadata.setBatchNumMessages("300");
        final ProducerMetadata<byte[], byte[]> tm = Mockito.spy(producerMetadata);
        final ProducerFactoryBean<byte[], byte[]> producerFactoryBean = new ProducerFactoryBean<byte[], byte[]>(tm, "localhost:9092");
        final Producer producer = producerFactoryBean.getObject();

        Assert.assertTrue(producer != null);

        Mockito.verify(tm, Mockito.times(1)).getPartitioner();
        Mockito.verify(tm, Mockito.times(1)).getCompressionCodec();
        Mockito.verify(tm, Mockito.times(1)).getValueEncoder();
        Mockito.verify(tm, Mockito.times(1)).getKeyEncoder();
        Mockito.verify(tm, Mockito.times(1)).isAsync();
        Mockito.verify(tm, Mockito.times(2)).getBatchNumMessages();
    }
}
