package org.springframework.integration.kafka.support;

import junit.framework.Assert;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.message.MessageAndMetadata;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.integration.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Soby Chacko
 */
public class KafkaConsumerContextTest {

    @Test
    public void testReceiveMessageForSingleTopicFromSingleStream() {
        final KafkaConsumerContext kafkaConsumerContext = new KafkaConsumerContext();
        final ListableBeanFactory beanFactory = Mockito.mock(ListableBeanFactory.class);
        final ConsumerConfiguration consumerConfiguration = Mockito.mock(ConsumerConfiguration.class);
        final Map<String, ConsumerConfiguration> map = new HashMap<String, ConsumerConfiguration>();
        map.put("name", consumerConfiguration);
        Mockito.when(beanFactory.getBeansOfType(ConsumerConfiguration.class)).thenReturn(map);
        kafkaConsumerContext.setBeanFactory(beanFactory);

        final KafkaStream stream = Mockito.mock(KafkaStream.class);
        final List<KafkaStream<byte[], byte[]>> streams = new ArrayList<KafkaStream<byte[], byte[]>>();
        streams.add(stream);
        final Map<String, List<KafkaStream<byte[], byte[]>>> messageStreams = new HashMap<String, List<KafkaStream<byte[], byte[]>>>();
        messageStreams.put("topic", streams);

        Mockito.when(consumerConfiguration.getConsumerMapWithMessageStreams()).thenReturn(messageStreams);
        final ConsumerIterator iterator = Mockito.mock(ConsumerIterator.class);
        Mockito.when(stream.iterator()).thenReturn(iterator);
        final MessageAndMetadata messageAndMetadata = Mockito.mock(MessageAndMetadata.class);
        Mockito.when(iterator.next()).thenReturn(messageAndMetadata);
        Mockito.when(messageAndMetadata.message()).thenReturn("got message");
        Mockito.when(messageAndMetadata.topic()).thenReturn("topic");

        final Message<Map<String, List<Object>>> messages = kafkaConsumerContext.receive(1);
        Assert.assertEquals(messages.getPayload().size(), 1);
        Assert.assertEquals(messages.getPayload().get("topic").size(), 1);
        Assert.assertEquals(messages.getPayload().get("topic").get(0), "got message");

        Mockito.verify(beanFactory, Mockito.times(1)).getBeansOfType(ConsumerConfiguration.class);
        Mockito.verify(consumerConfiguration, Mockito.times(1)).getConsumerMapWithMessageStreams();
        Mockito.verify(stream, Mockito.times(1)).iterator();
        Mockito.verify(iterator, Mockito.times(1)).next();
        Mockito.verify(messageAndMetadata, Mockito.times(1)).message();
        Mockito.verify(messageAndMetadata, Mockito.times(1)).topic();
    }

    @Test
    public void testReceiveMessageForSingleTopicFromMultipleStreams() {
        final KafkaConsumerContext kafkaConsumerContext = new KafkaConsumerContext();
        final ListableBeanFactory beanFactory = Mockito.mock(ListableBeanFactory.class);
        final ConsumerConfiguration consumerConfiguration = Mockito.mock(ConsumerConfiguration.class);
        final Map<String, ConsumerConfiguration> map = new HashMap<String, ConsumerConfiguration>();
        map.put("name", consumerConfiguration);
        Mockito.when(beanFactory.getBeansOfType(ConsumerConfiguration.class)).thenReturn(map);
        kafkaConsumerContext.setBeanFactory(beanFactory);

        final KafkaStream stream1 = Mockito.mock(KafkaStream.class);
        final KafkaStream stream2 = Mockito.mock(KafkaStream.class);
        final KafkaStream stream3 = Mockito.mock(KafkaStream.class);
        final List<KafkaStream<byte[], byte[]>> streams = new ArrayList<KafkaStream<byte[], byte[]>>();
        streams.add(stream1);
        streams.add(stream2);
        streams.add(stream3);
        final Map<String, List<KafkaStream<byte[], byte[]>>> messageStreams = new HashMap<String, List<KafkaStream<byte[], byte[]>>>();
        messageStreams.put("topic", streams);

        Mockito.when(consumerConfiguration.getConsumerMapWithMessageStreams()).thenReturn(messageStreams);
        final ConsumerIterator iterator = Mockito.mock(ConsumerIterator.class);
        Mockito.when(stream1.iterator()).thenReturn(iterator);
        Mockito.when(stream2.iterator()).thenReturn(iterator);
        Mockito.when(stream3.iterator()).thenReturn(iterator);
        final MessageAndMetadata messageAndMetadata = Mockito.mock(MessageAndMetadata.class);
        Mockito.when(iterator.next()).thenReturn(messageAndMetadata);
        Mockito.when(messageAndMetadata.message()).thenReturn("got message");
        Mockito.when(messageAndMetadata.topic()).thenReturn("topic");

        final Message<Map<String, List<Object>>> messages = kafkaConsumerContext.receive(1);
        Assert.assertEquals(messages.getPayload().size(), 1);
        Assert.assertEquals(messages.getPayload().get("topic").size(), 3);
        Assert.assertEquals(messages.getPayload().get("topic").get(0), "got message");
        Assert.assertEquals(messages.getPayload().get("topic").get(1), "got message");
        Assert.assertEquals(messages.getPayload().get("topic").get(2), "got message");

        Mockito.verify(beanFactory, Mockito.times(1)).getBeansOfType(ConsumerConfiguration.class);
        Mockito.verify(consumerConfiguration, Mockito.times(1)).getConsumerMapWithMessageStreams();
        Mockito.verify(stream1, Mockito.times(1)).iterator();
        Mockito.verify(stream2, Mockito.times(1)).iterator();
        Mockito.verify(stream3, Mockito.times(1)).iterator();

        Mockito.verify(iterator, Mockito.times(3)).next();
        Mockito.verify(messageAndMetadata, Mockito.times(3)).message();
        Mockito.verify(messageAndMetadata, Mockito.times(3)).topic();
    }

