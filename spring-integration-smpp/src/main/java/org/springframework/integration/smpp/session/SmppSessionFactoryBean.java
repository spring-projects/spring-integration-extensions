/* Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.integration.smpp.session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsmpp.DefaultPDUReader;
import org.jsmpp.DefaultPDUSender;
import org.jsmpp.SynchronizedPDUSender;
import org.jsmpp.bean.BindType;
import org.jsmpp.bean.NumberingPlanIndicator;
import org.jsmpp.bean.TypeOfNumber;
import org.jsmpp.extra.SessionState;
import org.jsmpp.session.MessageReceiverListener;
import org.jsmpp.session.SMPPSession;
import org.jsmpp.session.SessionStateListener;
import org.jsmpp.session.connection.Connection;
import org.jsmpp.session.connection.ConnectionFactory;
import org.jsmpp.session.connection.socket.SocketConnection;
import org.jsmpp.util.DefaultComposer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.Lifecycle;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.Ordered;
import org.springframework.util.Assert;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Factory bean to create a {@link SMPPSession}. Usually, you need little more than the {@link #host},
 * the {@link #port}, perhaps a {@link #password}, and a {@link #systemId}.
 * <p/>
 * The {@link SMPPSession } represents a connection to a SMSC, through which SMS messages are sent and received.
 * <p/>
 * Here is a breakdown of the supported parameters on this factory bean:
 * <p/>
 * <ul>
 *     <li>host - the SMSC host to which the session is bound   (think of this as the host of your email server)</li>
 *     <li>port - the SMSC port to which the session is bound (think of this as a port on your email server)</li>
 *     <li>bindType - values of type {@link org.jsmpp.bean.BindType}. The bind type specifies whether this
 *     {@link SMPPSession} can send ({@link org.jsmpp.bean.BindType#BIND_TX}),
 *     receive ({@link org.jsmpp.bean.BindType#BIND_RX}), or both send and receive
 *     ({@link org.jsmpp.bean.BindType#BIND_TRX}).</li>
 *     <li>systemId - the system ID for the server being bound to</li>
 *     <li>password - the password for the server being bound to</li>
 *     <li>systemType - the SMSC system type</li>
 *     <li>addrTon - a value from the {@link org.jsmpp.bean.TypeOfNumber} enumeration. Default is
 *     {@link org.jsmpp.bean.TypeOfNumber#UNKNOWN}</li>
 *     <li>addrNpi - a value from  the {@link org.jsmpp.bean.NumberingPlanIndicator} enumeration.
 *     Default is {@link org.jsmpp.bean.NumberingPlanIndicator#UNKNOWN}</li>
 *     <li>addressRange - can be null. Specifies the address range.</li>
 *     <li>timeout - a good default value is 60000  (1 minute)</li>
 *     <li>transactionTimeout - timeout for doing work with session. e.g. sending message (default 2 seconds)</li>
 *     <li>reconnect - boolean whether we allow the session to reconnect. (default true)</li>
 *     <li>reconnectInterval - interval between reconnection in milliseconds. (default 5 seconds)</li>
 * </ul>
 *
 *
 * @author Josh Long
 * @see org.jsmpp.session.SMPPSession#SMPPSession()
 * @see org.jsmpp.session.SMPPSession#connectAndBind(String, int, org.jsmpp.session.BindParameter)
 * @see org.jsmpp.session.SMPPSession#connectAndBind(String, int, org.jsmpp.bean.BindType, String, String, String, org.jsmpp.bean.TypeOfNumber, org.jsmpp.bean.NumberingPlanIndicator, String, long)
 * @since 1.0
 */
