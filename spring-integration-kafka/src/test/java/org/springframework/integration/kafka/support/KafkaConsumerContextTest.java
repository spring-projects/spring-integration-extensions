package org.springframework.integration.kafka.support;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.integration.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Soby Chacko
 */
public class KafkaConsumerContextTest {

    @Test
    public void testMergeResultsFromMultipleConsumerConfiguration() {
        final KafkaConsumerContext kafkaConsumerContext = new KafkaConsumerContext();
        final ListableBeanFactory beanFactory = Mockito.mock(ListableBeanFactory.class);
        final ConsumerConfiguration consumerConfiguration1 = Mockito.mock(ConsumerConfiguration.class);
        final ConsumerConfiguration consumerConfiguration2 = Mockito.mock(ConsumerConfiguration.class);

        final Map<String, ConsumerConfiguration> map = new HashMap<String, ConsumerConfiguration>();
        map.put("config1", consumerConfiguration1);
        map.put("config2", consumerConfiguration2);

        Mockito.when(beanFactory.getBeansOfType(ConsumerConfiguration.class)).thenReturn(map);
        kafkaConsumerContext.setBeanFactory(beanFactory);

        final Map<String, List<Object>> result1 = new HashMap<String, List<Object>>();
        final List<Object> l1 = new ArrayList<Object>();
        l1.add("got message1 - l1");
        l1.add("got message2 - l1");
        result1.put("topic1", l1);

        final Map<String, List<Object>> result2 = new HashMap<String, List<Object>>();
        final List<Object> l2 = new ArrayList<Object>();
        l2.add("got message1 - l2");
        l2.add("got message2 - l2");
        l2.add("got message3 - l2");

        result1.put("topic2", l2);

        Mockito.when(consumerConfiguration1.receive()).thenReturn(result1);
        Mockito.when(consumerConfiguration2.receive()).thenReturn(result2);

        final Message<Map<String, List<Object>>> messages = kafkaConsumerContext.receive();
        Assert.assertEquals(messages.getPayload().size(), 2);
        Assert.assertEquals(messages.getPayload().get("topic1").size(), 2);
        Assert.assertEquals(messages.getPayload().get("topic1").get(0), "got message1 - l1");
        Assert.assertEquals(messages.getPayload().get("topic1").get(1), "got message2 - l1");

        Assert.assertEquals(messages.getPayload().get("topic2").size(), 3);
        Assert.assertEquals(messages.getPayload().get("topic2").get(0), "got message1 - l2");
        Assert.assertEquals(messages.getPayload().get("topic2").get(1), "got message2 - l2");
        Assert.assertEquals(messages.getPayload().get("topic2").get(2), "got message3 - l2");
    }
}