    @Test
    public void testReceiveMessageForMultipleTopicsFromMultipleStreams() {
        final KafkaConsumerContext kafkaConsumerContext = new KafkaConsumerContext();
        final ListableBeanFactory beanFactory = Mockito.mock(ListableBeanFactory.class);
        final ConsumerConfiguration consumerConfiguration = Mockito.mock(ConsumerConfiguration.class);
        final Map<String, ConsumerConfiguration> map = new HashMap<String, ConsumerConfiguration>();
        map.put("name", consumerConfiguration);
        Mockito.when(beanFactory.getBeansOfType(ConsumerConfiguration.class)).thenReturn(map);
        kafkaConsumerContext.setBeanFactory(beanFactory);

        final KafkaStream stream1 = Mockito.mock(KafkaStream.class);
        final KafkaStream stream2 = Mockito.mock(KafkaStream.class);
        final KafkaStream stream3 = Mockito.mock(KafkaStream.class);
        final List<KafkaStream<byte[], byte[]>> streams = new ArrayList<KafkaStream<byte[], byte[]>>();
        streams.add(stream1);
        streams.add(stream2);
        streams.add(stream3);
        final Map<String, List<KafkaStream<byte[], byte[]>>> messageStreams = new HashMap<String, List<KafkaStream<byte[], byte[]>>>();
        messageStreams.put("topic1", streams);
        messageStreams.put("topic2", streams);
        messageStreams.put("topic3", streams);

        Mockito.when(consumerConfiguration.getConsumerMapWithMessageStreams()).thenReturn(messageStreams);
        final ConsumerIterator iterator = Mockito.mock(ConsumerIterator.class);
        Mockito.when(stream1.iterator()).thenReturn(iterator);
        Mockito.when(stream2.iterator()).thenReturn(iterator);
        Mockito.when(stream3.iterator()).thenReturn(iterator);
        final MessageAndMetadata messageAndMetadata = Mockito.mock(MessageAndMetadata.class);
        Mockito.when(iterator.next()).thenReturn(messageAndMetadata);
        Mockito.when(messageAndMetadata.message()).thenReturn("got message");
        Mockito.when(messageAndMetadata.topic()).thenReturn("topic");

        final Message<Map<String, List<Object>>> messages = kafkaConsumerContext.receive(1);
        Assert.assertEquals(messages.getPayload().size(), 3);
        Assert.assertEquals(messages.getPayload().get("topic1").size(), 3);
        Assert.assertEquals(messages.getPayload().get("topic2").size(), 3);
        Assert.assertEquals(messages.getPayload().get("topic3").size(), 3);

        Assert.assertEquals(messages.getPayload().get("topic1").get(0), "got message");
        Assert.assertEquals(messages.getPayload().get("topic1").get(1), "got message");
        Assert.assertEquals(messages.getPayload().get("topic1").get(2), "got message");

        Assert.assertEquals(messages.getPayload().get("topic2").get(0), "got message");
        Assert.assertEquals(messages.getPayload().get("topic2").get(1), "got message");
        Assert.assertEquals(messages.getPayload().get("topic2").get(2), "got message");

        Assert.assertEquals(messages.getPayload().get("topic3").get(0), "got message");
        Assert.assertEquals(messages.getPayload().get("topic3").get(1), "got message");
        Assert.assertEquals(messages.getPayload().get("topic3").get(2), "got message");

//        Mockito.verify(beanFactory, Mockito.times(1)).getBeansOfType(ConsumerConfiguration.class);
//        Mockito.verify(consumerConfiguration, Mockito.times(1)).getConsumerMapWithMessageStreams();
//        Mockito.verify(stream1, Mockito.times(1)).iterator();
//        Mockito.verify(stream2, Mockito.times(1)).iterator();
//        Mockito.verify(stream3, Mockito.times(1)).iterator();
//
//        Mockito.verify(iterator, Mockito.times(3)).next();
//        Mockito.verify(messageAndMetadata, Mockito.times(3)).message();
//        Mockito.verify(messageAndMetadata, Mockito.times(3)).topic();
    }
}