public class SmppSessionFactoryBean implements FactoryBean<ExtendedSmppSession>, SmartLifecycle, InitializingBean,
		DisposableBean {

	private Set<MessageReceiverListener> messageReceiverListeners = new HashSet<MessageReceiverListener>();
	private boolean autoStartup;
	private volatile boolean running;
	private Log log = LogFactory.getLog(getClass());
	private SessionStateListener sessionStateListener;
	private boolean ssl = false;
	private String host = "127.0.0.1";
	private String addressRange;
	private long timeout = 60 * 1000;// 1 minute
	private long transactionTimeout = 2 * 1000; // 2 seconds
	private int port = 2775;	// good default though this has been known to change
	private BindType bindType = BindType.BIND_TRX; // bind as a 'transceiver' - only 3.4 of the spec <em>requires</em> support for this
	private String systemId = getClass().getSimpleName().toLowerCase();	 // what would typically be called 'user' in a user/pw scheme
	private String password;
	private String systemType = "cp";
	private TypeOfNumber addrTon = TypeOfNumber.UNKNOWN;
	private NumberingPlanIndicator addrNpi = NumberingPlanIndicator.UNKNOWN;
	private long reconnectInterval = 5 * 1000; // 5 seconds
	private boolean reconnect = true; // flag whether we want to reconnect
	private volatile boolean destroyed = false; // flag that this session factory has been disposed

	private ExtendedSmppSessionAdaptingDelegate product;
	private final ProxyFactoryBean sessionFactoryBean = new ProxyFactoryBean();

	private ExecutorService reconnectingExecutor;
	private boolean reconnectingExecutorSet;

	public void setSsl(boolean ssl) {
		this.ssl = ssl;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setBindType(BindType bindType) {
		this.bindType = bindType;
	}

	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setSystemType(String systemType) {
		this.systemType = systemType;
	}

	public void setAddrTon(TypeOfNumber addrTon) {
		this.addrTon = addrTon;
	}

	public void setAddrNpi(NumberingPlanIndicator addrNpi) {
		this.addrNpi = addrNpi;
	}

	/**
	 * this specifies the range of numbers we want to <em>listen</em> to - as a consumer. If you
	 * specify '1234' as a destination address, and want to listen / receive all messages sent
	 * to that number, then specify '1234' as the {@link #addressRange}.
	 *
	 * @param addressRange the range of phone numbers to receive from.
	 */
	public void setAddressRange(String addressRange) {
		this.addressRange = addressRange;
	}

	/**
	 * Setting timeout for the session. This value is used to establish connection to SMSC, e.g. trying to establish
	 * connection. (default is 1 minute). This should not be confused with {@link #setTransactionTimeout(long)} which
	 * is the timeout to perform request on the actual session after it has been established.
	 *
	 * @param timeout timeout in milliseconds
	 */
	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	/**
	 * Setting transaction timeout preforming request on the session. ({@link SMPPSession#setTransactionTimer(long)}.
	 * This transaction timeout is similar to the concept of send timeout / request timeout. (default 2 seconds).
	 * If you receive a lot of {@link org.jsmpp.extra.ResponseTimeoutException} for waiting response from your SMSC,
	 * this indicates you need to increase this value.
	 *
	 * @param transactionTimeout transaction timeout in milliseconds
	 */
	public void setTransactionTimeout(long transactionTimeout) {
		this.transactionTimeout = transactionTimeout;
	}

	public void setSessionStateListener(SessionStateListener sessionStateListener) {
		this.sessionStateListener = sessionStateListener;
	}

	public void setMessageReceiverListeners(Set<MessageReceiverListener> messageReceiverListeners) {
		this.messageReceiverListeners = messageReceiverListeners;
	}

	/**
	 * Creating new SMPPSession. This will create default SMPPSession for non-SSL connection or create SMPPSession
	 * using different factory for SSL connection.
	 * @return SMPP session
	 */
	private SMPPSession createNewSession() {
		final SMPPSession newSession;
		if (!ssl) {
			newSession = new SMPPSession();
		} else {
			newSession = new SMPPSession(new SynchronizedPDUSender(new DefaultPDUSender(
					new DefaultComposer())), new DefaultPDUReader(), sslConnectionFactory);
		}
		newSession.setTransactionTimer(transactionTimeout);
		return newSession;
	}

	/**
	 * Logic to build smpp session
	 * @return the configured SMPPSession
	 * @throws Exception should anything go wrong
	 */
	private ExtendedSmppSessionAdaptingDelegate buildSmppSession() throws Exception {
		final SMPPSession smppSession = createNewSession();
		final ExtendedSmppSessionAdaptingDelegate extendedSmppSessionAdaptingDelegate;
		if (reconnect) {
			sessionFactoryBean.setAutodetectInterfaces(false);
			sessionFactoryBean.setTarget(smppSession);
			final SMPPSession proxiedSession = (SMPPSession)sessionFactoryBean.getObject();

			extendedSmppSessionAdaptingDelegate = new ExtendedSmppSessionAdaptingDelegate(
					proxiedSession, new AutoReconnectLifecycle(proxiedSession));
		} else {
			extendedSmppSessionAdaptingDelegate = new ExtendedSmppSessionAdaptingDelegate(
					smppSession, new ConnectingLifecycle(smppSession));
		}

		for (MessageReceiverListener mrl : this.messageReceiverListeners)
			extendedSmppSessionAdaptingDelegate.addMessageReceiverListener(mrl);

		// if session state listener not null, add it
		if (sessionStateListener != null) {
			extendedSmppSessionAdaptingDelegate.addSessionStateListener(sessionStateListener);
		}

		extendedSmppSessionAdaptingDelegate.setBindType(this.bindType);
		return extendedSmppSessionAdaptingDelegate;
	}

	public void setAutoStartup(boolean autoStartup) {
		this.autoStartup = autoStartup;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isAutoStartup() {
		return this.autoStartup;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void stop(Runnable callback) {
		this.stop();
		callback.run();
	}

	/**
	 * {@inheritDoc}
	 */
	public void start() {
		log.debug("starting up in " + getClass().getName() + "#start().");
		if (reconnectingExecutor == null) {
			this.reconnectingExecutor = Executors.newFixedThreadPool(1);
		}

		( product).start();
		this.running = true;
	}

	/**
	 * {@inheritDoc}
	 */
	public void stop() {
		log.debug("shutting down in " + getClass().getName() + "#stop().");
		(  product).stop();

		// if we are running default executor, shut it down
		if (!reconnectingExecutorSet && reconnectingExecutor != null) {
			reconnectingExecutor.shutdown();
			this.reconnectingExecutor = null;
		}
		this.running = false;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isRunning() {
		return this.running;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getPhase() {
		return Ordered.LOWEST_PRECEDENCE;
	}

	/**
	 * {@inheritDoc}
	 * <p/>
	 * delegates to {@link #buildSmppSession()}
	 */
	public ExtendedSmppSession getObject() throws Exception {
		return product;
	}

	/**
	 * {@inheritDoc}
	 */
	public Class<?> getObjectType() {
		return ExtendedSmppSessionAdaptingDelegate.class;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isSingleton() {
		return true;
	}

	/**
	 * Set whether we want to reconnect the session. Default is true.
	 *
	 * @param reconnect true/false
	 */
	public void setReconnect(boolean reconnect) {
		this.reconnect = reconnect;
	}

	/**
	 * Set session reconnection interval. Default is 5 seconds.
	 *
	 * @param reconnectInterval reconnection interval in milliseconds
	 */
	public void setReconnectInterval(long reconnectInterval) {
		this.reconnectInterval = reconnectInterval;
	}

	/**
	 * Set executor service for performing SMPP reconnection.
	 * @param reconnectingExecutor executor service
	 */
	public void setReconnectingExecutor(ExecutorService reconnectingExecutor) {
		this.reconnectingExecutor = reconnectingExecutor;
		this.reconnectingExecutorSet = true;
	}

	/**
	 * {@inheritDoc}
	 */
	public void afterPropertiesSet() throws Exception {

		// NB, the reference handed back by {@link org.springframework.beans.factory.FactoryBean#getObject()}  isn't itself
		// managed, only the factory, so we cache it and then delegate through the factory's lifecycle methods.

		Assert.notNull(this.systemId, "the systemId can't be null");
		Assert.notNull(this.host, "the host can't be null");
		Assert.notNull(this.port, "the port can't be null");

		this.product = buildSmppSession();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void destroy() throws Exception {
		this.destroyed = true;
	}

	/**
	 * singleton {@link ConnectionFactory} that handles SSL
	 */
	final private static ConnectionFactory sslConnectionFactory = new ConnectionFactory() {

		public Connection createConnection(String host, int port) throws IOException {
			SocketFactory socketFactory = SSLSocketFactory.getDefault();
			Socket socket = socketFactory.createSocket(host, port);
			return new SocketConnection(socket);
		}
	};

	/**
	 * lifecycle implementation that simply {@link SMPPSession#connectAndBind(String, int, org.jsmpp.session.BindParameter)} and
	 * {@link org.jsmpp.session.SMPPSession#unbindAndClose()}.
	 */
	private class ConnectingLifecycle implements Lifecycle {

		private volatile boolean running;

		private SMPPSession session;

		private ConnectingLifecycle(SMPPSession smppSession) {
			this.session = smppSession;
		}

		public boolean isRunning() {
			return this.running;
		}

		public void stop() {
			if (session != null) {
				if (session.getSessionState().isBound()) {
					try {
						session.unbindAndClose();
					} catch (Exception t) {
						log.warn("Couldn't close and unbind the session", t);
					}
				}
			} else {
				log.warn("The smppSession given to close is null");
			}
		}

		public void start() {
			try {
				session.connectAndBind(host, port, bindType, systemId, password, systemType, addrTon, addrNpi, addressRange, timeout);
				this.running = true;
			} catch (IOException e) {
				if (log.isDebugEnabled()) {
					log.error("Error happened when trying to connect to " + host + ":" + port, e);
				}
				else {
					log.error("Error happened when trying to connect to " + host + ":" + port + ". Cause: "
							+ e.getMessage());
				}
			}
		}
	}

	/**
	 * Lifecycle implementation that will try to re-establish connection with specific interval. At the start of the
	 * connection.
	 *
	 * @author Johanes Soetanto
	 */
	private class AutoReconnectLifecycle implements Lifecycle {

		private final Logger log = LoggerFactory.getLogger(AutoReconnectLifecycle.class);
		private final SMPPSession session;
		private volatile boolean running;

		/**
		 * Creating auto reconnect lifecycle using SMPP session and reconnect interval in milliseconds
		 * @param smppSession reference to SMPP session
		 */
		private AutoReconnectLifecycle(SMPPSession smppSession) {
			this.session = smppSession;
		}

		@Override
		public boolean isRunning() {
			return this.running;
		}

		@Override
		public void stop() {
			if (session != null) {
				if (session.getSessionState().isBound()) {
					try {
						session.unbindAndClose();
					} catch (Exception t) {
						log.warn("Couldn't close and unbind the session", t);
					}
				}
			} else {
				log.warn("The smppSession given to close is null");
			}
		}

		@Override
		public void start() {
			connect();

			if (!running) {
				log.debug("Try to connect at later time. The delay is {}ms", reconnectInterval);
				scheduleReconnect();
			} else {
				registerSessionCloseListener();
			}
		}

		/**
		 * Register session state listener to reconnect when session is closed by server.
		 */
		private void registerSessionCloseListener() {
			log.debug("Registering session close listener");
			session.addSessionStateListener(new SessionStateListener() {
				@Override
				public void onStateChange(SessionState newState, SessionState oldState, Object source) {
					// when session is closed but client session has not been destroyed can indicates client
					// lose connection to server
					if (newState.equals(SessionState.CLOSED)) {
						running = false;
						if (!destroyed) {
							log.info("Session to {}:{} has been closed. Try to reconnect later", host, port);

							final SMPPSession newSession = createNewSession();
							newSession.setMessageReceiverListener(product.getDelegateMessageListener());
							if (sessionStateListener != null) {
								session.addSessionStateListener(sessionStateListener);
							}
							sessionFactoryBean.setTarget(newSession);

							scheduleReconnect();
						}
					}
				}
			});
		}

		/**
		 * Perform connection logic.
		 */
		private void connect() {
			try {
				session.connectAndBind(host, port, bindType, systemId, password, systemType,
						addrTon, addrNpi, addressRange, timeout);
				this.running = true;

			}
			catch (IOException e) {
				if (log.isDebugEnabled()) {
					log.error("Error happened when trying to connect to " + host + ":" + port, e);
				}
				else {
					log.error("Error happened when trying to connect to {}:{}. Cause: {}",
							new Object[]{host, port, e.getMessage()});
				}
			}
		}

		/**
		 * Schedule a session reconnection.
		 */
		private void scheduleReconnect() {

			reconnectingExecutor.submit(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(reconnectInterval);

						int attempt = 0;
						// if this session is still not run and the session has not been destroyed, re-connect
						while (!running && !destroyed) {
							log.info("Reconnecting attempt #{} ...", ++attempt);
							connect();

							if (!running) {
								// if still not running, then perform another sleep
								Thread.sleep(reconnectInterval);
							}
						}

						if (running) {
							log.info("Successfully reconnect at attempt #{}", attempt);
							// if finish re-connection loop and session is run we register session close listener
							registerSessionCloseListener();
						}

					} catch (InterruptedException e) {
						log.info("Interrupted when trying to connect to {}:{}", host, port);
					}
				}
			});
		}
	}

}
