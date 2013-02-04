/* Copyright 2002-2013 the original author or authors.
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
package org.springframework.integration.smpp.session;

import org.jsmpp.bean.BindType;
import org.jsmpp.session.ClientSession;
import org.jsmpp.session.MessageReceiverListener;

/**
 * Represents an {@link org.jsmpp.session.SMPPSession} that has a few extra capabilities:
 * <p/>
 * <ol><li>supports registration of multiple {@link org.jsmpp.session.MessageReceiverListener}s</li></ol>
 *
 * @author Josh Long
 * @since 1.0
 */
public interface ExtendedSmppSession extends ClientSession {

	/**
	 * a {@link MessageReceiverListener} implementation to be added to the set of existing listeners.
	 * <p/>
	 * NB: the contract for each of these is the same as for a single instance: don't take too long when doing your processing. This is even more
	 * important now that multiple implementations need to share the same callback slice time.
	 *
	 * @param messageReceiverListener the message receiver listener
	 */
	void addMessageReceiverListener(MessageReceiverListener messageReceiverListener);

	/**
	 * We need to know this to determine whether or not this session can handle the requirements we need.
	 *
	 * @return the {@link BindType}
	 */
	BindType getBindType();

	void start() ;

	void stop() ;

}
