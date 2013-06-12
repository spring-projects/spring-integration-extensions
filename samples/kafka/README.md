Spring Integration Kafka Sample
================================

This sample demonstrates the usage of the *[Spring Integration Kafka][]* adapters.

Running samples in eclipse:
---------------------------

1. Clone spring-integration-extensions git repo.
2. `cd ~/spring-integration-extensions/samples/kafka`
3. Run `./gradlew` to download all dependencies.
4. Run `./gradlew eclipse` to generate eclipse project.
5. Open Eclipse->File->import->General-Existing Projects into workspace
6. Enter ~/spring-integration-extensions/samples/kafka as Project root dir and click finish.
7. To run simple producer 
	* navigate to `org.springframework.integration.samples.kafka.outbound.SimpleProducer`
	* Rightclick select Run As Java Application.
8. To run simple consumer
	* navigate to `org.springframework.integration.samples.kafka.inbound.SimpleConsumer`
	* Rightclick select Run As Java Application.
