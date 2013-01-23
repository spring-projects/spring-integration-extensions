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
package org.springframework.integration.splunk.support;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.integration.Message;
import org.springframework.integration.MessageHandlingException;
import org.springframework.integration.MessagingException;
import org.springframework.integration.splunk.core.DataReader;
import org.springframework.integration.splunk.core.DataWriter;
import org.springframework.integration.splunk.event.SplunkEvent;

/**
 * Bundles common core logic for the Splunk components.
 *
 * @author Jarred Li
 * @since 1.0
 *
 */
public class SplunkExecutor implements InitializingBean {

	private static final Log logger = LogFactory.getLog(SplunkExecutor.class);

	private DataReader reader;
	private DataWriter writer;

	public SplunkExecutor() {
	}

	/**
	 * Verifies and sets the parameters. E.g. initializes the to be used
	 */
	public void afterPropertiesSet() {

	}

	/**
	 * Executes the outbound Splunk Operation.
	 *
	 */
	public Object executeOutboundOperation(final Message<?> message) {
		try {
			SplunkEvent payload = (SplunkEvent) message.getPayload();
			writer.write(payload);
		} catch (Exception e) {
			String errorMsg = "error in writing data into Splunk";
			logger.warn(errorMsg, e);
			throw new MessageHandlingException(message, errorMsg, e);
		}
		return null;
	}

	public void handleMessage(final Message<?> message) {
		executeOutboundOperation(message);
	}

	/**
	 * Execute the Splunk operation.
	 */
	public List<SplunkEvent> poll() {
		logger.debug("poll start:");
		List<SplunkEvent> queryData = null;
		try {
			queryData = reader.search();
		} catch (Exception e) {
			String errorMsg = "search Splunk data failed";
			logger.warn(errorMsg, e);
			throw new MessagingException(errorMsg, e);
		}
		return queryData;
	}

	public void setReader(DataReader reader) {
		this.reader = reader;
	}

	public void setWriter(DataWriter writer) {
		this.writer = writer;
	}


}
