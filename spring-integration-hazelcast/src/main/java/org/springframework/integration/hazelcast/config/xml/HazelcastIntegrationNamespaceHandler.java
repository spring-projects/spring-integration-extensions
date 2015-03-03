/*
 * Copyright 2015 the original author or authors.
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
package org.springframework.integration.hazelcast.config.xml;

import org.springframework.integration.config.xml.AbstractIntegrationNamespaceHandler;

/**
 * SI - Hazelcast Integration base NamespaceHandler class.
 * 
 * @author Eren Avsarogullari
 * @since 1.0.0
 *
 */
public class HazelcastIntegrationNamespaceHandler extends AbstractIntegrationNamespaceHandler {
	
	private static final String INBOUND_CHANNEL_ADAPTER = "inbound-channel-adapter";
	private static final String OUTBOUND_CHANNEL_ADAPTER = "outbound-channel-adapter";
	private static final String CQ_INBOUND_CHANNEL_ADAPTER = "cq-inbound-channel-adapter";
	private static final String DS_INBOUND_CHANNEL_ADAPTER = "ds-inbound-channel-adapter";
	
	public void init() {
		registerBeanDefinitionParser(INBOUND_CHANNEL_ADAPTER, new HazelcastEventDrivenInboundChannelAdapterParser());
		registerBeanDefinitionParser(OUTBOUND_CHANNEL_ADAPTER, new HazelcastOutboundChannelAdapterParser());
		registerBeanDefinitionParser(CQ_INBOUND_CHANNEL_ADAPTER, new HazelcastContinuousQueryInboundChannelAdapterParser());
		registerBeanDefinitionParser(DS_INBOUND_CHANNEL_ADAPTER, new HazelcastDistributedSQLInboundChannelAdapterParser());
	}
	
}