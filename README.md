Spring Integration Extensions
=============================

The Spring Integration Extensions project provides extension modules for [Spring Integration][]. This project is part of the [SpringSource organization][] on GitHub.

## Available Modules

* [Amazon Web Services (AWS)][] Support
* [Hazelcast][] Support
* [Kafka][] Support
* [MQ Telemetry Transport (MQTT)][] Support
* [Print][] Support
* [SMB][] Support
* [SMPP][] Support
* [Splunk][] Support
* [Voldemort][] Support
* [XQuery][] Support
* Zip Support (Compression and Uncompression)

## Samples

Under the `samples` directory, you will find samples for the various modules. Please refer to the documentation of each sample for further details.

## Getting support

Check out the [spring-integration][spring-integration tag] tag on [Stack Overflow][].

These extensions are community-supported projects and, unlike Spring Integation itself, they are not released on a regular schedule.
If you have specific requests about an extension, open a GitHub issue for consideration.
Contributions are always welcome.

## Related GitHub projects

* [Spring Integration][]
* [Spring Integration Samples][]
* [Spring Integration Templates][]
* [Spring Integration Dsl Kotlin][]
* [Spring Integration Pattern Catalog][]

## Issue Tracking

Report issues via the [Spring Integration Extensions JIRA][].

## Building from source

Each module of the *Spring Integration Extensions* project is hosted as independent project with its own release cycle. For the build process of individual modules we recommend using a [Gradle][]-based build system modelled after the [Spring Integration][] project. Also, the *Spring Integration Adapter Template* for [SpringSource Tool Suite][] (STS) provides a [Gradle][]-based build system. For more information, please see [How to Create New Components][].

Therefore, the following build instructions should generally apply for most, if not all, *Spring Integration Extensions*. In the instructions below, [`./gradlew`][] is invoked from the root of the source tree and serves as a cross-platform, self-contained bootstrap mechanism for the build. The only prerequisites are [Git][] and JDK 1.6+.

### Check out the sources

`git clone git://github.com/spring-projects/spring-integration-extensions.git`

### Go into the directory of a specific module

`cd module-name`

### Compile and test, build all jars

`./gradlew build`

### Install the modules jars into your local Maven cache

`./gradlew install`

... and discover more commands with `./gradlew tasks`. See also the [Gradle build and release FAQ][].

## Import sources into your IDE

### Using Eclipse / STS

When using [SpringSource Tool Suite][] you can directly import Gradle based projects:

`File -> Import -> Gradle Project`

Just make sure that the Gradle Support for STS is installed. Alternatively, you can also generate the Eclipse metadata (.classpath and .project files) using Gradle:

`./gradlew eclipse`

Once complete, you may then import the projects into Eclipse as usual:

`File -> Import -> Existing projects into workspace`

### Using IntelliJ IDEA

To generate IDEA metadata (.iml and .ipr files), do the following:

    ./gradlew idea

## Contributing

[Pull requests][] are welcome. Please see the [contributor guidelines][] for details. Additionally, if you are contributing, we recommend following the process for Spring Integration as outlined in the [administrator guidelines][].

## Creating Custom Adapters

In order to simplify the process of writing custom components for Spring Integration, we provide a Template project for [SpringSource Tool Suite][] (STS) version 3.0.0 and greater. This template is part of the [Spring Integation Templates][] project. For more information please read [How to Create New Components][].

## Staying in touch

Follow the Spring Integration team members and contributors on Twitter:

* [@m\_f\_](https://twitter.com/m\_f\_) - Mark Fisher
* [@ghillert](https://twitter.com/ghillert) - Gunnar Hillert
* [@z_oleg](https://twitter.com/z_oleg) - Oleg Zhurakousky
* [@gprussell](https://twitter.com/gprussell) - Gary Russell

## License

The Spring Integration Extensions Framework is released under version 2.0 of the [Apache License][] unless noted differently for individual extension Modules, but this should be the rare exception.

**We look forward to your contributions!!**

[Spring Integration]: https://github.com/spring-projects/spring-integration
[SpringSource organization]: https://github.com/spring-projects
[spring-integration tag]: https://stackoverflow.com/questions/tagged/spring-integration
[Stack Overflow]: https://stackoverflow.com/faq
[Spring Integration Extensions JIRA]: https://jira.springsource.org/browse/INTEXT
[the lifecycle of an issue]: https://github.com/cbeams/spring-framework/wiki/The-Lifecycle-of-an-Issue
[Gradle]: https://gradle.org
[`./gradlew`]: https://vimeo.com/34436402
[Git]: https://help.github.com/set-up-git-redirect
[Gradle build and release FAQ]: https://github.com/spring-projects/spring-framework/wiki/Gradle-build-and-release-FAQ
[Pull requests]:https://help.github.com/en/articles/creating-a-pull-request
[contributor guidelines]: https://github.com/spring-projects/spring-integration/blob/master/CONTRIBUTING.adoc
[administrator guidelines]: https://github.com/spring-projects/spring-integration/wiki/Administrator-Guidelines
[Spring Integration Samples]: https://github.com/spring-projects/spring-integration-samples
[Spring Integration Templates]: https://github.com/spring-projects/spring-integration-templates/tree/master/si-sts-templates
[Spring Integration Dsl Kotlin]: https://github.com/spring-projects/spring-integration-extensions/tree/master/spring-integration-kotlin-dsl
[Spring Integration Pattern Catalog]: https://github.com/spring-projects/spring-integration-pattern-catalog
[SpringSource Tool Suite]: https://www.springsource.org/sts
[How to Create New Components]: https://github.com/spring-projects/spring-integration-extensions/wiki/How-to-Create-New-Components
[Apache License]: https://www.apache.org/licenses/LICENSE-2.0

[SMB]: https://en.wikipedia.org/wiki/Server_Message_Block
[SMPP]: https://en.wikipedia.org/wiki/Short_Message_Peer-to-Peer
[Print]: https://docs.oracle.com/javase/6/docs/technotes/guides/jps/index.html
[Kafka]: https://kafka.apache.org/
[Voldemort]: https://www.project-voldemort.com/voldemort/
[MQ Telemetry Transport]: https://mqtt.org/
[Websockets]: https://www.html5rocks.com/en/tutorials/websockets/basics/
[XQuery]: https://en.wikipedia.org/wiki/XQuery
[Splunk]: https://www.splunk.com/
[Amazon Web Services (AWS)]: https://aws.amazon.com/
[MQ Telemetry Transport (MQTT)]: https://mqtt.org/
[Hazelcast]: https://hazelcast.org/
