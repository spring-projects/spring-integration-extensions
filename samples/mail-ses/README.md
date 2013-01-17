AWS SES MailSender sample
=========================

This sample uses a Spring-provided *JavaMailSender* to send emails. This application demonstrates that by only replacing the *JavaMailSender* XML bean declaration with the *DefaultAmazonSESMailSender*, existing applications can send emails using Amazon SES without changing application code. The *DefaultAmazonSESMailSender* is provided by the *Spring Integration Extensions AWS Module*.

