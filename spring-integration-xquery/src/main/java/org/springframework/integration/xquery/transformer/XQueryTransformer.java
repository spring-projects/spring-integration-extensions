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
package org.springframework.integration.xquery.transformer;

import java.util.List;

import org.springframework.integration.Message;
import org.springframework.integration.transformer.AbstractTransformer;
import org.springframework.integration.xquery.core.XQueryExecutor;
import org.springframework.integration.xquery.support.XQueryResultMapper;
import org.springframework.util.Assert;
import org.w3c.dom.Node;


/**
 * Transform the incoming message payload to the type specified in the {@link #resultType}
 * attribute. Currently {@link #resultType} supports only the following values out of the box
 *
 * 1. {@link String}
 * 2. {@link Boolean}
 * 3. {@link Number}
 * 4. {@link Node}
 *
 * For any other custom conversion, provide an instance of {@link XQueryResultMapper} in the
 * {@link #resultMapper} attribute. The {@link #resultMapper} and {@link #resultType} attribute
 * values are mutually exclusive to each other. If none of the above two attributes are
 * provided then the value of the {@link #resultType} defaults to {@link String}.
 *
 * @author Amol Nayak
 *
 * @since 2.2
 *
 */
@SuppressWarnings("rawtypes")
public class XQueryTransformer extends AbstractTransformer {

	private XQueryExecutor executor;

	private Class<?> resultType;

	private XQueryResultMapper resultMapper;

	@Override
	public void onInit() {
		Assert.notNull(executor,"No XQueryExecutor instance provided");
		if(resultMapper == null && resultType == null) {
			resultType = String.class;
		}
	}

	/* (non-Javadoc)
	 * @see org.springframework.integration.transformer.AbstractTransformer#doTransform(org.springframework.integration.Message)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected Object doTransform(Message<?> message) throws Exception {
		Object transformed;
		List<Object> queryResult;
		if(resultType != null) {
			queryResult = (List<Object>) executor.execute(message, resultType);
		}
		else {
			queryResult = executor.execute(message, resultMapper);
		}

		if(queryResult != null && queryResult.size() == 1) {
			transformed = queryResult.get(0);
		}
		else {
			transformed = queryResult;
		}

		return transformed;
	}

	/**
	 * Sets the XQuery executor instance to be used by the {@link XQueryTransformer}
	 * @param executor
	 */
	public void setExecutor(XQueryExecutor executor) {
		Assert.notNull(executor,"Provide a non null XQueryExecutor instance");
		this.executor = executor;
	}

	/**
	 * Sets the Type of the result, if none specified and if a {@link XQueryResultMapper} instance is
	 * not provided, String is assumed by default.
	 * The permitted value of the class is one of the
	 * String.class, Boolean.class, Number.class or Node.class. For any other type of class
	 * Provide an instance of {@link XQueryResultMapper}
	 *
	 * @param resultType
	 */
	public void setResultType(Class<?> resultType) {
		Assert.notNull(resultType,"Provide a non null value for the result type");
		Assert.isTrue(resultMapper == null,"Only one of the result mapper of the resultType can be set");
		Assert.isTrue(String.class == resultType || Boolean.class == resultType
						|| Number.class == resultType || Node.class == resultType,
					"Valid values for the result type class is String, Boolean, Number or Node, " +
					"for any other type, provide a custom implementation of XQueryResultMapper");
		this.resultType = resultType;
	}

	/**
	 * Sets the ResultMapper to be used for mapping the result of the XQuery Object to
	 * an appropriate object.
	 * @param resultMapper
	 */
	public void setResultMapper(XQueryResultMapper resultMapper) {
		Assert.notNull(resultMapper,"Provide a non null value for the result mapper");
		Assert.isTrue(resultType == null,"Only one of the result mapper of the resultType can be set");
		this.resultMapper = resultMapper;
	}
}
