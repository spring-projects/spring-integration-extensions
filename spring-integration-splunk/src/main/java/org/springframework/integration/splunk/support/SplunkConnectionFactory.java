/*
 * Copyright 2011-2013 the original author or authors.
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

import org.springframework.integration.splunk.core.Connection;
import org.springframework.integration.splunk.core.ConnectionFactory;

import com.splunk.Service;

/**
 * Factory to create Splunk connection.
 *
 * @author Jarred Li
 * @author David Turanski
 * @since 1.0
 *
 */
public class SplunkConnectionFactory implements ConnectionFactory<Service> {

	private SplunkServer splunkServer;
	private SplunkConnection connection;
	public SplunkConnectionFactory(SplunkServer server) {
		this.splunkServer = server;
	}

	/* (non-Javadoc)
	 * @see org.springframework.integration.splunk.core.ServiceFactory#getService()
	 */
	public synchronized Connection<Service> getConnection() throws Exception {
		if (connection == null || !connection.isOpen()) {
			connection = new SplunkConnection(splunkServer);
		}
		return connection;
	}

}
