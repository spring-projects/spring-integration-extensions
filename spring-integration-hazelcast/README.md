SPRING INTEGRATION HAZELCAST SUPPORT
====================================

## HAZELCAST EVENT-DRIVEN INBOUND CHANNEL ADAPTER

Hazelcast provides distributed data structures such as

* com.hazelcast.core.IMap,
* com.hazelcast.core.MultiMap,
* com.hazelcast.core.IList,
* com.hazelcast.core.ISet,
* com.hazelcast.core.IQueue,
* com.hazelcast.core.ITopic,
* com.hazelcast.core.ReplicatedMap.

It also provides event listeners in order to listen to the modifications performed on these data structures.

* com.hazelcast.core.EntryListener<K, V>
* com.hazelcast.core.ItemListener<E>
* com.hazelcast.core.MessageListener<E>

Hazelcast Event-Driven Inbound Channel Adapter listens related cache events and sends event messages to defined channel. It supports both XML and JavaConfig driven configurations.

#### XML Driven Configuration :
```
   <int-hazelcast:inbound-channel-adapter channel="mapChannel"
					  cache="map"
					  cache-events="UPDATED, REMOVED"
					  cache-listening-policy="SINGLE" />
```
Basically, Hazelcast Event-Driven Inbound Channel Adapter requires following attributes :

* **channel :** Specifies channel which message is sent.
* **cache :** Specifies the distributed Object reference which is listened. It is mandatory attribute.
* **cache-events :** Specifies cache events which are listened. It is optional attribute and its default value is ADDED. Its supported values are as follows :

1. Supported cache event types for IMap and MultiMap : ADDED, REMOVED, UPDATED, EVICTED, EVICT_ALL and CLEAR_ALL.
2. Supported cache event types for ReplicatedMap : ADDED, REMOVED, UPDATED, EVICTED.
3. Supported cache event types for IList, ISet and IQueue : ADDED, REMOVED.
4. There is no need to cache event type definition for ITopic.

* **cache-listening-policy :** Specifies cache listening policy as SINGLE or ALL. It is optional attribute and its default value is SINGLE. Each Hazelcast inbound channel adapter listening same cache object with same cache-events attribute, can receive a single event message or all event messages. If it is ALL, all Hazelcast inbound channel adapters listening same cache object with same cache-events attribute, will receive same event messages. If it is SINGLE, they will receive unique event messages.

Sample namespace and schemaLocation definitions are as follows :
```
xmlns:int-hazelcast= “http://www.springframework.org/schema/integration/hazelcast”

xsi:schemaLocation="http://www.springframework.org/schema/integration/hazelcast
		  https://www.springframework.org/schema/integration/hazelcast/spring-integration-hazelcast.xsd”
```
Sample definitions are as follows :

**Distributed Map :**
```
<int:channel id="mapChannel"/>

<int-hazelcast:inbound-channel-adapter channel="mapChannel"
					          cache="map"
					          cache-events="UPDATED, REMOVED" />

<bean id="map" factory-bean="instance" factory-method="getMap">
	<constructor-arg value="map"/>
</bean>

<bean id="instance" class="com.hazelcast.core.Hazelcast"
			factory-method="newHazelcastInstance">
	<constructor-arg>
		<bean class="com.hazelcast.config.Config" />
	</constructor-arg>
</bean>
```

**Distributed MultiMap :**
```
<int:channel id="multiMapChannel"/>

<int-hazelcast:inbound-channel-adapter channel="multiMapChannel"
					          cache="multiMap"
					          cache-events="ADDED, REMOVED, CLEAR_ALL" />

<bean id="multiMap" factory-bean="instance" factory-method="getMultiMap">
	<constructor-arg value="multiMap"/>
</bean>
```

**Distributed List :**
```
<int:channel id="listChannel"/>

<int-hazelcast:inbound-channel-adapter  channel="listChannel"
					           cache="list"
					           cache-events="ADDED, REMOVED"
					           cache-listening-policy="ALL" />

<bean id="list" factory-bean="instance" factory-method="getList">
	<constructor-arg value="list"/>
</bean>
```

**Distributed Set :**
```
<int:channel id="setChannel"/>

<int-hazelcast:inbound-channel-adapter channel="setChannel" cache="set" />

<bean id="set" factory-bean="instance" factory-method="getSet">
	<constructor-arg value="set"/>
</bean>
```

