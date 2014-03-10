package org.springframework.integration.dsl.support;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.integration.handler.MessageProcessor;
import org.springframework.integration.handler.MethodInvokingMessageProcessor;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;

/**
 * @author Artem Bilan
 */
public class BeanNameMessageProcessor<T> implements MessageProcessor<T>, BeanFactoryAware {

	private final String object;

	private final String methodName;

	private MessageProcessor<T> delegate;

	public BeanNameMessageProcessor(String object, String methodName) {
		this.object = object;
		this.methodName = methodName;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		Assert.notNull(beanFactory);
		Object target = beanFactory.getBean(object);
		this.delegate = new MethodInvokingMessageProcessor<T>(target, this.methodName);
	}

	@Override
	public T processMessage(Message<?> message) {
		return this.delegate.processMessage(message);
	}

}
