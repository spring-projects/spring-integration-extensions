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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.jgroups.JChannel;
import org.jgroups.Receiver;
import org.junit.Test;

/**
 * 
 * @author Jaroslaw Palka <jaroslaw.palka@symentis.pl>
 * @since 1.0.0
 *
 */
public class JGroupsInboundEndpointTest {

	@Test
	public void should_set_receiver_on_endpoint_start() {

		JChannel jgroupsChannel = mock(JChannel.class);
		JGroupsInboundEndpoint inboundEndpoint = new JGroupsInboundEndpoint(jgroupsChannel);

		inboundEndpoint.start();

		verify(jgroupsChannel).setReceiver(any(Receiver.class));

	}
}
