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

/**
 * The core service that would be performing the push operations to the hand held
 * devices from the java applications
 *
 * @author Amol Nayak
 *
 * @since 1.0
 *
 */
public interface PushNotifyService {

	/**
	 * Push the message to multiple receivers identified by these multiple
	 * receiver ids.
	 *
	 * @param message The map that represents the key value pair of the message to be sent.
	 * @param attributes The map of attributes apart from the message that might be needed to be
	 * 					sent along with the request. These could be the service specific configuration parameters
	 * 					that would be used to change the behavior of the message sent. The values of the attributes
	 * 					if defined by the implementation.
	 * @param receiverId The unique identifiers that identifies the target devices to which the
	 * 					notification is to be sent.
	 * @return
	 */
	PushResponse push(Map<String, Object> message, Map<String, String> attributes, String... receiverIds) throws IOException;
}
