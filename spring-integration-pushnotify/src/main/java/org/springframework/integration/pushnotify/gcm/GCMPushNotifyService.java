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

import org.springframework.integration.pushnotify.PushNotifyService;

/**
 * The interface containing the methods specific to Google Cloud Messaging (GCM)
 * push messaging to android devices.
 *
 * @author Amol Nayak
 *
 * @since 1.0
 *
 */
public interface GCMPushNotifyService extends PushNotifyService {

	/**
	 * The parameter used to carry the registration id of the device to which the notification
	 * is to be sent
	 */
	public static final String REGISTRATION_ID = "registration_id";

	/**
	 * The parameter used to carry the registration ids of the device to which the notification
	 * is to be sent
	 */
	public static final String REGISTRATION_IDS = "registration_ids";

	/**
	 * The collapse key parameter of the request
	 */
	public static final String COLLAPSE_KEY = "collapse_key";

	/**
	 * The prefix to the parameter for sending the data in case the request
	 * is sent out as plain text
	 */
	public static final String DATA_KEY_PREFIX = "data.";

	/**
	 * The key used in the JSON request to send the data to the GCM service
	 */
	public static final String DATA_KEY = "data";

	/**
	 * The key used to specify if the messages should be held and not pushed if
	 * the device is idle.
	 */
	public static final String DELAY_WHILE_IDLE = "delay_while_idle";

	/**
	 * The parameter that gives the time the message will live and will not be discarded
	 * before it is delivered to the device.
	 */
	public static final String TIME_TO_LIVE = "time_to_live";

	/**
	 * The key in the JSON response that indicates that an error has occurred while sending the message
	 */
	public static final String ERROR = "error";

	/**
	 * The key in the JSON response giving the message id of the successfully sent message
	 */
	public static final String MESSAGE_ID = "message_id";

	/**
	 *The key in the JSON response giving the canonnical id of the device to which the message was posted
	 *may be present only if the message_id attribute is present
	 */
	public static final String CANONICAL_MESSAGES = "canonical_ids";

	/**
	 * The key giving the number of failed messages in the request
	 */
	public static final String FAILURE_MESSAGES = "failure";

	/**
	 * The key giving the number of successful messages in the request
	 */
	public static final String SUCCESS_MESSAGES = "success";

	/**
	 * The key giving a unique id to the multi cast request, this is returned by the GCM server
	 */
	public static final String MULTICAST_ID = "multicast_id";


	/**
	 * The key present in the pain text response indicating the request is successful, the value
	 * of this key is the message id
	 */
	public static final String RESPONSE_TOKEN_ID = "id";


	/**
	 * The content type if the request is of type JSON.
	 */
	public static final String JSON_TYPE = "application/json";

	/**
	 * The content type if the request is of type plain text.
	 */
	public static final String PLAIN_TEXT_TYPE = "application/x-www-form-urlencoded;charset=UTF-8";

	/**
	 * The Web service for the GCM services
	 */
	public static final String GCM_SERVICE_URL = "https://android.googleapis.com/gcm/send";
}
