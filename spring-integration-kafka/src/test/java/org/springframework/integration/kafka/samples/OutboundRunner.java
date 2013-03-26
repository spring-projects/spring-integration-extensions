package org.springframework.integration.kafka.samples;


import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.message.GenericMessage;

public class OutboundRunner {

    public static void main(String args[]) {
       ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
                       "../config/xml/kafkaOutboundAdapterParserTests-context.xml",
               OutboundRunner.class);


        ctx.start();

        final MessageChannel channel = ctx.getBean("inputToKafka", MessageChannel.class);
                System.out.println(channel.getClass());


               for(int i= 0; i < 1000; i++)  {
                channel.send(new GenericMessage<String>("hello Fom ob adapter -  " + i));
               }
        System.out.println("message sent");
    }

}
