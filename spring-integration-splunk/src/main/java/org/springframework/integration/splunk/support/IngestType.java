/*
 * Copyright 2011-2012 the original author or authors.
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

/**
 * Method of pushing data into Splunk.
 *
 * Stream: Establish a connection, keep it open, and stream events until the connection is closed.Better for high volume input.
 * Tcp: Create raw socket and send event data into the socket
 * Submit: Send event data into Splunk with HTTP REST api
 *
 * @author Jarred Li
 * @since 1.0
 *
 */
public enum IngestType {
	stream("stream"), tcp("tcp"), submit("submit");

	private String type;

	IngestType(String ingestType) {
		this.type = ingestType;
	}

	public String getIngestType() {
		return type;
	}
}