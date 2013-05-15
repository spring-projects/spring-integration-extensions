Spring Integration Mqtt Adapters
=================================================

`inbound` and `outbound` channel adapters are provided for [MQ Telemetry Transport (MQTT)][]. The current implementation uses the [Eclipse Paho][] client.

Example configurations...

	<int-mqtt:message-driven-channel-adapter id="twoTopicsAdapter"
		client-id="foo"
		url="tcp://localhost:1883"
		topics="bar, baz"
		channel="out" />

	<int-mqtt:outbound-channel-adapter id="withDefaultConverter"
			client-id="foo"
			url="tcp://localhost:1883"
			default-qos="1"
			default-retained="true"
			default-topic="bar"
			channel="target" />


Spring integration messages sent to the outbound adapter can have headers `mqtt_topic, mqtt_qos, mqtt_retained` which will override the defaults configured on the adapter.

Inbound messages will have headers 

    mqtt_topic       - the topic from which the message was received
    mqtt_duplicate   - true if the message is a duplicate
    mqtt_qos         - the quality of service



Both adapters use a `MqttPahoClientFactory` to get a client instance; the same factory also provides connection options from configured properties (such as user/password). The client factory bean (`DefaultMqttPahoClientFactory`) is provided to the adapter using the `client-factory` attribute. When not provided, a default factory instance is used.


Currently tested with the RabbitMQ MQTT plugin.


##Note:

Currently, the Paho java client is not mavenized; there is an [open paho bug][] to resolve this. In the meantime, you can manually add the jar to your maven repo:

    mvn install:install-file -DgroupId=org.eclipse.paho -DartifactId=MQTT-Java -Dversion=3.0 -Dpackaging=jar -Dfile=/path/to/org.eclipse.paho.client.mqttv3.jar 



Check out the [Spring Integration forums][] and the [spring-integration][spring-integration tag] tag
on [Stack Overflow][]. [Commercial support][] is available, too.

## Related GitHub projects

* [Spring Integration][]
* [Spring Integration Samples][]
* [Spring Integration Templates][]
* [Spring Integration Dsl Groovy][]
* [Spring Integration Dsl Scala][]
* [Spring Integration Pattern Catalog][]

For more information, please also don't forget to visit the [Spring Integration][] website.

## Eclipse Paho

* [Eclipse Paho][]

[Spring Integration]: https://github.com/SpringSource/spring-integration
[Commercial support]: http://springsource.com/support/springsupport
[Spring Integration forums]: http://forum.springsource.org/forumdisplay.php?42-Integration
[spring-integration tag]: http://stackoverflow.com/questions/tagged/spring-integration
[Spring Integration Samples]: https://github.com/SpringSource/spring-integration-samples
[Spring Integration Templates]: https://github.com/SpringSource/spring-integration-templates/tree/master/si-sts-templates
[Spring Integration Dsl Groovy]: https://github.com/SpringSource/spring-integration-dsl-groovy
[Spring Integration Dsl Scala]: https://github.com/SpringSource/spring-integration-dsl-scala
[Spring Integration Pattern Catalog]: https://github.com/SpringSource/spring-integration-pattern-catalog
[Stack Overflow]: http://stackoverflow.com/faq
[Eclipse Paho]: http://www.eclipse.org/paho/
[open paho bug]: https://bugs.eclipse.org/bugs/show_bug.cgi?id=382471
[MQ Telemetry Transport (MQTT)]: http://mqtt.org/