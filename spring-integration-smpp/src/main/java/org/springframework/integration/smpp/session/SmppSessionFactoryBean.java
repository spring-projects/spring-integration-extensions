package org.springframework.integration.smpp.session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsmpp.DefaultPDUReader;
import org.jsmpp.DefaultPDUSender;
import org.jsmpp.SynchronizedPDUSender;
import org.jsmpp.bean.BindType;
import org.jsmpp.bean.NumberingPlanIndicator;
import org.jsmpp.bean.TypeOfNumber;
import org.jsmpp.session.MessageReceiverListener;
import org.jsmpp.session.SMPPSession;
import org.jsmpp.session.SessionStateListener;
import org.jsmpp.session.connection.Connection;
import org.jsmpp.session.connection.ConnectionFactory;
import org.jsmpp.session.connection.socket.SocketConnection;
import org.jsmpp.util.DefaultComposer;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Factory bean to create a {@link SMPPSession}. Usually, you need little more than the {@link #host},
 * the {@link #port}, perhaps a {@link #password}, and a {@link #systemId}.
 * <p/>
 * The {@link SMPPSession } represents a connection to a SMSC, through which SMS messages are sent and received.
 * <p/>
 * Here is a breakdown of the supported parameters on this factory bean:
 * <p/>
 * host				 the SMSC host to which the session is bound   (think of this as the host of your email server)
 * port				 the SMSC port to which the session is bound (think of this as a port on your email server)
 * bindType		 values of type {@link org.jsmpp.bean.BindType}. the bind type specifies whether this {@link SMPPSession} can send ({@link org.jsmpp.bean.BindType#BIND_TX}), receive ({@link org.jsmpp.bean.BindType#BIND_RX}), or both send and receive ({@link org.jsmpp.bean.BindType#BIND_TRX}).
 * systemId		 the system ID for the server being bound to
 * password		 the password for the server being bound to
 * systemType	 the SMSC system type
 * addrTon			a value from the {@link org.jsmpp.bean.TypeOfNumber} enumeration. default is {@link org.jsmpp.bean.TypeOfNumber#UNKNOWN}
 * addrNpi			a value from  the {@link org.jsmpp.bean.NumberingPlanIndicator} enumeration. Default is {@link org.jsmpp.bean.NumberingPlanIndicator#UNKNOWN}
 * addressRange can be null. Specifies the address range.
 * timeout			a good default value is 60000  (1 minute)
 *
 * @author Josh Long
 *         <p/>
 *         todo support a proxied SMPPSession that automatically recovers from disconnects a la the examples {@link org.jsmpp.examples.gateway.AutoReconnectGateway}
 * @see org.jsmpp.session.SMPPSession#SMPPSession()
 * @see org.jsmpp.session.SMPPSession#connectAndBind(String, int, org.jsmpp.session.BindParameter)
 * @see org.jsmpp.session.SMPPSession#connectAndBind(String, int, org.jsmpp.bean.BindType, String, String, String, org.jsmpp.bean.TypeOfNumber, org.jsmpp.bean.NumberingPlanIndicator, String, long)
 * @since 2.1
 */
public class SmppSessionFactoryBean implements FactoryBean<ExtendedSmppSession>, SmartLifecycle, InitializingBean {

	/**
	 * impl of {@link Lifecycle} that connects and disconnects respectively in
	 * {@link org.springframework.context.Lifecycle#start()} and {@link org.springframework.context.Lifecycle#stop()}
	 *
	 * @author Josh Long
	 */
	private Set<MessageReceiverListener> messageReceiverListeners = new HashSet<MessageReceiverListener>();
	private boolean autoStartup;
	private volatile boolean running;
	private Log log = LogFactory.getLog(getClass());
	private SessionStateListener sessionStateListener;
	private boolean ssl = false;
	private String host = "127.0.0.1";
	private String addressRange;
	private long timeout = 60 * 1000;// 1 minute
	private int port = 2775;	// good default though this has been known to change
	private BindType bindType = BindType.BIND_TRX; // bind as a 'transceiver' - only 3.4 of the spec <em>requires</em> support for this
	private String systemId = getClass().getSimpleName().toLowerCase();	 // what would typically be called 'user' in a user/pw scheme
	private String password;
	private String systemType = "cp";
	private TypeOfNumber addrTon = TypeOfNumber.UNKNOWN;
	private NumberingPlanIndicator addrNpi = NumberingPlanIndicator.UNKNOWN;

	private ExtendedSmppSessionAdaptingDelegate product;

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

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public void setSessionStateListener(SessionStateListener sessionStateListener) {
		this.sessionStateListener = sessionStateListener;
	}

	public void setMessageReceiverListeners(MessageReceiverListener... listeners) {
		setMessageReceiverListeners(new HashSet<MessageReceiverListener>(Arrays.asList(listeners)));
	}

	public void setMessageReceiverListeners(Set<MessageReceiverListener> messageReceiverListeners) {
		this.messageReceiverListeners = messageReceiverListeners;
	}

	/**
	 * @return the configured SMPPSession
	 * @throws Exception should anything go wrong
	 */
	private ExtendedSmppSessionAdaptingDelegate buildSmppSession() throws Exception {
		SMPPSession smppSession = null;
		if (!ssl) {
			smppSession = new SMPPSession();
		} else {
			smppSession = new SMPPSession(new SynchronizedPDUSender(new DefaultPDUSender(new DefaultComposer())), new DefaultPDUReader(), sslConnectionFactory);
		}

		ExtendedSmppSessionAdaptingDelegate extendedSmppSessionAdaptingDelegate = new ExtendedSmppSessionAdaptingDelegate(smppSession, new ConnectingLifecycle(smppSession));

		for (MessageReceiverListener mrl : this.messageReceiverListeners)
			extendedSmppSessionAdaptingDelegate.addMessageReceiverListener(mrl);

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
	public void stop(Runnable callback) {
		try {
			log.debug("shutting down in " + getClass().getName() + "#stop(Runnable).");
			callback.run();
		} catch (Throwable throwable) {
			log.warn("error when trying to shutdown " + getClass().getName() + ", could not invoke the callback's Runnable#run method");
		}
		this.stop();
	}

	/**
	 * {@inheritDoc}
	 */
	public void start() {
		log.debug("starting up in " + getClass().getName() + "#start().");
		( product).start();
		this.running = true;
	}

	/**
	 * {@inheritDoc}
	 */
	public void stop() {
		log.debug("shutting down in " + getClass().getName() + "#stop().");
		(  product).stop();
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
					} catch (Throwable t) {
						log.warn("couldn't close and unbind the session", t);
					}
				}
			} else {
				log.warn("the smppSession given to close is null");
			}
		}

		public void start() {
			try {
				session.connectAndBind(host, port, bindType, systemId, password, systemType, addrTon, addrNpi, addressRange, timeout);
				this.running = true;
			} catch (IOException e) {
				log.error("something happened when trying to connect", e);
			}
		}
	}
}

/*    private void reconnectAfter(final long timeInMillis) {
        new Thread() {
            @Override
            public void run() {
                logger.info("Schedule reconnect after " + timeInMillis + " millis");
                try {
                    Thread.sleep(timeInMillis);
                } catch (InterruptedException e) {
                }

                int attempt = 0;
                while (session == null || session.getSessionState().equals(SessionState.CLOSED)) {
                    try {
                        logger.info("Reconnecting attempt #" + (++attempt) + "...");
                        session = newSession();
                    } catch (IOException e) {
                        logger.error("Failed opening connection and bind to " + remoteIpAddress + ":" + remotePort, e);
                        // wait for a second
                        try { Thread.sleep(1000); } catch (InterruptedException ee) {}
                    }
                }
            }
        }.start();
    }


     private class SessionStateListenerImpl implements SessionStateListener {
        public void onStateChange(SessionState newState, SessionState oldState,
                Object source) {
            if (newState.equals(SessionState.CLOSED)) {
                logger.info("Session closed");
                reconnectAfter(reconnectInterval);
            }
        }
    }

    */
