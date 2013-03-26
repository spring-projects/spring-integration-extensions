Spring Integration Kafka Adapter
=================================================



Welcome to the *Spring Integration Kafka adapter*. This template is meant as a starting point for new Spring Integration Adapters.This template provides the following components:

* Outbound Channel Adapter
* Outbound Gateway
* Inbound Channel Adapter

However, this template may also be useful in order to create other Spring Integration extensions containing for example *Transformers*, Marshallers etc.

# STS issues to be aware of

* [STS-2866](https://issuetracker.springsource.com/browse/STS-2866)

As of the current stable version of STS 3.0.0, it is currently impossible to replace version numbers in file names. Therefore, the versions of the XML schemas are hard-coded to a value of *1.0*. If you have defined a different version through the STS template wizard, please change the schema versions accordingly. You will need to rename:

	src/main/resources/your_package/config/xml/spring-integration-1.0.xsd

You will also need to change the version numbers in file:

	src/main/resources/META-INF/spring.schemas

This issue will be addressed for STS 3.1.0.

# FAQ

This section provides some additional information, that may help you to create better *Spring Integration* components. 

## How are the packages structured?

### Base package

In many instances, the base package may not contain any classes at all. However, if you define custom Spring Integration message headers or provide module specific exceptions types, this package will be a good choice to store those types of classes.

### config.xml

Parser classes for the XML Namespace support go into this package.

### config.annotation

If your *Spring Integration* module provides custom annotations, the relevant configuration classes go into this package.

### config.xml

Used for common configuration classes.

### core

Contains the core component logic that is typically shared across the various components you define. 

### inbound

Contains classes for inbound adapters.

### outbound

Contains classes for outbound adapters.

### support

Contains for example utility classes.

## I need to add AOP Advices to my adapters

Use *FactoryBeans* that wrap your adapter. See the *Spring Integration JPA Adapter* for an example.

## How do I provide documentation for my custom modules?

We typically recommend 2 approaches:

* DocBook 
* Markdown formatted README.md files

### DocBook

Traditionally, *Spring* projects have relied on DocBook to provide documentation. By conforming to the DocBook XML syntax, you are easily able to generate PDF and HTML documentation. The adapter template provide DocBook support out of the box. You can find preliminary stubbed out documentation under *src/reference/docbook*. In order to build the reference documentation (results will be in `build/reference`), execute:

    ./gradlew reference

### Markdown formatted README.md files

If you use *GitHub* for the hosting of your projects, you may also consider using its sophisticated Markdown support. GitHub will provide a nice rendering of your readme files right in the source code repository. 

## Can I install the artifacts of my adapter to the local Maven cache?

Yes. To build and install jars into your local Maven cache, please execute:

    ./gradlew install

Please also review the settings in **publish-maven.gradle**. Within that file you can specify various POM.xml-specific meta-data such as licensing, developer and scm information.

# Building

If you encounter out of memory errors during the build, increase available heap and permgen for Gradle:

    GRADLE_OPTS='-XX:MaxPermSize=1024m -Xmx1024m'

To build and install jars into your local Maven cache:

    ./gradlew install

To build api Javadoc (results will be in `build/api`):

    ./gradlew api

To build reference documentation (results will be in `build/reference`):

    ./gradlew reference

To build complete distribution including `-dist`, `-docs`, and `-schema` zip files (results will be in `build/distributions`)

    ./gradlew dist

# IDE Support

While your custom Spring Integration Adapter is initially created with SpringSource Tool Suite, you in fact end up with a Gradle-based project. As such, the created project can be imported into other IDEs as well.

## Using SpringSource Tool Suite

Gradle projects can be directly imported into STS. But please make sure that you have the Gradle support installed.

## Using Plain Eclipse

To generate Eclipse metadata (*.classpath* and *.project* files), do the following:

    ./gradlew eclipse

Once complete, you may then import the project into Eclipse as usual:

 *File -> Import -> Existing projects into workspace*

Browse to the root directory of the project and it should import free of errors.

## Using IntelliJ IDEA

To generate IDEA metadata (.iml and .ipr files), do the following:

    ./gradlew idea

# Further Resources

## Getting support

Check out the [Spring Integration forums][] and the [spring-integration][spring-integration tag] tag
on [Stack Overflow][]. [Commercial support][] is available, too.

## Related GitHub projects

* [Spring Integration][]
* [Spring Integration Samples][]
* [Spring Integration Templates][]
* [Spring Integration Dsl Groovy][]
* [Spring Integration Dsl Scala][]
* [Spring Integration Pattern Catalog][]

For more information, please also don't forget to visit the [Spring Integration][] website.

[Spring Integration]: https://github.com/SpringSource/spring-integration
[Commercial support]: http://springsource.com/support/springsupport
[Spring Integration forums]: http://forum.springsource.org/forumdisplay.php?42-Integration
[spring-integration tag]: http://stackoverflow.com/questions/tagged/spring-integration
[Spring Integration Samples]: https://github.com/SpringSource/spring-integration-samples
[Spring Integration Templates]: https://github.com/SpringSource/spring-integration-templates/tree/master/si-sts-templates
[Spring Integration Dsl Groovy]: https://github.com/SpringSource/spring-integration-dsl-groovy
[Spring Integration Dsl Scala]: https://github.com/SpringSource/spring-integration-dsl-scala
[Spring Integration Pattern Catalog]: https://github.com/SpringSource/spring-integration-pattern-catalog
[Stack Overflow]: http://stackoverflow.com/faq
