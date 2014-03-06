package org.springframework.integration.dsl.support;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.util.Assert;

/**
 * @author Artem Bilan
 * @since 4.0
 */
public class MessageChannelReference implements MessageChannel {

	private final String name;

	public MessageChannelReference(String name) {
		Assert.notNull(name);
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public boolean send(Message<?> message) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean send(Message<?> message, long timeout) {
		throw new UnsupportedOperationException();
	}

}
