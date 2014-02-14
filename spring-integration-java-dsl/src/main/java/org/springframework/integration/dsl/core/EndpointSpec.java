package org.springframework.integration.dsl.core;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.core.GenericTypeResolver;
import org.springframework.integration.dsl.support.PollerSpec;
import org.springframework.integration.dsl.tuple.Tuple;
import org.springframework.integration.dsl.tuple.Tuple2;
import org.springframework.integration.scheduling.PollerMetadata;

/**
 * @author Artem Bilan
 * @since 4.0
 */
public abstract class EndpointSpec<S extends EndpointSpec<S, F, H>, F extends BeanNameAware, H> extends IntegrationComponentSpec<S, Tuple2<F, H>> {

	@SuppressWarnings("unchecked")
	protected EndpointSpec(H handler) {
		try {
			Class<?> fClass = GenericTypeResolver.resolveTypeArguments(this.getClass(), EndpointSpec.class)[1];
			F endpointFactoryBean = (F) fClass.newInstance();
			this.target = Tuple.of(endpointFactoryBean, handler);
		}
		catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public S id(String id) {
		this.target.getT1().setBeanName(id);
		return super.id(id);
	}

	public abstract S phase(int phase);

	public abstract S autoStartup(boolean autoStartup);

	public abstract S poller(PollerMetadata pollerMetadata);

	public S poller(PollerSpec pollerMetadataSpec) {
		return this.poller(pollerMetadataSpec.get());
	}

	@Override
	protected final Tuple2<F, H> doGet() {
		throw new UnsupportedOperationException();
	}

}
