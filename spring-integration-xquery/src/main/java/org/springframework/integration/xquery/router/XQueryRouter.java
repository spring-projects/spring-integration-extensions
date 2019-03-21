/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.integration.xquery.router;

import java.util.ArrayList;
import java.util.List;

import org.springframework.integration.router.AbstractMappingMessageRouter;
import org.springframework.integration.xquery.core.XQueryExecutor;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;

/**
 * The message router that will evaluate the provided XQuery on the xml to determine the channel(s)
 * to which the message will be routed
 *
 * @author Amol Nayak
 * @author Gary Russell
 *
 * @since 1.0
 *
 */
public class XQueryRouter extends AbstractMappingMessageRouter {

	private XQueryExecutor executor;

	private Class<?> resultType;

	@Override
	public void onInit() throws Exception {
		super.onInit();
		Assert.notNull(executor,"No XQueryExecutor instance provided");
		if(resultType == null) {
			resultType = String.class;
		}
	}

	/* (non-Javadoc)
	 * @see org.springframework.integration.router.AbstractMappingMessageRouter#getChannelKeys(org.springframework.integration.Message)
	 */
	@Override
	protected List<Object> getChannelKeys(Message<?> message) {
		List<Object> channelKeys = null;
		//Accept the key types in this class and then execute the appropriate method
		channelKeys = new ArrayList<Object>(executor.execute(message, resultType));
		return channelKeys;
	}



	@Override
	public String getComponentType() {
		return "int-xml:xquery-router";
	}

	/**
	 * Sets the executor to be used for executing the XQueries
	 * @param executor
	 */
	public void setExecutor(XQueryExecutor executor) {
		Assert.notNull(executor,"Provide a non null implementation of the executor");
		this.executor = executor;
	}

	/**
	 * Determines the type of the object that would returned in the {@link List} returned
	 * from getChannelKeys method. If non specified, {@link String} is assumed.
	 * @param resultType
	 */
	public void setResultType(Class<?> resultType) {
		this.resultType = resultType;
	}
}
