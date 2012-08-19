Spring Integration Extension for Amazon Web Services (AWS)
==========================================================


##Small Introduction to Amazon Web Services (AWS)


Launched in 2006, Amazon Web Services (AWS) started providing key infrastructure for business as web services, which, also known as cloud computing. Using cloud computing businesses can adopt a new business model whereby they do not have to plan and invest in procuring their own IT infrastructure. They can use the infrastructure and services provided by the cloud service provider and pay as they use the services.
Visit [http://aws.amazon.com/products/] for more details about various products offered by Amazon as a part their cloud computing services.

##Introduction to Spring Integration's extensions to AWS

This guide intends to explain in brief about various adapters available for the AWS and sample xml tag definition for each of them and code snippets wherever necessary,

The library aims to provide adapters for following AWS Services

* **Amazon Simple Email Service (SES)**
* **Amazon Simple Storage Service (S3)** (Development complete, coming soon)
* **Amazon Simple Queue Service (SQS)** (Development complete, coming soon)
* **Amazon DynamoDB** (Analysis ongoing)
* **Amazon SimpleDB** (Not initiated)
* **Amazon SNS** (Not initiated)

Of the above libraries, SES and SNS have outbound adapters only. All other
services have inbound and outbound adapters. The SQS inbound adapter is capable of receiving notifications sent out from SNS where the topic is an SQS Queue.
For DymamoDB and SimpleDB, apart from inbound and outbound adapters we shall provide a *MessageStore* implementation too.


###Executing the test cases.

All the test cases for the adapters are present in the *src/test/java* folder. On executing the build, maven's surefire plugin will execute all
the tests. Please note that all the tests ending with **AWSTests.java* connect
to the actual webservices and are excluded by default in the maven build.
All other tests rely on mocking to test the functionality. You need to execute the **AWSTests.java*
explicitly, manually to test the conectivity to AWS using your credentials.
All these **AWSTests.java* tests look for the file *awscredentials.properties* in the classpath.
To be on the safer side create one at location *src/test/resources* as this file
*spring-integration-aws/src/test/resources/awscredentials.properties* is added to *.gitignore* file.
This will prevent this file to be checked in accidently and revealing your credentials.
This file needs to have two properties *accessKey* and *secretKey* holding the values of your access key and secret key respectively.
> **Note: AWS Services are chargeable and we recommend not to execute the **AWSTests.java* as part of your regular builds.
AWS does provide a free tier which is sufficient to perform your tests without being
charged (not true for DynamoDB though), however keep a check on your account usage regularly.
Get more information about AWS free tier at [http://aws.amazon.com/free/][]**



#Adapters

##Simple Email Service (SES)


###Introduction

Amazon Simple Email Service (SES) is a web service for sending emails from the cloud. It supports two types of mails currently, the simple mail and raw email.
Use simple mail if your application just needs to send out emails with some html formatting and without embedded images or attachments. Raw emails gives more flixibility to
send complex emails with embedded images and attachments.
For more details about Amazon SES and its pricing visit [http://aws.amazon.com/ses/]
We have an outbound channel adapter for the Amazon SES Service for sending out simple and raw emails with complete namespace support for configuring the adapter.
To prevent misuse of the service, Amazon has enforced some restrictions, for sandbox you need to verify email ids which would be used in *to, cc, bcc* and *from*. For more details on how to use SES and
other details refer to the SES documentation at [http://aws.amazon.com/documentation/ses/][].

###Outbound Channel Adapter

Below xml snippet is a simple definition of the outbound channel adapter


	<integration:channel id="outboundAdapterChannel"/>
	<int-aws-ses:outbound-channel-adapter
			propertiesFile="classpath:awscredentials.properties"
			 channel="outboundAdapterChannel"/>


*propertiesFile* attribute contains the AWS access key and secret key. The
name of the properties are *accessKey* and *secretKey* respectively.
An alternative to the *propertiesFile* attribute is the *accessKey* and the
*secretKey* attributes containing the values of access key and the secret key. The definition will look as below.

	<integration:channel id="outboundAdapterChannel"/>
	<int-aws-ses:outbound-channel-adapter
			accessKey="{your access key}"
			secreKey="{your secret key}"
			channel="outboundAdapterChannel"/>

Both the approaches for providing the credentials are mutually exclusive to each other.

####Sending Mail Messages
We shall now see a java code snippet to send a mail using the SNS adapter


* **Simple Mail Message**

	A Simple Mail Message does not support attachments and embedded contents. It supports basic html content to be sent as the mail body.

		Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("fromEmailId", "xyz@somemail.com");
		headers.put("htmlFormat", true);
		headers.put("subject", "Mail Sent from AWS SES Outbound adapter");
		headers.put("toEmailId", "abc@anothermail.com");

		Message<String> msg =
		MessageBuilder.withPayload("<html><i>A Simple Mail Message sent from " +
		"Amazon SES Outbound adapter from Spring integration<i></html>")
			.copyHeaders(headers)
			.build();
		channel.send(msg);

	The above piece off code is pretty simple.
	 * We add four headers to the message for the *to email id*, *from email id*, *subject* and a *flag* to indicate whether the content is an html content or plain text content.
	 * Of these headers the *from email id* and *subject* are mandatory. We can specify one or more of *to*, *cc* or *bcc* email addresses.
	 * The message needs to have a payload of string which is either a plain
	  text or html content. The content will be rendered a html only if the
	 *htmlFormat *header is set appropriately.
	 * The value of the *htmlFormat* header can be Boolean *true*, or *y*,*yes*
	  or *true* as String. For String the value is case insensitive. Any other value will be considered as *false*.
	 * The message is sent over the channel to which is the input channel of
	 the outbound channel adapter.
	 * See *o.s.i.aws.ses.AmazonSESMailHeaders* for all the possible header values supported.

* **Raw Mail Message**

	Use raw mail when you need more flexibility to send mails, like setting mime types and email headers. As long as the content complies with the standard email format standard you can use this means for sending the mail to your recipients. In the below sample we use the spring's *o.s.mail.javamail.MimeMessageHelper* to construct the Mime message.

			Session session = Session.getDefaultInstance(new Properties());
			MimeMessageHelper helper = new MimeMessageHelper(new MimeMessage(session),true);
			helper.setTo("abc@somemail.com");
			helper.setFrom("xyz@anothermail.com");
			helper.setText("A Sample Embedded image");
			helper.addAttachment(file.getName(),new File("<File path to the attachment>"));
			helper.setSubject("Name Pic");
			Message<MimeMessage> message =
			MessageBuilder.withPayload(helper.getMimeMessage()).build();
			channel.send(message);

	The messages over this channel are consumed by the outbound SES adapter and the mail is sent out using SES.


[http://aws.amazon.com/products/]: http://aws.amazon.com/products/
[http://aws.amazon.com/ses/]: http://aws.amazon.com/ses/
[http://aws.amazon.com/documentation/ses/]: http://aws.amazon.com/documentation/ses/
[http://aws.amazon.com/free/]: http://aws.amazon.com/free/