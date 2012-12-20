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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.integration.x.ip.serializer.ByteArrayCrLfSerializer;
import org.springframework.integration.x.ip.serializer.StatefulDeserializer;
import org.springframework.integration.x.ip.sockjs.support.SockJsFrame;

/**
 * @author Gary Russell
 * @since 3.0
 *
 */
public abstract class AbstractSockJsDeserializer<T> implements StatefulDeserializer<T> {

	protected final Log logger = LogFactory.getLog(this.getClass());

	protected final ByteArrayCrLfSerializer crlfDeserializer = new ByteArrayCrLfSerializer();

	protected volatile int maxMessageSize = 2048;

	private final Map<InputStream, BasicState> streamState = new ConcurrentHashMap<InputStream, BasicState>();

	private volatile boolean simpleData;

	void setMaxMessageSize(int maxMessageSize) {
		this.maxMessageSize = maxMessageSize;
	}

	public void setSimpleData(boolean simpleData) {
		this.simpleData = simpleData;
	}

	protected BasicState getStreamState(InputStream inputStream) {
		return streamState.get(inputStream);
	}

	protected List<SockJsFrame> checkStreaming(InputStream inputStream) throws IOException {
		BasicState isStreaming = this.streamState.get(inputStream);
		if (isStreaming == null) { //Consume the headers - TODO - check status
			StringBuilder headersBuilder = new StringBuilder();
			byte[] headers = new byte[this.maxMessageSize];
			int headersLength;
			do {
				headersLength = this.crlfDeserializer.fillToCrLf(inputStream, headers);
				String header = new String(headers, 0, headersLength, "UTF-8");
				headersBuilder.append(header).append("\r\n");
			}
			while (headersLength > 0);
			BasicState basicState = new BasicState();
			List<SockJsFrame> decodedHeaders = decodeHeaders(headersBuilder.toString(), basicState);
			this.streamState.put(inputStream, basicState);
			return decodedHeaders;
		}
		return null;
	}

	private List<SockJsFrame> decodeHeaders(String frameData, BasicState state) {
		// TODO: Full header separation - mvc utils?
		List<SockJsFrame> dataList = new ArrayList<SockJsFrame>();
		if (logger.isDebugEnabled()) {
			logger.debug("Received:Headers\r\n" + frameData);
		}
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
//		System.out.println(cookies);
		dataList.add(new SockJsFrame(SockJsFrame.TYPE_HEADERS, frameData));
		if (cookies.length() > 8) {
			dataList.add(new SockJsFrame(SockJsFrame.TYPE_COOKIES, cookies));
		}
		return dataList;
	}

	protected void checkClosure(int bite) throws IOException {
		if (bite < 0) {
			logger.debug("Socket closed during message assembly");
			throw new IOException("Socket closed during message assembly");
		}
	}

	public void removeState(InputStream inputStream) {
		this.streamState.remove(inputStream);
	}

	public BasicState getState(InputStream inputStream) {
		return this.streamState.get(inputStream);
	}

	public abstract T deserialize(InputStream inputStream) throws IOException;

	protected SockJsFrame decodeToFrame(String data) {
		if (this.simpleData) {
			System.out.println("Received data:" + data);
			return new SockJsFrame(SockJsFrame.TYPE_DATA, data);
		}
		else if (data.length() == 1 && data.equals("h")) {
			System.out.println("Received:SockJS-Heartbeat");
			return new SockJsFrame(SockJsFrame.TYPE_HEARTBEAT, data);
		}
		else if (data.length() == 0x800 && data.startsWith("hhhhhhhhhhhhh")) {
			System.out.println("Received:SockJS-XHR-Prelude");
			return new SockJsFrame(SockJsFrame.TYPE_PRELUDE, data);
		}
		else if (data.length() == 1 && data.equals("o")) {
			System.out.println("Received:SockJS-Open");
			return new SockJsFrame(SockJsFrame.TYPE_OPEN, data);
		}
		else if (data.length() > 0 && data.startsWith("c")) {
			System.out.println("Received SockJS-Close:" + data.substring(1));
			return new SockJsFrame(SockJsFrame.TYPE_CLOSE, data.substring(1));
		}
		else if (data.length() > 0 && data.startsWith("a")) {
			System.out.println("Received data:" + data.substring(1));
			return new SockJsFrame(SockJsFrame.TYPE_DATA, data.substring(1));
		}
		else {
			System.out.println("Received unexpected:" + new String(data));
			return new SockJsFrame(SockJsFrame.TYPE_UNEXPECTED, data);
		}
	}

	public static class BasicState {

		private volatile boolean gzipping;

		private volatile GZIPInputStream gzipInputStream;

		private volatile GZIPFeederInputStream gzipFeederInputStream;

		private volatile boolean closeInitiated;

		private volatile boolean expectingPong;

		private volatile SockJsFrame pendingFrame;

		private final List<byte[]> fragments = new ArrayList<byte[]>();

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

		public boolean isCloseInitiated() {
			return this.closeInitiated;
		}

		public void setCloseInitiated(boolean closeInitiated) {
			this.closeInitiated = closeInitiated;
		}

		public boolean isExpectingPong() {
			return this.expectingPong;
		}

		public void setExpectingPong(boolean expectingPong) {
			this.expectingPong = expectingPong;
		}

		public SockJsFrame getPendingFrame() {
			return pendingFrame;
		}

		public void setPendingFrame(SockJsFrame pendingFrame) {
			this.pendingFrame = pendingFrame;
		}

		public List<byte[]> getFragments() {
			return fragments;
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