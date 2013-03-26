package org.springframework.integration.kafka.config.xml;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.kafka.support.KafkaBroker;
import org.springframework.integration.kafka.support.KafkaBrokerDefaults;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Soby Chacko
 * @since 1.0
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class KafkaBrokerParserTests {

	@Autowired
	private ApplicationContext appContext;

	@Test
	public void testCustomKafkaBrokerConfiguration() {
		KafkaBroker broker = appContext.getBean("kafkaBroker", KafkaBroker.class);

		Assert.assertEquals("localhost:2181", broker.getZkConnect());
        Assert.assertEquals("10000", broker.getZkConnectionTimeout());
        Assert.assertEquals("10000", broker.getZkSessionTimeout());
        Assert.assertEquals("200", broker.getZkSyncTime());
	}

    @Test
    public void testDefaultKafkaBrokerConfiguration() {
        KafkaBroker broker = appContext.getBean("defaultKafkaBroker", KafkaBroker.class);

        Assert.assertEquals(KafkaBrokerDefaults.ZK_CONNECT, broker.getZkConnect());
        Assert.assertEquals(KafkaBrokerDefaults.ZK_CONNECTION_TIMEOUT, broker.getZkConnectionTimeout());
        Assert.assertEquals(KafkaBrokerDefaults.ZK_SESSION_TIMEOUT, broker.getZkSessionTimeout());
        Assert.assertEquals(KafkaBrokerDefaults.ZK_SYNC_TIME, broker.getZkSyncTime());
    }

}
