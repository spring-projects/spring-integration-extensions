Spring Integration - Amazon S3 Sample
=====================================

## Introduction

This sample demonstrate the usage of the *Spring Integration* support for
[Amazon Simple Storage Service][] (Amazon S3). The sample covers 2 use-cases:

* **Upload** a file to Amazon S3 using the *s3-outbound-channel-adapter*
* **Poll** files from Amazon S3 using the *s3-inbound-channel-adapter*

## How to Run the Sample

You can execute this sample simply via [Maven][]:

	$ mvn clean package exec:java

Once executed you should see the following screen:

	=========================================================

	     Welcome to the Spring Integration Amazon S3 Sample

	    For more information please visit:
	https://github.com/SpringSource/spring-integration-extensions

	=========================================================
	What would you like to do?
		1. Upload a file to Amazon S3
		2. Poll files from Amazon S3
		q. Quit the application
	 >

Once you have selected either **Option 1** or **Option 2**, you will be asked to
provide the **Access Key ID** and the **Secret Access Key** for *Amazon Web Services*.

You may also consider providing the **Access Key ID** and the **Secret Access Key**
via the command line:

	$ mvn clean package exec:java -DaccessKey=12345 -DsecretKey=12345

In that case you will not be asked to provide that information again.

## s3-outbound-channel-adapter

When you selected the option to upload a file you will be asked to specify the file you would like to upload:

	Please enter the path to the file you want to upload: 
	
Enter a path such as `/demo/data/myfile.txt`.

## s3-inbound-channel-adapter

The polled files will be stored under `s3-local-storage`.

--------------------------------------------------------------------------------

For help please take a look at the [Spring Integration documentation][]

[Amazon Simple Storage Service]: https://aws.amazon.com/s3/
[Maven]: https://maven.apache.org/
[Spring Integration documentation]: https://www.springsource.org/spring-integration
