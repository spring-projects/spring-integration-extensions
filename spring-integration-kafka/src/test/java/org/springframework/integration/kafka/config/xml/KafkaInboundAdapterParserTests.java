package org.springframework.integration.kafka.config.xml;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.endpoint.SourcePollingChannelAdapter;
import org.springframework.integration.kafka.core.KafkaExecutor;
import org.springframework.integration.kafka.support.KafkaServer;
import org.springframework.integration.kafka.support.KafkaZooKeeperServerDefaults;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class KafkaInboundAdapterParserTests {

	@Autowired
	private ApplicationContext appContext;

	/**
	 * Test method for {@link org.springframework.integration.kafka.config.xml.KafkaInboundChannelAdapterParser#parseSource(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)}.
	 */
	@Test
	public void testParseSourceElementParserContext() throws Exception {
		SourcePollingChannelAdapter adapter = appContext.getBean("kafkaInboundChannelAdapter",
				SourcePollingChannelAdapter.class);

        Assert.assertNotNull(adapter);
		Assert.assertFalse(adapter.isAutoStartup());

        KafkaExecutor executor = appContext.getBean("kafkaInboundChannelAdapter.kafkaExecutor",
                KafkaExecutor.class);
        Assert.assertNotNull(executor);
        Assert.assertEquals(executor.getMaxMessagesPerPoll(), 1);
        Assert.assertEquals(executor.getReceiveTimeout(), "5000");

        KafkaServer kafkaServer = executor.getKafkaServer();

        Assert.assertEquals(kafkaServer.getZkConnect(), KafkaZooKeeperServerDefaults.ZK_CONNECT);
        Assert.assertEquals(kafkaServer.getZkConnectionTimeout(),
                KafkaZooKeeperServerDefaults.ZK_CONNECTION_TIMEOUT);
        Assert.assertEquals(kafkaServer.getGroupId(), "test_group");
        Assert.assertEquals(kafkaServer.getZkSessionTimeout(),
                KafkaZooKeeperServerDefaults.ZK_SESSION_TIMEOUT);
        Assert.assertEquals(kafkaServer.getAutoCommitInterval(),
                KafkaZooKeeperServerDefaults.AUTO_COMMIT_INTERVAL);
        Assert.assertEquals(kafkaServer.getZkSyncTime(), KafkaZooKeeperServerDefaults.ZK_SYNC_TIME);
	}

}
