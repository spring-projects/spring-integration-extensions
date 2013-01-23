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
package org.springframework.integration.splunk.event;

/**
 * @author David Turanski
 *
 */
@SuppressWarnings("serial")
public class ProxyEvent extends SplunkEvent {
	// ----------------------------------
		// Proxy
		// ----------------------------------

		/**
		 * The action taken by the proxy.
		 */
		public static String PROXY_ACTION = "action";
		/**
		 * The destination of the network traffic (the remote host).
		 */
		public static String PROXY_DEST = "dest";
		/**
		 * The content-type of the requested HTTP resource.
		 */
		public static String PROXY_HTTP_CONTENT_TYPE = "http_content_type";
		/**
		 * The HTTP method used to request the resource.
		 */
		public static String PROXY_HTTP_METHOD = "http_method";
		/**
		 * The HTTP referrer used to request the HTTP resource.
		 */
		public static String PROXY_HTTP_REFER = "http_refer";
		/**
		 * The HTTP response code.
		 */
		public static String PROXY_HTTP_RESPONSE = "http_response";
		/**
		 * The user agent used to request the HTTP resource.
		 */
		public static String PROXY_HTTP_USER_AGENT = "http_user_agent";
		/**
		 * The product name of the vendor technology generating Network Protection
		 * data, such as IDP, Providentia, and ASA.
		 */
		public static String PROXY_PRODUCT = "product";
		/**
		 * The source of the network traffic (the client requesting the connection).
		 */
		public static String PROXY_SRC = "src";
		/**
		 * The HTTP response code indicating the status of the proxy request.
		 */
		public static String PROXY_STATUS = "status";
		/**
		 * The user that requested the HTTP resource.
		 */
		public static String PROXY_USER = "user";
		/**
		 * The URL of the requested HTTP resource.
		 */
		public static String PROXY_URL = "url";
		/**
		 * The vendor technology generating Network Protection data, such as IDP,
		 * Providentia, and ASA.
		 */
		public static String PROXY_VENDOR = "vendor";


		public void setProxyAction(String proxyAction) {
			addPair(PROXY_ACTION, proxyAction);
		}

		public void setProxyDest(String proxyDest) {
			addPair(PROXY_DEST, proxyDest);
		}

		public void setProxyHttpContentType(String proxyHttpContentType) {
			addPair(PROXY_HTTP_CONTENT_TYPE, proxyHttpContentType);
		}

		public void setProxyHttpMethod(String proxyHttpMethod) {
			addPair(PROXY_HTTP_METHOD, proxyHttpMethod);
		}

		public void setProxyHttpRefer(String proxyHttpRefer) {
			addPair(PROXY_HTTP_REFER, proxyHttpRefer);
		}

		public void setProxyHttpResponse(int proxyHttpResponse) {
			addPair(PROXY_HTTP_RESPONSE, proxyHttpResponse);
		}

		public void setProxyHttpUserAgent(String proxyHttpUserAgent) {
			addPair(PROXY_HTTP_USER_AGENT, proxyHttpUserAgent);
		}

		public void setProxyProduct(String proxyProduct) {
			addPair(PROXY_PRODUCT, proxyProduct);
		}

		public void setProxySrc(String proxySrc) {
			addPair(PROXY_SRC, proxySrc);
		}

		public void setProxyStatus(int proxyStatus) {
			addPair(PROXY_STATUS, proxyStatus);
		}

		public void setProxyUser(String proxyUser) {
			addPair(PROXY_USER, proxyUser);
		}

		public void setProxyUrl(String proxyUrl) {
			addPair(PROXY_URL, proxyUrl);
		}

		public void setProxyVendor(String proxyVendor) {
			addPair(PROXY_VENDOR, proxyVendor);
		}

}
