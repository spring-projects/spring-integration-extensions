package org.springframework.integration.reactor.config.xml;

import org.springframework.integration.config.xml.AbstractIntegrationNamespaceHandler;

/**
 * Namespace handler for Reactor Spring Integration components.
 *
 * @author Jon Brisbin
 */
public class ReactorNamespaceHandler extends AbstractIntegrationNamespaceHandler {
	@Override
	public void init() {
		registerBeanDefinitionParser("tcp-inbound-channel-adapter", new TcpServerInboundChannelAdapterParser());
	}
}
