SPRING INTEGRATION ETCD SUPPORT
====================================

## ETCD LEADER ELECTION

If you need to elect a leader (e.g. for highly available message consumer where only one node should receive messages)
you just need to create a `LeaderInitiator`. Example:

```java
@Bean
public EtcdClient etcdClient() {
	return new EtcdClient(URI.create("http://localhost:4001"));
}

@Bean
public LeaderInitiator initiator() {
	LeaderInitiator initiator = new LeaderInitiator(etcdClient());
	return initiator;
}
```

Then when a node is elected leader it will send `OnGrantedEvent` to all application listeners. See
the [Spring Integration User Guide](http://docs.spring.io/spring-integration/reference/htmlsingle/#endpoint-roles)
for more information on how to use those events to control messaging endpoints.
