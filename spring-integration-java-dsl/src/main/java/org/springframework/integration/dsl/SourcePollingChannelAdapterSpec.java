package org.springframework.integration.dsl;

import org.springframework.integration.config.SourcePollingChannelAdapterFactoryBean;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.core.EndpointSpec;
import org.springframework.integration.scheduling.PollerMetadata;

/**
 * @author Artem Bilan

 */
public final class SourcePollingChannelAdapterSpec
		extends EndpointSpec<SourcePollingChannelAdapterSpec, SourcePollingChannelAdapterFactoryBean, MessageSource<?>> {

	SourcePollingChannelAdapterSpec(MessageSource<?> messageSource) {
		super(messageSource);
		this.target.getT1().setSource(messageSource);
	}

	public SourcePollingChannelAdapterSpec phase(int phase) {
		this.target.getT1().setPhase(phase);
		return _this();
	}

	public SourcePollingChannelAdapterSpec autoStartup(boolean autoStartup) {
		this.target.getT1().setAutoStartup(autoStartup);
		return _this();
	}

	public SourcePollingChannelAdapterSpec poller(PollerMetadata pollerMetadata) {
		this.target.getT1().setPollerMetadata(pollerMetadata);
		return _this();
	}

}
