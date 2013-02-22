Spring Integration - SMB Sample
===============================

## Introduction

This samples demonstrate some simple usage of the *Spring Integration* support for [Server Message Block][] (SMB). The sample will poll an SMB remote directory.

## How to Run the Sample

You can execute this sample simply via [Maven][]:

    $ mvn clean package exec:java

The sample will ask you for:

* SMB Host
* SMB Share and Directory
* SMB Username
* SMB Password

When running the sample you should see the following screen output:

	14:19:02.145 INFO  [org.springframework.integration.samples.smb.Main.main()][org.springframework.integration.samples.smb.Main]
	=========================================================
                                                         
	     Welcome to the Spring Integration Smb Sample    
                                                         
	    For more information please visit:               
	https://github.com/SpringSource/spring-integration-extensions
                                                         
	=========================================================
	Please enter the:
		- SMB Host
		- SMB Share and Directory
		- SMB Username
		- SMB Password
	Host: mySmbHost
	Share and Directory (e.g. myFile/path/to/): demoShare/demoPath/
	Username (e.g. guest): guest
	Password (can be empty):
	14:19:22.130 INFO  [org.springframework.integration.samples.smb.Main.main()][org.springframework.integration.samples.smb.Main]
	=========================================================
                                                         
	    Please press 'q + Enter' to quit the application.
                                                         
	=========================================================
	Polling from Share: smb://guest@mySmbHost/demoShare/demoPath/
	14:19:23.044 INFO  [task-scheduler-1][org.springframework.integration.samples.smb] File Name: demo.data(597)

The polled files will be stored under `target/smb-out`.

--------------------------------------------------------------------------------

For help please take a look at the [Spring Integration documentation][]

[Server Message Block]: http://en.wikipedia.org/wiki/Server_Message_Block
[Maven]: http://maven.apache.org/
[Spring Integration documentation]: http://www.springsource.org/spring-integration
