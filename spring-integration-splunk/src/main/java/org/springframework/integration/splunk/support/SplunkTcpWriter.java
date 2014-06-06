/*
 * Copyright 2002-2014 the original author or authors.
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

import java.io.IOException;
import java.net.Socket;

import com.splunk.Args;
import com.splunk.Input;
import com.splunk.Service;

import org.springframework.integration.splunk.core.ServiceFactory;
import org.springframework.util.Assert;

/**
 *
 * A {@code org.springframework.integration.splunk.core.DataWriter}
 * that creates a socket on a given port
 *
 * @author David Turanski
 *
 */
public class SplunkTcpWriter extends AbstractSplunkDataWriter {
	private int port;

	public SplunkTcpWriter(ServiceFactory serviceFactory, Args args) {
		super(serviceFactory, args);
	}

	@Override
	protected Socket createSocket(Service service) throws IOException {

		Input input = service.getInputs().get(String.valueOf(port));
		Assert.notNull(input, "no input defined for port " + port);
		Assert.isTrue(!input.isDisabled(),String.format("input on port %d is disabled",port));
		return service.open(port);
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}


}
