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
package org.springframework.integration.splunk.support;

import com.splunk.Args;

/**
 * Build {@link Args} instance. Supports method chaining
 * @author David Turanski
 *
 */
public class ArgsBuilder {
	
	private String sourceType;
	private String source;
	private String host;
	private String hostRegex;

	public Args build() {
		Args args = new Args();
		if (sourceType != null) {
			args.put("sourcetype", sourceType);
		}
		if (source != null) {
			args.put("source", source);
		}

		if (host != null) {
			args.put("host", host);
		}

		if (hostRegex != null) {
			args.put("host_regex", hostRegex);
		}
		return args;
	}

	/**
	 * @param sourceType the sourceType to set
	 */
	public ArgsBuilder setSourceType(String sourceType) {
		this.sourceType = sourceType;
		return this;
	}

	/**
	 * @param source the source to set
	 */
	public ArgsBuilder setSource(String source) {
		this.source = source;
		return this;
	}

	/**
	 * @param host the host to set
	 */
	public ArgsBuilder setHost(String host) {
		this.host = host;
		return this;
	}

	/**
	 * @param hostRegex the hostRegex to set
	 */
	public ArgsBuilder setHostRegex(String hostRegex) {
		this.hostRegex = hostRegex;
		return this;
	}
}
