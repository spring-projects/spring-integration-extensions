/*
 * Copyright 2002-2012 the original author or authors.
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
package org.springframework.integration.voldemort.inbound;

import org.springframework.expression.Expression;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.integration.Message;
import org.springframework.integration.context.IntegrationObjectSupport;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.expression.ExpressionUtils;
import org.springframework.integration.voldemort.convert.VoldemortConverter;
import voldemort.client.StoreClient;
import voldemort.versioning.Versioned;

/**
 * Voldemort polling inbound adapter implementation. Regularly tries to retrieve object with a given key.
 *
 * @author Lukasz Antoniak
 * @since 1.0
 */
public class VoldemortMessageSource extends IntegrationObjectSupport implements MessageSource<Object> {
	private final StoreClient client;
	private final VoldemortConverter converter;

	/**
	 * Key expression which will be evaluated on every call to the {@link #receive()} method.
	 */
	private volatile Expression keyExpression;
	private volatile StandardEvaluationContext evaluationContext;
	private volatile boolean deleteAfterPoll = false;

	/**
	 * Creates new message source.
	 *
	 * @param client Voldemort store client.
	 * @param converter Message converter.
	 */
	public VoldemortMessageSource(StoreClient client, VoldemortConverter converter) {
		this.client = client;
		this.converter = converter;
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
	public Message<Object> receive() {
		final Object key = keyExpression.getValue( evaluationContext, Object.class );
		final Versioned value = client.get( key );
		if ( value != null ) {
			if ( deleteAfterPoll ) {
				client.delete( key );
			}
			return converter.toMessage( key, value );
		}
		return null;
	}

	@Override
	public String getComponentType() {
		return "voldemort:inbound-channel-adapter";
	}

	public void setDeleteAfterPoll(boolean deleteAfterPoll) {
		this.deleteAfterPoll = deleteAfterPoll;
	}

	public Expression getKeyExpression() {
		return keyExpression;
	}

	public void setKeyExpression(Expression keyExpression) {
		this.keyExpression = keyExpression;
	}
}