**Distributed Queue :**
```
<int:channel id="queueChannel"/>

<int-hazelcast:inbound-channel-adapter  channel="queueChannel"
					           cache="queue"
					           cache-events="REMOVED"
					           cache-listening-policy="ALL" />

<bean id="queue" factory-bean="instance" factory-method="getQueue">
	<constructor-arg value="queue"/>
</bean>
```

**Distributed Topic :**
```
<int:channel id="topicChannel"/>

<int-hazelcast:inbound-channel-adapter channel="topicChannel" cache="topic" />

<bean id="topic" factory-bean="instance" factory-method="getTopic">
	<constructor-arg value="topic"/>
</bean>
```

**Replicated Map :**
```
<int:channel id="replicatedMapChannel"/>

<int-hazelcast:inbound-channel-adapter channel="replicatedMapChannel"
					          cache="replicatedMap"
					          cache-events="ADDED, UPDATED, REMOVED"
 					          cache-listening-policy="SINGLE"  />

<bean id="replicatedMap" factory-bean="instance" factory-method="getReplicatedMap">
	<constructor-arg value="replicatedMap"/>
</bean>
<bean id="instance" class="com.hazelcast.core.Hazelcast"
			factory-method="newHazelcastInstance">
	<constructor-arg>
		<bean class="com.hazelcast.config.Config" />
	</constructor-arg>
</bean>
```

#### JavaConfig Driven Configuration :

The following sample shows Distributed Map configuration. Same configuration can be used for other distributed data structures(IMap, MultiMap, ReplicatedMap, IList, ISet, IQueue and ITopic).
```
@Bean
public PollableChannel distributedMapChannel() {
	return new QueueChannel();
}

@Bean
public IMap<Integer, String> distributedMap() {
	return hazelcastInstance().getMap("Distributed_Map");
}

@Bean
public HazelcastInstance hazelcastInstance() {
	return Hazelcast.newHazelcastInstance();
}

@Bean
public HazelcastEventDrivenMessageProducer hazelcastEventDrivenMessageProducer() {
	final HazelcastEventDrivenMessageProducer producer = new HazelcastEventDrivenMessageProducer(distributedMap());
	producer.setOutputChannel(distributedMapChannel());
	producer.setCacheEventTypes("ADDED,REMOVED,UPDATED,CLEAR_ALL");
	producer.setCacheListeningPolicy(CacheListeningPolicyType.SINGLE);

	return producer;
}

```

**Reference :** https://docs.hazelcast.org/docs/latest/manual/html/distributed-data-structures.html


## HAZELCAST CONTINUOUS QUERY INBOUND CHANNEL ADAPTER

Hazelcast Continuous Query enables to listen to the modifications performed on specific map entries. Hazelcast Continuous Query Inbound Channel Adapter is an event-driven channel adapter and listens to related distributed map events in the light of defined predicate. It supports both XML and JavaConfig driven configurations.

#### XML Driven Configuration :
```
<int-hazelcast:cq-inbound-channel-adapter
				channel="cqMapChannel"
				cache="cqMap"
				cache-events="UPDATED, REMOVED"
				predicate="name=TestName AND surname=TestSurname"
				include-value="true"
				cache-listening-policy="SINGLE" />
```
Basically, it requires four attributes as follows :

* **channel :** Specifies channel which message is sent.
* **cache :** Specifies distributed Map reference which is listened. It is mandatory attribute.
* **cache-events :** Specifies cache events which are listened. It is optional attribute with ADDED default value. Supported values are ADDED, REMOVED, UPDATED, EVICTED, EVICT_ALL and CLEAR_ALL.
* **predicate :** Specifies predicate to listen to the modifications performed on specific map entries. It is mandatory attribute.
* **include-value :** Specifies including of value and oldValue in continuous query result. It is optional attribute with 'true' default value.
* **cache-listening-policy :** Specifies cache listening policy as SINGLE or ALL. It is optional attribute and its default value is SINGLE. Each Hazelcast CQ inbound channel adapter listening same cache object with same cache-events attribute, can receive a single event message or all event messages. If it is ALL, all Hazelcast CQ inbound channel adapters listening same cache object with same cache-events attribute, will receive same event messages. If it is SINGLE, they will receive unique event messages.

