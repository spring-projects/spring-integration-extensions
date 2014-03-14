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

import org.springframework.integration.router.AbstractMappingMessageRouter;

/**
 * @author Artem Bilan
 */
public final class RouterSpec<R extends AbstractMappingMessageRouter> extends AbstractRouterSpec<RouterSpec<R>, R> {

	RouterSpec(R router) {
		super(router);
	}

	public RouterSpec<R> resolutionRequired(boolean resolutionRequired) {
		this.target.setResolutionRequired(resolutionRequired);
		return _this();
	}

	public RouterSpec<R> prefix(String prefix) {
		this.target.setPrefix(prefix);
		return _this();
	}

	public RouterSpec<R> suffix(String suffix) {
		this.target.setSuffix(suffix);
		return _this();
	}

	public RouterSpec<R> channelMapping(String key, String channelName) {
		this.target.setChannelMapping(key, channelName);
		return _this();
	}

}
