package org.springframework.integration.kafka.config.xml;


import org.springframework.context.support.ClassPathXmlApplicationContext;

public class InboundRunner {

    public static void main(String args[]) {
       ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
                       "kafkaInboundAdapterParserTests-context.xml",
               InboundRunner.class);
       ctx.start();
    }

}
