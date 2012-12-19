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

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.DirectFieldAccessor;
import org.springframework.core.serializer.Deserializer;
import org.springframework.integration.Message;
import org.springframework.integration.MessageHandlingException;
import org.springframework.integration.ip.tcp.connection.AbstractTcpConnectionInterceptor;
import org.springframework.integration.ip.tcp.connection.TcpConnection;
import org.springframework.integration.ip.tcp.connection.TcpConnectionInterceptor;
import org.springframework.integration.ip.tcp.connection.TcpConnectionInterceptorFactory;
import org.springframework.integration.ip.tcp.connection.TcpNetConnection;
import org.springframework.integration.ip.tcp.connection.TcpNioConnection;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.x.ip.sockjs.serializer.WebSocketSerializer;
import org.springframework.integration.x.ip.sockjs.support.SockJsFrame;
import org.springframework.util.Assert;

/**
 * @author Gary Russell
 * @since 3.0
 *
 */
public class WebSocketTcpConnectionInterceptorFactory implements TcpConnectionInterceptorFactory {

	private static final Log logger = LogFactory.getLog(WebSocketTcpConnectionInterceptor.class);

	@Override
	public TcpConnectionInterceptor getInterceptor() {
		return new WebSocketTcpConnectionInterceptor();
	}

	private class WebSocketTcpConnectionInterceptor extends AbstractTcpConnectionInterceptor {

		private boolean shook;

		private InputStream theInputStream;

		@Override
		public boolean onMessage(Message<?> message) {
			Assert.isInstanceOf(SockJsFrame.class, message.getPayload());
			SockJsFrame payload = (SockJsFrame) message.getPayload();
			if (payload.getRsv() > 0) {
				if (logger.isDebugEnabled()) {
					logger.debug("Reserved bits:" + payload.getRsv());
				}
				this.protocolViolation(message);
			}
			else if (payload.getType() == SockJsFrame.TYPE_INVALID) {
				if (logger.isDebugEnabled()) {
					logger.debug("Invalid Opcode");
				}
				this.protocolViolation(message);
			}
			else if (payload.getType() == SockJsFrame.TYPE_CLOSE) {
				try {
					if (logger.isDebugEnabled()) {
						logger.debug("Close, status:" + payload.getStatus());
					}
					// If we initiated the close, just close.
					if (!this.getRequiredDeserializer().getState(this.getTheInputStream()).isCloseInitiated()) {
						this.send(message);
					}
					this.close();
				}
				catch (Exception e) {
					throw new MessageHandlingException(message, "Send failed", e);
				}
			}
			else if (payload.getType() == SockJsFrame.TYPE_PING) {
				try {
					if (logger.isDebugEnabled()) {
						logger.debug("Ping:" + new String(payload.getBinary(), "UTF-8"));
					}
					if (payload.getBinary().length > 125) {
						this.protocolViolation(message);
					}
					else {
						if (!this.getRequiredDeserializer().getState(this.getTheInputStream()).isCloseInitiated()) {
							SockJsFrame pong = new SockJsFrame(SockJsFrame.TYPE_PONG, payload.getBinary());
							this.send(MessageBuilder.withPayload(pong).build());
						}
					}
				}
				catch (Exception e) {
					throw new MessageHandlingException(message, "Send failed", e);
				}
			}
			else if (payload.getType() == SockJsFrame.TYPE_PONG) {
				if (logger.isDebugEnabled()) {
					logger.debug("Pong");
				}
			}
			else if (this.shook) {
				return super.onMessage(message);
			}
			else {
				try {
					doHandshake(payload);
					this.shook = true;
				}
				catch (Exception e) {
					throw new MessageHandlingException(message, "Handshake failed", e);
				}
			}
			return true;
		}

		private void protocolViolation(Message<?> message) {
			if (logger.isDebugEnabled()) {
				logger.debug("Protocol violation - closing");
			}
			SockJsFrame close = new SockJsFrame(SockJsFrame.TYPE_CLOSE, "Protocol Error");
			close.setStatus((short) 1002);
			try {
				this.getRequiredDeserializer().getState(this.getTheInputStream()).setCloseInitiated(true);
				this.send(MessageBuilder.withPayload(close).build());
			}
			catch (Exception e) {
				throw new MessageHandlingException(message, "Send failed", e);			}
		}

		@Override
		public void close() {
			try {
				InputStream inputStream = getTheInputStream();
				if (inputStream != null) {
					this.getRequiredDeserializer().removeState(inputStream);
				}
			}
			catch (IOException e) {
			}
			super.close();
		}

		/**
		 * Hack - need to add getInputStream() to TcpConnection.
		 * @return
		 * @throws IOException
		 */
		private InputStream getTheInputStream() throws IOException {
			if (this.theInputStream != null) {
				return this.theInputStream;
			}
			TcpConnection theConnection = this.getTheConnection();
			InputStream inputStream = null;
			if (theConnection instanceof TcpNioConnection) {
				inputStream = (InputStream) new DirectFieldAccessor(theConnection).getPropertyValue("pipedInputStream");
			}
			else if (theConnection instanceof TcpNetConnection) {
				Socket socket = (Socket) new DirectFieldAccessor(theConnection).getPropertyValue("socket");
				if (socket != null) {
					inputStream = socket.getInputStream();
				}
			}
			this.theInputStream = inputStream;
			return inputStream;
		}

		private void doHandshake(SockJsFrame frame) throws Exception {
			SockJsFrame handshake = this.getRequiredDeserializer().generateHandshake(frame);
			this.send(MessageBuilder.withPayload(handshake).build());
		}

		private WebSocketSerializer getRequiredDeserializer() {
			Deserializer<?> deserializer = this.getDeserializer();
			Assert.state(deserializer instanceof WebSocketSerializer,
					"Deserializer must be a WebSocketSerializer");
			return (WebSocketSerializer) deserializer;
		}
	}

}
