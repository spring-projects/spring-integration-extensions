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
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.serializer.Serializer;
import org.springframework.integration.ip.tcp.serializer.SoftEndOfStreamException;
import org.springframework.integration.x.ip.sockjs.SockJsUtils;
import org.springframework.integration.x.ip.sockjs.support.SockJsFrame;
import org.springframework.util.Assert;

/**
 * @author Gary Russell
 * @since 3.0
 *
 */
public class WebSocketSerializer extends AbstractSockJsDeserializer<SockJsFrame> implements Serializer<Object> {

	private static final String HTTP_1_1_101_WEB_SOCKET_PROTOCOL_HANDSHAKE_SPRING_INTEGRATION =
			"HTTP/1.1 101 Web Socket Protocol Handshake - Spring Integration\r\n";

	private final Log logger = LogFactory.getLog(this.getClass());

	private final Map<InputStream, StringBuilder> fragments = new ConcurrentHashMap<InputStream, StringBuilder>();

	private volatile boolean server;

	public void setServer(boolean server) {
		this.server = server;
	}

	public void removeFragments(InputStream inputStream) {
		this.fragments.remove(inputStream);
	}

	public void serialize(final Object frame, OutputStream outputStream)
			throws IOException {
		String data = "";
		SockJsFrame theFrame = null;
		if (frame instanceof String) {
			data = (String) frame;
			theFrame = new SockJsFrame(SockJsFrame.TYPE_DATA, data);
		}
		else if (frame instanceof SockJsFrame) {
			theFrame = (SockJsFrame) frame;
			if (theFrame.getType() == SockJsFrame.TYPE_DATA) {
				data = theFrame.getPayload();
			}
		}
		if (data != null && data.startsWith(HTTP_1_1_101_WEB_SOCKET_PROTOCOL_HANDSHAKE_SPRING_INTEGRATION)) {
			outputStream.write(data.getBytes());
			return;
		}
		int lenBytes;
		int payloadLen = this.server ? 0 : 0x80; //masked
		boolean pong = data.startsWith("pong:");
		String theData = pong ? data.substring(5) : data;
		int length = theFrame.getType() == SockJsFrame.TYPE_DATA_BINARY ? theFrame.getBinary().length : theData.length();
		if (length >= Math.pow(2, 16)) {
			lenBytes = 8;
			payloadLen |= 127;
		}
		else if (length > 125) {
			lenBytes = 2;
			payloadLen |= 126;
		}
		else {
			lenBytes = 0;
			payloadLen |= length;
		}
		int mask = (int) System.currentTimeMillis();
		ByteBuffer buffer = ByteBuffer.allocate(length + 6 + lenBytes);
		if (pong) {
			buffer.put((byte) 0x8a);
		}
		else if (theFrame.getType() == SockJsFrame.TYPE_CLOSE) {
			buffer.put((byte) 0x88);
			payloadLen |= 2;
		}
		else if (theFrame.getType() == SockJsFrame.TYPE_DATA_BINARY) {
			buffer.put((byte) 0x82);
		}
		else {
			// Final fragment; text
			buffer.put((byte) 0x81);
		}
		buffer.put((byte) payloadLen);
		if (lenBytes == 2) {
			buffer.putShort((short) length);
		}
		else if (lenBytes == 8) {
			buffer.putLong(length);
		}

		byte[] maskBytes = new byte[4];
		if (!server) {
			buffer.putInt(mask);
			buffer.position(buffer.position() - 4);
			buffer.get(maskBytes);
		}
		if (theFrame.getType() == SockJsFrame.TYPE_CLOSE) {
			buffer.putShort(theFrame.getStatus());
		}
		else {
			byte[] bytes = theFrame.getType() == SockJsFrame.TYPE_DATA_BINARY ? theFrame.getBinary() : theData.getBytes("UTF-8");
			for (int i = 0; i < bytes.length; i++) {
				if (server) {
					buffer.put(bytes[i]);
				}
				else {
					buffer.put((byte) (bytes[i] ^ maskBytes[i % 4]));
				}
			}
		}
		outputStream.write(buffer.array(), 0, buffer.position());
	}

