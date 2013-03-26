package org.springframework.integration.kafka.outbound;

import kafka.api.TopicMetadata;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import kafka.producer.ProducerPool;
import kafka.producer.async.DefaultEventHandler;
import kafka.producer.async.EventHandler;
import kafka.serializer.Encoder;
import org.springframework.integration.Message;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.integration.kafka.support.KafkaProducerContext;
import scala.collection.mutable.HashMap;

import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: chackos
 * Date: 3/26/13
 * Time: 12:54 AM
 * To change this template use File | Settings | File Templates.
 */
public class KafkaProducerMessageHandler extends AbstractMessageHandler {

    private final KafkaProducerContext kafkaProducerContext;
    private Encoder kafkaEncoder;
    private String topic;

    public KafkaProducerMessageHandler(final KafkaProducerContext kafkaProducerContext) {
        this.kafkaProducerContext = kafkaProducerContext;
    }

    public Encoder getKafkaEncoder() {
        return kafkaEncoder;
    }

    public void setKafkaEncoder(Encoder kafkaEncoder) {
        this.kafkaEncoder = kafkaEncoder;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    @Override
    protected void handleMessageInternal(Message<?> message) throws Exception {
        final Properties props = new Properties();
        //props.put("zk.connect", kafkaProducerContext.getZkConnect());
        props.put("broker.list", kafkaProducerContext.getBrokerList());
        final ProducerConfig config = new ProducerConfig(props);

        final EventHandler eventHandler  = new DefaultEventHandler(config, null, kafkaEncoder,
                        kafkaEncoder, new ProducerPool(config), new HashMap<String, TopicMetadata>());

        final kafka.producer.Producer prod = new kafka.producer.Producer(config, eventHandler);

        final kafka.javaapi.producer.Producer producer =
                       new kafka.javaapi.producer.Producer<String, String>(prod);

        producer.send(new KeyedMessage<Integer, Object>(getTopic(), message.getPayload()));

    }
}