Sample definition is as follows :
```
<int:channel id="cqMapChannel"/>

<int-hazelcast:cq-inbound-channel-adapter
				channel="cqMapChannel"
				cache="cqMap"
				cache-events="UPDATED, REMOVED"
				predicate="name=TestName AND surname=TestSurname"
				include-value="true"
				cache-listening-policy="SINGLE"/>

<bean id="cqMap" factory-bean="instance" factory-method="getMap">
	<constructor-arg value="cqMap"/>
</bean>

<bean id="instance" class="com.hazelcast.core.Hazelcast"
			factory-method="newHazelcastInstance">
	<constructor-arg>
		<bean class="com.hazelcast.config.Config" />
	</constructor-arg>
</bean>
```

#### JavaConfig Driven Configuration :
```
@Bean
public PollableChannel cqDistributedMapChannel() {
	return new QueueChannel();
}

@Bean
public IMap<Integer, String> cqDistributedMap() {
	return hazelcastInstance().getMap("CQ_Distributed_Map");
}

@Bean
public HazelcastInstance hazelcastInstance() {
	return Hazelcast.newHazelcastInstance();
}

@Bean
public HazelcastContinuousQueryMessageProducer hazelcastContinuousQueryMessageProducer() {
	final HazelcastContinuousQueryMessageProducer producer =
		new HazelcastContinuousQueryMessageProducer(cqDistributedMap(), "surname=TestSurname");
	producer.setOutputChannel(cqDistributedMapChannel());
	producer.setCacheEventTypes("UPDATED");
	producer.setIncludeValue(false);

	return producer;
}

```
**Reference :** https://docs.hazelcast.org/docs/latest/manual/html/continuousquery.html

## HAZELCAST CLUSTER MONITOR INBOUND CHANNEL ADAPTER

Hazelcast Cluster Monitor enables to listen to the modifications performed on cluster. Hazelcast Cluster Monitor Inbound Channel Adapter is an event-driven channel adapter and listens to related Membership, Distributed Object, Migration, Lifecycle and Client events. It supports both XML and JavaConfig driven configurations.

#### XML Driven Configuration :
```
<int-hazelcast:cm-inbound-channel-adapter
				 channel="monitorChannel"
				 hazelcast-instance="instance"
				 monitor-types="MEMBERSHIP, DISTRIBUTED_OBJECT, MIGRATION, LIFECYCLE, CLIENT" />
```
Basically, it requires four attributes as follows :

* **channel :** Specifies channel which message is sent.
* **hazelcast-instance :** Specifies Hazelcast Instance reference to listen cluster events. It is mandatory attribute.
* **monitor-types :** Specifies monitor types which are listened. It is optional attribute with MEMBERSHIP default value. Supported values are MEMBERSHIP, DISTRIBUTED_OBJECT, MIGRATION, LIFECYCLE, CLIENT.

Sample definition is as follows :
```
<int:channel id="monitorChannel"/>

<int-hazelcast:cm-inbound-channel-adapter
				 channel="monitorChannel"
				 hazelcast-instance="instance"
				 monitor-types="MEMBERSHIP, DISTRIBUTED_OBJECT" />

<bean id="instance" class="com.hazelcast.core.Hazelcast"
			factory-method="newHazelcastInstance">
	<constructor-arg>
		<bean class="com.hazelcast.config.Config" />
	</constructor-arg>
</bean>
```

#### JavaConfig Driven Configuration :
```
@Bean
public PollableChannel cmonChannel() {
	return new QueueChannel();
}

@Bean
public HazelcastInstance hazelcastInstance() {
	return Hazelcast.newHazelcastInstance();
}

@Bean
public HazelcastClusterMonitorMessageProducer hazelcastClusterMonitorMessageProducer() {
	final HazelcastClusterMonitorMessageProducer producer = new HazelcastClusterMonitorMessageProducer(hazelcastInstance());
	producer.setOutputChannel(cmonChannel());
	producer.setMonitorEventTypes("DISTRIBUTED_OBJECT");

	return producer;
}
```
**Reference :** https://docs.hazelcast.org/docs/latest/manual/html/distributedevents.html


## HAZELCAST DISTRIBUTED-SQL INBOUND CHANNEL ADAPTER

Hazelcast allows to run distributed queries on the distributed map. Hazelcast Distributed SQL Inbound Channel Adapter is a poller-driven inbound channel adapter. It runs defined distributed-sql and returns results in the light of iteration type. It supports both XML and JavaConfig driven configurations.

