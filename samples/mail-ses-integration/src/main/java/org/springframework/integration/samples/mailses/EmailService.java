package org.springframework.integration.samples.mailses;

import org.springframework.integration.annotation.Header;
import org.springframework.integration.annotation.Payload;
import org.springframework.integration.mail.MailHeaders;

public interface EmailService {


	void send(

			@Header(MailHeaders.FROM)
			String fromEmail,

			@Header(MailHeaders.TO)
			String toEmail,

			@Header(MailHeaders.SUBJECT)
			String subject,

			@Payload
			String body);

}
