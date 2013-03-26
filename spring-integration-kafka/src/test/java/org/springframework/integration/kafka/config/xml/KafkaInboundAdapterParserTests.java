package org.springframework.integration.kafka.config.xml;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.endpoint.SourcePollingChannelAdapter;
import org.springframework.integration.kafka.core.KafkaExecutor;
import org.springframework.integration.kafka.support.KafkaBroker;
import org.springframework.integration.kafka.support.KafkaBrokerDefaults;
import org.springframework.integration.kafka.support.KafkaConsumerContext;
import org.springframework.integration.kafka.support.KafkaConsumerDefaults;
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

        KafkaConsumerContext consumerContext = appContext.getBean("kafkaInboundChannelAdapter.kafkaConsumerContext",
                KafkaConsumerContext.class);
        Assert.assertNotNull(consumerContext);
        Assert.assertEquals(consumerContext.getMaxMessagesPerPoll(), 100);
        Assert.assertEquals(consumerContext.getReceiveTimeout(), "5000");
        Assert.assertEquals(consumerContext.getAutoCommitInterval(),
                KafkaConsumerDefaults.AUTO_COMMIT_INTERVAL);

        KafkaExecutor executor = appContext.getBean("kafkaInboundChannelAdapter.kafkaExecutor",
                KafkaExecutor.class);
        KafkaBroker kafkaBroker = executor.getKafkaBroker();

        Assert.assertEquals(kafkaBroker.getZkConnect(), KafkaBrokerDefaults.ZK_CONNECT);
        Assert.assertEquals(kafkaBroker.getZkConnectionTimeout(),
                KafkaBrokerDefaults.ZK_CONNECTION_TIMEOUT);
        Assert.assertEquals(kafkaBroker.getZkSessionTimeout(),
                KafkaBrokerDefaults.ZK_SESSION_TIMEOUT);

        Assert.assertEquals(kafkaBroker.getZkSyncTime(), KafkaBrokerDefaults.ZK_SYNC_TIME);
	}

}
