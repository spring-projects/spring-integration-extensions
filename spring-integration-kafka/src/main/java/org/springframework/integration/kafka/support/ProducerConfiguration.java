package org.springframework.integration.kafka.support;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.serializer.DefaultEncoder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.springframework.integration.Message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * @author Soby Chacko
 */
public class ProducerConfiguration<K,V> {

    private Producer<K,V> producer;
    private ProducerMetadata<K,V> producerMetadata;

    public ProducerConfiguration(final ProducerMetadata<K, V> producerMetadata, final Producer<K, V> producer){
        this.producerMetadata = producerMetadata;
        this.producer = producer;
    }

    public ProducerMetadata<K, V> getProducerMetadata() {
        return producerMetadata;
    }

    public void send(final Message<?> message) throws Exception {
        final V v = getPayload(message);

        if (message.getHeaders().containsKey("messageKey")) {
            final K k = getKey(message);
            producer.send(new KeyedMessage<K, V>(producerMetadata.getTopic(), k, v));
        } else {
            producer.send(new KeyedMessage<K, V>(producerMetadata.getTopic(), v));
        }
    }

    @SuppressWarnings("unchecked")
    private V getPayload(final Message<?> message) throws Exception {
        if (producerMetadata.getValueEncoder().getClass().isAssignableFrom(DefaultEncoder.class)) {
            return (V) getByteStream(message.getPayload());
        } else if (message.getPayload().getClass().isAssignableFrom(producerMetadata.getValueClassType())) {
            return producerMetadata.getValueClassType().cast(message.getPayload());
        }
        throw new Exception("Message payload type is not matching with what is configured");
    }

    @SuppressWarnings("unchecked")
    private K getKey(final Message<?> message) throws Exception {
        final Object key = message.getHeaders().get("messageKey");
        if (producerMetadata.getKeyEncoder().getClass().isAssignableFrom(DefaultEncoder.class)) {
            return (K) getByteStream(key);
        } else {
            return message.getHeaders().get("messageKey", producerMetadata.getKeyClassType());
        }
    }

    private static boolean isRawByteArray(final Object obj){
        if (obj instanceof byte[]){
            return true;
        }
        return false;
    }

    private static byte[] getByteStream(final Object obj) throws IOException {
        if (isRawByteArray(obj)){
            return (byte[])obj;
        }
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }

    @Override
    public boolean equals(Object obj){
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
