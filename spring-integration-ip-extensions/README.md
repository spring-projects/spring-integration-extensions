Spring Integration IP Extensions
=================================================

Welcome to the Spring Integration IP Extensions project. It is intended to supplement the spring-integration-ip module with, for example, custom serializers/deserializers.


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


WebSocket Server Demo
=====================

This demonstrates how to use the TCP adapters to provide a very lightweight websocket server.

Run WebSocketServerTests as a Java Application (main) and open

file:///.../spring-integration-extensions/spring-integration-ip-extensions/src/test/java/org/springframework/integration/ip/extensions/sockjs/ws.html

in a browser.

Opening the page opens the WebSocket.

Sending 'start' begins sending an incrementing # once per second.
'stop' stops the stream (leaving the socket open), 'start' resumes
again. Multiple browser instances get their own sequence #.


