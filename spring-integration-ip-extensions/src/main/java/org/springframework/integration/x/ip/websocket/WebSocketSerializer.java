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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.DirectFieldAccessor;
import org.springframework.core.serializer.Serializer;
import org.springframework.integration.MessagingException;
import org.springframework.integration.ip.tcp.serializer.SoftEndOfStreamException;
import org.springframework.integration.x.ip.serializer.AbstractHttpSwitchingDeserializer;
import org.springframework.integration.x.ip.serializer.DataFrame;
import org.springframework.util.Assert;

/**
 * @author Gary Russell
 * @since 3.0
 *
 */
public class WebSocketSerializer extends AbstractHttpSwitchingDeserializer implements Serializer<Object> {

	private static final String HTTP_1_1_101_WEB_SOCKET_PROTOCOL_HANDSHAKE_SPRING_INTEGRATION =
			"HTTP/1.1 101 Web Socket Protocol Handshake - Spring Integration\r\n";

	private static final Set<Short> INVALID_STATUS = new HashSet<Short>(
		Arrays.asList((short) 1004, (short) 1005, (short) 1006, (short) 1012, (short) 1013, (short) 1014, (short) 1015));

	private volatile boolean server;

	private boolean validateUtf8;

	public void setServer(boolean server) {
		this.server = server;
	}

	/**
	 * Validate UTF-8 (required for Autobahn tests).
	 * @param validateUtf8
	 */
	public void setValidateUtf8(boolean validateUtf8) {
		this.validateUtf8 = validateUtf8;
	}

	@Override
	protected DataFrame createDataFrame(int type, String frameData) {
		return new WebSocketFrame(type, frameData);
	}

	@Override
	protected BasicState createState() {
		return new WebSocketState();
	}

