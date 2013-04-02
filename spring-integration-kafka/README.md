Spring Integration Kafka Adapter
=================================================


Welcome to the *Spring Integration Kafka adapter*. Apache Kafka is a distributed publish-subscribe messaging system that is designed for handling terra bytes of high throughput
data at constant time. For more information on Kafka and its design goals, please see [Kafka main page](http://kafka.apache.org/)

Spring Integration Kafka adapters are built for Kafka 0.8 and since Kafka 0.8 is not backward compatible with any previous versions of Kafka, Spring Integration will not
support any Kafka versions prior to 0.8. As of this writing, Kafka 0.8 is still WIP.

Spring Integration Kafka project currently supports the two following components.

* Inbound Channel Adapter
* Outbound Channel Adapter

Inbound Channel Adapter:
--------------------------------------------

The Inbound channel adapter is used to consume messages from Kafka. These messages will be placed into a Spring Integration channel as Spring Integration specific Messages.

Here is how an inbound channel adapter is configured:

```xml
	<int-kafka:inbound-channel-adapter id="kafkaInboundChannelAdapter"
           kafka-consumer-context-ref="consumerContext"
           auto-startup="false"
           channel="inputFromKafka">
        <int:poller fixed-delay="100" time-unit="MILLISECONDS" receive-timeout="5000" max-messages-per-poll="1000"/>
    </int-kafka:inbound-channel-adapter>
```

Since this inbound channel adapter uses a Polling Channel under the hood, it must be configured with a Poller. By configuring
a correct combination of receive-timeout and max-messages-per-poll, this adapter can effectively function like a message driven endpoint, i.e, you constantly
receive data from Kafka without blocking the inbound adapter thread indefinitely. For example, in the above configuration,
the poller is configured to receive 1000 messages in a single polling. If it does not receive any data for 5 seconds,
it times out and restart again after 100 milliseconds.

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

Outbound Channel Adapter:
--------------------------------------------

The Outbound channel adapter is used to send messages to Kafka. Messages are read from a Spring Integration channel and the payload from these messages are sent to Kafka.

Here is how an outbound channel adapter is configured:

```xml
	<int-kafka:outbound-channel-adapter kafka-producer-context-ref="producerContext"
                                            auto-startup="false"
                                            channel="inputToKafka" kafka-encoder="kafkaEncoder" topic="mytest">
            <int:poller fixed-delay="10" time-unit="MILLISECONDS" receive-timeout="5000"/>
    </int-kafka:outbound-channel-adapter>
```

The usecases for which Kafka is used, you normally have a large amount of messages to send. For such use cases, it is recommended to use
a Queue channel as the source from which the outbound adapter reads the messages. In that way, you are not blocking the sending thread
until the send operation is completed. If you are using QueueChannel, then a poller must be configured along with it.
In the above configuration, the outbound adapter will poll the channel and receive all messages in the queue. If there are no messages,
it will wait there for 5 seconds and timeout. Then it is restarted again after 10 milliseconds.

In the same way a decoder is used in the inbound adapter, it is recommended to use an appropriate encoder for outbound adapter.
you can plug any kind of serialization mechanisms for doing this. Spring Ingegration Kafka adapter provides an Apache Avro backed
encoding mechanism. Here is how you would configure a kafka encoder:

```xml
    <bean id="kafkaEncoder" class="org.springframework.integration.kafka.serializer.avro.AvroBackedKafkaEncoder">
            <constructor-arg type="java.lang.Class" value="java.lang.String" />
    </bean>
```


