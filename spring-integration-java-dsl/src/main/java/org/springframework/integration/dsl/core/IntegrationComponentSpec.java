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

package org.springframework.integration.dsl.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.expression.spel.standard.SpelExpressionParser;

/**
 * The common Builder abstraction.
 *
 * @author Artem Bilan
 */
public abstract class IntegrationComponentSpec<S extends IntegrationComponentSpec<S, T>, T> {

	protected final static SpelExpressionParser PARSER = new SpelExpressionParser();

	protected final Log logger = LogFactory.getLog(getClass());

	protected volatile T target;

	protected String id;

	protected S id(String id) {
		this.id = id;
		return _this();
	}

	public final String getId() {
		return id;
	}

	public final T get() {
		if (this.target == null) {
			this.target = this.doGet();
		}
		return this.target;
	}

	protected abstract T doGet();

	@SuppressWarnings("unchecked")
	protected final S _this() {
		return (S) this;
	}

}
