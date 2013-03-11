/*
 * Copyright 2002-2013 the original author or authors.
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
package org.springframework.integration.aws.core;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.util.StringUtils;

import com.amazonaws.AmazonWebServiceClient;


/**
 * The abstract factory class that will be used by all the client operations to acquire
 * the appropriate implementation of the {@link AmazonWebServiceClient} based on the URL
 * passed to the <i>getClient</i> method
 *
 * @author Amol Nayak
 *
 * @since 0.5
 *
 */
public abstract class AbstractAWSClientFactory<T extends AmazonWebServiceClient> implements AWSClientFactory<T> {

	/**
	 * A map storing the {@link AmazonWebServiceClient} endpoint as the key and the SQS Client as the
	 * value. Since setting the endpoint is not a thread safe operation once the client is instantiated,
	 * we maintain a map of endpoint and the {@link AmazonWebServiceClient} instantiated the first time a request
	 * for the designated endpoint is received
	 */
	private final ConcurrentHashMap<String, T> clientMap = new ConcurrentHashMap<String, T>();

	/**
	 * The String constant for HTTP
	 */
	protected final static String HTTP = "http://";

	/**
	 * The String constant for HTTPS
	 */
	protected final static String HTTPS = "https://";

	/**
	 * The String constant for SMTP
	 */
	protected final String SMTP = "smtp://";

	/**
	 * The default protocol to be used in case none is provided
	 */
	protected final static String DEFAULT_PROTOCOL = HTTPS;

	private T defaultEndpointInstance;


	/**
	 * Returns the cached implementation of the {@link AmazonWebServiceClient} based on the URL provided.
	 * the client instance is acquired using the abstract <i>getClientImplementation</i> method.
	 * The instance is added to the client map with the endpoint string as the key and the
	 * {@link AmazonWebServiceClient} as the value.
	 *
	 * @param url the URL for which the client is requested.
	 * @return the implementation of the {@link AmazonWebServiceClient} to be used for the provided url
	 */
	public final T getClient(String url) {
		String endpoint = getEndpointFromURL(url);
		if(endpoint == null) {
			if(defaultEndpointInstance == null) {
				defaultEndpointInstance = getClientImplementation();
			}
			return defaultEndpointInstance;
		}
		if(!clientMap.containsKey(endpoint)) {
			T client = getClientImplementation();
			client.setEndpoint(endpoint);
			T existingClient = clientMap.putIfAbsent(endpoint, client);
			if(existingClient != null) {
				//in rare scenarios where a new implementation was created after
				//checking for the existence of the endpoint in the  client map
				client = existingClient;
			}
			return client;
		}
		else {
			return clientMap.get(endpoint);
		}
	}

	/**
	 * Return a copy of the client map
	 * @return the copy of the clientMap
	 */
	public final Map<String, T> getClientMap() {
		return new HashMap<String, T>(clientMap);
	}

	/**
	 * Clears the complete cache
	 */
	public final void clear() {
		clientMap.clear();
	}

	/**
	 * Extracts the endpoint from the URL provided
	 *
	 * @return Will return null if the provided url is empty
	 */
	private String getEndpointFromURL(String stringUrl) {
		if(!StringUtils.hasText(stringUrl)) {
			return null;
		}
		String endpoint;
		try {
			if(!(stringUrl.startsWith(HTTP)
					|| stringUrl.startsWith(HTTPS)
					|| stringUrl.startsWith(SMTP))) {
				stringUrl = DEFAULT_PROTOCOL + stringUrl;
			}
			URL url = new URL(stringUrl);
			String host = url.getHost();
			String protocol = url.getProtocol();
			if(StringUtils.hasText(protocol)) {
				endpoint = protocol + "://" + host;
			}
			else {
				endpoint = host;
			}
		} catch (MalformedURLException e) {
			throw new AWSOperationException(null, "The URL \"" + stringUrl + "\" is malformed",e);
		}
		return endpoint;
	}

	/**
	 * The subclass needs to implement this method and return an appropriate implementation
	 */
	protected abstract T getClientImplementation();


}