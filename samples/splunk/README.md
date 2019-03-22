Spring Integration Splunk Sample
================================

This sample demonstrates the usage of the *[Spring Integration Splunk][]* adapters.

## Requirements

### Splunk Server

You need to install or have access to a [Splunk][] Server. You can download [Splunk][] from:

* https://www.splunk.com/download

#### Data Inputs

Depending on your Splunk Server installation, you may have to open up a TCP by adding an additional *data input*. E.g. the sample uses TCP port  **9999**.

For instructions please see:

* https://docs.splunk.com/Documentation/Splunk/latest/Data/Monitornetworkports

### Spring Integration Splunk dependencies

The sample application depends on the [Spring Integration Splunk][] support. As of Oct 1, 2012, the dependencies for *Spring Integration Splunk* are available through the [SpringSource Maven Repository][]:

* https://repo.springsource.org/simple/libs-snapshot-local/org/springframework/integration/spring-integration-splunk/0.5.0.BUILD-SNAPSHOT

However, if you prefer, you can manually build the *Spring Integration Splunk* project. In order order to do so, follow the following steps:

1. Change to folder `spring-integration-extensions/spring-integration-splunk`
2. Execute `./gradlew publish`

This should install the [Spring Integration Splunk] jar files to your local Maven repository. Please see the [Spring Integration Splunk][] project for further information.

## Generate the Eclipse project:

Now you can generate the Eclipse project for the provided sample:

1. Change to folder `samples/splunk`
2. Execute `./gradlew eclipse`

## Run the example applications

1. Update the [Splunk][] server info in `src/main/resources/org/springframework/integration/samples/splunk/SplunkCommon-context.xml`
2. Run the main classes.

You may want to run the *Outbound Channel Adapter* application first in order to push some data into [Splunk][]. After that, run the *Inbound Channel Adapter*  application to read the data.

### Outbound Channel Adapter

* **Submit**: SplunkOutboundChannelAdapterSubmitSample
* **Tcp**: SplunkOutboundChannelAdapterTcpSample
* **Stream**: SplunkOutboundChannelAdapterStreamSample

### Inbound Channel Adapter

* **Blocking search**: SplunkInboundChannelAdapterBlockingSample
* **Non blocking search**: SplunkInboundChannelAdapterNonBlockingSample
* **Realtime search**: SplunkInboundChannelAdapterRealtimeSample
* **Export search**: SplunkInboundChannelAdapterExportSample
* **Saved search**: SplunkInboundChannelAdapterSavedSample

[Splunk]: https://www.splunk.com/
[Spring Integration Splunk]: https://github.com/SpringSource/spring-integration-extensions/tree/master/spring-integration-splunk
[SpringSource Maven Repository]: https://repo.springsource.org/
