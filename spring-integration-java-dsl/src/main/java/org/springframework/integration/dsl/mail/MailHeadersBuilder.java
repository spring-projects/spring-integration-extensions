/*
 * Copyright 2014 the original author or authors.
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

package org.springframework.integration.dsl.mail;

import org.springframework.integration.dsl.support.MapBuilder;
import org.springframework.integration.mail.MailHeaders;

/**
 * @author Artem Bilan
 * @author Gary Russell
 */
public class MailHeadersBuilder extends MapBuilder<MailHeadersBuilder, String, Object> {

	MailHeadersBuilder() {
	}

	public MailHeadersBuilder subject(String subject) {
		return put(MailHeaders.SUBJECT, subject);
	}

	public MailHeadersBuilder subjectExpression(String subject) {
		return putExpression(MailHeaders.SUBJECT, subject);
	}

	public MailHeadersBuilder to(String to) {
		return put(MailHeaders.TO, to);
	}

	public MailHeadersBuilder toExpression(String to) {
		return putExpression(MailHeaders.TO, to);
	}

	public MailHeadersBuilder cc(String cc) {
		return put(MailHeaders.CC, cc);
	}

	public MailHeadersBuilder ccExpression(String cc) {
		return putExpression(MailHeaders.CC, cc);
	}

	public MailHeadersBuilder bcc(String bcc) {
		return put(MailHeaders.BCC, bcc);
	}

	public MailHeadersBuilder bccExpression(String bcc) {
		return putExpression(MailHeaders.BCC, bcc);
	}

	public MailHeadersBuilder from(String from) {
		return put(MailHeaders.FROM, from);
	}

	public MailHeadersBuilder fromExpression(String from) {
		return putExpression(MailHeaders.FROM, from);
	}

	public MailHeadersBuilder replyTo(String replyTo) {
		return put(MailHeaders.REPLY_TO, replyTo);
	}

	public MailHeadersBuilder replyToExpression(String replyTo) {
		return putExpression(MailHeaders.REPLY_TO, replyTo);
	}

	/**
	 * @param multipartMode header value
	 * @return this
	 * @see org.springframework.mail.javamail.MimeMessageHelper
	 */
	public MailHeadersBuilder multipartMode(int multipartMode) {
		return put(MailHeaders.MULTIPART_MODE, multipartMode);
	}

	public MailHeadersBuilder multipartModeExpression(String multipartMode) {
		return putExpression(MailHeaders.MULTIPART_MODE, multipartMode);
	}

	public MailHeadersBuilder attachmentFilename(String attachmentFilename) {
		return put(MailHeaders.ATTACHMENT_FILENAME, attachmentFilename);
	}

	public MailHeadersBuilder attachmentFilenameExpression(String attachmentFilename) {
		return putExpression(MailHeaders.ATTACHMENT_FILENAME, attachmentFilename);
	}

	public MailHeadersBuilder contentType(String contentType) {
		return put(MailHeaders.CONTENT_TYPE, contentType);
	}

	public MailHeadersBuilder contentTypeExpression(String contentType) {
		return putExpression(MailHeaders.CONTENT_TYPE, contentType);
	}

	private MailHeadersBuilder putExpression(String key, String expression) {
		return put(key, PARSER.parseExpression(expression));
	}

}
