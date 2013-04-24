Spring Integration Kafka Adapter
=================================================


Welcome to the *Spring Integration Kafka adapter*. Apache Kafka is a distributed publish-subscribe messaging system that is designed for handling terra bytes of high throughput
data at constant time. For more information on Kafka and its design goals, please see [Kafka main page](http://kafka.apache.org/)

Spring Integration Kafka adapters are built for Kafka 0.8 and since Kafka 0.8 is not backward compatible with any previous versions of Kafka, Spring Integration will not
support any Kafka versions prior to 0.8. As of this writing, Kafka 0.8 is still WIP.

Spring Integration Kafka project currently supports the two following components.

* Outbound Channel Adapter
* Inbound Channel Adapter

Outbound Channel Adapter:
--------------------------------------------

The Outbound channel adapter is used to send messages to Kafka. Messages are read from a Spring Integration channel. You can specify this channel in your application context and then wire
this in your application where you need to send messages to kafka. Following is a regular queue channel available with stock Spring Integration.

```xml
    <int:channel id="inputToKafka">
            <int:queue/>
    </int:channel>

Then you can send messages to the channel to send to Kafka. In the current version of the outbound adapter,
you have to specify a message key and the topic as header values and the message to send as the payload.
Here is an example.

```java
    final MessageChannel channel = ctx.getBean("inputToKafka", MessageChannel.class);

    channel.send(
            MessageBuilder.withPayload(payload).
                    setHeader("messageKey", "key")
                    .setHeader("topic", "test").build());

This would create a message with a payload. In addition to this, it also creates two header entries as key/value pairs - one for
the message key and another for the topic under this message is being sent to Kafka.Then this message will be
sent to the channel.

That is all the Java application code that you have to write to send messages to Kafka. The adapter takes care of everything
from that point onwards.

Here is how you would configure an outbound channel adapter:

```xml
    <int-kafka:outbound-channel-adapter id="kafkaOutboundChannelAdapter"
                                        kafka-producer-context-ref="kafkaProducerContext"
                                        auto-startup="false"
                                        channel="inputToKafka"
            >
        <int:poller fixed-delay="1000" time-unit="MILLISECONDS" receive-timeout="0" task-executor="taskExecutor"/>
    </int-kafka:outbound-channel-adapter>

The key aspect in this configuration is the producer-context-ref which points to a Kafka Producer context
that contains all the producer configuration for all the topics that this adapter is expected to handle.
More on this is given below. The channel that we defined earlier is configured with the adapter and therefore
any message sent to that channel will be handled by this adapter. You can also configure a poller depending on the
type of channel that you use. In this case, since we use a queue based channel we specify a poller in the configuration.
This poller will poll the queue using a given task executor. If no task executor is given, the default task executor will be used.
If no messages are available in the queue it will timeout immediately because of the receive-timeout configuration
and poll again with a delay of 1 second.

Producer context is at the heart of the kafka outbound adapter. Here is an example of how you may configure one.

```xml
    <int-kafka:producer-context id="kafkaProducerContext">
        <int-kafka:producer-configurations>
            <int-kafka:producer-configuration broker-list="localhost:9092"
                       key-class-type="java.lang.String"
                       value-class-type="java.lang.String"
                       topic="test1"
                       value-encoder="kafkaEncoder"
                       key-encoder="kafkaEncoder"
                       compression-codec="default"/>
            <int-kafka:producer-configuration broker-list="localhost:9092"
                       topic="test2"
                       compression-codec="default"
                       async="true"/>
        </int-kafka:producer-configurations>
    </int-kafka:producer-context>

There are a few things going on here. So, lets go one by one. First of all, producer context is simply holder of, as the name
indicates, a context for the Kafa producer. It contains one ore more producer configurations. Each producer configuration
ultimately generates a Kafka native producer from the configuration. Each producer configuration is per topic based right now.
If you go by the above example, there are two producers generated from this configuration - one for topic named
test1 and other for test2. Each producer can take the following:

    broker-list            list of comma separated brokers that this producer connects to
    topic                  topic name
    compression-codec      any compression to be used Supported compression codec are gzip and snappy. Anything else would
                           result in no compression
    value-encoder          serializer to be used for encoding messages.
    key-encoder            serializer to be used for encoding the partition key
    key-class-type         Type of the key class. This will be ignored if no key-encoder is provided
    value-class-type       The type of the value class. This will be ignored if no value-encoder is provided.
    partitioner            custom implementation of a Kafka Partitioner interface.
    async                  true/false - default is false. Setting this to true would make the Kafka producer to use
                           an async producer
    batch-num-messages     number of messages to batch at the producer. If async is false, then this has no effect.

The value-encoder and key-encoder are referring to other spring beans. They are essentially implementations of an
interface provided by Kafka, the Encoder interface. Similarly, partitioner also refers a Spring bean which implements
the Kafka Partitioner interface.

Here is an example of configuring an encoder.

```xml
    <bean id="kafkaEncoder" class="org.springframework.integration.kafka.serializer.avro.AvroBackedKafkaEncoder">
        <constructor-arg value="java.lang.String" />
    </bean>

Spring Integration Kafaka adapter provides Apache Avro backed encoders out of the box, as this is a popular choice
for serialization in the big data spectrum. If no encoders are specified as beans, the default encoders provided
by Kafka will be used. On that not, if the encoder is configured only for the message and not for the key, the same encoder
will be used for both. These are standard Kafka behaviors. Spring Integration Kafka adapter does simply enforce those.
When default encoders are used, there are two ways a message can be sent. Either, the sender of the message to the channel
can simply put byte arrays as message key and payload. Or, the key and value can be sent as Java Serializable object.
In the latter case, the Kafka adapter will automatically convert them to byte arrays before sending it to Kafka broker.
If the encoders are default and the objets sent are not serializalbe, then that would cause an error. By providing explicit encoders
it is totally up to the developer to configure how the objects are serialized. In that case, the objects may or may not implement
the Serializable interface.

Kafka provides a StringEncoder out of the box. It takes a Kafka specific VerifiableProperties object along with its
constructor that wraps a regular Java.util.Properties object. The StringEncoder is great when writing a direct Java client.
However, when using Spring Integration Kafka adapter, a wrapper class for this same StringEncoder is available which makes
using it from Spring a bit easier as you don't have to create any Kafka specific objects to create a StringEncoder. Rather, you can inject
any properties to it in the Spring way. Kafka StringEncoder looks at a property for encoding from the properties provided.
This same value can be injected as a property on the bean. Spring Integration provided StringEncoder is available
in the package org.springframework.integration.kafka.serializer.common.StringEncoder. The avro support for serialization is
also available in a package called avro under serializer.

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







