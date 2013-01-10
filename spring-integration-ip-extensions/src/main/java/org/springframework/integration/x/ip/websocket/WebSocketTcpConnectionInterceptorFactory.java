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
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.DirectFieldAccessor;
import org.springframework.core.serializer.Deserializer;
import org.springframework.integration.Message;
import org.springframework.integration.MessageHandlingException;
import org.springframework.integration.MessageHeaders;
import org.springframework.integration.MessagingException;
import org.springframework.integration.aggregator.ResequencingMessageGroupProcessor;
import org.springframework.integration.aggregator.ResequencingMessageHandler;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageHandler;
import org.springframework.integration.endpoint.EventDrivenConsumer;
import org.springframework.integration.ip.tcp.connection.AbstractTcpConnectionInterceptor;
import org.springframework.integration.ip.tcp.connection.TcpConnection;
import org.springframework.integration.ip.tcp.connection.TcpConnectionInterceptor;
import org.springframework.integration.ip.tcp.connection.TcpConnectionInterceptorFactory;
import org.springframework.integration.ip.tcp.connection.TcpNetConnection;
import org.springframework.integration.ip.tcp.connection.TcpNioConnection;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.x.ip.websocket.WebSocketSerializer.WebSocketState;
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

		private volatile boolean shook;

		private volatile InputStream theInputStream;

		private final DirectChannel resequenceChannel = new DirectChannel();

		private final EventDrivenConsumer resequencer;

		public WebSocketTcpConnectionInterceptor() {
			super();
			ResequencingMessageHandler handler = new ResequencingMessageHandler(new ResequencingMessageGroupProcessor());
			handler.setReleasePartialSequences(true);
			DirectChannel resequenced = new DirectChannel();
			resequenced.setBeanName("resequencedWSFrames");
			handler.setOutputChannel(resequenced);
			this.resequencer = new EventDrivenConsumer(this.resequenceChannel, handler);
			resequenced.subscribe(new MessageHandler() {

				@Override
				public void handleMessage(Message<?> message) throws MessagingException {
					doOnMessage(message);
				}
			});
			this.resequencer.afterPropertiesSet();
			this.resequencer.start();
		}

		/**
		 * When using NIO, we have to resequence the messages because frames may
		 * arrive out of order. This is particularly an issue for some of the
		 * Autobahn tests where, for example, many pings are sent and the test
		 * expects the pongs to come back in the same order.
		 */
		@Override
		public boolean onMessage(Message<?> message) {
			if (this.getTheConnection() instanceof TcpNioConnection && message.getHeaders().getCorrelationId() != null) {
				resequenceChannel.send(message);
				return true;
			}
			else {
				return this.doOnMessage(message);
			}
		}

		public boolean doOnMessage(Message<?> message) {
			Assert.isInstanceOf(WebSocketFrame.class, message.getPayload());
			WebSocketFrame payload = (WebSocketFrame) message.getPayload();
			InputStream inputStream = null;
			try {
				inputStream = this.getTheInputStream();
			}
			catch (IOException e1) {
				this.protocolViolation(message);
			}

			WebSocketState state = (WebSocketState) this.getRequiredDeserializer().getState(inputStream);
			Assert.notNull(state, "State must not be null:" + message);
			if (logger.isTraceEnabled()) {
				logger.trace(state);
			}
			if (payload.getRsv() > 0) {
				if (logger.isDebugEnabled()) {
					logger.debug("Reserved bits:" + payload.getRsv());
				}
				this.protocolViolation(message);
			}
			else if (payload.getType() == WebSocketFrame.TYPE_CLOSE) {
				try {
					if (logger.isDebugEnabled()) {
						logger.debug("Close, status:" + payload.getStatus());
					}
					// If we initiated the close, just close.
					if (!state.isCloseInitiated()) {
						if (payload.getStatus() < 0) {
							payload.setStatus((short) 1000);
						}
						this.send(message);
					}
					this.close();
				}
				catch (Exception e) {
					throw new MessageHandlingException(message, "Send failed", e);
				}
			}
			else if (state == null || state.isCloseInitiated()) {
				if (logger.isWarnEnabled()) {
					logger.warn("Message dropped - close initiated:" + message);
				}
			}
			else if ((payload.getType() & 0xff) == WebSocketFrame.TYPE_INVALID) {
				if (logger.isDebugEnabled()) {
					logger.debug("Invalid:" + payload.getPayload());
				}
				this.protocolViolation(message);
			}
			else if (payload.getType() == WebSocketFrame.TYPE_FRAGMENTED_CONTROL) {
				if (logger.isDebugEnabled()) {
					logger.debug("Fragmented Control Op");
				}
				this.protocolViolation(message);
			}
			else if (payload.getType() == WebSocketFrame.TYPE_PING) {
				try {
					if (logger.isDebugEnabled()) {
						logger.debug("Ping:" + new String(payload.getBinary(), "UTF-8"));
					}
					if (payload.getBinary().length > 125) {
						this.protocolViolation(message);
					}
					else {
						WebSocketFrame pong = new WebSocketFrame(WebSocketFrame.TYPE_PONG, payload.getBinary());
						this.send(MessageBuilder.withPayload(pong)
								.copyHeaders(message.getHeaders())
								.build());
					}
				}
				catch (Exception e) {
					throw new MessageHandlingException(message, "Send failed", e);
				}
			}
			else if (payload.getType() == WebSocketFrame.TYPE_PONG) {
				if (logger.isDebugEnabled()) {
					logger.debug("Pong");
				}
			}
			else if (this.shook) {
				MessageBuilder<?> messageBuilder = MessageBuilder.fromMessage(message);
				// TODO: Move to subclass of TcpMessageMapper when INT-2877 is merged
				if (state.getPath() != null) {
					messageBuilder.setHeader(WebSocketHeaders.PATH, state.getPath());
				}
				if (state.getQueryString() != null) {
					messageBuilder.setHeader(WebSocketHeaders.QUERY_STRING, state.getQueryString());
				}
				return super.onMessage(
						messageBuilder.build());
			}
			else {
				try {
					doHandshake(payload, message.getHeaders());
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
				logger.debug("Protocol violation - closing; " + message);
			}
			WebSocketFrame frame = (WebSocketFrame) message.getPayload();
			String error = "Protocol Error" + frame.getPayload() == null ? "" : (":" + frame.getPayload());
			WebSocketFrame close = new WebSocketFrame(WebSocketFrame.TYPE_CLOSE, error);
			close.setStatus(frame.getType() == WebSocketFrame.TYPE_INVALID_UTF8 ? (short) 1007 : (short) 1002);
			try {
				((WebSocketState) this.getRequiredDeserializer().getState(this.getTheInputStream())).setCloseInitiated(true);
				this.send(MessageBuilder.withPayload(close)
						.copyHeaders(message.getHeaders())
						.build());
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

		private void doHandshake(WebSocketFrame frame, MessageHeaders messageHeaders) throws Exception {
			try {
				WebSocketFrame handshake = this.getRequiredDeserializer().generateHandshake(frame);
				this.send(MessageBuilder.withPayload(handshake)
						.copyHeaders(messageHeaders)
						.build());
			}
			catch (WebSocketUpgradeException e) {
				this.send(MessageBuilder
						.withPayload(
								new WebSocketFrame(WebSocketFrame.TYPE_DATA, "HTTP/1.1 " +
										e.getMessage() + e.getHeaders()))
						.copyHeaders(messageHeaders)
						.build());
				this.close();
			}
		}

		private WebSocketSerializer getRequiredDeserializer() {
			Deserializer<?> deserializer = this.getDeserializer();
			Assert.state(deserializer instanceof WebSocketSerializer,
					"Deserializer must be a WebSocketSerializer");
			return (WebSocketSerializer) deserializer;
		}
	}

}
