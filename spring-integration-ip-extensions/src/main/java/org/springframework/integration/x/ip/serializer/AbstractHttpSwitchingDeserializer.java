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
package org.springframework.integration.x.ip.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base class for (de)Serializers that start with an HTTP-like protocol then
 * switch to some other protocol.
 *
 * @author Gary Russell
 * @since 3.0
 *
 */
public abstract class AbstractHttpSwitchingDeserializer implements StatefulDeserializer<DataFrame> {

	protected final Log logger = LogFactory.getLog(this.getClass());

	protected volatile int maxMessageSize = 2048;

	private final Map<InputStream, BasicState> streamState = new ConcurrentHashMap<InputStream, BasicState>();

	protected final ByteArrayCrLfSerializer crlfDeserializer = new ByteArrayCrLfSerializer();

	private static final Pattern requestLinePattern = Pattern.compile("GET *([^ ]+) *HTTP/");

	public void setMaxMessageSize(int maxMessageSize) {
		this.maxMessageSize = maxMessageSize;
	}

	protected ByteArrayCrLfSerializer getCrlfDeserializer() {
		return crlfDeserializer;
	}

	public abstract DataFrame deserialize(InputStream inputStream) throws IOException;

	protected BasicState getStreamState(InputStream inputStream) {
		return streamState.get(inputStream);
	}

	/**
	 * Returns null if we've switched from HTTP-like protocol; headers otherwise.
	 * @param inputStream
	 * @return null or list of DataFrame, where the first frame contains the headers.
	 * Implementations may add additional frames.
	 * @throws IOException
	 */
	protected List<DataFrame> checkStreaming(InputStream inputStream) throws IOException {
		BasicState isStreaming = this.streamState.get(inputStream);
		if (isStreaming == null) { //Consume the headers - TODO - check status
			StringBuilder headersBuilder = new StringBuilder();
			byte[] headers = new byte[this.maxMessageSize];
			String path = null;
			String queryString = null;
			int headersLength;
			do {
				headersLength = this.crlfDeserializer.fillToCrLf(inputStream, headers);
				String header = new String(headers, 0, headersLength, "UTF-8");
				if (path == null) {
					if (header.startsWith("GET")) {
						Matcher requestLineMatcher = requestLinePattern.matcher(header);
						if (requestLineMatcher.find()) {
							path = requestLineMatcher.group(1);
							if (path.contains("?")) {
								int queryStarts = path.indexOf("?");
								queryString = path.substring(queryStarts + 1);
								path = path.substring(0, queryStarts);
							}
						}
					}
				}
				headersBuilder.append(header).append("\r\n");
			}
			while (headersLength > 0);
			BasicState basicState = createState();
			basicState.setPath(path);
			basicState.setQueryString(queryString);
			List<DataFrame> dataList = new ArrayList<DataFrame>();
			List<DataFrame> decodedHeaders = decodeHeaders(headersBuilder.toString(), basicState, dataList);
			this.streamState.put(inputStream, basicState);
			return decodedHeaders;
		}
		return null;
	}

	protected BasicState createState() {
		return new BasicState();
	}

	protected void checkClosure(int bite) throws IOException {
		if (bite < 0) {
			logger.debug("Socket closed during message assembly");
			throw new IOException("Socket closed during message assembly");
		}
	}

	protected List<DataFrame> decodeHeaders(String frameData, BasicState state, List<DataFrame> dataList) {
		// TODO: Full header separation - mvc utils?
		if (logger.isDebugEnabled()) {
			logger.debug("Received:Headers\r\n" + frameData);
		}
		dataList.add(createDataFrame(DataFrame.TYPE_HEADERS, frameData));
		return dataList;
	}

	protected DataFrame createDataFrame(int type, String frameData) {
		return new DataFrame(type, frameData);
	}

	public void removeState(Object key) {
		this.streamState.remove(key);
	}

	public BasicState getState(Object key) {
		return this.streamState.get(key);
	}

	public static class BasicState {

		private volatile String path;

		private volatile String queryString;

		private volatile DataFrame pendingFrame;

		private final List<byte[]> fragments = new ArrayList<byte[]>();

		public DataFrame getPendingFrame() {
			return pendingFrame;
		}

		public void setPendingFrame(DataFrame pendingFrame) {
			this.pendingFrame = pendingFrame;
		}

		public List<byte[]> getFragments() {
			return fragments;
		}

		public String getPath() {
			return path;
		}

		private void setPath(String path) {
			this.path = path;
		}

		public String getQueryString() {
			return queryString;
		}

		private void setQueryString(String queryString) {
			this.queryString = queryString;
		}

		@Override
		public String toString() {
			return "BasicState [" +
					(this.path != null ? ("path=" + this.path) : "") +
					(this.queryString != null ? (", queryString=" + this.queryString) : "") +
					(this.pendingFrame != null ? (", pendingFrame=" + this.pendingFrame) : "") +
					(this.fragments.size() > 0 ? (", fragments.size()=" + this.fragments.size()) : "") +
					"]";
		}

	}

}