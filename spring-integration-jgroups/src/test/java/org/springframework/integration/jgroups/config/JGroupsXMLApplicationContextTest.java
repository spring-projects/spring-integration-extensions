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
package org.springframework.integration.jgroups.config;

import static org.fest.assertions.Assertions.assertThat;

import org.jgroups.JChannel;
import org.junit.Test;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.integration.endpoint.PollingConsumer;
import org.springframework.integration.jgroups.JGroupsHeaderMapper;
import org.springframework.integration.jgroups.JGroupsInboundEndpoint;

/**
 *
 * @author Jaroslaw Palka <jaroslaw.palka@symentis.pl>
 * @since 1.0.0
 *
 */
public class JGroupsXMLApplicationContextTest {

	@Test
	public void should_create_jgroups_channel_from_xml_application_context() {

		FileSystemXmlApplicationContext context = new FileSystemXmlApplicationContext("src/test/resources/jgroups-jchannel.xml");

		JChannel jChannel = context.getBean(JChannel.class);

		assertThat(jChannel.getClusterName()).isEqualTo("cluster");

	}

	@Test
	public void should_create_jgroups_inbound_channel_adapter() {
		FileSystemXmlApplicationContext context = new FileSystemXmlApplicationContext("src/test/resources/inbound-channel-adapter.xml");

		JChannel cluster = context.getBean("cluster", JChannel.class);

		JGroupsInboundEndpoint clusterAdapter = context.getBean("cluster-adapter", JGroupsInboundEndpoint.class);

		assertThat(cluster).isSameAs(clusterAdapter.getJChannel());

	}

	@Test
	public void should_create_jgroups_inbound_channel_adapter_with_custom_header_mapper() {
		FileSystemXmlApplicationContext context = new FileSystemXmlApplicationContext(
				"src/test/resources/custom-inbound-channel-adapter-header-mapper.xml");

		JGroupsHeaderMapper headerMapper = context.getBean("custom-header-mapper", JGroupsHeaderMapper.class);

		JGroupsInboundEndpoint clusterAdapter = context.getBean("cluster-adapter", JGroupsInboundEndpoint.class);

		assertThat(clusterAdapter.getHeaderMapper()).isSameAs(headerMapper);

	}

	@Test
	public void should_create_jgroups_outbound_channel_adapter() throws Exception {
		FileSystemXmlApplicationContext context = new FileSystemXmlApplicationContext("src/test/resources/outbound-channel-adapter.xml");

		PollingConsumer clusterAdapter = context.getBean("cluster-adapter", PollingConsumer.class);

		assertThat(clusterAdapter).isNotNull();

	}
}
