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
package org.springframework.integration.pushnotify;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

/**
 * The Common superclass implementing the common validations and other functionalities
 * for the {@link PushNotifyService} implementations
 *
 * @author Amol Nayak
 *
 * @since 1.0
 *
 */
public abstract class AbstractPushNotifyService implements PushNotifyService {

	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * Flag to enable the service to retry the requests that were unsuccessful either due to
	 * network issues or some recoverable error from the server. If set to true, exponential
	 * backoff and retry is used. Enabled by default.
	 */
	private volatile boolean retryRequests = true;

	private static final long INITIAL_RETRY_DELAY = 1000L;

	private volatile long maxRetryDelay = 10000L; //Defaults to 10 seconds.


	/**
	 * The implemented version does some basic validation and delegates to
	 * the {@link #doPush(Map, String...)
	 *
	 * @param message
	 * @param attributes
	 * @param receiverId
	 * @return
	 */
	@Override
	public final PushResponse push(Map<String, Object> message, Map<String, String> attributes, String... receiverIds)
		throws IOException {
		Assert.notNull(message, "provided message is 'null'");
		Assert.isTrue(message.size() > 0,"provided message is an empty map");
		Assert.notNull(receiverIds,"provided receiver id is 'null'");
		Assert.isTrue(receiverIds.length > 0, "Must provide at least on receiver id");
		boolean retryRequest = retryRequests;
		PushResponse response;
		int base = 1;
		int retryAttempt = 1;	//for logging
		//logic to exponentially retry the request
		long delta = (long)(Math.random() * 1000);
		while(true) {
			response =  doPush(message, attributes, receiverIds);
			if(response == null && retryRequest) {
				  long delay = INITIAL_RETRY_DELAY * (2 * base - 1)/2;
				  long totalDelay = delay + delta;
				  if(totalDelay <= maxRetryDelay) {
					  String logMessage = String.format("Retry attempt %d, retrying after %d ms", retryAttempt++, totalDelay);
					  logger.info(logMessage);
					  base *= 2;
					  try {
						  Thread.sleep(totalDelay);
					  } catch (InterruptedException e) {
						  Thread.currentThread().interrupt();
					  }
				  }
				  else {
					  break;
				  }
			}
			else {
				break;
			}
		}
		return response;
	}

	/**
	 * The sub classes need to implement the method that will push the given messages to the provided
	 * receiver ids
	 *
	 * @param message
	 * @param attributes
	 * @param receiverIds
	 * @return
	 */
	protected abstract PushResponse doPush(Map<String, Object> message, Map<String, String> attributes, String... receiverIds) throws IOException;

	/**
	 * Checks if requests are to be retried using exponential backoff or not.
	 * @return
	 */
	public boolean shouldRetryRequests() {
		return retryRequests;
	}

	/**
	 * Set to true if requests are to be retried using exponential backoff.
	 * @param retryRequests
	 */
	public void setRetryRequests(boolean retryRequests) {
		this.retryRequests = retryRequests;
	}

	/**
	 * Gets the max interval for which the request thread would sleep to retry a request
	 * to the service
	 *
	 */
	public long getMaxRetryDelay() {
		return maxRetryDelay;
	}

	/**
	 * Sets the max interval in milli seconds for which the thread will sleep before retrying
	 * the request again with the push services
	 *
	 * @param maxRetryInterval
	 */
	public void setMaxRetryDelay(long maxRetryDelay) {
		this.maxRetryDelay = maxRetryDelay;
	}
}
