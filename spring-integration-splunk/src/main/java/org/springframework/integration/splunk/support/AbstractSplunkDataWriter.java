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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.integration.splunk.core.Connection;
import org.springframework.integration.splunk.core.ConnectionFactory;
import org.springframework.integration.splunk.core.DataWriter;
import org.springframework.integration.splunk.event.SplunkEvent;
import org.springframework.util.Assert;

import com.splunk.Args;
import com.splunk.Service;

/**
 * Base class for {@link DataWriter}s to write data into Splunk.
 *
 * @author Jarred Li
 * @author David Turanski
 * @since 1.0
 *
 */
public abstract class AbstractSplunkDataWriter implements DataWriter, SmartLifecycle {

	protected final Log logger = LogFactory.getLog(getClass());

	protected ConnectionFactory<Service> connectionFactory;
	 
	protected Socket socket;
	
	protected Service service;

	protected Args args;

	private boolean running;

	private int phase;

	private boolean autoStartup = true;
	

	 
	protected AbstractSplunkDataWriter(ConnectionFactory<Service> connectionFactory, Args args) {
		Assert.notNull(connectionFactory,"connectionFactory cannot be null");
		this.connectionFactory = connectionFactory;
		
		Assert.notNull(args, "args cannot be null");
		this.args = args;
	}

	public void write(SplunkEvent event) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("writing event to splunk:" + event);
		}
		 
		doWrite(event, socket, service, args);
	}

   protected void doWrite(SplunkEvent event, Socket socket, Service service, Args args) throws IOException {
	 	OutputStream ostream = socket.getOutputStream();
		Writer writer = new OutputStreamWriter(ostream, "UTF8");
		writer.write(event.toString());
		writer.flush();
   }

   protected abstract Socket createSocket(Service service) throws IOException;

 
	public Args getArgs() {
		return args;
	}

	/* (non-Javadoc)
	 * @see org.springframework.context.Lifecycle#start()
	 */
	public synchronized void start() {
		try {
		Connection<Service> connection = connectionFactory.getConnection();
		this.service = connection.getTarget();
		 
		socket = createSocket(service);
		 
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		this.running = true;
	}

	/* (non-Javadoc)
	 * @see org.springframework.context.Lifecycle#stop()
	 */
	public synchronized void stop() {
		if (!running) {
			return;
		}
		try {
			if (socket != null) {
				socket.close();
			}
	
			connectionFactory.getConnection().close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		this.running = false;
	}

	/* (non-Javadoc)
	 * @see org.springframework.context.Lifecycle#isRunning()
	 */
	public boolean isRunning() {
		return this.running;
	}

	/* (non-Javadoc)
	 * @see org.springframework.context.Phased#getPhase()
	 */
	public int getPhase() {
		return this.phase;
	}
	
	public void setPhase(int phase) {
		this.phase = phase;
	}

	/* (non-Javadoc)
	 * @see org.springframework.context.SmartLifecycle#isAutoStartup()
	 */
	public boolean isAutoStartup() {
		return this.autoStartup;
	}
	
	public void setAutoStartup(boolean autoStartup) {
		this.autoStartup = autoStartup; 
	}

	/* (non-Javadoc)
	 * @see org.springframework.context.SmartLifecycle#stop(java.lang.Runnable)
	 */
	public synchronized void stop(Runnable callback) {
		this.stop();
		callback.run();
	}
}
