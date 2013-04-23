package org.springframework.integration.kafka.support;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.serializer.DefaultEncoder;
import kafka.serializer.StringEncoder;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.integration.Message;
import org.springframework.integration.kafka.serializer.avro.AvroBackedKafkaEncoder;
import org.springframework.integration.kafka.test.utils.NonSerializableTestKey;
import org.springframework.integration.kafka.test.utils.NonSerializableTestPayload;
import org.springframework.integration.kafka.test.utils.TestKey;
import org.springframework.integration.kafka.test.utils.TestPayload;
import org.springframework.integration.support.MessageBuilder;

import java.io.ByteArrayInputStream;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;

/**
 * @author Soby Chacko
 */
public class TopicConfigurationTests {

    @Test
    @SuppressWarnings("unchecked")
    public void testSendMessageWithNonDefaultKeyAndValueEncoders() throws Exception {
        final TopicMetadata<String, String> topicMetadata = new TopicMetadata<String, String>("test");
        topicMetadata.setValueEncoder(new StringEncoder(null));
        topicMetadata.setKeyEncoder(new StringEncoder(null));
        topicMetadata.setKeyClassType(String.class);
        topicMetadata.setValueClassType(String.class);
        final Producer<String, String> producer = Mockito.mock(Producer.class);

        final TopicConfiguration<String, String> configuration = new TopicConfiguration<String, String>(topicMetadata, producer);

        final Message<String> message = MessageBuilder.withPayload("test message").
                                           setHeader("messageKey", "key")
                                           .setHeader("topic", "test").build();

        configuration.send(message);

        Mockito.verify(producer, Mockito.times(1)).send(Mockito.any(KeyedMessage.class));

        ArgumentCaptor<KeyedMessage> argument = ArgumentCaptor.forClass(KeyedMessage.class);
        Mockito.verify(producer).send(argument.capture());

        KeyedMessage capturedKeyMessage = argument.getValue();

        Assert.assertEquals(capturedKeyMessage.key(), "key");
        Assert.assertEquals(capturedKeyMessage.message(), "test message");
        Assert.assertEquals(capturedKeyMessage.topic(), "test");
    }

