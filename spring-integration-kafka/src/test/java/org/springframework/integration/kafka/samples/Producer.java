package org.springframework.integration.kafka.samples;

import kafka.api.TopicMetadata;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import kafka.producer.ProducerPool;
import kafka.producer.async.DefaultEventHandler;
import kafka.producer.async.EventHandler;
import org.springframework.integration.kafka.outbound.NaivePartitioner;
import org.springframework.integration.kafka.serializer.avro.AvroBackedKafkaEncoder;
import scala.collection.mutable.HashMap;

import java.util.Properties;

public class Producer {

    public static void main(String[] args) {

        final Properties props = new Properties();
        props.put("zk.connect", "127.0.0.1:2181");
        //props.put("serializer.class", "org.springframework.integration.kafka.serializer.avro.AvroBackedKafkaEncoder");
        //props.put("serializer.class", "kafka.serializer.StringEncoder");

        props.put("broker.list", "localhost:9092");
        final ProducerConfig config = new ProducerConfig(props);

        AvroBackedKafkaEncoder encoder = new AvroBackedKafkaEncoder(java.lang.String.class);
        final EventHandler eventHandler  = new DefaultEventHandler(config, new NaivePartitioner(), encoder,
                encoder, new ProducerPool(config), new HashMap<String, TopicMetadata>());


        final kafka.producer.Producer prod = new kafka.producer.Producer(config, eventHandler);

//        final kafka.javaapi.producer.Producer producer =
//                       new kafka.javaapi.producer.Producer<String, String>(config);

        final kafka.javaapi.producer.Producer producer =
               new kafka.javaapi.producer.Producer<String, String>(prod);

        for(int i = 0; i < 500000; i++) {
            String messageStr = new String("Message_" + i);
            producer.send(new KeyedMessage<String, String>("mytest", messageStr));
        }

    }
}
