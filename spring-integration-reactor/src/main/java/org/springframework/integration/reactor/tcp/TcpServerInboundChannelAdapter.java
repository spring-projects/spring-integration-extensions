package org.springframework.integration.reactor.tcp;

import org.springframework.integration.Message;
import org.springframework.integration.MessageHeaders;
import org.springframework.integration.core.MessageHandler;
import org.springframework.integration.core.SubscribableChannel;
import org.springframework.integration.endpoint.AbstractEndpoint;
import org.springframework.integration.message.GenericMessage;
import org.springframework.util.ReflectionUtils;
import reactor.R;
import reactor.core.Environment;
import reactor.core.Reactor;
import reactor.fn.Consumer;
import reactor.fn.Event;
import reactor.fn.registry.Registration;
import reactor.fn.selector.Selector;
import reactor.fn.tuples.Tuple2;
import reactor.io.Buffer;
import reactor.spring.integration.support.Type1UUIDMessageIdGenerator;
import reactor.tcp.TcpConnection;
import reactor.tcp.TcpServer;
import reactor.tcp.encoding.Codec;
import reactor.tcp.netty.NettyTcpServer;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;

import static reactor.fn.Functions.$;

/**
 * @author Jon Brisbin
 */
public class TcpServerInboundChannelAdapter<IN, OUT> extends AbstractEndpoint implements SubscribableChannel {

	private static Field ID_GEN;

	static {
		try {
			ID_GEN = MessageHeaders.class.getDeclaredField("idGenerator");
			ReflectionUtils.makeAccessible(ID_GEN);
			ID_GEN.set(null, new Type1UUIDMessageIdGenerator());
		} catch (NoSuchFieldException e) {
			throw new IllegalStateException(e);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	private final Tuple2<Selector, Object> incoming = $();
	private final Tuple2<Selector, Object> outgoing = $();
	private final    Environment                      env;
	private final    InetSocketAddress                bindAddress;
	private final    Codec<Buffer, IN, OUT>           codec;
	private final    Reactor                          eventsReactor;
	private final    Consumer<TcpConnection<IN, OUT>> connectionConsumer;
	private volatile TcpServer<IN, OUT>               server;

	public TcpServerInboundChannelAdapter(Environment env,
																				InetSocketAddress bindAddress,
																				String eventsDispatcher,
																				Codec<Buffer, IN, OUT> codec) {
		super();
		this.env = env;
		this.bindAddress = bindAddress;
		this.codec = codec;

		this.eventsReactor = R.reactor().using(env).dispatcher(eventsDispatcher).get();
		this.connectionConsumer = new Consumer<TcpConnection<IN, OUT>>() {
			@Override
			public void accept(final TcpConnection<IN, OUT> connection) {
				connection.consume(new Consumer<IN>() {
					@Override
					public void accept(IN in) {
						eventsReactor.notify(incoming.getT2(), Event.wrap(new GenericMessage<IN>(in)));
					}
				});

				eventsReactor.on(outgoing.getT1(), new Consumer<Event<Message<?>>>() {
					@SuppressWarnings("unchecked")
					@Override
					public void accept(Event<Message<?>> ev) {
						connection.send((OUT) ev.getData());
					}
				});
			}
		};
	}

	public Reactor getEventsReactor() {
		return eventsReactor;
	}

	@Override
	protected void onInit() throws Exception {
		this.server = new TcpServer.Spec<IN, OUT>(NettyTcpServer.class)
				.using(env)
				.using(eventsReactor)
				.listen(bindAddress.getHostString(), bindAddress.getPort())
				.codec(codec)
				.consume(connectionConsumer)
				.get();
		super.onInit();
	}

	@Override
	protected void doStart() {
		server.start();
	}

	@Override
	protected void doStop() {
		server.shutdown();
	}

	@Override
	public boolean subscribe(MessageHandler handler) {
		eventsReactor.on(incoming.getT1(), new MessageHandlerConsumer(handler));
		return false;
	}

	@Override
	public boolean unsubscribe(MessageHandler handler) {
		for (Registration<? extends Consumer<? extends Event<?>>> reg : eventsReactor.getConsumerRegistry()) {
			if (reg.getObject() instanceof MessageHandlerConsumer && ((MessageHandlerConsumer) reg.getObject()).handler == handler) {
				reg.cancel();
			}
		}
		return false;
	}

	@Override
	public boolean send(Message<?> message) {
		eventsReactor.notify(outgoing.getT2(), Event.wrap(message));
		return false;
	}

	@Override
	public boolean send(Message<?> message, long timeout) {
		return send(message);
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
