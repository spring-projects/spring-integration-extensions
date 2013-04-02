/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.integration.voldemort.outbound;

import org.springframework.expression.Expression;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.integration.Message;
import org.springframework.integration.expression.ExpressionUtils;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.integration.voldemort.support.PersistMode;
import org.springframework.integration.voldemort.support.VoldemortHeaders;
import voldemort.client.StoreClient;

/**
 * Voldemort outbound adapter implementation.
 *
 * @author Lukasz Antoniak
 * @since 1.0
 */
public class VoldemortStoringMessageHandler extends AbstractMessageHandler {
	private final StoreClient client;

	private volatile StandardEvaluationContext evaluationContext;
	private volatile Expression keyExpression = new SpelExpressionParser().parseExpression( "headers." + VoldemortHeaders.KEY );

	private volatile PersistMode persistMode = PersistMode.PUT;

	/**
	 * Creates new message sender.
	 *
	 * @param client Voldemort store client.
	 */
	public VoldemortStoringMessageHandler(StoreClient client) {
		this.client = client;
	}

	@Override
	protected void onInit() throws Exception {
		super.onInit();
		if ( getBeanFactory() != null ) {
			evaluationContext = ExpressionUtils.createStandardEvaluationContext( getBeanFactory() );
		}
		else {
			evaluationContext = ExpressionUtils.createStandardEvaluationContext();
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void handleMessageInternal(Message<?> message) throws Exception {
		final Object key = keyExpression.getValue( evaluationContext, message, Object.class );
		switch ( determinePersistMode( message ) ) {
			case PUT:
				client.put( key, message.getPayload() );
				break;
			case DELETE:
				client.delete( key );
				break;
		}
	}

	/**
	 * Computes desired persist mode for a given message. Default output adapter's configuration
	 * can be overridden with {@link VoldemortHeaders#PERSIST_MODE} message header which supports
	 * direct or text representation of {@link PersistMode} enumeration.
	 *
	 * @param message Spring Integration message.
	 * @return Persist mode.
	 */
	private PersistMode determinePersistMode(Message<?> message) {
		final Object confValue = message.getHeaders().get( VoldemortHeaders.PERSIST_MODE );
		if ( confValue instanceof PersistMode ) {
			return (PersistMode) confValue;
		}
		else if ( confValue instanceof String ) {
			return PersistMode.valueOf( (String) confValue );
		}
		return persistMode;
	}

	@Override
	public String getComponentType() {
		return "voldemort:outbound-channel-adapter";
	}

	public void setKey(String key) {
		setKeyExpression( new LiteralExpression( key ) );
	}

	public void setKeyExpression(Expression keyExpression) {
		this.keyExpression = keyExpression;
	}

	public void setPersistMode(PersistMode persistMode) {
		this.persistMode = persistMode;
	}
}
