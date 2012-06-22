Spring Integration Print Adapter
=================================================

Welcome to the Spring Integration Adapter Template. This template is meant as a starting point for new Spring Integration Adapters. This temaplate provides the following
stubbed out components:

* Outbound Channel Adapter
* Outbound Gateway
* Inbound Channel Adapter

# STS issues to be aware of

* [STS-1790](https://issuetracker.springsource.com/browse/STS-1790) - Allow for more flexibility in defining top level packages in the Spring STS Template Wizard
* [STS-2680](https://issuetracker.springsource.com/browse/STS-2680) - New Template Wizard projects aren't categorized on first launch

# FAQ

## I need to add AOP Advices to my Adapters

Use FactoryBeans that wrap your adapter. See JPA Adapter for example.

## More to come...

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

# Using SpringSource Tool Suite

	Gradle projects can be directly imported into STS

# Using PLain Eclipse

To generate Eclipse metadata (.classpath and .project files), do the following:

    ./gradlew eclipse

Once complete, you may then import the projects into Eclipse as usual:

 *File -> Import -> Existing projects into workspace*

Browse to the *'spring-integration'* root directory. All projects should import
free of errors.

# Using IntelliJ IDEA

To generate IDEA metadata (.iml and .ipr files), do the following:

    ./gradlew idea

For more information, please visit the Spring Integration website at:
[http://www.springsource.org/spring-integration](http://www.springsource.org/spring-integration)
