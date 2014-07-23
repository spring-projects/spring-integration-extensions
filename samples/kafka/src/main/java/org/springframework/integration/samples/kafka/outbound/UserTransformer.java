package org.springframework.integration.samples.kafka.outbound;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.integration.samples.kafka.user.User;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.transformer.Transformer;
import org.springframework.messaging.Message;

/**
 * @author Soby Chacko
 */
public class UserTransformer implements Transformer {

    Log logger = LogFactory.getLog(getClass());

    @Override
    public Message<?> transform(Message<?> message) {
        if(message.getPayload().getClass().isAssignableFrom(User.class)) {
            User user = (User) message.getPayload();
            user.setFirstName(user.getFirstName().toString()+user.getFirstName());
            logger.info("user confirmed " + user.getFirstName());
            return MessageBuilder.withPayload(user).copyHeaders(message.getHeaders()).build();
        }
        return message;
    }
}
