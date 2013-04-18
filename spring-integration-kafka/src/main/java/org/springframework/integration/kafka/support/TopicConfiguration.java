package org.springframework.integration.kafka.support;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.serializer.DefaultEncoder;
import org.springframework.integration.Message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * @author Soby Chacko
 */
public class TopicConfiguration<K,V> {

    private Producer<K,V> producer;
    private TopicMetadata<K,V> topicMetadata;

    public TopicConfiguration(final TopicMetadata<K,V> topicMetadata){
        this.topicMetadata = topicMetadata;
    }

    public TopicMetadata<K, V> getTopicMetadata() {
        return topicMetadata;
    }

    public void setProducer(Producer<K, V> producer) {
        this.producer = producer;
    }

    public void send(final Message<?> message) throws Exception {
        final V v = getPayload(message);

        if (message.getHeaders().containsKey("messageKey")) {
            final K k = getKey(message);
            producer.send(new KeyedMessage<K, V>(topicMetadata.getTopic(), k, v));
        } else {
            producer.send(new KeyedMessage<K, V>(topicMetadata.getTopic(), v));
        }
    }

    @SuppressWarnings("unchecked")
    private V getPayload(final Message<?> message) throws Exception {
        if (topicMetadata.getKafkaEncoder().getClass().isAssignableFrom(DefaultEncoder.class)) {
            return (V) getByteStream(message.getPayload());
        } else if (message.getPayload().getClass().isAssignableFrom(topicMetadata.getValueClass())) {
            return topicMetadata.getValueClass().cast(message.getPayload());
        }
        throw new Exception("Message payload type is not matching with what is configured");
    }

    @SuppressWarnings("unchecked")
    private K getKey(final Message<?> message) throws Exception {
        final Object key = message.getHeaders().get("messageKey");
        if (topicMetadata.getKafkaKeyEncoder().getClass().isAssignableFrom(DefaultEncoder.class)) {
            return (K) getByteStream(key);
        } else {
            return message.getHeaders().get("messageKey", topicMetadata.getKeyClass());
        }
    }

    private byte[] getByteStream(final Object obj) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }
}
