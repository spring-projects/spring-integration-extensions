/*
 * Copyright 2002-2012 the original author or authors.
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

import org.springframework.integration.x.ip.serializer.DataFrame;

/**
 * @author Gary Russell
 * @since 3.0
 *
 */
public class WebSocketFrame extends DataFrame {

	public static final int TYPE_FRAGMENTED_CONTROL = 256;

	public static final int TYPE_INVALID_UTF8 = 512;

	public static final int TYPE_PING = 5;

	public static final int TYPE_PONG = 6;

	public static final int TYPE_OPEN = 7;

	public static final int TYPE_CLOSE = 8;

	private static final String[] typeToString = new String[] {
		"Invalid", "Headers", "**", "**", "Data", "Ping", "Pong", "Open", "Close"
	};

	private volatile short status = -1;

	private volatile int rsv;

	public WebSocketFrame(int type, String payload) {
		super(type, payload);
	}

	public WebSocketFrame(int type, byte[] binary) {
		super(type, binary);
	}

	public WebSocketFrame(int type, String payload, byte[] binary) {
		super(type, payload, binary);
	}

	public short getStatus() {
		return status;
	}

	public void setStatus(short status) {
		this.status = status;
	}

	public void setRsv(int rsv) {
		this.rsv = rsv;
	}

	public int getRsv() {
		return rsv;
	}

	@Override
	public String toString() {
		int len = 0;
		boolean trunc = false;
		if (this.payload!= null) {
			len = Math.min(100, payload.length());
			trunc = len < payload.length();
		}
		String typeAsString;
		if (type < typeToString.length) {
			typeAsString = typeToString[type & 0xff];
		}
		else {
			typeAsString = Integer.toString(type);
		}
		return "WebSocketFrame [type=" + typeAsString + (payload == null ? "" : ", payload=" + payload.substring(0, len) +
				(trunc ? "..." : "")) +
				", binary=" + binary +
				(binary != null ? ", binary.length=" + binary.length : "") +
				", status=" + status + ", rsv=" + rsv + "]";
	}

}
