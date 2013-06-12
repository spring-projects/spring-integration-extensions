package org.springframework.integration.reactor.tcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.integration.core.MessageHandler;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.core.SubscribableChannel;
import org.springframework.integration.endpoint.AbstractEndpoint;
import org.springframework.integration.history.MessageHistory;
import org.springframework.integration.history.TrackableComponent;
import org.springframework.integration.message.ErrorMessage;
import org.springframework.integration.support.MessageBuilder;
import reactor.R;
import reactor.core.Environment;
import reactor.core.Reactor;
import reactor.fn.Consumer;
import reactor.fn.Event;
import reactor.fn.registry.Registration;
import reactor.fn.selector.Selector;
import reactor.fn.tuples.Tuple2;
import reactor.io.Buffer;
import reactor.tcp.TcpConnection;
import reactor.tcp.TcpServer;
import reactor.tcp.encoding.Codec;
import reactor.tcp.netty.NettyTcpServer;

import java.net.InetSocketAddress;

import static reactor.fn.Functions.$;
import static reactor.fn.Functions.T;

/**
 * Reactor TcpServer-based Inbound Channel Adapter implementation.
 *
 * @author Jon Brisbin
 */
public class TcpServerInboundChannelAdapter<IN, OUT>
		extends AbstractEndpoint
		implements MessageProducer, SubscribableChannel, TrackableComponent {

	private final Logger                   log      = LoggerFactory.getLogger(getClass());
	private final Tuple2<Selector, Object> incoming = $();
	private final Tuple2<Selector, Object> outgoing = $();
	private final    Environment                      env;
	private final    InetSocketAddress                bindAddress;
	private final    Codec<Buffer, IN, OUT>           codec;
	private final    Reactor                          eventsReactor;
	private final    Consumer<TcpConnection<IN, OUT>> connectionConsumer;
	private volatile MessageChannel                   outputChannel;
	private volatile MessageChannel                   errorChannel;
	private volatile boolean shouldTrack = false;
	private volatile TcpServer<IN, OUT> server;

	public TcpServerInboundChannelAdapter(Environment env,
																				InetSocketAddress bindAddress,
																				String eventsDispatcher,
																				Codec<Buffer, IN, OUT> codec) {
		super();
		this.env = env;
		this.bindAddress = bindAddress;
		this.codec = codec;

		this.eventsReactor = R.reactor().using(env).dispatcher(eventsDispatcher).get();
		this.eventsReactor.on(T(Throwable.class), new Consumer<Event<Throwable>>() {
			@Override
			public void accept(Event<Throwable> ev) {
				if (null != errorChannel) {
					errorChannel.send(new ErrorMessage(ev.getData()));
				} else {
					log.error(ev.getData().getMessage(), ev.getData());
				}
			}
		});

		this.connectionConsumer = new Consumer<TcpConnection<IN, OUT>>() {
			@Override
			public void accept(final TcpConnection<IN, OUT> connection) {
				final MessageChannel replies = new AbstractMessageChannel() {
					@SuppressWarnings("unchecked")
					@Override
					protected boolean doSend(Message<?> message, long timeout) {
						connection.send((OUT) message.getPayload());
						return true;
					}
				};

				eventsReactor.on(outgoing.getT1(), new Consumer<Event<Message<?>>>() {
					@SuppressWarnings("unchecked")
					@Override
					public void accept(Event<Message<?>> ev) {
						connection.send((OUT) ev.getData());
					}
				});

				connection.consume(new Consumer<IN>() {
					@Override
					public void accept(IN in) {
						Message<IN> msg = MessageBuilder.withPayload(in)
																						.setErrorChannel(errorChannel)
																						.setReplyChannel(replies)
																						.build();
						if (shouldTrack) {
							msg = MessageHistory.write(msg, TcpServerInboundChannelAdapter.this);
						}

						outputChannel.send(msg);
					}
				});
			}
		};
	}

	@Override
	public void setOutputChannel(MessageChannel outputChannel) {
		this.outputChannel = outputChannel;
	}

	public void setErrorChannel(MessageChannel errorChannel) {
		this.errorChannel = errorChannel;
	}

	@Override
	public void setShouldTrack(boolean shouldTrack) {
		this.shouldTrack = shouldTrack;
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
		return true;
	}

	@Override
	public boolean unsubscribe(MessageHandler handler) {
		for (Registration<? extends Consumer<? extends Event<?>>> reg : eventsReactor.getConsumerRegistry()) {
			if (reg.getObject() instanceof MessageHandlerConsumer && ((MessageHandlerConsumer) reg.getObject()).handler == handler) {
				reg.cancel();
			}
		}
		return true;
	}

	@Override
	public boolean send(Message<?> message) {
		eventsReactor.notify(outgoing.getT2(), Event.wrap(message));
		return true;
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
