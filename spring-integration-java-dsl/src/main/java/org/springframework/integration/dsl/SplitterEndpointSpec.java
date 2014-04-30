/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.integration.dsl;

import org.springframework.integration.dsl.core.ConsumerEndpointSpec;
import org.springframework.integration.splitter.AbstractMessageSplitter;

/**
 * @author Artem Bilan
 */
public final class SplitterEndpointSpec<S extends AbstractMessageSplitter>
		extends ConsumerEndpointSpec<SplitterEndpointSpec<S>, S> {

	SplitterEndpointSpec(S splitter) {
		super(splitter);
	}

	public SplitterEndpointSpec<S> applySequence(boolean applySequence) {
		this.target.getT2().setApplySequence(applySequence);
		return _this();
	}

}
