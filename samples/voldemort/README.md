Spring Integration - Project Voldemort Sample
=============================================

## Overview

This sample illustrates the usage of the [Voldemort][] adapters provided by the [Spring Integration Extensions][] project. The samples provides 2 simple use-cases:

1. A user-entered String is persisted to [Voldemort][] using the *Voldemort Outbound Channel Adapter*.
2. [Voldemort][] is polled every 5 seconds using the *Voldemort Inbound Channel Adapter* and the retrieved values are printed to the console using the *Logging Channel Adapter*.

## Requirements

This sample requires a [Voldemort][] instance to be running locally on port 6666. In case you need to customize, please modify the respective values in `src/main/resources/META-INF/spring/integration/spring-integration-context.xml`.

```xml
<bean id="config" class="voldemort.client.ClientConfig">
	<property name="bootstrapUrls" value="tcp://localhost:6666" />
</bean>
```

## How to Run the Sample

You can run the Voldemort sample by either:

* running the "Main" class from within STS (Right-click on Main class --> Run As --> Java Application)
* or from the command line:
    - mvn package
    - mvn exec:java

Once the application is started, you will be asked to entered a String:

	13:24:54.275 INFO  [org.springframework.integration.samples.voldemort.Main.main()][org.springframework.integration.samples.voldemort.Main]
	=========================================================
                                                         
      Welcome to the Spring Integration Voldemort Sample!
                                                         
	    For more information please visit:               
	    http://www.springsource.org/spring-integration   
                                                         
	=========================================================
	13:24:55.460 INFO  [org.springframework.integration.samples.voldemort.Main.main()][org.springframework.integration.samples.voldemort.Main]
	=========================================================
                                                         
	    Please press 'q + Enter' to quit the application.
                                                         
	=========================================================
	Please enter a string and press <enter>: myString
	Persisting String: 'myString' with key 'hello'.

Every time the Voldemort database is polled, you will the following output:

	13:27:40.449 INFO  [task-scheduler-10][org.springframework.integration.samples.voldemort] [Payload=myString][Headers={timestamp=1364923660449, id=6c861b02-5084-4606-b7bb-b1910e14598f, voldemort_key=hello}]
	13:27:45.449 INFO  [task-scheduler-2][org.springframework.integration.samples.voldemort] [Payload=myString][Headers={timestamp=1364923665449, id=8f6eaecc-08ec-41eb-815b-7140d2b660a0, voldemort_key=hello}]

--------------------------------------------------------------------------------

For help please take a look at the Spring Integration documentation:

http://www.springsource.org/spring-integration

[Spring Integration Extensions]: https://github.com/SpringSource/spring-integration-extensions
[Voldemort]: http://www.project-voldemort.com/

