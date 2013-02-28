Spring Integration Extensions
=============================

The Spring Integration Extensions project provides extension modules for [Spring Integration][]. This project is part of the [SpringSource organization][] on GitHub.

## Available Modules

* [Amazon Web Services (AWS)][] Support
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

Check out the [Spring Integration forums][] and the [spring-integration][spring-integration tag] tag
on [Stack Overflow][]. [Commercial support][] is available too.

## Related GitHub projects

* [Spring Integration][]
* [Spring Integration Samples][]
* [Spring Integration Templates][]
* [Spring Integration Dsl Groovy][]
* [Spring Integration Dsl Scala][]
* [Spring Integration Pattern Catalog][]

## Issue Tracking

Report issues via the [Spring Integration Extensions JIRA][].

## Building from source

Each module of the *Spring Integration Extensions* project is hosted as independent project with its own release cycle. For the build process of individual modules we recomend using a [Gradle][]-based build system modelled after the [Spring Integration][] project. Also, the *Spring Integration Adapter Template* for [SpringSource Tool Suite][] (STS) provides a [Gradle][]-based build system. For more information, please see [How to Create New Components][].

Therefore, the following build instructions should generally apply for most, if not all, *Spring Integration Extensions*. In the instructions below, [`./gradlew`][] is invoked from the root of the source tree and serves as a cross-platform, self-contained bootstrap mechanism for the build. The only prerequisites are [Git][] and JDK 1.6+.

### Check out the sources

`git clone git://github.com/SpringSource/spring-integration-extensions.git`

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

[Spring Integration]: https://github.com/SpringSource/spring-integration
[SpringSource organization]: https://github.com/SpringSource
[Spring Integration forums]: http://forum.springsource.org/forumdisplay.php?42-Integration
[spring-integration tag]: http://stackoverflow.com/questions/tagged/spring-integration
[Stack Overflow]: http://stackoverflow.com/faq
[Commercial support]: http://springsource.com/support/springsupport
[Spring Integration Extensions JIRA]: http://jira.springsource.org/browse/INTEXT
[the lifecycle of an issue]: https://github.com/cbeams/spring-framework/wiki/The-Lifecycle-of-an-Issue
[Gradle]: http://gradle.org
[`./gradlew`]: http://vimeo.com/34436402
[Git]: http://help.github.com/set-up-git-redirect
[Gradle build and release FAQ]: https://github.com/SpringSource/spring-framework/wiki/Gradle-build-and-release-FAQ
[Pull requests]: http://help.github.com/send-pull-requests
[contributor guidelines]: https://github.com/SpringSource/spring-integration/wiki/Contributor-guidelines
[administrator guidelines]: https://github.com/SpringSource/spring-integration/wiki/Administrator-Guidelines
[Spring Integration Samples]: https://github.com/SpringSource/spring-integration-samples
[Spring Integration Templates]: https://github.com/SpringSource/spring-integration-templates/tree/master/si-sts-templates
[Spring Integration Dsl Groovy]: https://github.com/SpringSource/spring-integration-dsl-groovy
[Spring Integration Dsl Scala]: https://github.com/SpringSource/spring-integration-dsl-scala
[Spring Integration Pattern Catalog]: https://github.com/SpringSource/spring-integration-pattern-catalog
[SpringSource Tool Suite]: http://www.springsource.org/sts
[How to Create New Components]: https://github.com/SpringSource/spring-integration-extensions/wiki/How-to-Create-New-Components
[Apache License]: http://www.apache.org/licenses/LICENSE-2.0

[SMB]: http://en.wikipedia.org/wiki/Server_Message_Block
[SMPP]: http://en.wikipedia.org/wiki/Short_Message_Peer-to-Peer
[Print]: http://docs.oracle.com/javase/6/docs/technotes/guides/jps/index.html
[Kafka]: http://kafka.apache.org/
[Voldemort]: http://www.project-voldemort.com/voldemort/
[MQ Telemetry Transport]: http://mqtt.org/
[Websockets]: http://www.html5rocks.com/en/tutorials/websockets/basics/
[XQuery]: http://en.wikipedia.org/wiki/XQuery
[Splunk]:http://www.splunk.com/
[Amazon Web Services (AWS)]: http://aws.amazon.com/
[MQ Telemetry Transport (MQTT)]: http://mqtt.org/