#### XML Driven Configuration :
```
<int-hazelcast:ds-inbound-channel-adapter
			     channel="dsMapChannel"
			     cache="dsMap"
 			     iteration-type="ENTRY"
                 distributed-sql="active=false OR age >= 25 OR name = 'TestName'">
	<int:poller fixed-delay="100"/>
</int-hazelcast:ds-inbound-channel-adapter>
```
Basically, it requires a poller and four attributes such as

* **channel :** Specifies channel which message is sent. It is mandatory attribute.
* **cache :** Specifies distributed Map reference which is queried. It is mandatory attribute.
* **iteration-type :** Specifies result type. Distributed SQL can be run on EntrySet, KeySet, LocalKeySet or Values. It is optional attribute with VALUE default value. Supported values are ENTRY, KEY, LOCAL_KEY and VALUE.
* **distributed-sql :** Specifies where clause of sql statement. It is mandatory attribute.

Sample definition is as follows :
```
<int:channel id="dsMapChannel"/>

<int-hazelcast:ds-inbound-channel-adapter
			channel="dsMapChannel"
			cache="dsMap"
			iteration-type="ENTRY"
			distributed-sql="active=false OR age >= 25 OR name = 'TestName'">
	<int:poller fixed-delay="100"/>
</int-hazelcast:ds-inbound-channel-adapter>

<bean id="dsMap" factory-bean="instance" factory-method="getMap">
	<constructor-arg value="dsMap"/>
</bean>

<bean id="instance" class="com.hazelcast.core.Hazelcast"
			factory-method="newHazelcastInstance">
	<constructor-arg>
		<bean class="com.hazelcast.config.Config" />
	</constructor-arg>
</bean>
```

#### JavaConfig Driven Configuration :
```
@Bean
public PollableChannel dsDistributedMapChannel() {
	return new QueueChannel();
}

@Bean
public IMap<Integer, String> dsDistributedMap() {
	return hazelcastInstance().getMap("DS_Distributed_Map");
}

@Bean
public HazelcastInstance hazelcastInstance() {
	return Hazelcast.newHazelcastInstance();
}

@Bean
@InboundChannelAdapter(value = "dsDistributedMapChannel", poller = @Poller(maxMessagesPerPoll = "1"))
public HazelcastDistributedSQLMessageSource hazelcastDistributedSQLMessageSource() {
	final HazelcastDistributedSQLMessageSource messageSource =
		new HazelcastDistributedSQLMessageSource(dsDistributedMap(),
			"name='TestName' AND surname='TestSurname'");
	messageSource.setIterationType(DistributedSQLIterationType.ENTRY);

	return messageSource;
}
```
**Reference :** https://docs.hazelcast.org/docs/latest/manual/html/distributedquery.html


## HAZELCAST OUTBOUND CHANNEL ADAPTER

Hazelcast Outbound Channel Adapter listens its defined channel and writes incoming messages to related distributed cache. It expects one of cache, cache-expression or HazelcastHeaders.CACHE_NAME for distributed object definition. Supported Distributed Objects : IMap, MultiMap, ReplicatedMap, IList, ISet, IQueue and ITopic. It supports both XML and JavaConfig driven configurations.

#### XML Driven Configuration :
```
<int-hazelcast:outbound-channel-adapter channel="mapChannel" cache="distributedMap" key-expression="payload.id" extract-payload="false"/>
```
Basically, it requires the following attributes :

**channel :** Specifies channel which message is sent.
* **cache :** Specifies distributed object reference. It is optional attribute.
* **cache-expression :** Specifies distributed object via Spring Expression Language(SpEL). It is optional attribute.
* **key-expression :** Specifies key of K,V pair via Spring Expression Language(SpEL). It is optional attribute and required for just IMap, MultiMap and ReplicatedMap distributed data structures.
* **extract-payload :** Specifies whole message or just payload to send. It is optional attribute with  **true** default value. If it is true, just payload will be written to distributed object. Otherwise, whole message will be written by covering both message header and payload.

**Sample Definitions :**
```
<int-hazelcast:outbound-channel-adapter channel="mapChannel" cache="distributedMap" key-expression="payload.id" extract-payload="false"/>
```
**OR**
```
<int-hazelcast:outbound-channel-adapter channel="mapChannel" cache-expression="headers['CACHE_HEADER']" key-expression="payload.key" extract-payload="true"/>
```
By setting distributed object name in the header, messages can be written to different distributed objects via same channel.

