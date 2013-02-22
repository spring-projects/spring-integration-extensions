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
package org.springframework.integration.x.ip.websocket;

import org.springframework.beans.DirectFieldAccessor;
import org.springframework.integration.ip.tcp.connection.TcpConnectionEvent;
import org.springframework.integration.ip.tcp.connection.TcpConnectionSupport;

/**
 * @author Gary Russell
 * @since 3.0
 *
 */
public class WebSocketEvent extends TcpConnectionEvent {

	private static final long serialVersionUID = -6788341703196233248L;

	public enum WebSocketEventType implements EventType {
		HANDSHAKE_COMPLETE,
		WEBSOCKET_CLOSED
	}

	private final String path;

	private final String queryString;

	public WebSocketEvent(TcpConnectionSupport connection, WebSocketEventType type, String path, String queryString) {
		super(connection, type, (String) new DirectFieldAccessor(connection).getPropertyValue("connectionFactoryName"));
		this.path = path;
		this.queryString = queryString;
	}

	public String getPath() {
		return path;
	}

	public String getQueryString() {
		return queryString;
	}

	@Override
	public String toString() {
		return super.toString().replace("TcpConnectionEvent", "WebSocketEvent")
				.replace("]", ", path=" + this.path + ", queryString=" + this.queryString + "]");
	}

}