	@Override
	public void serialize(final Object frame, OutputStream outputStream)
			throws IOException {
		String data = "";
		WebSocketFrame theFrame = null;
		if (frame instanceof String) {
			data = (String) frame;
			theFrame = new WebSocketFrame(WebSocketFrame.TYPE_DATA, data);
		}
		else if (frame instanceof WebSocketFrame) {
			theFrame = (WebSocketFrame) frame;
			data = theFrame.getPayload();
		}
		if (data != null && data.startsWith("HTTP/1.1")) {
			outputStream.write(data.getBytes());
			return;
		}
		int lenBytes;
		int payloadLen = this.server ? 0 : 0x80; //masked
		boolean close = theFrame.getType() == WebSocketFrame.TYPE_CLOSE;
		boolean ping = theFrame.getType() == WebSocketFrame.TYPE_PING;
		boolean pong = theFrame.getType() == WebSocketFrame.TYPE_PONG;
		byte[] bytes = theFrame.getBinary() != null ? theFrame.getBinary() : data.getBytes("UTF-8");

		int length = bytes.length;
		if (close) {
			length += 2;
		}
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
		if (ping) {
			buffer.put((byte) 0x89);
		}
		else if (pong) {
			buffer.put((byte) 0x8a);
		}
		else if (close) {
			buffer.put((byte) 0x88);
		}
		else if (theFrame.getType() == WebSocketFrame.TYPE_DATA_BINARY) {
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
		if (close) {
			buffer.putShort(theFrame.getStatus());
			// TODO: mask status when client
		}
		for (int i = 0; i < bytes.length; i++) {
			if (server) {
				buffer.put(bytes[i]);
			}
			else {
				buffer.put((byte) (bytes[i] ^ maskBytes[i % 4]));
			}
		}
		outputStream.write(buffer.array(), 0, buffer.position());
	}

	@Override
	public DataFrame deserialize(InputStream inputStream) throws IOException {
		DataFrame frame = null;
		BasicState state = this.getState(inputStream);
		if (state != null) {
			frame = state.getPendingFrame();
		}
		while (frame == null || (frame.getPayload() == null && frame.getBinary() == null)) {
			frame = doDeserialize(inputStream, frame);
			if (frame.getPayload() == null && frame.getBinary() == null) {
				state.setPendingFrame(frame);
			}
		}
		return frame;
	}

	private DataFrame doDeserialize(InputStream inputStream, DataFrame protoFrame) throws IOException {
		List<DataFrame> headers = checkStreaming(inputStream);
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
		boolean invalid = false;
		String invalidText = null;
		boolean fragmentedControl = false;
		int lenBytes = 0;
		byte[] mask = new byte[4];
		int maskInx = 0;
		int rsv = 0;
		while (!done ) {
			bite = inputStream.read() & 0xff;
//			logger.debug("Read:" + Integer.toHexString(bite));
			bite = checkclosed(bite, inputStream);
			if (bite < 0 && n == 0) {
				throw new SoftEndOfStreamException("Stream closed between payloads");
			}
			checkClosure(bite);
			switch (n++) {
			case 0:
				fin = (bite & 0x80) > 0;
				rsv = (bite & 0x70) >> 4;
				bite &= 0x0f;
				switch (bite) {
				case 0x00:
					logger.debug("Continuation, fin=" + fin);
					if (protoFrame == null) {
						invalid = true;
						invalidText = "Unexpected continuation frame";
					}
					else {
						binary = protoFrame.getType() == WebSocketFrame.TYPE_DATA_BINARY;
					}
					this.getState(inputStream).setPendingFrame(null);
					break;
				case 0x01:
					logger.debug("Text, fin=" + fin);
					if (protoFrame != null) {
						invalid = true;
						invalidText = "Expected continuation frame";
					}
					break;
				case 0x02:
					logger.debug("Binary, fin=" + fin);
					if (protoFrame != null) {
						invalid = true;
						invalidText = "Expected continuation frame";
					}
					binary = true;
					break;
				case 0x08:
					logger.debug("Close, fin=" + fin);
					fragmentedControl = !fin;
					close = true;
					break;
				case 0x09:
					ping = true;
					binary = true;
					fragmentedControl = !fin;
					logger.debug("Ping, fin=" + fin);
					break;
				case 0x0a:
					pong = true;
					fragmentedControl = !fin;
					logger.debug("Pong, fin=" + fin);
					break;
				case 0x03:
				case 0x04:
				case 0x05:
				case 0x06:
				case 0x07:
				case 0x0b:
				case 0x0c:
				case 0x0d:
				case 0x0e:
				case 0x0f:
					invalid = true;
					invalidText = "Reserved opcode " + Integer.toHexString(bite);
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
					len = len << 8 | (bite & 0xff);
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

		WebSocketFrame frame;

		if (fragmentedControl) {
			frame = new WebSocketFrame(WebSocketFrame.TYPE_FRAGMENTED_CONTROL, "Fragmented control frame", buffer);
		}
		else if (invalid) {
			frame = new WebSocketFrame(WebSocketFrame.TYPE_INVALID, invalidText, buffer);
		}
		else if (!fin) {
			List<byte[]> fragments = this.getState(inputStream).getFragments();
			fragments.add(buffer);
			logger.debug("Fragment");
			return new WebSocketFrame(binary ? WebSocketFrame.TYPE_DATA_BINARY : WebSocketFrame.TYPE_DATA, (String) null);
		}
		else if (ping) {
			frame = new WebSocketFrame(WebSocketFrame.TYPE_PING, buffer);
		}
		else if (pong) {
			String data = new String(buffer, "UTF-8");
			frame = new WebSocketFrame(WebSocketFrame.TYPE_PONG, data);
		}
		else if (close) {
			String data = new String(buffer, "UTF-8");
			if (data.length() >= 2) {
				data = data.substring(2);
			}
			WebSocketFrame closeFrame = new WebSocketFrame(WebSocketFrame.TYPE_CLOSE, data);
			short status = 1000;
			if (buffer.length >= 2) {
				status = (short) ((buffer[0] << 8) | (buffer[1] & 0xff));
				closeFrame.setStatus(status);
			}
			if (buffer.length == 1 || buffer.length > 125 ||
					(buffer.length > 2 && !validateUtf8IfNecessary(buffer, 2, data)) ||
					status < 1000 || INVALID_STATUS.contains(status) || (status >= 1016 && status < 3000) || status >= 5000) {
				// Simply close in this case; no close reply
				((WebSocketState) this.getState(inputStream)).setCloseInitiated(true);
			}
			frame = closeFrame;
		}
		else {
			List<byte[]> fragments = this.getState(inputStream).getFragments();
			if (fragments.size() == 0) {
				if (binary) {
					frame = new WebSocketFrame(WebSocketFrame.TYPE_DATA_BINARY, buffer);
				}
				else {
					String data = new String(buffer, "UTF-8");
					if (!validateUtf8IfNecessary(buffer, 0, data)) {
						frame = new WebSocketFrame(WebSocketFrame.TYPE_INVALID_UTF8, "Invalid UTF-8", buffer);
					}
					else {
						frame = new WebSocketFrame(WebSocketFrame.TYPE_DATA, data);
					}
				}
			}
			else {
				fragments.add(buffer);
				int utf8Len = 0;
				for (byte[] fragment : fragments) {
					utf8Len += fragment.length;
				}
				byte[] reconstructed = new byte[utf8Len];
				int utf8Pos = 0;
				for (byte[] fragment : fragments) {
					System.arraycopy(fragment, 0, reconstructed, utf8Pos, fragment.length);
					utf8Pos += fragment.length;
				}
				fragments.clear();
				if (binary) {
					frame = new WebSocketFrame(WebSocketFrame.TYPE_DATA_BINARY, reconstructed);
				}
				else {
					String data = new String(reconstructed, "UTF-8");
					if (!validateUtf8IfNecessary(reconstructed, 0, data)) {
						frame = new WebSocketFrame(WebSocketFrame.TYPE_INVALID_UTF8, "Invalid UTF-8", reconstructed);
					}
					else {
						frame = new WebSocketFrame(WebSocketFrame.TYPE_DATA, data);
					}
				}
			}
		}
		if (rsv > 0) {
			frame.setRsv(rsv);
		}
		return frame;
	}

	/**
	 * TODO: workaround for INT-2936
	 */
	private int checkclosed(int bite, InputStream inputStream) {
		int theBite = bite;
		if (theBite == 0xff) { // possibly a closed stream
			String streamClass = inputStream.getClass().getName();
			if (streamClass.endsWith("TcpNioConnection$ChannelInputStream")) {
				DirectFieldAccessor dfa = new DirectFieldAccessor(inputStream);
				try {
					if ((Boolean) dfa.getPropertyValue("isClosed") &&
							inputStream.available() == 0) {
						theBite = -1;
					}
				}
				catch (Exception e) {
					if (logger.isDebugEnabled()) {
						logger.debug("Failed to check closed", e);
					}
				}
			}
		}
		return theBite;
	}

	private boolean validateUtf8IfNecessary(byte[] buffer, int offset, String data) {
		if (this.validateUtf8) {
			try {
				byte[] bytes = data.getBytes("UTF-8");
				if (bytes.length != buffer.length - offset) {
					return false;
				}
				for (int i = 0; i < bytes.length; i++) {
					if (buffer[i + offset] != bytes[i]) {
						return false;
					}
				}
			}
			catch (UnsupportedEncodingException e) {
				throw new MessagingException("UTF-8 Conversion error");
			}
		}
		return true;
	}

	@Override
	protected void checkClosure(int bite) throws IOException {
		if (bite < 0) {
			logger.debug("Socket closed during message assembly");
			throw new IOException("Socket closed during message assembly");
		}
	}

	@Override
	public void removeState(Object inputStream) {
		super.removeState(inputStream);
	}

	public WebSocketFrame generateHandshake(WebSocketFrame frame) throws Exception {
		Assert.isTrue(frame.getType() == WebSocketFrame.TYPE_HEADERS, "Expected headers:" + frame);
		String[] headers = frame.getPayload().split("\\r\\n");
		String key = null;
		String version = null;
		for (String header : headers) {
			if (header.toLowerCase().startsWith("sec-websocket-key")) {
				key = header.split(":")[1].trim();
			}
			else if (header.toLowerCase().startsWith("sec-websocket-version")) {
				version = header.split(":")[1].trim();
			}
		}
		if (key == null) {
			throw new WebSocketUpgradeException("400 Bad Request: No sec-websocket-key header detected");
		}
		else if (!"13".equals(version)) {
			throw new WebSocketUpgradeException("426 Upgrade Required", "sec-websocket-version: 13\r\n");
		}
		String handshake = HTTP_1_1_101_WEB_SOCKET_PROTOCOL_HANDSHAKE_SPRING_INTEGRATION +
						   "Upgrade: WebSocket\r\n" +
						   "Connection: Upgrade\r\n" +
						   "Sec-WebSocket-Accept: " + this.generateWebSocketAccept(key) + "\r\n\r\n";
		return new WebSocketFrame(WebSocketFrame.TYPE_DATA, handshake);
	}

	private String generateWebSocketAccept(String key) throws NoSuchAlgorithmException  {
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		String toDigest = key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
		byte[] acceptStringBytes  = md.digest(toDigest.getBytes());
		acceptStringBytes = Base64.encodeBase64(acceptStringBytes);
		String acceptString = new String(acceptStringBytes);
		return acceptString;
	}

	public static class WebSocketState extends BasicState {

		private volatile boolean closeInitiated;

		private volatile boolean expectingPong;

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

	}
}
