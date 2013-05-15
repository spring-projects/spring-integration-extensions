/**
 * Copyright 2013 Jaroslaw Palka<jaroslaw.palka@symentis.pl>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.integration.jgroups;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.integration.support.MessageBuilder;

public class JGroupsOutboundEndpointTest {

	@Test
	public void should_sent_message_over_jgroups_channel() throws Exception {
		JChannel jgroupsChannel = Mockito.mock(JChannel.class);
		JGroupsHeaderMapper headerMapper = Mockito.mock(JGroupsHeaderMapper.class);
		
		
		JGroupsOutboundEndpoint outboundEndpoint = new JGroupsOutboundEndpoint(jgroupsChannel, headerMapper);
	
		outboundEndpoint.handleMessage(MessageBuilder.withPayload("message").build());
		
		ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
		
		verify(jgroupsChannel).send(captor.capture());
		
		assertThat(captor.getValue().getObject()).isEqualTo("message");
		
	}

}
