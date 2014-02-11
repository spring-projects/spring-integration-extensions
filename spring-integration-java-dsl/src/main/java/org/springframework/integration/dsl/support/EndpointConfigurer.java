package org.springframework.integration.dsl.support;

import org.springframework.integration.dsl.EndpointSpec;

/**
 * @author Artem Bilan
 * @since 4.0
 */
public interface EndpointConfigurer<S extends EndpointSpec<?, ?>> {

	void configure(S spec);

}
