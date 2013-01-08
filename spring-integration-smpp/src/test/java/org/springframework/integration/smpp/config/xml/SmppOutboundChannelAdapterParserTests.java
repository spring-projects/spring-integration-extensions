/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.springframework.integration.smpp.config.xml;

import org.jsmpp.bean.TypeOfNumber;
import org.jsmpp.util.TimeFormatter;
import org.junit.After;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.Message;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.endpoint.EventDrivenConsumer;
import org.springframework.integration.smpp.core.SmppConstants;
import org.springframework.integration.smpp.outbound.SmppOutboundChannelAdapter;
import org.springframework.integration.smpp.session.ExtendedSmppSession;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.test.util.TestUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


/**
 *
 * @author Johanes Soetanto
 * @since 2.2
 *
 */
public class SmppOutboundChannelAdapterParserTests {

	private ConfigurableApplicationContext context;

	private EventDrivenConsumer consumer;

	@Test
	public void testRetrievingOutboundChannelAdapterParser() throws Exception {
		setUp("SmppOutboundChannelAdapterParserTests.xml", getClass());


		final AbstractMessageChannel inputChannel = TestUtils.getPropertyValue(this.consumer, "inputChannel", AbstractMessageChannel.class);
		assertEquals("target", inputChannel.getComponentName());

        final SmppOutboundChannelAdapter gateway = TestUtils.getPropertyValue(this.consumer, "handler", SmppOutboundChannelAdapter.class);

        ExtendedSmppSession session = TestUtils.getPropertyValue(gateway, "smppSession", ExtendedSmppSession.class);
        assertNotNull(session);

        TypeOfNumber ton = TestUtils.getPropertyValue(gateway, "defaultSourceAddressTypeOfNumber", TypeOfNumber.class);
        assertEquals(ton, TypeOfNumber.SUBSCRIBER_NUMBER);

        String sourceAddress = TestUtils.getPropertyValue(gateway, "defaultSourceAddress", String.class);
        assertEquals("12345", sourceAddress);

        // this is not set, should be default value
        TimeFormatter timeFormatter = TestUtils.getPropertyValue(gateway, "timeFormatter", TimeFormatter.class);
        assertNotNull(timeFormatter);

        // I send message
        Message<String> message = MessageBuilder.withPayload("Yuhuu !!! i am connected using Spring Integration namespace")
                .setHeader(SmppConstants.SRC_ADDR, "pavel")
                .setHeader(SmppConstants.DST_ADDR, "pavel")
                .build();

        MessagingTemplate template = context.getBean("messagingTemplate", MessagingTemplate.class);
        template.send("target", message);
	}

	@After
	public void tearDown(){
		if(context != null){
			context.close();
		}
	}

	public void setUp(String name, Class<?> cls){
		context    = new ClassPathXmlApplicationContext(name, cls);
		consumer   = this.context.getBean("smppOutboundChannelAdapter", EventDrivenConsumer.class);
	}

}
