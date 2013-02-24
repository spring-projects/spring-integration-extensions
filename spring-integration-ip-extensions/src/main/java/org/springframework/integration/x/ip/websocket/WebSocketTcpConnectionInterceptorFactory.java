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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.serializer.Deserializer;
import org.springframework.integration.Message;
import org.springframework.integration.MessageHandlingException;
import org.springframework.integration.MessageHeaders;
import org.springframework.integration.MessagingException;
import org.springframework.integration.aggregator.ResequencingMessageGroupProcessor;
import org.springframework.integration.aggregator.ResequencingMessageHandler;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.context.IntegrationObjectSupport;
import org.springframework.integration.core.MessageHandler;
import org.springframework.integration.endpoint.EventDrivenConsumer;
import org.springframework.integration.ip.tcp.connection.TcpConnection;
import org.springframework.integration.ip.tcp.connection.TcpConnectionInterceptorFactory;
import org.springframework.integration.ip.tcp.connection.TcpConnectionInterceptorSupport;
import org.springframework.integration.ip.tcp.connection.TcpConnectionSupport;
import org.springframework.integration.ip.tcp.connection.TcpNioConnection;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.x.ip.websocket.WebSocketEvent.WebSocketEventType;
import org.springframework.integration.x.ip.websocket.WebSocketSerializer.WebSocketState;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.util.Assert;

/**
 * @author Gary Russell
 * @since 3.0
 *
 */
public class WebSocketTcpConnectionInterceptorFactory extends IntegrationObjectSupport
		implements TcpConnectionInterceptorFactory {

	private static final Message<WebSocketFrame> PING = MessageBuilder.withPayload(
			new WebSocketFrame(WebSocketFrame.TYPE_PING, "Ping from SI")).build();

	private static final Log logger = LogFactory.getLog(WebSocketTcpConnectionInterceptor.class);

	private final Map<TcpConnection, WebSocketTcpConnectionInterceptor> connections =
			new ConcurrentHashMap<TcpConnection, WebSocketTcpConnectionInterceptor>();

	private volatile TaskScheduler taskScheduler;

	private volatile long pingInterval = 25000;

	private final Runnable pinger = new Runnable() {

		@Override
		public void run() {
			long pingFilter = System.currentTimeMillis() - pingInterval;
			for (Entry<TcpConnection, WebSocketTcpConnectionInterceptor> entry : connections.entrySet()) {
				TcpConnection connection = entry.getKey();
				String connectionId = connection.getConnectionId();
				if (entry.getValue().getLastSend() <= pingFilter) {
					try {
						if (logger.isDebugEnabled()) {
							logger.debug("Sending Ping to " + connectionId);
						}
						connection.send(PING);
					}
					catch (Exception e) {
						logger.error("Failed to send Ping to " + connectionId, e);
						connection.close();
					}
				}
				else {
					if (logger.isTraceEnabled()) {
						logger.trace("Skipping PING for " + connectionId + "  due to recent send");
					}
				}
			}
			if (pingInterval > 0) {
				taskScheduler.schedule(pinger, new Date(System.currentTimeMillis() + pingInterval));
			}
		}
	};

	@Override
	public void setTaskScheduler(TaskScheduler taskScheduler) {
		this.taskScheduler = taskScheduler;
	}

	public void setPingInterval(long pingInterval) {
		this.pingInterval = pingInterval;
	}

	@Override
	protected void onInit() throws Exception {
		super.onInit();
		if (this.pingInterval > 0) {
			if (this.taskScheduler == null) {
				this.taskScheduler = this.getTaskScheduler();
			}
			this.taskScheduler.schedule(this.pinger, new Date(System.currentTimeMillis() + this.pingInterval));
		}
	}

	@Override
	public TcpConnectionInterceptorSupport getInterceptor() {
		return new WebSocketTcpConnectionInterceptor();
	}

	public WebSocketTcpConnectionInterceptor locateInterceptor(TcpConnection connection) {
		return this.connections.get(connection);
	}


	public class WebSocketTcpConnectionInterceptor extends TcpConnectionInterceptorSupport {

		private volatile boolean shook;

		private final DirectChannel resequenceChannel = new DirectChannel();

		private final EventDrivenConsumer resequencer;

		private long lastSend;

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

		public long getLastSend() {
			return lastSend;
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
			WebSocketState state = getState(message);
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
					WebSocketEvent event = new WebSocketEvent(this.getTheConnection(),
							WebSocketEventType.WEBSOCKET_CLOSED, state.getPath(), state.getQueryString());
					this.getTheConnection().publishEvent(event);
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
						logger.debug("Ping received on " + this.getConnectionId() + ":"
								+ new String(payload.getBinary(), "UTF-8"));
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
					logger.debug("Pong received on " + this.getConnectionId());
				}
			}
			else if (this.shook) {
				return super.onMessage(message);
			}
			else {
				try {
					doHandshake(payload, message.getHeaders());
					this.shook = true;
					WebSocketEvent event = new WebSocketEvent(this.getTheConnection(),
							WebSocketEventType.HANDSHAKE_COMPLETE, state.getPath(), state.getQueryString());
					this.getTheConnection().publishEvent(event);
				}
				catch (Exception e) {
					throw new MessageHandlingException(message, "Handshake failed", e);
				}
			}
			return true;
		}

		private WebSocketState getState(Object object) {
			Object stateKey = null;
			stateKey = this.getTheConnection().getDeserializerStateKey();
			Assert.notNull(stateKey, "StateKey must not be null:" + object);
			WebSocketState state = (WebSocketState) this.getRequiredDeserializer().getState(stateKey);
			Assert.notNull(state, "State must not be null:" + object);
			return state;
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
				Object stateKey = this.getTheConnection().getDeserializerStateKey();
				if (stateKey != null) {
					WebSocketState webSocketState = (WebSocketState) this.getRequiredDeserializer().getState(stateKey);
					if (webSocketState != null) {
						webSocketState.setCloseInitiated(true);
					}
					this.send(MessageBuilder.withPayload(close)
							.copyHeaders(message.getHeaders())
							.build());
				}
			}
			catch (Exception e) {
				throw new MessageHandlingException(message, "Send failed", e);
			}
		}

		@Override
		public void close() {
			connections.remove(this.getTheConnection());
			Object stateKey = this.getTheConnection().getDeserializerStateKey();
			if (stateKey != null) {
				this.getRequiredDeserializer().removeState(stateKey);
			}
			super.close();
		}


		@Override
		public void send(Message<?> message) throws Exception {
			super.send(message);
			this.lastSend = System.currentTimeMillis();
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

		public Map<String, String> getAdditionalHeaders() {
			Map<String, String> headers = new HashMap<String, String>();
			WebSocketState state = this.getState(this.getConnectionId());
			if (state.getPath() != null) {
				headers.put(WebSocketHeaders.PATH, state.getPath());
			}
			if (state.getQueryString() != null) {
				headers.put(WebSocketHeaders.QUERY_STRING, state.getQueryString());
			}
			return headers;
		}

		@Override
		public void setTheConnection(TcpConnectionSupport theConnection) {
			connections.put(theConnection, this);
			super.setTheConnection(theConnection);
		}
	}

}
