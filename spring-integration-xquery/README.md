Spring Integration XQuery Support
=================================

# Overview

**XQuery** is a query and functional programming language that is used to query over a collection of XML data. XQuery is a super-set of **XPath 2.0**. Therefore, if you are looking for *XPath 2.0* functionality, you may consider using the provided components as well. For more information on *XQuery*, please see:

* http://en.wikipedia.org/wiki/XQuery
* http://en.wikipedia.org/wiki/XPath_2.0

The **XQuery API for Java** ([JSR225][]) is a specification for providing a common *XQuery API* to the Java programming language. The API can be used for *XML databases* as well as pure *XQuery processors*. For more information, please see:

* http://en.wikipedia.org/wiki/XQuery_API_for_Java
* http://xqj.net/
* http://xqj.net/javadoc/

Various products and libraries provide *XQuery* support. Below is a selection of available choices:

## XML Databases

* **BaseX** (BSD License) - http://basex.org/
* **eXist** (GNU LGPL) - http://www.exist-db.org/
* **MarkLogic** (Commercial License, Free Option) - http://developer.marklogic.com/express
* **Sedna** (Apache License, Version 2.0) - http://www.sedna.org/

## XQuery Processors

* **Zorba** (Apache License, Version 2.0) - http://www.zorba-xquery.com/
* **SAXON** (Mozilla Public License) - http://saxon.sourceforge.net/

*Zorba* is written in C++ but provides a Java API ([JSR225][]). *SAXON*, being a pure Java implementation, can be used as a normal Java library. Of all the available XQuery supporting options, *SAXON* is probably the the most widely used choice for Java developers.

# Provided Spring Integration Components

Currently, the *Spring Integration XQuery Module* provides the following components:

* XQuery Router
* XQuery Transformer

## Java Implementation

The class *o.s.i.xquery.core.XQueryExecutor* is the backbone for all *XQuery* related operations within this Spring Integration module. Its corresponding test class is *o.s.i.xquery.core.AbstractXQueryExecutorTests* with the subclasses:

* *o.s.i.xquery.core.SaxonXQueryExecutorTests*
* *o.s.i.xquery.core.SednaXQueryExecutorTests*

Each of these sub-classes instantiates the *XQueryExecutor* with the corresponding implementations of the *javax.xml.xquery.[XQDataSource][]* interface. The *XQueryExecutor* provides a setter for specifying the [XQDataSource][] to use. If not explicitly specified, it will default to use *Saxon*, if available on the classpath.

The XQuery Executor is tested using Saxon and Sedna. Furthermore, we provide the [Spring Integration XQuery Samples][] that additionally also cover *BaseX*. *BaseX* is not included in the actual Spring Integration module, as it conflicts with the *Saxon* jars. Both, the *Sedna* and the *BaseX* XML database need to be started and the datasource needs to connect to them. This is even true, if the XML being queried is not present in the database (as we typically provide it with the payload of the Spring Integration *Message*).

See the class comments of *o.s.i.xquery.core.SednaXQueryExecutorTests* for instructions on starting the *Sedna* server and executing the tests using *Sedna*'s implementation. Additional information can also be found at:

* http://xqj.net/

Of course, much more detailed information is also available on the respective product websites.

## XML Namespace Support

The Spring Integration XQuery components also provide XML Namespace support in order to simplify their configuration.

### Routers

#### With **xquery** sub element

	<int-xquery:xquery-router id="xqueryRouterOne" input-channel="xpathRouterOne">
	        <int-xquery:xquery>
	             <![CDATA[
	                    declare variable $name as xs:string external;
	                    declare variable $class as xs:int external;
	                    for     $student in /mappings/students/student,
	                            $subject in /mappings/subjects/subject
	                    where   $student/@id = $subject/students/studentId
	                    and	$student/name = $name
	                    and	$student/class = $class
	                    return $subject/name/text()
	             ]]>
	        </int-xquery:xquery>
	         <int-xquery:xquery-parameter name="name" expression="headers['name']"/>
	         <int-xquery:xquery-parameter name="class" value="1"/>
	</int-xquery:xquery-router>

The configuration is pretty simple. The XQuery in this example is specified using the *xquery* sub-element. The query once executed will yield the names of the output channels. XQueries can have *named parameters* specified. In this case we have two named parameters: **name** and **class**.

The value or the expression to find the value for these named parameters is specified using the *xquery-parameter* sub-element. This sub-element provides two possible ways to provide parameters. In the first parameter, the value of the **name** parameter is deduced using a SpEL expression. For the second parameter named **class**, a statically defined value is used.

