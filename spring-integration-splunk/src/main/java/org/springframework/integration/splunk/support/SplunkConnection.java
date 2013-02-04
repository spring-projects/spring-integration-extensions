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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.springframework.integration.splunk.core.Connection;

import com.splunk.Service;

/**
 * Connection to Splunk service
 *
 * @author Jarred Li
 * @since 1.0
 *
 */
public class SplunkConnection implements Connection<Service> {

	private Service service;
	 

	public SplunkConnection(SplunkServer splunkServer) {
		final Map<String, Object> args = new HashMap<String, Object>();
		if (splunkServer.getHost() != null) {
			args.put("host", splunkServer.getHost());
		}
		if (splunkServer.getPort() != 0) {
			args.put("port", splunkServer.getPort());
		}
		if (splunkServer.getScheme() != null) {
			args.put("scheme", splunkServer.getScheme());
		}
		if (splunkServer.getApp() != null) {
			args.put("app", splunkServer.getApp());
		}
		if (splunkServer.getOwner() != null) {
			args.put("owner", splunkServer.getOwner());
		}

		args.put("username", splunkServer.getUsername());
		args.put("password", splunkServer.getPassword());
		
		 ExecutorService executor = Executors.newSingleThreadExecutor();
		 
		 Future<Service> future = executor.submit(new Callable<Service>(){
			public Service call() throws Exception {
				return Service.connect(args);
			}
		});
		
		 try {
			if (splunkServer.getTimeout() > 0) {
				service = future.get(splunkServer.getTimeout(),TimeUnit.MILLISECONDS);
			} else {
				service = future.get();
			}
		} catch (Exception e) {
			throw new RuntimeException(String.format("could not connect to Splunk Server @ %s:%d - %s",
					splunkServer.getHost(),splunkServer.getPort(),e.getMessage()));
		}
		
	}

	/* (non-Javadoc)
	 * @see org.springframework.integration.splunk.core.IService#close()
	 */
	public void close() {
		service.logout();
	}

	/* (non-Javadoc)
	 * @see org.springframework.integration.splunk.core.IService#isOpen()
	 */
	public boolean isOpen() {
		boolean result = true;
		try {
			service.getApplications();
		} catch (Throwable t) {
			result = false;
		}
		return result;
	}
	

	/* (non-Javadoc)
	 * @see org.springframework.integration.splunk.core.IService#getService()
	 */
	public Service getTarget() {
		return service;
	}

}
