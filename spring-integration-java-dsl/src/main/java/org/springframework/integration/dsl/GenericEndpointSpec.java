package org.springframework.integration.dsl;

import org.springframework.messaging.MessageHandler;

/**
 * @author Artem Bilan
 * @since 4.0
 */
public final class GenericEndpointSpec<C extends MessageHandler> extends EndpointSpec<GenericEndpointSpec<C>, C> {

	GenericEndpointSpec(C messageHandler) {
		super(messageHandler);
	}

}
