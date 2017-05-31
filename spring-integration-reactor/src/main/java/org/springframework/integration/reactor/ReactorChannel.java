package org.springframework.integration.reactor;

import org.springframework.integration.Message;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.integration.core.MessageHandler;
import org.springframework.integration.core.SubscribableChannel;
import org.springframework.util.Assert;
import reactor.R;
import reactor.core.Environment;
import reactor.core.Reactor;
import reactor.fn.Consumer;
import reactor.fn.Event;
import reactor.fn.dispatch.SynchronousDispatcher;
import reactor.fn.registry.Registration;

/**
 * Simple {@link Reactor}-based {@link SubscribableChannel} implementation.
 *
 * @author Jon Brisbin
 */
public class ReactorChannel extends AbstractMessageChannel implements SubscribableChannel {

	private final Reactor reactor;

	public ReactorChannel(Environment env, Reactor reactor, String dispatcher) {
		Assert.notNull(env, "Reactor Environment cannot be null.");
		if (null == reactor) {
			Reactor.Spec rspec = R.reactor().using(env);
			if (null != dispatcher) {
				if ("sync".equals(dispatcher)) {
					rspec.using(SynchronousDispatcher.INSTANCE);
				} else {
					rspec.dispatcher(dispatcher);
				}
			}
			this.reactor = rspec.get();
		} else {
			this.reactor = reactor;
		}
	}

	@Override
	protected boolean doSend(Message<?> message, long timeout) {
		reactor.notify(Event.wrap(message));
		return true;
	}

	@Override
	public boolean subscribe(MessageHandler handler) {
		reactor.on(new MessageHandlerConsumer(handler));
		return true;
	}

	@Override
	public boolean unsubscribe(MessageHandler handler) {
		for (Registration<? extends Consumer<? extends Event<?>>> reg : reactor.getConsumerRegistry()) {
			if (reg.getObject() instanceof MessageHandlerConsumer && ((MessageHandlerConsumer) reg.getObject()).handler == handler) {
				reg.cancel();
			}
		}
		return true;
	}

	private static class MessageHandlerConsumer implements Consumer<Event<Message<?>>> {
		private final MessageHandler handler;

		private MessageHandlerConsumer(MessageHandler handler) {
			this.handler = handler;
		}

		@Override
		public void accept(Event<Message<?>> ev) {
			handler.handleMessage(ev.getData());
		}
	}

}
