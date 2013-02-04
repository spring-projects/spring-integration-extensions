/*
 * Copyright 2002-2013 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.springframework.integration.splunk.event;

/**
 * @author David Turanski
 *
 */
@SuppressWarnings("serial")
public class EmailEvent extends SplunkEvent {
	// ----------------------------------
		// Email tracking
		// ----------------------------------

		/**
		 * The person to whom an email is sent.
		 */
		public static String EMAIL_RECIPIENT = "recipient";
		/**
		 * The person responsible for sending an email.
		 */
		public static String EMAIL_SENDER = "sender";
		/**
		 * The email subject line.
		 */
		public static String EMAIL_SUBJECT = "subject";
		
		public void setEmailRecipient(String emailRecipient) {
			addPair(EMAIL_RECIPIENT, emailRecipient);
		}

		public void setEmailSender(String emailSender) {
			addPair(EMAIL_SENDER, emailSender);
		}

		public void setEmailSubject(String emailSubject) {
			addPair(EMAIL_SUBJECT, emailSubject);
		}

}
