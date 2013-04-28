package org.springframework.integration.kafka.support;

import junit.framework.Assert;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.MessageAndMetadata;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Soby Chacko
 */
public class ConsumerConfigurationTests {

    @Test
    public void testReceiveMessageForSingleTopicFromSingleStream() {
        final ConsumerMetadata consumerMetadata = Mockito.mock(ConsumerMetadata.class);
        final ConsumerConnector consumerConnector = Mockito.mock(ConsumerConnector.class);

        final ConsumerConfiguration consumerConfiguration = new ConsumerConfiguration(consumerMetadata,
                consumerConnector);
        consumerConfiguration.setMaxMessages(1);

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

        final Map<String, List<Object>> messages = consumerConfiguration.receive();
        Assert.assertEquals(messages.size(), 1);
        Assert.assertEquals(messages.get("topic").size(), 1);
        Assert.assertEquals(messages.get("topic").get(0), "got message");

        Mockito.verify(stream, Mockito.times(1)).iterator();
        Mockito.verify(iterator, Mockito.times(1)).next();
        Mockito.verify(messageAndMetadata, Mockito.times(1)).message();
        Mockito.verify(messageAndMetadata, Mockito.times(1)).topic();
    }

    @Test
    public void testReceiveMessageForSingleTopicFromMultipleStreams() {
        final ConsumerMetadata consumerMetadata = Mockito.mock(ConsumerMetadata.class);
        final ConsumerConnector consumerConnector = Mockito.mock(ConsumerConnector.class);

        final ConsumerConfiguration consumerConfiguration = new ConsumerConfiguration(consumerMetadata,
                consumerConnector);
        consumerConfiguration.setMaxMessages(3);

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

        final Map<String, List<Object>> messages = consumerConfiguration.receive();
        Assert.assertEquals(messages.size(), 1);
        Assert.assertEquals(messages.get("topic").size(), 3);
        Assert.assertEquals(messages.get("topic").get(0), "got message");
        Assert.assertEquals(messages.get("topic").get(1), "got message");
        Assert.assertEquals(messages.get("topic").get(2), "got message");

        Mockito.verify(stream1, Mockito.atLeastOnce()).iterator();
        Mockito.verify(stream2, Mockito.atLeastOnce()).iterator();
        Mockito.verify(stream3, Mockito.atLeastOnce()).iterator();

        Mockito.verify(iterator, Mockito.atLeast(3)).next();
        Mockito.verify(messageAndMetadata, Mockito.times(3)).message();
        Mockito.verify(messageAndMetadata, Mockito.atLeastOnce()).topic();
    }

    @Test
    public void testReceiveMessageForMultipleTopicsFromMultipleStreams() {
        final ConsumerMetadata consumerMetadata = Mockito.mock(ConsumerMetadata.class);
        final ConsumerConnector consumerConnector = Mockito.mock(ConsumerConnector.class);

        final ConsumerConfiguration consumerConfiguration = new ConsumerConfiguration(consumerMetadata,
                consumerConnector);
        consumerConfiguration.setMaxMessages(9);

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
        final ConsumerIterator iterator1 = Mockito.mock(ConsumerIterator.class);
        final ConsumerIterator iterator2 = Mockito.mock(ConsumerIterator.class);
        final ConsumerIterator iterator3 = Mockito.mock(ConsumerIterator.class);

        Mockito.when(stream1.iterator()).thenReturn(iterator1);
        Mockito.when(stream2.iterator()).thenReturn(iterator2);
        Mockito.when(stream3.iterator()).thenReturn(iterator3);
        final MessageAndMetadata messageAndMetadata1 = Mockito.mock(MessageAndMetadata.class);
        final MessageAndMetadata messageAndMetadata2 = Mockito.mock(MessageAndMetadata.class);
        final MessageAndMetadata messageAndMetadata3 = Mockito.mock(MessageAndMetadata.class);

        Mockito.when(iterator1.next()).thenReturn(messageAndMetadata1);
        Mockito.when(iterator2.next()).thenReturn(messageAndMetadata2);
        Mockito.when(iterator3.next()).thenReturn(messageAndMetadata3);

        Mockito.when(messageAndMetadata1.message()).thenReturn("got message1");
        Mockito.when(messageAndMetadata1.topic()).thenReturn("topic1");
        Mockito.when(messageAndMetadata2.message()).thenReturn("got message2");
        Mockito.when(messageAndMetadata2.topic()).thenReturn("topic2");
        Mockito.when(messageAndMetadata3.message()).thenReturn("got message3");
        Mockito.when(messageAndMetadata3.topic()).thenReturn("topic3");

        final Map<String, List<Object>> messages = consumerConfiguration.receive();

        int sum = 0;
        for (List<Object> l : messages.values()){
            sum += l.size();
        }
        Assert.assertEquals(sum, 9);
    }
}
