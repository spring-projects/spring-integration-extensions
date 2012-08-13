Spring Integration Print Support
================================

## Overview

The *Spring Integration Print* allows you to print payloads using the [Java Print Service][]. As such one *Outbound Channel Adapter* is provided. 

## Requirements

This module depends by default on Spring Integration 2.2. When using this module, please be aware that printing using [Java Print Service] can be unpredictable depending on the uses platform and the used printer. For some more details, please see:

* http://hillert.blogspot.com/2011/12/java-print-service-frustrations.html

Currently the Print Support has been tested using a *Brother HL-4070CDW* which exhibits several issues such as the inability to execute a print job that prints more than one copy for each page. If you use the Print Module, please report back any compatibility issues with your used printers. 

## Dependency Declaration

### Gradle

	dependencies {
		…
		compile "org.springframework.integration:spring-integration-print:1.0.0.BUILD-SNAPSHOT"
		…
	}

### Maven

	<dependencies>
		…
		<dependency>
			<groupId>org.springframework.integration</groupId>
			<artifactId>spring-integration-print</artifactId>
			<version>1.0.0.BUILD-SNAPSHOT</version>
		</dependency>
		…
	</dependencies>

## Print Outbound Channel Adapter

The *Print Outbound Channel Adapter* is implemented using the *PrintMessageHandler* class. It in turn delegates to the *PrintServiceExecutor* class. The *PrintServiceExecutor* is responsible for initializing the Java *[PrintService][]*. As such, you can initialize the *PrintServiceExecutor* with providing a:

* Printer Name
* Custom implementation of the *[PrintService][]* interface (Useful for testing)

If you provide neither a printer name nor an implementation of the *[PrintService][]* interface, then the systems default printer is chosen.

The following properties can be set on the *Print Outbound Channel Adapter*:

### printJobName

*Optional* property that let's you specify the name of the print job as it is added to the print queue. Under the covers, this property will create a *javax.print.attribute.standard.JobName* and add it to the respective *PrintRequestAttributeSet*. This property is not used, if not specified (No explicit default value).

### mediaSizeName

*Optional* property that let's you specify the [MediaSizeName][]. Under the covers, this property will add the provided [MediaSizeName][] to the respective *PrintRequestAttributeSet*. This property is not used, if not specified (No explicit default value).

### sides

Let's you specify how the pages are are printed to the selected medium, e.g. duplex or one-sided. Under the covers, this property will add the provided [Sides][] instance to the respective *PrintRequestAttributeSet*. This property is not used, if not specified (No explicit default value).

### copies

Let's you specify an integer value that indicates how many time each print page shall be printed. Under the covers, this property will add a [Copies][] instance to the respective *PrintRequestAttributeSet*. This property is not used, if not specified (No explicit default value).

### mediaTray

Let's you specify the media tray/bin for the print job. This property can be used instead of providing the *mediaSizeName* property. Under the covers, this property will add the provided [MediaTray][] instance to the respective *PrintRequestAttributeSet*. This property is not used, if not specified (No explicit default value).

### chromaticity

Let's you specify whether you want to do monochrome or color printing. Under the covers, this property will add the provided [Chromaticity][] instance to the respective *PrintRequestAttributeSet*. This property is not used, if not specified (No explicit default value).

### docFlavor

Let's you specify the [DocFlavor]. If not specified, this property will default to *DocFlavor.STRING.TEXT_PLAIN*.

**IMPORTANT!** Every property is validated against the to be used [Java Print Service]. Therefore, if the selected [Java Print Service] is not supported, an *IllegalArgumentException* will be thrown. 

### Determining you printer capabilities.

In order to get some indication what capabilities are supported by your printer, you may want to too look at the *PrintServiceExecutor*'s *getPrinterInfo()* method. When executed, it returns a list of:

* Supported [DocFlavor][]s
* Supported Attribute Categories
* Supported [Print Attribute][]s

## Namespace Support

*Spring Integration* also provides XML Namespace support for the Print support. Simply add the following Namespace declaration:

	<?xml version="1.0" encoding="UTF-8"?>
	<beans xmlns="http://www.springframework.org/schema/beans"
		xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xmlns:int="http://www.springframework.org/schema/integration"
		xmlns:int-print="http://www.springframework.org/schema/integration/print"
		xsi:schemaLocation="
			http://www.springframework.org/schema/integration http://www.springframework.org/schema/integration/spring-integration.xsd
			http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
			http://www.springframework.org/schema/integration/print http://www.springframework.org/schema/integration/print/spring-integration-print.xsd">
			…
	</beans>

An example of declaring a *Print Outbound Channel Adapter* is shown below:

	<int-print:outbound-channel-adapter id="printOutboundChannelAdapter"
		channel="input"
		copies="2"
		doc-flavor="java.lang.String"
		mime-type="text/plain; charset=utf-8"
		sides="DUPLEX"/>


[Java Print Service]: http://docs.oracle.com/javase/6/docs/technotes/guides/jps/index.html
[PrintService]: http://docs.oracle.com/javase/6/docs/api/javax/print/PrintService.html
[Chromaticity]: http://docs.oracle.com/javase/6/docs/api/javax/print/attribute/standard/Chromaticity.html
[Copies]: http://docs.oracle.com/javase/6/docs/api/javax/print/attribute/standard/Copies.html
[DocFlavor]: http://docs.oracle.com/javase/6/docs/api/javax/print/DocFlavor.html
[MediaTray]: http://docs.oracle.com/javase/6/docs/api/javax/print/attribute/standard/MediaTray.html
[Print Attribute]: http://docs.oracle.com/javase/6/docs/api/javax/print/attribute/Attribute.html
[MediaSizeName]: http://docs.oracle.com/javase/6/docs/api/javax/print/attribute/standard/MediaSizeName.html
[Sides]: http://docs.oracle.com/javase/6/docs/api/javax/print/attribute/standard/Sides.html