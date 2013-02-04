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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.Message;
import org.springframework.integration.annotation.Header;
import org.springframework.integration.ip.IpHeaders;
import org.springframework.integration.support.MessageBuilder;

/**
 * @author Gary Russell
 * @since 3.0
 *
 */
public class WebSocketServerTests {

	public static void main(String[] args) throws Exception {
		new ClassPathXmlApplicationContext(WebSocketServerTests.class.getSimpleName() + "-context.xml", WebSocketServerTests.class);
		System.out.println("Hit Enter To Terminate...");
		System.in.read();
		System.exit(0);
	}

	public static class DemoService implements ApplicationListener<WebSocketEvent> {

		private static final Log logger = LogFactory.getLog(DemoService.class);

		private final Map<String, AtomicInteger> clients = new HashMap<String, AtomicInteger>();

		private final Map<String, AtomicInteger> paused = new HashMap<String, AtomicInteger>();

		public void startStop(String command, @Header(IpHeaders.CONNECTION_ID) String connectionId) {
			if ("stop".equalsIgnoreCase(command)) {
				AtomicInteger clientInt = clients.remove(connectionId);
				if (clientInt != null) {
					paused.put(connectionId, clientInt);
				}
				logger.info("Connection " + connectionId + " stopped");
			}
			else if ("start".equalsIgnoreCase(command)) {
				AtomicInteger clientInt = paused.remove(connectionId);
				clientInt = clientInt == null ? new AtomicInteger() : clientInt;
				clients.put(connectionId, clientInt);
				logger.info("Connection " + connectionId + " (re)started");
			}
			else {
				logger.info("Unexpected command: " + command);
			}
		}

		public List<Message<?>> getNext() {
			List<Message<?>> messages = new ArrayList<Message<?>>();
			for (Entry<String, AtomicInteger> entry : clients.entrySet()) {
				Message<String> message = MessageBuilder.withPayload(Integer.toString(entry.getValue().incrementAndGet()))
						.setHeader(IpHeaders.CONNECTION_ID, entry.getKey())
						.build();
				messages.add(message);
				logger.warn("Sending " + message.getPayload() + " to connection " + entry.getKey());
			}
			if (messages.size() == 0) {
				return null;
			}
			else {
				return messages;
			}
		}

		public void remove(String connetionId) {
			logger.warn("Error on write; removing " + connetionId);
			clients.remove(connetionId);
		}

		@Override
		public void onApplicationEvent(WebSocketEvent event) {
			logger.info(event);
			if (WebSocketEvent.HANDSHAKE_COMPLETE.equals(event.getType())) {
				startStop("start", event.getConnectionId());
			}
			else if (WebSocketEvent.WEBSOCKET_CLOSED.equals(event.getType())) {
				clients.remove(event.getConnectionId());
			}
		}
	}

}
