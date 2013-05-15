Spring Integration Voldemort Adapter
====================================

The Voldemort extension for Spring Integration (SI) project includes inbound
and outbound channel adapters.

Inbound channel adapter:
-----------------------------------------------------------------------------
Inbound channel adapter is used to retrieve data out of Voldemort database
and transfer objects into Spring Integration's channel. Component expects
user to provide Voldemort store client, message converter and desired object's
key.

### Example:
~~~~~xml
<beans xmlns:int="http://www.springframework.org/schema/integration"
       xmlns:int-voldemort="http://www.springframework.org/schema/integration/voldemort"
       xsi:schemaLocation="http://www.springframework.org/schema/integration http://www.springframework.org/schema/integration/spring-integration.xsd
           http://www.springframework.org/schema/integration/voldemort http://www.springframework.org/schema/integration/voldemort/spring-integration-voldemort.xsd">
    ...
    <int-voldemort:inbound-channel-adapter id="voldemortInKey" channel="voldemortInboundChannel" search-key="lukasz"
                                           store-client="storeClient" message-converter="messageConverter"
                                           delete-after-poll="true" auto-startup="true">
        <int:poller fixed-rate="1000" />
    </int-voldemort:inbound-channel-adapter>
    ...
</beans>
~~~~~

For more implementation details please review documentation and integration
test cases.

Outbound channel adapter:
-----------------------------------------------------------------------------
Outbound channel adapter is used to insert data into Voldemort database
from Spring Integration's channel. Component expects user to provide
Voldemort store client and message converter.

### Example:
~~~~~xml
<beans xmlns:int-voldemort="http://www.springframework.org/schema/integration/voldemort"
       xsi:schemaLocation="http://www.springframework.org/schema/integration/voldemort http://www.springframework.org/schema/integration/voldemort/spring-integration-voldemort.xsd">
    ...
    <int-voldemort:outbound-channel-adapter id="voldemortOut" channel="voldemortOutboundChannel"
                                            store-client="storeClient" message-converter="messageConverter"
                                            persist-mode="PUT" order="1" auto-startup="true" />
    ...
</beans>
~~~~~

For more implementation details please review documentation and integration
test cases.

Build
-----------------------------------------------------------------------------
For build instructions visit [Spring Integration on GitHub](https://github.com/SpringSource/spring-integration).