Spring Integration Kafka Adapter
=================================================


Welcome to the *Spring Integration Kafka adapter*. Apache Kafka is a distributed publish-subscribe messaging system that is designed for handling terra bytes of high throughput
data at constant time. For more information on Kafka and its design goals, please see [Kafka main page](http://kafka.apache.org/)

Spring Integration Kafka adapters are built for Kafka 0.8 and since Kafka 0.8 is not backward compatible with any previous versions of Kafka, Spring Integration will not
support any Kafka versions prior to 0.8. As of this writing, Kafka 0.8 is still WIP.

Spring Integration Kafka project currently supports the two following components.

* Inbound Channel Adapter
* Outbound Channel Adapter

Inbound Channel Adapter :
-------------------------------------------------

The Inbound channel adapter is used to consume messages from Kafka. These messages will be placed into a Spring Integration channel as Spring Integration specific Messages.

Here is how an Inbound channel adapter is configured:

```xml
	<int-kafka:inbound-channel-adapter id="kafkaInboundChannelAdapter"
           kafka-consumer-context-ref="consumerContext"
           auto-startup="false"
           channel="inputFromKafka">
        <int:poller fixed-delay="100" time-unit="MILLISECONDS" receive-timeout="5000" max-messages-per-poll="1000"/>
    </int-kafka:inbound-channel-adapter>
```

Inbound Kafka Adapter must specify a kafka-consumer-context-ref element and here is how it may be configured:

```xml
   <int-kafka:consumer-context id="consumerContext" kafka-broker-ref="kafkaBroker" kafka-decoder="kafkaDecoder"
               topic="mytest" streams="4"/>
```

Here is how a kafka-broker is configured for the consumer context:

```xml
    <int-kafka:broker id="kafkaBroker" zk-connect="localhost:2181" zk-connection-timeout="6000"
                    zk-session-timeout="6000"
                    zk-sync-time="2000" />
```

Kafka decoder is optional in the consumer context. However, it is highly recommended to provide a decoder as otherwise Kafka would default to its built in
deserizlier which is a no-op decoder and the consumer would receive raw byte arrays. Spring Integration Kafka adapter gives Apache Avro based data serialization components
out of the box. You can use any serialization component for this purpose. Here is how you would configure a kafka decoder that is Avro backed.

```xml
   <bean id="kafkaDecoder" class="org.springframework.integration.kafka.serializer.avro.AvroBackedKafkaDecoder">
           <constructor-arg type="java.lang.Class" value="java.lang.String" />
   </bean>
```
