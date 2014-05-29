Spring Integration Smpp Adapter
=================================================

The Spring Integration Smpp allows you to receive/send [Short Message Service][] (SMS) messages to a [Short message service center][] (SMSC) using the [SMPP][] protocol.

# Components

* Outbound Channel Adapter
* Outbound Gateway
* Inbound Channel Adapter
* Inbound Gateway

# Requirements

For running the tests you're going to need a good server to test with:

There are 2 options:

**SMPPSim** - http://www.seleniumsoftware.com/regform.php?itemdesc=SMPPSim.tar.gz

Simply download it, cd into the folder and execute `./startsmppsim.(sh|bat)`. Make sure the script is executable. The configuration for this simulator is in *conf/smppsim.props*

Another option is **smsssim** and smsctest from http://opensmpp.logica.com/CommonPart/Download/download2.html

Alternatively, the JSMPP project itself has an SMPP simulator as well. It is also possible to use a full-blow SMPP servers like *Kannel*.

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

# TODO

* figure out a clean way to furnish our own Executor implementation

# Resources

* http://www.techdive.in/java/send-sms-using-jsmpp
* http://www.linkedin.com/answers/technology/information-technology/telecommunications/TCH_ITS_TCI/461130-44316394

[SMPP]: http://en.wikipedia.org/wiki/Short_Message_Peer-to-Peer
[Short Message Service]: http://en.wikipedia.org/wiki/Short_Message_Service
[Short message service center]: http://en.wikipedia.org/wiki/Short_message_service_center