	@Override
	public SockJsFrame deserialize(InputStream inputStream) throws IOException {
		List<SockJsFrame> headers = checkStreaming(inputStream);
		if (headers != null) {
			return headers.get(0);
		}
		int bite;
		if (logger.isDebugEnabled()) {
			logger.debug("Available to read:" + inputStream.available());
		}
		boolean done = false;
		int len = 0;
		int n = 0;
		int dataInx = 0;
		byte[] buffer = null;
		boolean fin = false;
		boolean ping = false;
		boolean pong = false;
		boolean close = false;
		boolean binary = false;
		int lenBytes = 0;
		byte[] mask = new byte[4];
		int maskInx = 0;
		while (!done ) {
			bite = inputStream.read();
//			logger.debug("Read:" + Integer.toHexString(bite));
			if (bite < 0 && n == 0) {
				throw new SoftEndOfStreamException("Stream closed between payloads");
			}
			checkClosure(bite);
			switch (n++) {
			case 0:
				fin = (bite & 0x80) > 0;
				switch (bite) {
				case 0x01:
				case 0x81:
					logger.debug("Text, fin=" + fin);
					break;
				case 0x02:
				case 0x82:
					logger.debug("Binary, fin=" + fin);
					binary = true;
					break;
				case 0x89:
					ping = true;
					logger.debug("Ping, fin=" + fin);
					break;
				case 0x8a:
					pong = true;
					logger.debug("Pong, fin=" + fin);
					break;
				case 0x88:
					logger.debug("Close, fin=" + fin);
					close = true;
					break;
				default:
					throw new IOException("Unexpected opcode " + Integer.toHexString(bite));
				}
				break;
			case 1:
				if (this.server) {
					if ((bite & 0x80) == 0) {
						throw new IOException("Illegal: Expected masked data from client");
					}
					bite &= 0x7f;
				}
				if ((bite & 0x80) > 0) {
					throw new IOException("Illegal: Received masked data from server");
				}
				if (bite < 126) {
					len = bite;
					buffer = new byte[len];
				}
				else if (bite == 126) {
					lenBytes = 2;
				}
				else {
					lenBytes = 8;
				}
				break;
			case 2:
			case 3:
			case 4:
			case 5:
				if (lenBytes > 4 && bite != 0) {
					throw new IOException("Max supported length exceeded");
				}
			case 6:
				if (lenBytes > 3 && (bite & 0x80) > 0) {
					throw new IOException("Max supported length exceeded");
				}
			case 7:
			case 8:
			case 9:
				if (lenBytes-- > 0) {
					len = len << 8 | bite;
					if (lenBytes == 0) {
						buffer = new byte[len];
					}
					break;
				}
			default:
				if (this.server && maskInx < 4) {
					mask[maskInx++] = (byte) bite;
				}
				else {
					if (this.server) {
						bite ^= mask[dataInx % 4];
					}
					buffer[dataInx++] = (byte) bite;
				}
				done = (server ? maskInx == 4 : true) && dataInx >= len;
			}
		};
		String data =  new String(buffer, "UTF-8");
		if (!fin) {
			StringBuilder builder = this.fragments.get(inputStream);
			if (builder == null) {
				builder = new StringBuilder();
				this.fragments.put(inputStream, builder);
			}
			builder.append(data);
			return null;
		}
		else if (ping) {
			return new SockJsFrame(SockJsFrame.TYPE_PING, data);
		}
		else if (pong) {
			return new SockJsFrame(SockJsFrame.TYPE_PONG, data);
		}
		else if (close) {
			SockJsFrame closeFrame = new SockJsFrame(SockJsFrame.TYPE_CLOSE, data);
			closeFrame.setStatus((short) ((buffer[0] << 8) | (buffer[1] & 0xff)));
			return closeFrame;
		}
		else if (binary) {
			return new SockJsFrame(SockJsFrame.TYPE_DATA_BINARY, buffer);
		}
		else {
			StringBuilder builder = this.fragments.get(inputStream);
			if (builder == null) {
				return decodeToFrame(data);
			}
			else {
				builder.append(data).toString();
				this.removeFragments(inputStream);
				return this.decodeToFrame(builder.toString());
			}
		}
	}

	@Override
	protected void checkClosure(int bite) throws IOException {
		if (bite < 0) {
			logger.debug("Socket closed during message assembly");
			throw new IOException("Socket closed during message assembly");
		}
	}

	@Override
	public void removeState(InputStream inputStream) {
		super.removeState(inputStream);
		this.removeFragments(inputStream);
	}

	public SockJsFrame generateHandshake(SockJsFrame frame) throws Exception {
		Assert.isTrue(frame.getType() == SockJsFrame.TYPE_HEADERS, "Expected headers:" + frame);
		String[] headers = frame.getPayload().split("\\r\\n");
		String key = null;
		for (String header : headers) {
			if (header.toLowerCase().startsWith("sec-websocket-key")) {
				key = header.split(":")[1].trim();
				break;
			}
		}
		String handshake = HTTP_1_1_101_WEB_SOCKET_PROTOCOL_HANDSHAKE_SPRING_INTEGRATION +
						   "Upgrade: WebSocket\r\n" +
						   "Connection: Upgrade\r\n" +
						   "Sec-WebSocket-Accept: " + SockJsUtils.generateWebSocketAccept(key) + "\r\n\r\n";
		return new SockJsFrame(SockJsFrame.TYPE_DATA, handshake);
	}

}
