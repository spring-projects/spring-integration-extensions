/*
 * Copyright 2014 the original author or authors.
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

package org.springframework.integration.dsl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.integration.core.MessageSelector;
import org.springframework.integration.filter.ExpressionEvaluatingSelector;
import org.springframework.integration.router.RecipientListRouter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.core.DestinationResolutionException;
import org.springframework.util.StringUtils;

/**
 * @author Artem Bilan
 */
class DslRecipientListRouter extends RecipientListRouter {

	private final Map<String, String> expressionRecipientMap = new HashMap<String, String>();

	private final Map<String, MessageSelector> selectorRecipientMap = new HashMap<String, MessageSelector>();

	void addRecipient(String channelName, String expression) {
		this.expressionRecipientMap.put(channelName, expression);
	}

	void addRecipient(String channelName, MessageSelector selector) {
		this.selectorRecipientMap.put(channelName, selector);
	}

	Map<String, Object> getRecipients() {
		Map<String, Object> recipients =
				new HashMap<String, Object>(this.expressionRecipientMap.size() + this.selectorRecipientMap.size());
		recipients.putAll(this.expressionRecipientMap);
		recipients.putAll(this.selectorRecipientMap);
		return recipients;
	}

	@Override
	public void onInit() throws Exception {
		for (Map.Entry<String, String> recipient : this.expressionRecipientMap.entrySet()) {
			ExpressionEvaluatingSelector selector = null;
			String expression = recipient.getValue();
			if (StringUtils.hasText(expression)) {
				selector = new ExpressionEvaluatingSelector(expression);
				selector.setBeanFactory(this.getBeanFactory());
			}
			this.selectorRecipientMap.put(recipient.getKey(), selector);
		}

		List<Recipient> recipients = new ArrayList<Recipient>(this.selectorRecipientMap.size());

		for (Map.Entry<String, MessageSelector> entry : selectorRecipientMap.entrySet()) {
			recipients.add(new Recipient(this.resolveChannelName(entry.getKey()), entry.getValue()));
		}

		this.setRecipients(recipients);
		super.onInit();
	}

	private MessageChannel resolveChannelName(String channelName) {
		try {
			return this.getBeanFactory().getBean(channelName, MessageChannel.class);
		}
		catch (BeansException e) {
			throw new DestinationResolutionException("Failed to look up MessageChannel with name '"
					+ channelName + "' in the BeanFactory.");
		}
	}

}
