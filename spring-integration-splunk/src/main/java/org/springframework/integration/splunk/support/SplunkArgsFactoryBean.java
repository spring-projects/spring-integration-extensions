/*
 * Copyright 2002-2013 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.springframework.integration.splunk.support;

import org.springframework.beans.factory.FactoryBean;

import com.splunk.Args;

/**
 * {@link FactoryBean} that wraps {@link ArgsBuilder}
 * @author David Turanski
 *
 */
public class SplunkArgsFactoryBean implements FactoryBean<Args> {
	private ArgsBuilder argsBuilder = new ArgsBuilder();

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.FactoryBean#getObject()
	 */
	public Args getObject() throws Exception {
 		return argsBuilder.build();
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.FactoryBean#getObjectType()
	 */
	public Class<?> getObjectType() {
		return Args.class;
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.FactoryBean#isSingleton()
	 */
	public boolean isSingleton() {
		return true;
	}

	/**
	 * @param host the host to set
	 */
	public void setHost(String host) {
		this.argsBuilder.setHost(host);
	}

	/**
	 * @param hostRegex the hostRegex to set
	 */
	public void setHostRegex(String hostRegex) {
		this.argsBuilder.setHostRegex(hostRegex);
	}

	/**
	 * @param sourceType the sourceType to set
	 */
	public void setSourceType(String sourceType) {
		this.argsBuilder.setSourceType(sourceType);
	}

	/**
	 * @param source the source to set
	 */
	public void setSource(String source) {
		this.argsBuilder.setSource(source);
	}
}
