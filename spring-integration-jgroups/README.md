Spring Integration Extension for JGroups
========================================

The JGroups extension for Spring Integration project includes *Inbound-* and *Outbound Channel Adapters* and JGroups channel configuration helper factory classes.

## JGroups cluster

With this extension you can easily start JGroups cluster/group, providing
cluster/group name and JGroups configuration.

### Example

~~~~~xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:jgroups="http://www.springframework.org/schema/integration/jgroups"
    xmlns:int="http://www.springframework.org/schema/integration"
    xsi:schemaLocation="http://www.springframework.org/schema/integration/jgroups https://www.springframework.org/schema/integration/jgroups/spring-intergration-jgroups.xsd
        http://www.springframework.org/schema/integration https://www.springframework.org/schema/integration/spring-integration-2.2.xsd
        http://www.springframework.org/schema/beans https://www.springframework.org/schema/beans/spring-beans.xsd">

    <jgroups:cluster name="mygroup">
        <jgroups:xml-configurator resource="classpath:udp.xml" />
    </jgroups:cluster>

</beans>
~~~~~

This example starts new JGroups cluster, named mygroup and with configuration loaded from udp.xml.

## Inbound channel adapter

Inbound channel adapter is used to receive messages sent to the group. It expects
user to provide reference to the JGroups cluster object. You can also supply
optional reference to custom JGroups header mapper.

### Example

~~~~~xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:jgroups="http://www.springframework.org/schema/integration/jgroups"
    xmlns:int="http://www.springframework.org/schema/integration"
    xsi:schemaLocation="http://www.springframework.org/schema/integration/jgroups https://www.springframework.org/schema/integration/jgroups/spring-intergration-jgroups.xsd
        http://www.springframework.org/schema/integration https://www.springframework.org/schema/integration/spring-integration-2.2.xsd
        http://www.springframework.org/schema/beans https://www.springframework.org/schema/beans/spring-beans.xsd">


    <jgroups:cluster name="mygroup">
        <jgroups:xml-configurator resource="classpath:udp.xml" />
    </jgroups:cluster>

    <jgroups:inbound-channel-adapter id="cluster-adapter" cluster="mygroup" channel="inbound"/>

    <int:channel id="inbound">
        <int:queue/>
    </int:channel>

</beans>
~~~~~

## Outbound channel adapter

Outbound channel adapter is used to send messages to a group. It expects
user to provide reference to the JGroups cluster object. You can also supply
optional reference to custom JGroups header mapper.

### Example:
~~~~~xml
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:jgroups="http://www.springframework.org/schema/integration/jgroups"
    xmlns:int="http://www.springframework.org/schema/integration"
    xsi:schemaLocation="http://www.springframework.org/schema/integration/jgroups https://www.springframework.org/schema/integration/jgroups/spring-intergration-jgroups.xsd
        http://www.springframework.org/schema/integration https://www.springframework.org/schema/integration/spring-integration-2.2.xsd
        http://www.springframework.org/schema/beans https://www.springframework.org/schema/beans/spring-beans.xsd">

    <jgroups:cluster name="mygroup">
        <jgroups:xml-configurator resource="classpath:udp.xml" />
    </jgroups:cluster>

    <int:poller fixed-rate="100" default="true"/>

    <int:channel id="inbound">
        <int:queue/>
    </int:channel>

    <jgroups:outbound-channel-adapter id="cluster-adapter" cluster="mygroup" channel="inbound"/>

</beans>
~~~~~

## Build

For build instructions visit [Spring Integration on GitHub](https://github.com/SpringSource/spring-integration).