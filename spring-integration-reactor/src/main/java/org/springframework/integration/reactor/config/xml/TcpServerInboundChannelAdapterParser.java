package org.springframework.integration.reactor.config.xml;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.config.xml.AbstractChannelAdapterParser;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.springframework.integration.reactor.tcp.TcpServerInboundChannelAdapter;
import org.w3c.dom.Element;
import reactor.core.Environment;
import reactor.util.StringUtils;

import java.net.InetSocketAddress;

/**
 * {@code ChannelAdapaterParser} implementation for the {@link TcpServerInboundChannelAdapter}.
 *
 * @author Jon Brisbin
 */
public class TcpServerInboundChannelAdapterParser extends AbstractChannelAdapterParser {
	@Override
	protected AbstractBeanDefinition doParse(Element element, ParserContext parserContext, String channelName) {
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(TcpServerInboundChannelAdapter.class);

		String envBean = element.getAttribute("env");
		if (!StringUtils.hasText(envBean)) {
			builder.addConstructorArgValue(new Environment());
		} else {
			builder.addConstructorArgReference(envBean);
		}

		String bindHost = element.getAttribute("bind-host");
		String bindPort = element.getAttribute("bind-port");
		int port = 3000;
		if (StringUtils.hasText(bindPort)) {
			port = Integer.valueOf(bindPort);
		}
		InetSocketAddress bindAddress;
		if (!StringUtils.hasText(bindHost)) {
			bindAddress = new InetSocketAddress(port);
		} else {
			bindAddress = new InetSocketAddress(bindHost, port);
		}
		builder.addConstructorArgValue(bindAddress);

		String dispatcher = element.getAttribute("events-dispatcher");
		if (!StringUtils.hasText(dispatcher)) {
			dispatcher = Environment.RING_BUFFER;
		}
		builder.addConstructorArgValue(dispatcher);

		String codecBean = element.getAttribute("codec");
		if (StringUtils.hasText(codecBean)) {
			builder.addConstructorArgReference(codecBean);
		}

		String outputChannel = element.getAttribute("channel");
		if (StringUtils.hasText(outputChannel)) {
			builder.addPropertyReference("outputChannel", outputChannel);
		}
		String errorChannel = element.getAttribute("error-channel");
		if (StringUtils.hasText(errorChannel)) {
			builder.addPropertyReference("errorChannel", errorChannel);
		}

		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "auto-startup");
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "phase");

		return builder.getBeanDefinition();
	}
}
