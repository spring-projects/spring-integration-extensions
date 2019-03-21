/*
uytea * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.springframework.integration.smpp.config.xml;

import org.junit.After;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.integration.smpp.inbound.SmppInboundChannelAdapter;
import org.springframework.integration.smpp.session.ExtendedSmppSession;
import org.springframework.integration.test.util.TestUtils;

import static org.junit.Assert.*;


/**
 * @author Johanes Soetanto
 * @since 1.0
 *
 */
public class SmppInboundChannelAdapterParserTests {

	private ConfigurableApplicationContext context;

	private SmppInboundChannelAdapter consumer;

	@Test
	public void testInboundChannelAdapterParser() throws Exception {

		setUp("SmppInboundChannelAdapterParserTests.xml", getClass(), "smppInboundChannelAdapter");

		final AbstractMessageChannel outputChannel = TestUtils.getPropertyValue(this.consumer, "channel", AbstractMessageChannel.class);
		assertEquals("out", outputChannel.getComponentName());

		ExtendedSmppSession session = TestUtils.getPropertyValue(consumer, "smppSession", ExtendedSmppSession.class);
		assertNotNull(session);

		boolean autoStartup = TestUtils.getPropertyValue(consumer, "autoStartup" , Boolean.class);
		assertTrue(autoStartup);
	}

	@After
	public void tearDown(){
		if(context != null){
			context.close();
		}
	}

	public void setUp(String name, Class<?> cls, String consumerId){
		context    = new ClassPathXmlApplicationContext(name, cls);
		consumer   = this.context.getBean(consumerId, SmppInboundChannelAdapter.class);
	}

}