#### With **xquery** attribute

	<int-xquery:xquery-router id="xqueryRouterTwo"
							input-channel="xpathRouterOne"
							xquery="'Hello World'"
							converter="converter"
							xq-datasource="xqDs">
		<int-xquery:xquery-parameter name="name" ref="name"/>
	</int-xquery:xquery-router>

The above config now specifies the *xquery* attribute to specify the query. It additionally has the *converter* and the *xq-datasource* attributes. The *converter* holds a reference to the bean which implements the `org.springframework.integration.xml.DefaultXmlPayloadConverter` interface. By default it uses `org.springframework.integration.xml.DefaultXmlPayloadConverter` Please note that this interface and the implementation is from  **spring-integration-xml** project. The *xq-datasource* attribute holds a reference to a bean that implements the `javax.xml.xquery.XQDataSource` interface. By default it will use `net.sf.saxon.xqj.SaxonXQDataSource` implementation.

#### With **xquery-file-resource** attribute

	<int-xquery:xquery-router id="xqueryRouterFour"
							input-channel="xpathRouterOne"
							xquery-file-resource="org/springframework/integration/xquery/XQuery.xq"
							converter="converter"
							xq-datasource="xqDs">
		<int-xquery:xquery-parameter name="name" expression="headers['name']"/>
		<int-xquery:xquery-parameter name="class" value="1"/>
	</int-xquery:xquery-router>

Instead of the nested *xquery* element or the *xquery* attribute, we provide the path to the resource that contains the xquery, typically a *.xq* file.

Similar to the routers in the core module, the xquery routers accept the mapping subelement to provide an additional level of indirection and mapping from the obtained value(s) from xquery execution to the output channels. Thus, you can have the following subelement in the xquery router definition

	<int:mapping value="val1" channel="channelA" />
	<int:mapping value="val2" channel="channelB" />

### Transformers

Similar to routers, transformers too support accepting the xquery as a child sub element, attribute or an attribute with the resource path to the *.xq* file. Since the definitions and meaning of the common attributes are similar to that of the router,  we will not be explaining them again unless we have some attribute specific to transformers.

#### With **xquery** subelement

	<int-xquery:xquery-transformer id="xqueryTransformerOne"
							input-channel="xqueryTransformerInOne"
							output-channel="output">
		  <int-xquery:xquery>
			<![CDATA[
					declare variable $name as xs:string external;
					declare variable $class as xs:int external;
					for 	$student in /mappings/students/student,
							$subject in /mappings/subjects/subject
					where	$student/@id = $subject/students/studentId
					and	$student/name = $name
					and	$student/class = $class
					return $subject/name/text()
			 ]]>
		  </int-xquery:xquery>
		  <int-xquery:xquery-parameter name="name" expression="headers['name']"/>
		  <int-xquery:xquery-parameter name="class" value="1"/>
    </int-xquery:xquery-transformer>

#### With **xquery** attribute

	<int-xquery:xquery-transformer id="xqueryTransformerTwo"
						input-channel="xqueryTransformerOutTwo"
						output-channel="output"
						xquery="'Hello World'"
						converter="converter"
						xq-datasource="xqDs"
						format-output="true">
	</int-xquery:xquery-transformer>

An additional attibute *format-output* is mentioned, the value could be *true* or *false*. It informs the transformer to format the output xml generated after transformation.

#### With **xquery-file** attribute.

	<int-xquery:xquery-transformer id="xqueryTransformerThree"
						input-channel="xqueryTransformerIPThree"
						output-channel="output"
						xquery-file-resource="classpath:org/springframework/integration/xquery/XQueryTransform.xq"
						converter="converter"
						xq-datasource="xqDs">
	</int-xquery:xquery-transformer>

###Credits
We would like to thank **Ganesh Shetty** for his suggestion of inclusion of *XQuery* support in *Spring Integration*, giving the initial requirements and use cases for this module. We look forward for more support from the community for evaluating the libraries and provide their feedback.

[JSR225]: http://jcp.org/aboutJava/communityprocess/final/jsr225/index.html
[xqj.net]: http://xqj.net/
[XQDataSource]: http://xqj.net/javadoc/javax/xml/xquery/XQDataSource.html
[Spring Integration XQuery Samples]: https://github.com/SpringSource/spring-integration-samples/tree/master/basic/xquery