**OR**

If **cache** or **cache-expression** attributes are not defined, HazelcastHeaders.CACHE_NAME has to be set in Message.

#### JavaConfig Driven Configuration :
```
@Bean
public MessageChannel distributedMapChannel() {
	return new DirectChannel();
}

@Bean
public IMap<Integer, String> distributedMap() {
	return hzInstance().getMap("Distributed_Map");
}

@Bean
public HazelcastInstance hzInstance() {
	return Hazelcast.newHazelcastInstance();
}

@Bean
@ServiceActivator(inputChannel = "distributedMapChannel")
public HazelcastCacheWritingMessageHandler hazelcastCacheWritingMessageHandler() {
	final HazelcastCacheWritingMessageHandler handler = new HazelcastCacheWritingMessageHandler();
	handler.setDistributedObject(distributedMap());
	handler.setKeyExpression(new SpelExpressionParser().parseExpression("payload.id"));
	handler.setExtractPayload(true);

	return handler;
}

```

## HAZELCAST LEADER ELECTION

If you need to elect a leader (e.g. for highly available message consumer where only one node should receive messages)
you just need to create a `LeaderInitiator`. Example:

```java
@Bean
public HazelcastInstance hazelcastInstance() {
    return Hazelcast.newHazelcastInstance();
}

@Bean
public LeaderInitiator initiator() {
    return new LeaderInitiator(hazelcastInstance());
}
```

Then when a node is elected leader it will send `OnGrantedEvent` to all application listeners. See
the [Spring Integration User Guide](https://docs.spring.io/spring-integration/reference/html/#endpoint-roles)
for more information on how to use those events to control messaging endpoints.

## HAZELCAST MESSAGE STORE

For distributed messaging state management, for example for persistent `QueueChannel` or tracking `Aggregator` message groups, the `HazelcastMessageStore` implementation is provided:
```java
@Bean
public HazelcastInstance hazelcastInstance() {
    return Hazelcast.newHazelcastInstance();
}

@Bean
public MessageGroupStore messageStore() {
    return new HazelcastMessageStore(hazelcastInstance());
}
```

By default the `SPRING_INTEGRATION_MESSAGE_STORE` `IMap` is used to store messages and groups key/value manner.
Any custom `IMap` can be provided to the `HazelcastMessageStore`.
See [Spring Integration User Guide](https://docs.spring.io/spring-integration/reference/html/system-management-chapter.html#message-store) for more information about `MessageStore`.

## HAZELCAST METADATA STORE
An implementation of a [MetadataStore](https://docs.spring.io/spring-integration/reference/html/system-management-chapter.html#metadata-store) is available using a backing Hazelcast `IMap`

You can provide your own implementation of an `IMap` or rely on the default map created with name `SPRING_INTEGRATION_METADATA_STORE`.

```java
@Bean
public HazelcastInstance hazelcastInstance() {
    return Hazelcast.newHazelcastInstance();
}

@Bean
public MetadataStore metadataStore() {
    return new HazelcastMetadataStore(hazelcastInstance());
}
```

The `HazelcastMetadataStore` implements `ListenableMetadataStore` which allows you to register your own listeners of type `MetadataStoreListener` to listen for events via `addListener(MetadataStoreListener callback)`

See [Spring Integration User Guide](https://docs.spring.io/spring-integration/reference/html/system-management-chapter.html#metadatastore-listener) for more information about the `MetadataStoreListener` interface. 

## HAZELCAST LOCK REGISTRY
An implementation of a `LockRegistry` is available using a backing Hazelcast distributed `ILock` support: 

```java
@Bean
public HazelcastInstance hazelcastInstance() {
    return Hazelcast.newHazelcastInstance();
}

@Bean
public LockRegistry lockRegistry() {
    return new HazelcastLockRegistry(hazelcastInstance());
}
```

When used with a shared `MessageGroupStore` (e.g. `Aggregator` store management), the `HazelcastLockRegistry` can be use to provide this functionality across multiple application instances, such that only one instance can manipulate the group at a time.

NOTE: For all the distributed operations the CP Subsystem must be enabled on `HazelcastInstance`.  
