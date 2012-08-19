/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.integration.aws.ses.core;

import java.util.List;


/**
 * The Class representing the Amazon SES mail message.
 * use  {@link AmazonSESMailSender} for sending mail messages
 *
 * @see AmazonSESMailSender
 *
 * @author Amol Nayak
 *
 * @since 1.0
 *
 */
public class AmazonSESSimpleMailMessage {

	/**.
	 * This value will be used as the from email id of the mail being sent
	 */
	private String from;

	/**.
	 * This value will be set as the reply to header of the mail being sent out
	 */
	private List<String> replyTo;

	/**.
	 * The List of Email ids to send the mail message to
	 */
	private List<String> toList;

	/**.
	 * The List of Email ids to be added to cc in send the mail message to
	 */
	private List<String> ccList;

	/**.
	 * The List of Email ids to be added to bcc in send the mail message to
	 */
	private List<String>bccList;

	/**.
	 * The subject line to be set in the mail.
	 */
	private String subject;

	/**.
	 * The message text to be sent in the mail to be sent
	 */
	private String message;

	/**.
	 * Flag to indicate if the message text is html
	 */
	private boolean isHtml;


	/**.
	 * Getter for the from email id
	 * @return
	 */
	public String getFrom() {
		return from;
	}

	/**.
	 * Setter for the from email id
	 * @param from
	 */
	public void setFrom(String from) {
		this.from = from;
	}

	/**.
	 * Getter for the Reply to field
	 * @return
	 */
	public List<String> getReplyTo() {
		return replyTo;
	}

	/**
	 * Setter for the reply to field
	 * @param replyTo
	 */
	public void setReplyTo(List<String> replyTo) {
		this.replyTo = replyTo;
	}

	/**.
	 * Getter for the Email TO List
	 * @return
	 */
	public List<String> getToList() {
		return toList;
	}

	/**.
	 * Setter for the email TO List
	 * @param toList
	 */
	public void setToList(List<String> toList) {
		this.toList = toList;
	}

	/**.
	 * getter for the mail CC List
	 * @return
	 */
	public List<String> getCcList() {
		return ccList;
	}

	/**.
	 * Setter for the email CC List
	 * @param ccList
	 */
	public void setCcList(List<String> ccList) {
		this.ccList = ccList;
	}

	/**.
	 * Getter for the email BCC List
	 * @return
	 */
	public List<String> getBccList() {
		return bccList;
	}

	/**.
	 * Setter for the email BCC List
	 * @param bccList
	 */
	public void setBccList(List<String> bccList) {
		this.bccList = bccList;
	}

	/**.
	 * Get the email subject
	 * @return
	 */
	public String getSubject() {
		return subject != null?subject:"";
	}

	/**.
	 * get the email subject
	 * @param subject
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}

	/**.
	 * get the String email message
	 * @return
	 */
	public String getMessage() {
		return message != null?message:"";
	}

	/**.
	 * Set the email {@link String} message to send
	 * @param message
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * Flag to indicate whether the content is to be treated as HTML
	 * or plain text
	 * @return
	 */
	public boolean isHtml() {
		return isHtml;
	}

	/**
	 * Set whether the content is html or plain text
	 * @param isHtml
	 */
	public void setHtml(boolean isHtml) {
		this.isHtml = isHtml;
	}

}
