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

import java.util.Map;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.integration.support.MessageBuilder;

/**
 * InboundEndpoint which on start bounds itself, as receiver, to connected
 * JGroups channel.
 *
 * @author Jaroslaw Palka <jaroslaw.palka@symentis.pl>
 * @since 1.0.0
 *
 */
public class JGroupsInboundEndpoint extends MessageProducerSupport {

	private final JChannel jgroupsChannel;
	private final JGroupsHeaderMapper headerMapper;

	public JGroupsInboundEndpoint(JChannel jgroupsChannel, JGroupsHeaderMapper headerMapper) {
		super();
		this.jgroupsChannel = jgroupsChannel;
		this.headerMapper = headerMapper;
	}

	public JGroupsInboundEndpoint(JChannel jgroupsChannel) {
		this(jgroupsChannel, new DefaultJGroupsHeaderMapper());
	}

	@Override
	protected void doStart() {

		jgroupsChannel.setReceiver(new ReceiverAdapter() {

			@Override
			public void receive(Message msg) {
				Object object = msg.getObject();

				Map<String, Object> headers = headerMapper.toHeaders(msg);

				sendMessage(MessageBuilder.withPayload(object).copyHeaders(headers).build());
			}

		});

	}

	public JChannel getJChannel() {
		return jgroupsChannel;
	}

	public JGroupsHeaderMapper getHeaderMapper() {
		return headerMapper;
	}
}
