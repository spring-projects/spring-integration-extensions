/*
 * Copyright 2015 the original author or authors.
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
package com.onlinetechvision.integration.hazelcast.inbound;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.PollableChannel;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.hazelcast.core.ITopic;

/**
 * HazelcastDistributedTopicEventDrivenInboundChannelAdapterTest
 * 
 * @author Eren Avsarogullari
 * @since 1.0.0
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/HazelcastDistributedTopicEventDrivenInboundChannelAdapterTest-context.xml"})
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class HazelcastDistributedTopicEventDrivenInboundChannelAdapterTest {

	@Autowired
	private PollableChannel edTopicChannel1;
	
	@Resource
	private ITopic<User> edDistributedTopic1;
			
	@Test
	public void testEventDrivenForOnlyADDEDEntryEvent() {
		edDistributedTopic1.publish(new User(1, "TestName1", "TestSurname1"));
		Message<?> msg = edTopicChannel1.receive(2_000);
		Assert.assertNotNull(msg);
		Assert.assertNotNull(msg.getPayload());
		Assert.assertTrue(msg.getPayload() instanceof com.hazelcast.core.Message);
		Assert.assertEquals(1, ((User)((com.hazelcast.core.Message<?>)msg.getPayload()).getMessageObject()).getId());
		Assert.assertEquals("TestName1", ((User)((com.hazelcast.core.Message<?>)msg.getPayload()).getMessageObject()).getName());
		Assert.assertEquals("TestSurname1", ((User)((com.hazelcast.core.Message<?>)msg.getPayload()).getMessageObject()).getSurname());
	}
	
}