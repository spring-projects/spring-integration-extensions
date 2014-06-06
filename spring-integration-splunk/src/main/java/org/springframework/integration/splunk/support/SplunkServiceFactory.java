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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.DatatypeConverter;

import com.splunk.Service;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.integration.splunk.core.ServiceFactory;
import org.springframework.util.Assert;

/**
 * A {@link FactoryBean} for creating a {@link Service}
 *
 * @author David Turanski
 * @author Olivier Lamy
 */
public class SplunkServiceFactory implements ServiceFactory {

	private static final Log LOGGER = LogFactory.getLog(SplunkServiceFactory.class);

	private final List<SplunkServer> splunkServers;

	private final Map<SplunkServer, Service> servicePerServer = new ConcurrentHashMap<SplunkServer, Service>();


	public SplunkServiceFactory(SplunkServer splunkServer) {
		Assert.notNull(splunkServer);
		this.splunkServers = Arrays.asList(splunkServer);
	}

	/**
	 * @param splunkServers the {@code List<SplunkServer>} to build this {@code SplunkServiceFactory}
	 * @since 1.1
	 */
	public SplunkServiceFactory(List<SplunkServer> splunkServers) {
		Assert.notEmpty(splunkServers);
		this.splunkServers = new ArrayList<SplunkServer>(splunkServers);
	}

	@Override
	public synchronized Service getService() {
		return getServiceInternal();
	}

	private Service getServiceInternal() {

		for (SplunkServer splunkServer : splunkServers) {
			Service service = servicePerServer.get(splunkServer);
			// service already exist and no test on borrow it so simply use it

			if (service != null) {
				if (!splunkServer.isCheckServiceOnBorrow() || pingService(service)) {
					return service;
				}
				else {
					// fail so try next server
					continue;
				}
			}

			ExecutorService executor = Executors.newSingleThreadExecutor();

			Callable<Service> callable = buildServiceCallable(splunkServer);

			Future<Service> future = executor.submit(callable);

			try {
				if (splunkServer.getTimeout() > 0) {
					service = future.get(splunkServer.getTimeout(), TimeUnit.MILLISECONDS);
				}
				else {
					service = future.get();
				}

				servicePerServer.put(splunkServer, service);
				return service;
			}
			catch (Exception e) {
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info(String.format("could not connect to Splunk Server @ %s:%d - %s, try next one",
							splunkServer.getHost(), splunkServer.getPort(), e.getMessage()));
				}
			}
		}
		String message = String.format("could not connect to any of Splunk Servers %s", this.splunkServers);
		LOGGER.error(message);
		throw new RuntimeException(message);
	}

	private Callable<Service> buildServiceCallable(SplunkServer splunkServer) {
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

		String auth = splunkServer.getUsername() + ":" + splunkServer.getPassword();
		String authToken = "Basic " + DatatypeConverter.printBase64Binary(auth.getBytes());
		args.put("token", authToken);

		return new Callable<Service>() {
			public Service call()
					throws Exception {
				return Service.connect(args);
			}
		};
	}

	private boolean pingService(Service service) {
		try {
			service.getInfo();
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}

}
