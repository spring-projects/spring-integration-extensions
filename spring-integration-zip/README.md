Spring Integration Zip Support
==============================

Build Status : [![Build Status](http://build.spring.io/plugins/servlet/buildStatusImage/INTEXT-ZIP)](https://build.spring.io/browse/INTEXT-ZIP)

## Introduction

This *Spring Integration Extension* provides [Zip][] (un-) compression support. The following components are provided:

* Zip Transformer
* UnZip Transformer
* UnZip Result Splitter

## ZIP Compression Support

The following input data types can be **compressed**:

* File
* String
* byte[]
* Iterable

In input data types can be mixed as part of an Iterable. E.g. you should be
easily able to compress a collection containing Strings, byte arrays and Files.
It is important to note that nested Iterables are *NOT SUPPORTED* at present time.

The zip transformer can be customized by setting several properties:

### compressionLevel

Sets the compression level. Default is `Deflater#DEFAULT_COMPRESSION`

### useFileAttributes

Specifies whether the name of the file shall be used for the zip entry.

## ZIP Un-compression Support

The following input data types can be **decompressed**:

* File
* InputStream
* byte[]

When unzipping data, you can also specify a property **expectSingleResult**. If set
to *true* and more than *1* zip entry were detected, a **MessagingException** will be raised.
This property also influences the return type of the payload. If set to *false* (the *default*),
then the payload will be of type *SortedMap*, if *true*, however, the actual zip
entry will be returned.

Othe properties that can be set on the UnZipTransformer:

### deleteFiles

If the payload is an instance of `File`, this property specifies whether to delete the File after transformation. Default is *false*.

### workDirectory

Set the work-directory. The work directory is used when the ZipResultType is set to ZipResultType.FILE.
By default this property is set to the System temporary directory containing a sub-directory "ziptransformer".

### ZipResultType

Defines the format of the data returned after transformation. Available options are:

* File
* byte[]

## UnZipResultSplitter

The `UnZipResultSplitter` is useful in cases where Zip files contain more than *1*
zip entry.

## Zipping and Unzipping Large Files

TBD

## Java Package Structure

### Base package

The base package `org.springframework.integration.zip` contains the *ZipHeaders* class which defines the *Spring Integration* message headers that are specific to the Zip module.

### config.xml

This package contains the parser classes for the XML Namespace support.

### transformer

Contain the classes responsible for the actual (un-) zip operation:

* ZipTransformer
* UnZipTransformer

## Namespace Support

Full XML namespace support is provided.

## Building the Project

To build and install jars into your local Maven cache, please execute:

    ./gradlew install

If you encounter out of memory errors during the build, increase available heap and permgen for Gradle:

    GRADLE_OPTS='-XX:MaxPermSize=1024m -Xmx1024m'

To build api Javadoc (results will be in `build/api`):

    ./gradlew api

To build complete distribution including `-dist` and `-schema` zip files (results will be in `build/distributions`)

    ./gradlew dist

# IDE Support

While your custom Spring Integration Adapter is initially created with SpringSource Tool Suite, you in fact end up with a Gradle-based project.
As such, the created project can be imported into other IDEs as well.

## Using Spring Tool Suite

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

Check out the [spring-integration][spring-integration tag] tag on [Stack Overflow][].

## Related GitHub projects

* [Spring Integration][]
* [Spring Integration Samples][]
* [Spring Integration Templates][]
* [Spring Integration Java DSL][]
* [Spring Integration Dsl Groovy][]
* [Spring Integration Dsl Scala][]

For more information, please also don't forget to visit the [Spring Integration][] website.

[Spring Integration]: https://github.com/spring-projects/spring-integration
[spring-integration tag]: http://stackoverflow.com/questions/tagged/spring-integration
[Spring Integration Samples]: https://github.com/spring-projects/spring-integration-samples
[Spring Integration Templates]: https://github.com/spring-projects/spring-integration-templates/tree/master/si-sts-templates
[Spring Integration Java DSL]: https://github.com/spring-projects/spring-integration-java-dsl
[Spring Integration Dsl Groovy]: https://github.com/spring-projects/spring-integration-dsl-groovy
[Spring Integration Dsl Scala]: https://github.com/spring-projects/spring-integration-dsl-scala
[Stack Overflow]: http://stackoverflow.com/faq

[Zip]: http://en.wikipedia.org/wiki/Zip_%28file_format%29
