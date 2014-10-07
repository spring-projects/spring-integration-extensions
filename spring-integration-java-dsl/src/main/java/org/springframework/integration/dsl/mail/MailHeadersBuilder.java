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

import org.springframework.integration.dsl.support.Function;
import org.springframework.integration.dsl.support.FunctionExpression;
import org.springframework.integration.dsl.support.MapBuilder;
import org.springframework.integration.mail.MailHeaders;
import org.springframework.messaging.Message;

/**
 * @author Artem Bilan
 * @author Gary Russell
 */
public class MailHeadersBuilder extends MapBuilder<MailHeadersBuilder, String, Object> {

	public MailHeadersBuilder subject(String subject) {
		return put(MailHeaders.SUBJECT, subject);
	}

	public MailHeadersBuilder subjectExpression(String subject) {
		return putExpression(MailHeaders.SUBJECT, subject);
	}

	public <P> MailHeadersBuilder subjectFunction(Function<Message<P>, String> subject) {
		return put(MailHeaders.SUBJECT, new FunctionExpression<Message<P>>(subject));
	}

	public MailHeadersBuilder to(String... to) {
		return put(MailHeaders.TO, to);
	}

	public MailHeadersBuilder toExpression(String to) {
		return putExpression(MailHeaders.TO, to);
	}

	public <P> MailHeadersBuilder toFunction(Function<Message<P>, String[]> to) {
		return put(MailHeaders.TO, new FunctionExpression<Message<P>>(to));
	}

	public MailHeadersBuilder cc(String... cc) {
		return put(MailHeaders.CC, cc);
	}

	public MailHeadersBuilder ccExpression(String cc) {
		return putExpression(MailHeaders.CC, cc);
	}

	public <P> MailHeadersBuilder ccFunction(Function<Message<P>, String[]> cc) {
		return put(MailHeaders.CC, new FunctionExpression<Message<P>>(cc));
	}

	public MailHeadersBuilder bcc(String... bcc) {
		return put(MailHeaders.BCC, bcc);
	}

	public MailHeadersBuilder bccExpression(String bcc) {
		return putExpression(MailHeaders.BCC, bcc);
	}

	public <P> MailHeadersBuilder bccFunction(Function<Message<P>, String[]> bcc) {
		return put(MailHeaders.BCC, new FunctionExpression<Message<P>>(bcc));
	}

	public MailHeadersBuilder from(String from) {
		return put(MailHeaders.FROM, from);
	}

	public MailHeadersBuilder fromExpression(String from) {
		return putExpression(MailHeaders.FROM, from);
	}

	public <P> MailHeadersBuilder fromFunction(Function<Message<P>, String> from) {
		return put(MailHeaders.FROM, new FunctionExpression<Message<P>>(from));
	}

	public MailHeadersBuilder replyTo(String replyTo) {
		return put(MailHeaders.REPLY_TO, replyTo);
	}

	public MailHeadersBuilder replyToExpression(String replyTo) {
		return putExpression(MailHeaders.REPLY_TO, replyTo);
	}

	public <P> MailHeadersBuilder replyToFunction(Function<Message<P>, String> replyTo) {
		return put(MailHeaders.REPLY_TO, new FunctionExpression<Message<P>>(replyTo));
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

	public <P> MailHeadersBuilder multipartModeFunction(Function<Message<P>, Integer> multipartMode) {
		return put(MailHeaders.MULTIPART_MODE, new FunctionExpression<Message<P>>(multipartMode));
	}

	public MailHeadersBuilder attachmentFilename(String attachmentFilename) {
		return put(MailHeaders.ATTACHMENT_FILENAME, attachmentFilename);
	}

	public MailHeadersBuilder attachmentFilenameExpression(String attachmentFilename) {
		return putExpression(MailHeaders.ATTACHMENT_FILENAME, attachmentFilename);
	}

	public <P> MailHeadersBuilder attachmentFilenameFunction(Function<Message<P>, String> attachmentFilename) {
		return put(MailHeaders.ATTACHMENT_FILENAME, new FunctionExpression<Message<P>>(attachmentFilename));
	}

	public MailHeadersBuilder contentType(String contentType) {
		return put(MailHeaders.CONTENT_TYPE, contentType);
	}

	public MailHeadersBuilder contentTypeExpression(String contentType) {
		return putExpression(MailHeaders.CONTENT_TYPE, contentType);
	}

	public <P> MailHeadersBuilder contentTypeFunction(Function<Message<P>, String> contentType) {
		return put(MailHeaders.CONTENT_TYPE, new FunctionExpression<Message<P>>(contentType));
	}

	private MailHeadersBuilder putExpression(String key, String expression) {
		return put(key, PARSER.parseExpression(expression));
	}

	MailHeadersBuilder() {
	}

}