    /**
     * User does not set an explicit key/value encoder, but send a serializable object for both key/value
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testSendMessageWithDefaultKeyAndValueEncodersAndCustomSerializableKeyAndPayloadObject() throws Exception {
        final TopicMetadata<byte[], byte[]> topicMetadata = new TopicMetadata<byte[], byte[]>("test");
        topicMetadata.setValueEncoder(new DefaultEncoder(null));
        topicMetadata.setKeyEncoder(new DefaultEncoder(null));
        final Producer<byte[], byte[]> producer = Mockito.mock(Producer.class);

        final TopicConfiguration<byte[], byte[]> configuration = new TopicConfiguration<byte[], byte[]>(topicMetadata, producer);

        final Message<TestPayload> message = MessageBuilder.withPayload(new TestPayload("part1", "part2")).
                                           setHeader("messageKey", new TestKey("compositePart1", "compositePart2"))
                                           .setHeader("topic", "test").build();

        configuration.send(message);

        Mockito.verify(producer, Mockito.times(1)).send(Mockito.any(KeyedMessage.class));

        ArgumentCaptor<KeyedMessage> argument = ArgumentCaptor.forClass(KeyedMessage.class);
        Mockito.verify(producer).send(argument.capture());

        KeyedMessage capturedKeyMessage = argument.getValue();

        final byte[] keyBytes = (byte[])capturedKeyMessage.key();

        ByteArrayInputStream keyInputStream = new ByteArrayInputStream (keyBytes);
        ObjectInputStream keyObjectInputStream = new ObjectInputStream (keyInputStream);
        Object keyObj = keyObjectInputStream.readObject();

        final TestKey tk = (TestKey)keyObj;

        Assert.assertEquals(tk.getKeyPart1(), "compositePart1");
        Assert.assertEquals(tk.getKeyPart2(), "compositePart2");

        final byte[] messageBytes = (byte[])capturedKeyMessage.message();

        ByteArrayInputStream messageInputStream = new ByteArrayInputStream (messageBytes);
        ObjectInputStream messageObjectInputStream = new ObjectInputStream (messageInputStream);
        Object messageObj = messageObjectInputStream.readObject();

        final TestPayload tp = (TestPayload)messageObj;

        Assert.assertEquals(tp.getPart1(), "part1");
        Assert.assertEquals(tp.getPart2(), "part2");

        Assert.assertEquals(capturedKeyMessage.topic(), "test");
    }

    /**
     * User does not set an explicit key encoder, but a value encoder, and sends the corresponding data
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testSendMessageWithDefaultKeyEncoderAndNonDefaultValueEncoderAndCorrespondingData() throws Exception {
        final TopicMetadata<byte[], TestPayload> topicMetadata = new TopicMetadata<byte[], TestPayload>("test");
        final AvroBackedKafkaEncoder<TestPayload> encoder = new AvroBackedKafkaEncoder<TestPayload>(TestPayload.class);
        topicMetadata.setValueEncoder(encoder);
        topicMetadata.setKeyEncoder(new DefaultEncoder(null));
        topicMetadata.setValueClassType(TestPayload.class);
        final Producer<byte[], TestPayload> producer = Mockito.mock(Producer.class);

        final TopicConfiguration<byte[], TestPayload> configuration = new TopicConfiguration<byte[], TestPayload>(topicMetadata, producer);
        final TestPayload tp = new TestPayload("part1", "part2");
        final Message<TestPayload> message = MessageBuilder.withPayload(tp).
                                           setHeader("messageKey", "key")
                                           .setHeader("topic", "test").build();

        configuration.send(message);

        Mockito.verify(producer, Mockito.times(1)).send(Mockito.any(KeyedMessage.class));

        ArgumentCaptor<KeyedMessage> argument = ArgumentCaptor.forClass(KeyedMessage.class);
        Mockito.verify(producer).send(argument.capture());

        KeyedMessage capturedKeyMessage = argument.getValue();

        final byte[] keyBytes = (byte[])capturedKeyMessage.key();

        ByteArrayInputStream keyInputStream = new ByteArrayInputStream (keyBytes);
        ObjectInputStream keyObjectInputStream = new ObjectInputStream (keyInputStream);
        Object keyObj = keyObjectInputStream.readObject();

        Assert.assertEquals("key", keyObj);
        Assert.assertEquals(capturedKeyMessage.message(), tp);

        Assert.assertEquals(capturedKeyMessage.topic(), "test");
    }

    /**
     * User does set an explicit key encoder, but not a value encoder, and sends the corresponding data
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testSendMessageWithNonDefaultKeyEncoderAndDefaultValueEncoderAndCorrespondingData() throws Exception {
        final TopicMetadata<TestKey, byte[]> topicMetadata = new TopicMetadata<TestKey, byte[]>("test");
        final AvroBackedKafkaEncoder<TestKey> encoder = new AvroBackedKafkaEncoder<TestKey>(TestKey.class);
        topicMetadata.setKeyEncoder(encoder);
        topicMetadata.setValueEncoder(new DefaultEncoder(null));
        topicMetadata.setKeyClassType(TestKey.class);
        final Producer<TestKey, byte[]> producer = Mockito.mock(Producer.class);

        final TopicConfiguration<TestKey, byte[]> configuration = new TopicConfiguration<TestKey, byte[]>(topicMetadata, producer);
        final TestKey tk = new TestKey("part1", "part2");
        final Message<String> message = MessageBuilder.withPayload("test message").
                                           setHeader("messageKey", tk)
                                           .setHeader("topic", "test").build();

        configuration.send(message);

        Mockito.verify(producer, Mockito.times(1)).send(Mockito.any(KeyedMessage.class));

        ArgumentCaptor<KeyedMessage> argument = ArgumentCaptor.forClass(KeyedMessage.class);
        Mockito.verify(producer).send(argument.capture());

        KeyedMessage capturedKeyMessage = argument.getValue();

        Assert.assertEquals(capturedKeyMessage.key(), tk);

        final byte[] payloadBytes = (byte[])capturedKeyMessage.message();

        ByteArrayInputStream payloadBis = new ByteArrayInputStream (payloadBytes);
        ObjectInputStream payloadOis = new ObjectInputStream (payloadBis);
        Object payloadObj = payloadOis.readObject();

        Assert.assertEquals("test message", payloadObj);

        Assert.assertEquals(capturedKeyMessage.topic(), "test");
    }

    /**
     * User does not set an explicit key/value encoder, but send a serializable String key/value pair
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testSendMessageWithDefaultKeyAndValueEncodersAndStringKeyAndValue() throws Exception {
        final TopicMetadata<byte[], byte[]> topicMetadata = new TopicMetadata<byte[], byte[]>("test");
        topicMetadata.setValueEncoder(new DefaultEncoder(null));
        topicMetadata.setKeyEncoder(new DefaultEncoder(null));
        final Producer<byte[], byte[]> producer = Mockito.mock(Producer.class);

        final TopicConfiguration<byte[], byte[]> configuration = new TopicConfiguration<byte[], byte[]>(topicMetadata, producer);

        final Message<String> message = MessageBuilder.withPayload("test message").
                                           setHeader("messageKey", "key")
                                           .setHeader("topic", "test").build();

        configuration.send(message);

        Mockito.verify(producer, Mockito.times(1)).send(Mockito.any(KeyedMessage.class));

        ArgumentCaptor<KeyedMessage> argument = ArgumentCaptor.forClass(KeyedMessage.class);
        Mockito.verify(producer).send(argument.capture());

        KeyedMessage capturedKeyMessage = argument.getValue();
        final byte[] keyBytes = (byte[])capturedKeyMessage.key();

        ByteArrayInputStream keyBis = new ByteArrayInputStream (keyBytes);
        ObjectInputStream keyOis = new ObjectInputStream (keyBis);
        Object keyObj = keyOis.readObject();

        Assert.assertEquals("key", keyObj);

        final byte[] payloadBytes = (byte[])capturedKeyMessage.message();

        ByteArrayInputStream payloadBis = new ByteArrayInputStream (payloadBytes);
        ObjectInputStream payloadOis = new ObjectInputStream (payloadBis);
        Object payloadObj = payloadOis.readObject();

        Assert.assertEquals("test message", payloadObj);
        Assert.assertEquals(capturedKeyMessage.topic(), "test");
    }

    /**
     * User does not set an explicit key/value encoder, but send non-serializable object for both key/value
     */
    @Test(expected = NotSerializableException.class)
    @SuppressWarnings("unchecked")
    public void testSendMessageWithDefaultKeyAndValueEncodersButNonSerializableKeyAndValue() throws Exception {
        final TopicMetadata<byte[], byte[]> topicMetadata = new TopicMetadata<byte[], byte[]>("test");
        topicMetadata.setValueEncoder(new DefaultEncoder(null));
        topicMetadata.setKeyEncoder(new DefaultEncoder(null));
        final Producer<byte[], byte[]> producer = Mockito.mock(Producer.class);

        final TopicConfiguration<byte[], byte[]> configuration = new TopicConfiguration<byte[], byte[]>(topicMetadata, producer);

        final Message<NonSerializableTestPayload> message = MessageBuilder.withPayload(new NonSerializableTestPayload("part1", "part2")).
                                               setHeader("messageKey", new NonSerializableTestKey("compositePart1", "compositePart2"))
                                               .setHeader("topic", "test").build();
        configuration.send(message);
    }

