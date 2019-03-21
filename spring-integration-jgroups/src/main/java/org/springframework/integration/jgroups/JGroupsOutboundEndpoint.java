/**
 * Copyright 2013 Jaroslaw Palka<jaroslaw.palka@symentis.pl>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.integration.jgroups;

import org.jgroups.JChannel;
import org.springframework.integration.Message;
import org.springframework.integration.handler.AbstractMessageHandler;

/**
 * Outbound endpoint which sends messages to JGroups cluster/group. At the
 * moment it only supports sending messages to all members.
 *
 * @author Jaroslaw Palka <jaroslaw.palka@symentis.pl>
 * @since 1.0.0
 *
 */
public class JGroupsOutboundEndpoint extends AbstractMessageHandler {

	private final JChannel jgroupsChannel;
	private final JGroupsHeaderMapper headerMapper;

	public JGroupsOutboundEndpoint(JChannel jgroupsChannel, JGroupsHeaderMapper headerMapper) {
		super();
		this.jgroupsChannel = jgroupsChannel;
		this.headerMapper = headerMapper;
	}

	public JGroupsOutboundEndpoint(JChannel jgroupsChannel) {
		this(jgroupsChannel, new DefaultJGroupsHeaderMapper());
	}

	@Override
	protected void handleMessageInternal(Message<?> message) throws Exception {
		org.jgroups.Message target = new org.jgroups.Message();
		headerMapper.fromHeaders(message.getHeaders(), target);

		target.setObject(message.getPayload());

		jgroupsChannel.send(target);
	}

	public JChannel getJChannel() {
		return jgroupsChannel;
	}
}
