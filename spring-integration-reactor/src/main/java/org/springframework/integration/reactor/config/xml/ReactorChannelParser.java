package org.springframework.integration.reactor.config.xml;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.config.xml.AbstractChannelParser;
import org.springframework.integration.reactor.ReactorChannel;
import org.w3c.dom.Element;
import reactor.core.Environment;
import reactor.fn.dispatch.Dispatcher;
import reactor.util.StringUtils;

/**
 * XML namespace parser for the {@link org.springframework.integration.reactor.ReactorChannel}.
 *
 * @author Jon Brisbin
 */
public class ReactorChannelParser extends AbstractChannelParser {

	@Override
	protected BeanDefinitionBuilder buildBeanDefinition(Element element, ParserContext parserContext) {
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(ReactorChannel.class);

		String envBean = element.getAttribute("env");
		if (!StringUtils.hasText(envBean)) {
			builder.addConstructorArgValue(new Environment());
		} else {
			builder.addConstructorArgReference(envBean);
		}

		String eventsReactor = element.getAttribute("ref");
		if (StringUtils.hasText(eventsReactor)) {
			builder.addConstructorArgReference(eventsReactor);
		} else {
			builder.addConstructorArgValue(null);

			String dispatcherType = element.getAttribute("dispatcher");
			if (StringUtils.hasText(dispatcherType)) {
				builder.addConstructorArgValue(dispatcherType);
			} else {
				builder.addConstructorArgValue(null);
			}
		}

		return builder;
	}

}
