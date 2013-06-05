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
package org.springframework.integration.pushnotify.gcm;

import java.util.Map;

import org.springframework.integration.pushnotify.PushResponse;

/**
 * The response for the the GCM request.
 *
 * @author Amol Nayak
 *
 * @since 1.0
 *
 */
public interface GCMPushResponse extends PushResponse {

	/**
	 * The HTTP Response code that was received for the request made
	 *
	 * @return
	 */
	int getResponseCode();

	/**
	 * Gets the GCM error codes that was received if the received HTTP response code is other than 200 (successful) and
	 * 503 (temporarily available). The key is the receiver id and the value is the error code received
	 *
	 * @return
	 */
	Map<String, String> getErrorCodes();

	/**
	 * An identifiers from the server acknowledging the receipt of the message sent.
	 * The Map contains the key which is the receiverId provided and the value is the message
	 * id received from the GCM server.
	 *
	 * @return
	 */
	Map<String, String> getSentMessageIds();

	/**
	 * Gets the canonical id of the device to which the message was sent, this is optional and would
	 * not necessarily be present for all receiverIds. This id indicates that the device to which the message
	 * was sent has a new registration id with the server other than the one sent with the request.
	 * The sender should replace the registration id of the receiver with this canonical id and use it for any subsequent requests sent to this
	 * device.
	 *
	 * @return
	 */
	Map<String, String> getCanonicalIds();


	/**
	 * Gets the number of successful messages as part of the requests
	 *
	 */
	int getSuccessfulMessages();

	/**
	 * Gets the number of failed messages in the request
	 *
	 * @return
	 */
	int getFailedMessages();

	/**
	 *Gets the number of canonical ids present in the response
	 * @return
	 */
	int getNumberOfCanonicalIds();

	/**
	 * A long number giving the multicast id provided by the GCM server for multicast requests
	 * @return
	 */
	long getMulticastId();


}
