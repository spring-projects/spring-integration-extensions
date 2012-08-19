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
package org.springframework.integration.aws.ses;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.integration.aws.ses.core.AmazonSESMailSendException;
import org.springframework.integration.aws.ses.core.AmazonSESSimpleMailMessage;
import org.springframework.util.StringUtils;

/**
 * Builder class for building the Mail message.
 *
 * @author Amol Nayak
 *
 * @since 1.0
 *
 */
public class AmazonSESMailMessageBuilder {

	private String accessKey;
	private String subject;
	private String message;
	private String fromAddress;
	private List<String> toAddress;
	private List<String> ccAddress;
	private List<String> bccAddress;
	private List<String> replyToAddress;
	private boolean isHtml;


	/**.
	 * Default constructor to prevent instantiation
	 */
	private AmazonSESMailMessageBuilder() {

	}

	/**.
	 * New instance with the provided subject and message
	 * @param subject
	 * @param message
	 * @return
	 */
	public static AmazonSESMailMessageBuilder newBuilder(String accessKey) {
		AmazonSESMailMessageBuilder builder = new AmazonSESMailMessageBuilder();
		builder.accessKey = accessKey;
		return builder;
	}

	/**.
	 * Add a to address to the mail, can be of type String or Collection<String>
	 * @param param
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public AmazonSESMailMessageBuilder withToAddress(Object param) {
		if(param != null) {
			if(param instanceof String)
				toAddress =  Collections.singletonList((String)param);
			else if (isCollectionOfString(param))
				toAddress = new ArrayList<String>((Collection<String>)param);
			else
				throw new AmazonSESMailSendException(accessKey,
						"\"From Email Id\" is Expected to be String or Collection<String>, found "
						+ param.getClass().getCanonicalName(),
						null, null);
		}
		return this;
	}

	/**
	 * Add the From Address field, can be of type String
	 * @param param
	 * @return
	 */
	public AmazonSESMailMessageBuilder withFromAddress(Object param) {
		if(param != null) {
			if(param instanceof String) {
				if(StringUtils.hasText((String)param))
					fromAddress = (String)param;
				else
					throw new AmazonSESMailSendException(accessKey,
							"\"From Email Id\" is mandatory and cannot be empty",
							null, null);
			}
			else
				throw new AmazonSESMailSendException(accessKey,
						"\"From Email Id\" is Expected to be String, found " + param.getClass().getCanonicalName(),
						null, null);
		} else {
			throw new AmazonSESMailSendException(accessKey,
					"\"From Email Id\" is mandatory and cannot be null",
					null, null);
		}
		return this;
	}

	/**.
	 * Add a BCC address to the mail, can be of type String or Collection<String>
	 * @param param
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public AmazonSESMailMessageBuilder withBccAddress(Object param) {
		if(param != null) {
			if(param instanceof String)
				bccAddress =  Collections.singletonList((String)param);
			else if (isCollectionOfString(param))
				bccAddress = new ArrayList<String>((Collection<String>)param);
			else
				throw new AmazonSESMailSendException(accessKey,
						"\"BCC Email Id\" is Expected to be String or List<String>, found "
						+ param.getClass().getCanonicalName(),
						null, null);
		}
		return this;
	}

	/**.
	 * Add a CC address to the mail, can be of type String or Collection<String>
	 * @param param
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public AmazonSESMailMessageBuilder withCCAddress(Object param) {
		if(param != null) {
			if(param instanceof String)
				ccAddress =  Collections.singletonList((String)param);
			else if (isCollectionOfString(param))
				ccAddress = new ArrayList<String>((Collection<String>)param);
			else
				throw new AmazonSESMailSendException(accessKey,
						"\"CC Email Id\" is Expected to be String or Collection<String>, found "
						+ param.getClass().getCanonicalName(),
						null, null);
		}
		return this;
	}

	/**.
	 * Add a CC address to the mail, can be of type String or Collection<String>
	 * @param param
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public AmazonSESMailMessageBuilder withReplyToAddress(Object param) {
		if(param != null) {
			if(param instanceof String)
				replyToAddress =  Collections.singletonList((String)param);
			else if (isCollectionOfString(param))
				replyToAddress = new ArrayList<String>((Collection<String>)param);
			else
				throw new AmazonSESMailSendException(accessKey,
						"\"Reply To Email Id\" is Expected to be String or Collection<String>, found "
						+ param.getClass().getCanonicalName(),
						null, null);
		}
		return this;
	}


	/**.
	 * Sets the subject of the mail message being constructed
	 * @param subject
	 * @return
	 */
	public AmazonSESMailMessageBuilder withSubject(String subject) {
		this.subject = subject;
		return this;
	}

	/**.
	 * Sets the content of the mail message being constructed
	 * @param subject
	 * @return
	 */
	public AmazonSESMailMessageBuilder withMessage(String message) {
		this.message = message;
		return this;
	}

	/**.
	 * Sets whether the mail message is a HTML mail message
	 * @return
	 */
	public AmazonSESMailMessageBuilder withIsHtml(Object param) {
		if(param != null) {
			if(param instanceof Boolean) {
				isHtml = (Boolean)param;
			} else if(param instanceof String) {
				String strValue = (String)param;
				isHtml = "Y".equalsIgnoreCase(strValue)
				|| "YES".equalsIgnoreCase(strValue)
				|| "TRUE".equalsIgnoreCase(strValue);
			} else if(param instanceof Character) {
				Character charc = (Character)param;
				isHtml = 'Y' == charc.charValue() || 'y' == charc.charValue();
			} else
				throw new AmazonSESMailSendException(accessKey,
						"\"isHtml\" is Expected to be Boolean,String or Character, found "
						+ param.getClass().getCanonicalName(),
						null, null);
		}
		return this;
	}

	/**
	 * Builds the {@link AmazonSESSimpleMailMessage} instance
	 */
	public AmazonSESSimpleMailMessage build() {
		AmazonSESSimpleMailMessage msg = new AmazonSESSimpleMailMessage();
		if(isEmpty(toAddress) && isEmpty(ccAddress) && isEmpty(bccAddress))
			throw new AmazonSESMailSendException(accessKey,
					"At least one of toAddress, fromAddress or bccAddress is mandatory",
					null, null);

		if(!StringUtils.hasText(fromAddress))
			throw new AmazonSESMailSendException(accessKey,
					"From address is mandatory",
					null, null);

		msg.setSubject(subject);
		msg.setMessage(message);
		msg.setToList(toAddress);
		msg.setFrom(fromAddress);
		msg.setCcList(ccAddress);
		msg.setBccList(bccAddress);
		msg.setReplyTo(replyToAddress);
		msg.setHtml(isHtml);
		return msg;
	}

	/**.
	 * Helper method that finds if the passed parameter is of type
	 * {@link Collection} or {@link String}
	 * @param param
	 * @return
	 */
	private boolean isCollectionOfString(Object param) {
		if(param instanceof Collection<?>) {
			Collection<?> collection = (Collection<?>)param;
			if(!collection.isEmpty() && collection.iterator().next() instanceof String)
				return true;
		}
		return false;
	}

	/**
	 * Checks if the givem collection is empty
	 * @param collection
	 * @return
	 */
	private boolean isEmpty(Collection<?> collection) {
		return collection == null || collection.isEmpty();
	}

}
