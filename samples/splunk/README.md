Development
===================================
### to genereate eclipse project:
1. git clone https://github.com/leejianwei/spring-integration-extensions.git
2. change to "spring-integration-extensions/spring-integration-splunk" dir
3. ./gradlew publish
4. git clone https://github.com/leejianwei/spring-integration-splunk-sample.git
5. change to "spring-integration-splunk-sample" dir
6. ./gradlew eclipse
	
### to build project:
	./gradelw build

Create Twitter Application
=====================================
1. got to https://dev.twitter.com
2. clite "create an app"
3. fill all the data to generate twitter app
4. update "src/main/resources/twitter.properties" to your twitter application
5. update Splunk server info in the "src/main/resources/org/springframework/integration/splunk/example/SplunkCommon-context.xml"
6. run the main class:

### Twitter into Splunk: Get Tweet and push to Splunk with outbound channel adapter

	src/main/java/org/springframework/integration/splunk/example/outbound/twitter/TwiterMain.java
	src/main/resources/org/springframework/integration/splunk/example/outbound/twitter/SpringSplunkShowcaseTwitter-context.xml
	
change the "fromUsers" properties to the user name of your followers whose tweet will be pushed to Splunk
~~~~~xml
	<bean id="fromUserSelector"
		class="org.springframework.integration.splunk.example.outbound.twitter.FromUserSelector">
		<property name="fromUsers">
			<list>
				<value>AP</value>
				<value>nytimes</value>
			</list>
		</property>
	</bean>
~~~~~
	
### Database into Splunk: Get data from Database and push to Splunk with outbound channel adapter

	src/main/java/org/springframework/integration/splunk/example/outbound/jdbc/DatabaseMain.java
	
### Read data from Splunk: Read data from Splunk with inbound channel adapter, persist the data into Database.

	src/main/java/org/springframework/integration/splunk/example/inbound/SplunkMain.java
	