    /**
     * User does not set an explicit key/value encoder, but send non-serializable key and serializable value
     */
    @Test(expected = NotSerializableException.class)
    @SuppressWarnings("unchecked")
    public void testSendMessageWithDefaultKeyAndValueEncodersButNonSerializableKeyAndSerializableValue() throws Exception {
        final TopicMetadata<byte[], byte[]> topicMetadata = new TopicMetadata<byte[], byte[]>("test");
        topicMetadata.setValueEncoder(new DefaultEncoder(null));
        topicMetadata.setKeyEncoder(new DefaultEncoder(null));
        final Producer<byte[], byte[]> producer = Mockito.mock(Producer.class);

        final TopicConfiguration<byte[], byte[]> configuration = new TopicConfiguration<byte[], byte[]>(topicMetadata, producer);

        final Message<TestPayload> message = MessageBuilder.withPayload(new TestPayload("part1", "part2")).
                                               setHeader("messageKey", new NonSerializableTestKey("compositePart1", "compositePart2"))
                                               .setHeader("topic", "test").build();
        configuration.send(message);
    }

    /**
     * User does not set an explicit key/value encoder, but send serializable key and non-serializable value
     */
    @Test(expected = NotSerializableException.class)
    @SuppressWarnings("unchecked")
    public void testSendMessageWithDefaultKeyAndValueEncodersButSerializableKeyAndNonSerializableValue() throws Exception {
        final TopicMetadata<byte[], byte[]> topicMetadata = new TopicMetadata<byte[], byte[]>("test");
        topicMetadata.setValueEncoder(new DefaultEncoder(null));
        topicMetadata.setKeyEncoder(new DefaultEncoder(null));
        final Producer<byte[], byte[]> producer = Mockito.mock(Producer.class);

        final TopicConfiguration<byte[], byte[]> configuration = new TopicConfiguration<byte[], byte[]>(topicMetadata, producer);

        final Message<NonSerializableTestPayload> message = MessageBuilder.withPayload(new NonSerializableTestPayload("part1", "part2")).
                                               setHeader("messageKey", new TestKey("compositePart1", "compositePart2"))
                                               .setHeader("topic", "test").build();
        configuration.send(message);
    }
}
