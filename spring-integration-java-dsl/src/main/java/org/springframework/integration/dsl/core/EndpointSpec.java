/*
 * Copyright 2014 the original author or authors.
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

package org.springframework.integration.dsl.core;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.core.ResolvableType;
import org.springframework.integration.dsl.support.Function;
import org.springframework.integration.dsl.support.tuple.Tuple2;
import org.springframework.integration.dsl.support.tuple.Tuples;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.util.Assert;

/**
 * @author Artem Bilan

 */
public abstract class EndpointSpec<S extends EndpointSpec<S, F, H>, F extends BeanNameAware, H>
		extends IntegrationComponentSpec<S, Tuple2<F, H>> {

	@SuppressWarnings("unchecked")
	protected EndpointSpec(H handler) {
		Assert.notNull(handler);
		try {
			Class<?> fClass = ResolvableType.forClass(this.getClass()).as(EndpointSpec.class).resolveGenerics()[1];
			F endpointFactoryBean = (F) fClass.newInstance();
			this.target = Tuples.of(endpointFactoryBean, handler);
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

	public S poller(Function<PollerFactory, PollerSpec> pollers) {
		return poller(pollers.apply(new PollerFactory()));
	}

	public S poller(PollerSpec pollerMetadataSpec) {
		return this.poller(pollerMetadataSpec.get());
	}

	@Override
	protected final Tuple2<F, H> doGet() {
		throw new UnsupportedOperationException();
	}

}
