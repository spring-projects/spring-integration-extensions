/*
 * Copyright 2015-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.integration.hazelcast.inbound;

import javax.annotation.Resource;

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.hazelcast.HazelcastIntegrationTestUser;
import org.springframework.integration.hazelcast.inbound.util.HazelcastInboundChannelAdapterTestUtils;
import org.springframework.messaging.PollableChannel;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.hazelcast.core.ITopic;
import com.hazelcast.instance.HazelcastInstanceFactory;

/**
 * Hazelcast Distributed Topic Event Driven Inbound Channel Adapter Test
 *
 * @author Eren Avsarogullari
 * @since 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@DirtiesContext
public class HazelcastDistributedTopicEventDrivenInboundChannelAdapterTests {

	@Autowired
	private PollableChannel edTopicChannel1;

	@Resource
	private ITopic<HazelcastIntegrationTestUser> edDistributedTopic1;

	@AfterClass
	public static void shutdown() {
		HazelcastInstanceFactory.terminateAll();
	}

	@Test
	public void testEventDrivenForOnlyADDEDEntryEvent() {
		HazelcastInboundChannelAdapterTestUtils
				.testEventDrivenForTopicMessageEvent(edDistributedTopic1, edTopicChannel1);
	}

}
