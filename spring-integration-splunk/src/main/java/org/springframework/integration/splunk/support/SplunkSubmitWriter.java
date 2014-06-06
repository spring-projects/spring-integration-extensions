/*
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

import java.io.IOException;
import java.net.Socket;

import org.springframework.integration.splunk.core.ServiceFactory;
import org.springframework.integration.splunk.event.SplunkEvent;

import com.splunk.Args;
import com.splunk.Index;
import com.splunk.Receiver;
import com.splunk.Service;

/**
 * @author David Turanski
 * 
 */
public class SplunkSubmitWriter extends AbstractSplunkDataWriter {

	private String index;

	/**
	 * @param serviceFactory
	 * @param args
	 */
	public SplunkSubmitWriter(ServiceFactory serviceFactory, Args args) {
		super(serviceFactory, args);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.integration.splunk.support.SplunkDataWriter#doWrite
	 * (org.springframework.integration.splunk.event.SplunkEvent,
	 * java.net.Socket, com.splunk.Service, com.splunk.Args)
	 */
	@Override
	protected void doWrite(SplunkEvent event, Socket socket, Service service,
			Args args) throws IOException {

		Index index = getIndex();
		if (index != null) {
			index.submit(args, event.toString());
		} else {
			Receiver receiver = service.getReceiver();
			receiver.submit(args, event.toString());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.integration.splunk.support.SplunkDataWriter#createSocket
	 * (com.splunk.Service)
	 */
	@Override
	protected Socket createSocket(Service service) throws IOException {
		return null;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	private Index getIndex() {
		return (index == null) ? null : service.getIndexes().get(index);
	}

}
