/*
 * Copyright 2002-2013 the original author or authors.
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
import org.springframework.integration.kafka.support.KafkaProducerContext;
import org.springframework.integration.kafka.support.ProducerConfiguration;
import org.springframework.integration.kafka.support.ProducerMetadata;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Soby Chacko
 * @since 0.5
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class KafkaProducerContextParserPropertyTopicTests<K, V, T> {

	@Autowired
	private ApplicationContext appContext;

	@Test
	@SuppressWarnings("unchecked")
	public void testProducerContextConfiguration() {
		final KafkaProducerContext<K, V> producerContext = appContext.getBean("producerContext", KafkaProducerContext.class);
		Assert.assertNotNull(producerContext);

		final Map<String, ProducerConfiguration<K, V>> topicConfigurations = producerContext.getTopicsConfiguration();
		Assert.assertEquals(topicConfigurations.size(), 1);

		final ProducerConfiguration<K, V> producerConfigurationTest1 = topicConfigurations.get("producerConfiguration___topic.name_");
		Assert.assertNotNull(producerConfigurationTest1);
		final ProducerMetadata<K, V> producerMetadataTest1 = producerConfigurationTest1.getProducerMetadata();
		Assert.assertEquals(producerMetadataTest1.getTopic(), "test1");
	}
}
