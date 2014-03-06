package org.springframework.integration.dsl.support;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

/**
 * @author Artem Bilan
 * @since 4.0
 */
public class FixedSubscriberChannelPrototype implements MessageChannel {

	private final String name;

	public FixedSubscriberChannelPrototype() {
		this(null);
	}

	public FixedSubscriberChannelPrototype(String name) {
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

	@Override
	public String toString() {
		return "FixedSubscriberChannelPrototype{" +
				"name='" + name + '\'' +
				'}';
	}

}
