package org.springframework.integration.reactor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.message.GenericMessage;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * @author Jon Brisbin
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ReactorChannelTests {

	@Autowired
	CountDownLatch latch;
	@Autowired
	ReactorChannel reactor;

	@Test
	public void reactorMessageChannelsSendAndReceiveMessages() throws InterruptedException {
		reactor.send(new GenericMessage<String>("Hello World!"));

		latch.await(5, TimeUnit.SECONDS);
		assertThat("latch was counted down", latch.getCount(), is(0L));
	}

}
