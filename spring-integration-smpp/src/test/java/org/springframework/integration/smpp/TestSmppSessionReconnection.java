package org.springframework.integration.smpp;

import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.smpp.session.SmppSessionFactoryBean;
import org.springframework.integration.test.util.SocketUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This is to test whether reconnection will work. The test procedure is
 * <ol>
 *     <li>Client connects to SMSC</li>
 *     <li>SMSC shuts down</li>
 *     <li>SMSC restart</li>
 *     <li>Client reconnect to SMSC</li>
 * </ol>
 *
 * @author Johanes Soetanto
 * @since 1.0
 */
public class TestSmppSessionReconnection {

    private Logger log = LoggerFactory.getLogger(getClass());
    int port;
    String systemId = "pavel";
    String pass = "wpsd";
    MockSmppServer server;
    SmppSessionFactoryBean client;
    ExecutorService executor = Executors.newSingleThreadExecutor();
    int serverAcceptTimeout = 1500;
    int clientReconnectInterval = 1000;

    @Before
    public void setUp() throws Exception {
        port = SocketUtils.findAvailableServerSocket(13000);

        client = new SmppSessionFactoryBean();
        client.setPort(port);
        client.setSystemId(systemId);
        client.setPassword(pass);
        client.setReconnectInterval((long)clientReconnectInterval);
    }

    private void startServer() {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    log.debug("Starting server");
                    server = new MockSmppServer(port, systemId, pass);
                    server.setAcceptConnectionTimeout(serverAcceptTimeout);
                    server.onPostConstruct();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //@org.junit.Test
    public void testReconnection() throws Exception {
        startServer();

        Thread.sleep(500);

        log.debug("Starting client");
        client.afterPropertiesSet();
        client.start();
        Thread.sleep(3000);

        log.debug("Stopping server");
        server.stop();
        server.onDestroy();
        Thread.sleep(3000);

        log.debug("Starting server again");
        startServer();

        log.debug("The client should reconnect");
        Thread.sleep(3000);
    }

    // if client session is destroyed by the container, we don't want to try to re-establish session
    //@org.junit.Test
    public void testReconnection_whenClientSessionDestroyed() throws Exception {
        startServer();

        Thread.sleep(500);

        log.debug("Starting client");
        client.afterPropertiesSet();
        client.start();

        Thread.sleep(2000);
        log.debug("Destroy the client");
        client.destroy();
        Thread.sleep(3000);

        log.debug("Stopping server");
        server.stop();
        server.onDestroy();
        Thread.sleep(3000);

        log.debug("Starting server again");
        startServer();
        Thread.sleep(3000);
    }

    // if reconnect is disabled, well do not reconnect
    //@org.junit.Test
    public void testDisableReconnect() throws Exception {
        startServer();

        Thread.sleep(500);
        client.setReconnect(false);
        client.afterPropertiesSet();
        client.start();

        Thread.sleep(2000);
        log.debug("Stop server");
        server.stop();
        server.onDestroy();

        Thread.sleep(2000);
        log.debug("Starting server again");
        startServer();
        Thread.sleep(2000);
        log.debug("There should be no reconnection from client");
    }
}
