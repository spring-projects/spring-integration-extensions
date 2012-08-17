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
package org.springframework.integration.xquery.support;

import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.integration.Message;
import org.springframework.integration.handler.ExpressionEvaluatingMessageProcessor;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * The class that represents the XQuery parameter provided. It is also responsible
 * for the evaluation and returning the corresponding value against the given {@link Message}
 * instance
 *
 * @author Amol Nayak
 * @since 2.2
 *
 */
public class XQueryParameter {

	private final String parameterName;
	private volatile Object parameterValue;
	private volatile String expression;
	private volatile boolean isValueSet;
	//Will be used only when an expression is set
	private volatile ExpressionEvaluatingMessageProcessor<Object> messageProcessor;

	/**
	 * The default no argument constructor
	 */
	public XQueryParameter(String parameterName) {
		Assert.hasText(parameterName, "Non null non empty String value expected for parameter name");
		this.parameterName = parameterName;
	}
	/**
	 * The constructor that takes the parameter name and a static parameter value
	 *
	 * @param parameterName
	 * @param parameterValue
	 */
	public XQueryParameter(String parameterName, Object parameterValue) {
		this(parameterName);
		Assert.notNull(parameterValue, "Parameter value provided is null");
		this.parameterValue = parameterValue;
		isValueSet = true;
	}

	/**
	 * The constructor that takes the parameter name and the expression that would
	 * be evaluated to get the value of the parameter
	 * @param parameterName
	 * @param expression
	 */
	public XQueryParameter(String parameterName, String expression) {
		this(parameterName);
		Assert.isTrue(StringUtils.hasText(expression), "Non null non empty String value expected for expression");
		setExpression(expression);
	}


	/**
	 * Gets the parameter name of this instance
	 * @return
	 */
	public String getParameterName() {
		return parameterName;
	}


	/**
	 * Sets the parameter value, this is mutually exclusive with the expression
	 * @param parameterValue
	 */
	public void setParameterValue(Object parameterValue) {
		Assert.isTrue(!StringUtils.hasText(expression), "The parameter value and expression are mutually " +
				"exclusive, parameter expression already set");
		Assert.notNull(parameterValue, "Null parameter value provided");
		this.parameterValue = parameterValue;
		isValueSet = true;
	}


	/**
	 * Sets the expression that would be evaluated to get the parameter value
	 * @param expression
	 */
	public void setExpression(String expression) {
		Assert.isTrue(parameterValue == null, "The parameter value and expression are mutually exclusive" +
				", parameter value already set");
		Assert.isTrue(!StringUtils.hasText(this.expression), "Expression string is already set once, cannot reset it");
		Assert.hasText(expression, "Please provide a non null, non empty expression string");
		this.expression = expression;
		isValueSet = false;
		SpelExpressionParser parser = new SpelExpressionParser();
		Expression expr = parser.parseExpression(expression);
		messageProcessor = new ExpressionEvaluatingMessageProcessor<Object>(expr);
	}

	/**
	 * Evaluates the given message against the provided expression if one is set
	 * else, returns the static value provided. If none of the static value or
	 * expression are provided. The value returned is null.
	 * @param message
	 * @return
	 */
	public Object evaluate(Message<?> message) {
		if(isValueSet) {
			return parameterValue;
		}
		else {
			if(messageProcessor != null) {
				return messageProcessor.processMessage(message);
			}
			else {
				return null;
			}
		}
	}
}
