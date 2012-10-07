Spring Integration Splunk Adapter
=================================================

The SI adapter includes Outbound Channel Adapter and Inbound Channel Adapter.

Inbound channel adapter :
-----------------------------------------------------------------------------
Inbound channel adapter is used to get data out of Splunk and put into
Spring Integration's channel. There are 5 ways to get data out of Splunk:
* Blocking
* Non blocking
* Saved search
* Realtime
* Export


### Blocking search:
~~~~~xml
	<int-splunk:inbound-channel-adapter id="splunkInboundChannelAdapter"
		auto-startup="true" search="search spring:example"
		splunk-server-ref="splunkServer"
		channel="inputFromSplunk" mode="BLOCKING" earliestTime="-1d" latestTime="now" initEarliestTime="-1d">
		<int:poller fixed-rate="5" time-unit="SECONDS"/>
	</int-splunk:inbound-channel-adapter>
~~~~~

### Non blocking search:
~~~~~xml
	<int-splunk:inbound-channel-adapter id="splunkInboundChannelAdapter"
		auto-startup="true" search="search spring:example"
		splunk-server-ref="splunkServer"
		channel="inputFromSplunk" mode="NORMAL" earliestTime="-1d" latestTime="now" initEarliestTime="-1d">
		<int:poller fixed-rate="5" time-unit="SECONDS"/>
	</int-splunk:inbound-channel-adapter>
~~~~~

### Saved search:
~~~~~xml
	<int-splunk:inbound-channel-adapter id="splunkInboundChannelAdapter"
		auto-startup="true" savedSearch="test" splunk-server-ref="splunkServer"
		channel="inputFromSplunk" mode="SAVEDSEARCH" earliestTime="-1d" latestTime="now" initEarliestTime="-1d">
		<int:poller fixed-rate="5" time-unit="SECONDS"/>
	</int-splunk:inbound-channel-adapter>
~~~~~

### Realtime search:
~~~~~xml
	<int-splunk:inbound-channel-adapter id="splunkInboundChannelAdapter"
		auto-startup="true" search="search spring:example" splunk-server-ref="splunkServer" channel="inputFromSplunk"
		mode="REALTIME" earliestTime="-5s" latestTime="rt" initEarliestTime="-1d">
		<int:poller fixed-rate="5" time-unit="SECONDS"/>
	</int-splunk:inbound-channel-adapter>
~~~~~

### Export:
~~~~~xml
	<int-splunk:inbound-channel-adapter id="splunkInboundChannelAdapter"
		auto-startup="true" search="search spring:example" splunk-server-ref="splunkServer" channel="inputFromSplunk"
		mode="EXPORT" earliestTime="-5d" latestTime="now" initEarliestTime="-1d">
		<int:poller fixed-rate="5" time-unit="SECONDS"/>
	</int-splunk:inbound-channel-adapter>
~~~~~

Outbound channel adapter:
----------------------------------------------------------------------------------------------
Outbound channel adapter is used to put data into Splunk from
channels in Spring Integration. There are 3 kinds of method to put data
* REST(submit)
* stream
* tcp

### Submit:
~~~~~xml
	<int-splunk:outbound-channel-adapter
		id="splunkOutboundChannelAdapter" auto-startup="true" order="1"
		channel="outputToSplunk"
		splunk-server-ref="splunkServer" pool-server-connection="true"
		sourceType="spring-integration" source="example2" ingest="SUBMIT">
	</int-splunk:outbound-channel-adapter>

~~~~~

### Stream:
~~~~~xml
	<int-splunk:outbound-channel-adapter
		id="splunkOutboundChannelAdapter" auto-startup="true" order="1"
		channel="outputToSplunk" splunk-server-ref="splunkServer"
		ingest="STREAM">
	</int-splunk:outbound-channel-adapter>

~~~~~

### tcp
~~~~~xml
	<int-splunk:outbound-channel-adapter
		id="splunkOutboundChannelAdapter" auto-startup="true" order="1"
		channel="outputToSplunk" splunk-server-ref="splunkServer"
		ingest="TCP" tcpPort="9999">
	</int-splunk:outbound-channel-adapter>

~~~~~


Development
-----------------
### To build:

	./gradlew build

### To generate Eclipse metadata (.classpath and .project files), do the following:

	./gradlew eclipse

