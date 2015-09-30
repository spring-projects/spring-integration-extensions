/*
 * Copyright 2002-2015 the original author or authors.
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

package org.springframework.integration.xmpp.config;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.context.SmartLifecycle;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * This class configures an {@link XMPPTCPConnection} object.
 * This object is used for all scenarios to talk to a Smack server.
 *
 * @author Josh Long
 * @author Mark Fisher
 * @author Oleg Zhurakousky
 * @author Florian Schmaus
 * @author Artem Bilan
 *
 * @see XMPPTCPConnection
 * @since 2.0
 */
public class XmppConnectionFactoryBean extends AbstractFactoryBean<XMPPTCPConnection> implements SmartLifecycle {

	private final XMPPTCPConnectionConfiguration connectionConfiguration;

	private volatile String resource = null; // server will generate resource if not provided

	private volatile String user;

	private volatile String password;

	private volatile String subscriptionMode = "accept_all";

	private volatile XMPPTCPConnection connection;

	private volatile boolean autoStartup = true;

	private volatile int phase = Integer.MIN_VALUE;

	private final Object lifecycleMonitor = new Object();

	private volatile boolean running;


	public XmppConnectionFactoryBean(XMPPTCPConnectionConfiguration connectionConfiguration) {
		Assert.notNull(connectionConfiguration, "'connectionConfiguration' must not be null");
		this.connectionConfiguration = connectionConfiguration;
	}

	private XmppConnectionFactoryBean(XMPPTCPConnectionConfiguration.Builder connectionConfigurationBuilder) {
		this(connectionConfigurationBuilder.build());
	}

	public void setAutoStartup(boolean autoStartup) {
		this.autoStartup = autoStartup;
	}

	public void setPhase(int phase) {
		this.phase = phase;
	}

	public void setSubscriptionMode(String subscriptionMode) {
		this.subscriptionMode = subscriptionMode;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	@Override
	public Class<? extends XMPPTCPConnection> getObjectType() {
		return XMPPTCPConnection.class;
	}

	@Override
	protected XMPPTCPConnection createInstance() throws Exception {
		this.connection = new XMPPTCPConnection(this.connectionConfiguration);
		return this.connection;
	}

	public void start() {
		synchronized (this.lifecycleMonitor) {
			if (this.running) {
				return;
			}
			try {
				this.connection.connect();
				this.connection.addConnectionListener(new LoggingConnectionListener());
				if (StringUtils.hasText(this.user)) {
					this.connection.login(this.user, this.password, this.resource);
					Assert.isTrue(this.connection.isAuthenticated(), "Failed to authenticate user: " + this.user);
					if (StringUtils.hasText(this.subscriptionMode)) {
						Roster.SubscriptionMode subscriptionMode = Roster.SubscriptionMode.valueOf(this.subscriptionMode);
						Roster.getInstanceFor(this.connection).setSubscriptionMode(subscriptionMode);
					}
				}
				else {
					this.connection.loginAnonymously();
				}
				this.running = true;
			}
			catch (Exception e) {
				throw new BeanInitializationException("failed to connect to XMPP service for "
						+ this.connectionConfiguration.getServiceName(), e);
			}
		}
	}

	public void stop() {
		synchronized (this.lifecycleMonitor) {
			if (this.isRunning()) {
				this.connection.disconnect();
				this.running = false;
			}
		}
	}

	public void stop(Runnable callback) {
		this.stop();
		callback.run();
	}

	public boolean isRunning() {
		return this.running;
	}

	public int getPhase() {
		return this.phase;
	}

	public boolean isAutoStartup() {
		return this.autoStartup;
	}


	private class LoggingConnectionListener implements ConnectionListener {

		public void reconnectionSuccessful() {
			logger.debug("Reconnection successful");
		}

		public void reconnectionFailed(Exception e) {
			logger.debug("Reconnection failed", e);
		}

		public void reconnectingIn(int seconds) {
			logger.debug("Reconnecting in " + seconds + " seconds");
		}

		public void connectionClosedOnError(Exception e) {
			logger.debug("Connection closed on error", e);
		}

		public void connectionClosed() {
			logger.debug("Connection closed");
		}

		@Override
		public void connected(XMPPConnection connection) {
			logger.debug("Connection connected");
		}

		@Override
		public void authenticated(XMPPConnection connection, boolean resumed) {
			logger.debug("Connection authenticated");
		}

	}

}
