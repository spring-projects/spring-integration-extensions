package org.springframework.integration.samples.kafka.outbound;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.support.MessageBuilder;

public class SimpleProducer {

	private static final String CONFIG = "SimpleProducer-context.xml";
    private static final Log LOG = LogFactory.getLog(SimpleProducer.class);

    public static void main(final String args[]) {
        final ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(CONFIG, OutboundRunner.class);
        ctx.start();

        final MessageChannel channel = ctx.getBean("inputToKafka", MessageChannel.class);
        LOG.info(channel.getClass());

        //sending messages to Kafka server for topic test1
        for (int i = 0; i < 5; i++) {
            channel.send(
                    MessageBuilder.withPayload("Hello from simple producer to topic:test1 -  " + i)
                            .setHeader("messageKey", String.valueOf(i))
                            .setHeader("topic", "test1").build());

            LOG.info("message sent to topic test1 " + i);
        }

    }
}
