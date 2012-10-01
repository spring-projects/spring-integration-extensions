Development
===================================
### to genereate eclipse project:
The example application depends on spring-integration-splunk. You have to
build that project first.

1. change to "spring-integration-extensions/spring-integration-splunk" dir
2. ./gradlew publish

Then you can generate eclipse project for the examples 
3. change to "samples/splunk" dir
4. ./gradlew eclipse

Run example applications
=====================================
1. update Splunk server info in the "src/main/resources/org/springframework/integration/samples/splunk/SplunkCommon-context.xml"
2. run the main classes. 
You may run outbound channel adapter applications to push some data into Splunk.
After that, you can run inbound channel adapter applications to read the data.

	
### Outbound channel adapter

	Submit: SplunkOutboundChannelAdapterSubmitSample
	Tcp: SplunkOutboundChannelAdapterTcpSample
	Stream: SplunkOutboundChannelAdapterStreamSample
	
### Inbound channel adapter

	Blocking search: SplunkInboundChannelAdapterBlockingSample
	Non blocking search: SplunkInboundChannelAdapterNonBlockingSample
	Realtime search: SplunkInboundChannelAdapterRealtimeSample
	Export search: SplunkInboundChannelAdapterExportSample
	Saved search: SplunkInboundChannelAdapterSavedSample

	


	



