package org.springframework.integration.reactor.tcp;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.MessagingException;
import org.springframework.integration.core.MessageHandler;
import org.springframework.integration.message.GenericMessage;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import reactor.io.Buffer;

import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * @author Jon Brisbin
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class TcpServerInboundChannelAdapterTests {

	ExecutorService threadPool;
	@Autowired
	TcpServerInboundChannelAdapter<String, String> server;

	@Before
	public void setup() {
		threadPool = Executors.newCachedThreadPool();
	}

	@After
	public void cleanup() {
		threadPool.shutdown();
	}

	@Test
	public void testTcpServerInboundChannelAdapter() throws Exception {
		final CountDownLatch latch = new CountDownLatch(1);

		server.subscribe(new MessageHandler() {
			@Override
			public void handleMessage(Message<?> message) throws MessagingException {
				latch.countDown();
				System.out.println("msg: " + message);
				server.send(new GenericMessage<String>("Hello World!"));
			}
		});

		send(Buffer.wrap("Hello World!"));
		latch.await(5, TimeUnit.SECONDS);

		assertThat("latch was counted down", latch.getCount(), is(0L));
	}


	private void send(final Buffer data) {
		threadPool.submit(new Runnable() {
			@Override
			public void run() {
				try {
					SocketChannel channel = SocketChannel.open(new InetSocketAddress(3000));
					channel.write(data.byteBuffer());

				} catch (Exception e) {
					throw new IllegalStateException(e);
				}
			}
		});
	}

}
