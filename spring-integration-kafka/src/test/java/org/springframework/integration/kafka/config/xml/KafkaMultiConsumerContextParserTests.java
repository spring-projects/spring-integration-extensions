/*
 * Copyright 2014 the original author or authors.
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
package org.springframework.integration.kafka.config.xml;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.kafka.support.ConsumerConfiguration;
import org.springframework.integration.kafka.support.ConsumerMetadata;
import org.springframework.integration.kafka.support.KafkaConsumerContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Ilayaperumal Gopinathan
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class KafkaMultiConsumerContextParserTests<K,V> {

	@Autowired
	private ApplicationContext appContext;
	
	@SuppressWarnings("unchecked")
	@Test
	public void testConsumerContextConfigurations() {
		final KafkaConsumerContext<K,V> consumerContext = appContext.getBean("consumerContext", KafkaConsumerContext.class);
		Assert.assertNotNull(consumerContext);
		Map<String, ConsumerConfiguration<K,V>> consumerConfigurations = consumerContext.getConsumerConfigurations();
		final ConsumerConfiguration<K,V> cc = consumerConfigurations.get("default1");
		final ConsumerMetadata<K,V> cm = cc.getConsumerMetadata();
		Assert.assertTrue(cm.getTopicStreamMap().get("test1") == 3);
		Assert.assertTrue(cm.getTopicStreamMap().get("test2") == 4);
		Assert.assertNotNull(cm);
		final ConsumerConfiguration<K,V> cc2 = consumerConfigurations.get("default2");
		final ConsumerMetadata<K,V> cm2 = cc2.getConsumerMetadata();
		Assert.assertTrue(cm2.getTopicStreamMap().get("test3") == 1);
		Assert.assertNotNull(cm2);
	}
}
