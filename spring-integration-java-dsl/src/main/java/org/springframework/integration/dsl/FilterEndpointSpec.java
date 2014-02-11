package org.springframework.integration.dsl;

import org.springframework.integration.filter.MessageFilter;
import org.springframework.messaging.MessageChannel;

/**
 * @author Artem Bilan
 * @since 4.0
 */
public final class FilterEndpointSpec extends EndpointSpec<FilterEndpointSpec, MessageFilter> {

	FilterEndpointSpec(MessageFilter messageFilter) {
		super(messageFilter);
	}

	public FilterEndpointSpec throwExceptionOnRejection(boolean throwExceptionOnRejection) {
		this.getHandler().setThrowExceptionOnRejection(throwExceptionOnRejection);
		return _this();
	}

	public FilterEndpointSpec discardChannel(MessageChannel discardChannel) {
		this.getHandler().setDiscardChannel(discardChannel);
		return _this();
	}

	public FilterEndpointSpec discardWithinAdvice(boolean discardWithinAdvice) {
		this.getHandler().setDiscardWithinAdvice(discardWithinAdvice);
		return _this();
	}

}
