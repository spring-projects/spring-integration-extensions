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

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.integration.splunk.core.Connection;
import org.springframework.integration.splunk.core.DataWriter;
import org.springframework.integration.splunk.core.ConnectionFactory;
import org.springframework.integration.splunk.entity.SplunkData;
import org.springframework.util.Assert;

import com.splunk.Args;
import com.splunk.Index;
import com.splunk.Receiver;
import com.splunk.Service;

/**
 * Data writer to write data into Splunk. There are 3 ways to write data:
 * REST submit, TCP socket and HTTP stream.
 *
 * @author Jarred Li
 * @since 1.0
 *
 */
public class SplunkDataWriter implements DataWriter, InitializingBean {

	private static final Log logger = LogFactory.getLog(SplunkDataWriter.class);

	private ConnectionFactory<Service> connectionFactory;

	private String sourceType;

	private String source;

	private String index;

	private IngestType ingest = IngestType.stream; //tcp, stream, submit

	private int tcpPort;

	private String host;

	private String hostRegex;

	public SplunkDataWriter(ConnectionFactory<Service> f) {
		this.connectionFactory = f;
	}

	public void write(SplunkData data) throws Exception {
		logger.debug("write message to splunk:" + data);

		Connection<Service> connection = connectionFactory.getConnection();
		Service service = connection.getTarget();
		Index indexObject = null;
		Receiver receiver = null;
		OutputStream ostream;
		Socket socket;
		Writer writer = null;

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

		try {
			if (index != null) {
				indexObject = service.getIndexes().get(index);
			}
			else {
				receiver = service.getReceiver();
			}

			if ((ingest.equals(IngestType.stream) || ingest.equals(IngestType.tcp))) {
				if (ingest.equals("stream")) {
					if (indexObject != null)
						socket = indexObject.attach(args);
					else
						socket = receiver.attach(args);
				}
				else {
					socket = service.open(tcpPort);
				}
				ostream = socket.getOutputStream();
				writer = new OutputStreamWriter(ostream, "UTF8");
			}

			if ((ingest.equals(IngestType.stream) || ingest.equals(IngestType.tcp))) {
				writer.write(data.toString());
				writer.flush();
				writer.close();
			}
			else {
				if (index != null) {
					indexObject.submit(args, data.toString());
				}
				else {
					receiver.submit(args, data.toString());
				}
			}
		} finally {
			connection.close();
		}

	}


	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public void setIngest(IngestType ingest) {
		this.ingest = ingest;
	}

	public void setTcpPort(int tcpPort) {
		this.tcpPort = tcpPort;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setHostRegex(String hostRegex) {
		this.hostRegex = hostRegex;
	}


	public String getSourceType() {
		return sourceType;
	}

	public String getSource() {
		return source;
	}

	public String getIndex() {
		return index;
	}

	public IngestType getIngest() {
		return ingest;
	}

	public int getTcpPort() {
		return tcpPort;
	}

	public String getHost() {
		return host;
	}

	public String getHostRegex() {
		return hostRegex;
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(ingest, "You must specify ingest type");
	}

}
