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
import org.junit.After;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.integration.mapping.InboundMessageMapper;
import org.springframework.integration.mapping.OutboundMessageMapper;
import org.springframework.integration.smpp.inbound.SmppInboundGateway;
import org.springframework.integration.smpp.session.ExtendedSmppSession;
import org.springframework.integration.test.util.TestUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


/**
 * @author Johanes Soetanto
 * @since 2.2
 *
 */
public class SmppInboundGatewayParserTests {

	private ConfigurableApplicationContext context;

	private SmppInboundGateway gateway;

	@Test
	public void testRetrievingInboundGatewayParser() throws Exception {
		setUp("SmppInboundGatewayParserTests.xml", getClass(), "smppInboundGateway");

        // reply timeout
		long requestTimeout = TestUtils.getPropertyValue(gateway, "messagingTemplate.sendTimeout", Long.class);
		assertEquals(10000, requestTimeout);

        // request timeout
        long replyTimeout = TestUtils.getPropertyValue(gateway, "messagingTemplate.receiveTimeout", Long.class);
        assertEquals(5000, replyTimeout);

        ExtendedSmppSession session = TestUtils.getPropertyValue(gateway, "smppSession", ExtendedSmppSession.class);
        assertNotNull(session);
        System.out.println("Session: "+session);

        TypeOfNumber ton = TestUtils.getPropertyValue(gateway, "defaultSourceAddressTypeOfNumber", TypeOfNumber.class);
        assertEquals(ton, TypeOfNumber.INTERNATIONAL);

        String sourceAddress = TestUtils.getPropertyValue(gateway, "defaultSourceAddress", String.class);
        assertEquals("123456789", sourceAddress);

        // channels
        AbstractMessageChannel requestChannel = TestUtils.getPropertyValue(gateway, "requestChannel", AbstractMessageChannel.class);
        assertEquals("requestChannel", requestChannel.getComponentName());
        AbstractMessageChannel outputChannel = TestUtils.getPropertyValue(gateway, "replyChannel", AbstractMessageChannel.class);
        assertEquals("replyChannel", outputChannel.getComponentName());
        AbstractMessageChannel errorChannel = TestUtils.getPropertyValue(gateway, "errorChannel", AbstractMessageChannel.class);
        assertEquals("errorChannel", errorChannel.getComponentName());

        // mappers
        InboundMessageMapper inboundMessageMapper = TestUtils.getPropertyValue(gateway, "requestMapper", InboundMessageMapper.class);
        assertNotNull(inboundMessageMapper);
        OutboundMessageMapper outboundMessageMapper = TestUtils.getPropertyValue(gateway, "messageConverter.outboundMessageMapper", OutboundMessageMapper.class);
        assertNotNull(outboundMessageMapper);

	}



	@After
	public void tearDown() {
		if (context != null) {
			context.close();
		}
	}

	public void setUp(String name, Class<?> cls, String gatewayId) {
		context    = new ClassPathXmlApplicationContext(name, cls);
		gateway   = this.context.getBean(gatewayId, SmppInboundGateway.class);
	}

}
