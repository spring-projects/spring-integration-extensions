package org.springframework.integration.smpp;

/* Copyright 2002-2013 the original author or authors.
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

import org.jsmpp.bean.SMSCDeliveryReceipt;
import org.jsmpp.util.AbsoluteTimeFormatter;
import org.jsmpp.util.TimeFormatter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.handler.AbstractReplyProducingMessageHandler;
import org.springframework.integration.smpp.core.SmppConstants;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

@ContextConfiguration("classpath:TestSmppInboundGateway-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class TestUSSDInboundGateway {

    private final Logger logger = LoggerFactory.getLogger(TestUSSDInboundGateway.class);

    @Value("#{in1}")
    SubscribableChannel in1;

    long now = System.currentTimeMillis();

    String smsResponse = "this is a response created at " + now;

    private static TimeFormatter timeFormatter = new AbsoluteTimeFormatter();

    AtomicInteger count = new AtomicInteger();

    @Test
    public void testReceiveUSSDrequestAndReply() throws Throwable {

        // the gateway receives USSD request and reply to USSD gateway of Cellular operator
        // AbstractReplyProducingMessageHandler produces a reply to mobile station owner through USSD-GW
        // Handler is awaiting for request, after request is received handler send response
        //// inbound-gw: receive, produces reply
        //// inbound-gw: send

        MessageHandler inboundMessageHandler = new AbstractReplyProducingMessageHandler() {
            @Override
            protected Object handleRequestMessage(Message<?> requestMessage) {
                logger.debug(requestMessage.getPayload());
                count.incrementAndGet();
                return MessageBuilder.withPayload(smsResponse)
                        // copying request headers to response headers (TYPE_OF_NUMBER, NUMBERING_PLAN_INDICATOR,
                        // and OPTIONAL_PARAMETERS, including:
                        //  user_message_reference - for reply to sender
                        //  ussd_service_op - for setting USSD service operation type (USSR, PSSR, USSN, etc.)
                        .copyHeadersIfAbsent(requestMessage.getHeaders())
                        .setHeader(SmppConstants.REGISTERED_DELIVERY_MODE, SMSCDeliveryReceipt.SUCCESS)
                        .setHeader(SmppConstants.SCHEDULE_DELIVERY_TIME, timeFormatter.format(new Date()))
                        .build();
            }
        };
        this.in1.subscribe(inboundMessageHandler);


        Thread.sleep(1000 * 10);

        Assert.assertEquals(this.count.intValue(), 1);



    }
}


