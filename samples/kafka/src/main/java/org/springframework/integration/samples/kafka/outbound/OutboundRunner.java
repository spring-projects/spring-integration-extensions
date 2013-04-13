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
package org.springframework.integration.samples.kafka.outbound;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.message.GenericMessage;
import org.springframework.integration.support.MessageBuilder;

import java.util.ArrayList;
import java.util.List;

public class OutboundRunner {

    public static void main(String args[]) {
       ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
                       "kafkaOutboundAdapterParserTests-context.xml",
               OutboundRunner.class);


        ctx.start();

        final MessageChannel channel = ctx.getBean("inputToKafka", MessageChannel.class);
                System.out.println(channel.getClass());

                final List<String> payloads = new ArrayList<String>();
               for(int i= 0; i < 5000; i++)  {
                   channel.send(
                		   MessageBuilder.withPayload("hello Fom ob adapter -  " + i).setHeader("messageKey", String.valueOf(i)).build());
                   System.out.println("message sent " + i);

//                   payloads.add("hello Fom ob adapter -  " + i);
//                //channel.send(new GenericMessage<String>("hello Fom ob adapter -  " + i));
//                   System.out.println("Added " + i);
               }
//        channel.send(new GenericMessage<List<String>>(payloads));
//        new MessageBuilder().withPayload(payloads).setHeader("messageKey", headerValu)
//        System.out.println("message sent");
    }

}
