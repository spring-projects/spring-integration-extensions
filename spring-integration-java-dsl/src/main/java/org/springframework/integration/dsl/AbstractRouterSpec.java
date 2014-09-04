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

package org.springframework.integration.dsl;

import org.springframework.integration.dsl.core.MessageHandlerSpec;
import org.springframework.integration.router.AbstractMessageRouter;

/**
 * @author Artem Bilan
 */
public class AbstractRouterSpec<S extends AbstractRouterSpec<S, R>, R extends AbstractMessageRouter>
		extends MessageHandlerSpec<S, R> {

	AbstractRouterSpec(R router) {
		this.target = router;
	}

	public S ignoreSendFailures(boolean ignoreSendFailures) {
		this.target.setIgnoreSendFailures(ignoreSendFailures);
		return _this();
	}

	public S applySequence(boolean applySequence) {
		this.target.setApplySequence(applySequence);
		return _this();
	}

	@Override
	protected R doGet() {
		throw new UnsupportedOperationException();
	}

}
