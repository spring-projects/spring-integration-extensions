package org.springframework.integration.dsl.support;

import org.springframework.integration.handler.AbstractReplyProducingMessageHandler;
import org.springframework.integration.handler.MessageProcessor;
import org.springframework.integration.handler.MethodInvokingMessageProcessor;
import org.springframework.messaging.Message;

/**
 * @author Artem Bilan
 */
public final class BeanNameMethodInvokingMessageHandler extends AbstractReplyProducingMessageHandler {

	private final String object;

	private final String methodName;

	private MessageProcessor<Object> processor;

	public BeanNameMethodInvokingMessageHandler(String object, String methodName) {
		this.object = object;
		this.methodName = methodName;
	}

	@Override
	protected void doInit() {
		Object target = this.getBeanFactory().getBean(object);
		this.processor = new MethodInvokingMessageProcessor<Object>(target, this.methodName);
	}

	@Override
	protected Object handleRequestMessage(Message<?> requestMessage) {
		return this.processor.processMessage(requestMessage);
	}

}
