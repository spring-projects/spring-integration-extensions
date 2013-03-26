package org.springframework.integration.kafka.samples;


import org.springframework.context.support.ClassPathXmlApplicationContext;

public class InboundRunner {

    public static void main(String args[]) {
       ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
                       "../config/xml/kafkaInboundAdapterParserTests-context.xml",
               InboundRunner.class);
       ctx.start();
    }

}
