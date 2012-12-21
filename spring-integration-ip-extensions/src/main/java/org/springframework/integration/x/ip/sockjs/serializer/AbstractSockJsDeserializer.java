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
package org.springframework.integration.x.ip.sockjs.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.springframework.integration.x.ip.serializer.AbstractHttpSwitchingDeserializer;
import org.springframework.integration.x.ip.serializer.DataFrame;
import org.springframework.integration.x.ip.sockjs.support.SockJsFrame;
import org.springframework.integration.x.ip.websocket.WebSocketFrame;
import org.springframework.util.Assert;

/**
 * @author Gary Russell
 * @since 3.0
 *
 */
public abstract class AbstractSockJsDeserializer extends AbstractHttpSwitchingDeserializer {

	protected WebSocketFrame decodeToFrame(String data) {
		if (data.length() == 1 && data.equals("h")) {
			System.out.println("Received:SockJS-Heartbeat");
			return new WebSocketFrame(SockJsFrame.TYPE_HEARTBEAT, data);
		}
		else if (data.length() == 0x800 && data.startsWith("hhhhhhhhhhhhh")) {
			System.out.println("Received:SockJS-XHR-Prelude");
			return new WebSocketFrame(SockJsFrame.TYPE_PRELUDE, data);
		}
		else if (data.length() == 1 && data.equals("o")) {
			System.out.println("Received:SockJS-Open");
			return new WebSocketFrame(SockJsFrame.TYPE_OPEN, data);
		}
		else if (data.length() > 0 && data.startsWith("c")) {
			System.out.println("Received SockJS-Close:" + data.substring(1));
			return new WebSocketFrame(SockJsFrame.TYPE_CLOSE, data.substring(1));
		}
		else if (data.length() > 0 && data.startsWith("a")) {
			System.out.println("Received data:" + data.substring(1));
			return new WebSocketFrame(SockJsFrame.TYPE_DATA, data.substring(1));
		}
		else {
			System.out.println("Received unexpected:" + new String(data));
			return new WebSocketFrame(SockJsFrame.TYPE_UNEXPECTED, data);
		}
	}

	@Override
	protected org.springframework.integration.x.ip.serializer.AbstractHttpSwitchingDeserializer.BasicState createState() {
		return new SockJsState();
	}

	@Override
	protected List<DataFrame> decodeHeaders(String frameData, BasicState basicState, List<DataFrame> dataList) {
		Assert.isInstanceOf(SockJsState.class, basicState);
		SockJsState state = (SockJsState) basicState;
		List<DataFrame> decodedHeaders = super.decodeHeaders(frameData, state, dataList);
		String[] headers = frameData.split("\\r\\n");
		String cookies = "Cookie: ";
		for (String header : headers) {
			if (header.startsWith("Set-Cookie")) {
				String[] bits = header.split(": *");
				cookies += bits[1] + "; ";
			}
			else if (header.startsWith("Content-Encoding:") && header.contains("gzip")) {
				state.setGzipping(true);
			}
		}
		if (cookies.length() > 8) {
			decodedHeaders.add(new SockJsFrame(SockJsFrame.TYPE_COOKIES, cookies));
		}
		return decodedHeaders;
	}


	public static class SockJsState extends BasicState {

		protected volatile boolean gzipping;

		private volatile GZIPInputStream gzipInputStream;

		private volatile GZIPFeederInputStream gzipFeederInputStream;

		boolean isGzipping() {
			return gzipping;
		}

		void setGzipping(boolean gzipping) {
			this.gzipping = gzipping;
		}

		GZIPInputStream getGzipInputStream() throws IOException {
			if (this.gzipInputStream == null) {
				this.gzipInputStream = new GZIPInputStream(this.gzipFeederInputStream);
			}
			return this.gzipInputStream;
		}

		GZIPFeederInputStream getGzipFeederInputStream() {
			if (this.gzipFeederInputStream == null) {
				this.gzipFeederInputStream = new GZIPFeederInputStream();
			}
			return gzipFeederInputStream;
		}

	}

	public static class GZIPFeederInputStream extends InputStream {

		private volatile InputStream inputStream;


		void setInputStream(InputStream inputStream) {
			this.inputStream = inputStream;
		}

		@Override
		public int read() throws IOException {
			return this.inputStream.read();
		}

	}

}