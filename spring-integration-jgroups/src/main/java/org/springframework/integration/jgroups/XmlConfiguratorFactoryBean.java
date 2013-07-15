/**
 * Copyright 2013 Jaroslaw Palka<jaroslaw.palka@symentis.pl>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.integration.jgroups;

import java.io.InputStream;

import org.jgroups.conf.ProtocolStackConfigurator;
import org.jgroups.conf.XmlConfigurator;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

/**
 * Factory bean which creates ProcotolStackConfigurator (
 * {@link org.jgroups.conf.ProtocolStackConfigurator} ) based of XML file.
 * 
 * @author Jaroslaw Palka <jaroslaw.palka@symentis.pl>
 * @since 1.0.0
 * 
 */
public class XmlConfiguratorFactoryBean extends AbstractFactoryBean<ProtocolStackConfigurator> {

	private Resource resource;

	@Override
	public Class<?> getObjectType() {
		return ProtocolStackConfigurator.class;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}

	@Override
	protected ProtocolStackConfigurator createInstance() throws Exception {

		Assert.notNull(resource, "no XML resource with JGroups configuration");

		InputStream stream = resource.getInputStream();

		XmlConfigurator configurator = XmlConfigurator.getInstance(stream);

		return configurator;
	}

}
