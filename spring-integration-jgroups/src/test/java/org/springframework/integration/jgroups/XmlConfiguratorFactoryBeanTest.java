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

import static org.fest.assertions.Assertions.assertThat;
import static org.hamcrest.core.Is.is;

import org.jgroups.conf.ProtocolStackConfigurator;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.core.io.ClassPathResource;

public class XmlConfiguratorFactoryBeanTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void should_load_configuration_from_xml_file() throws Exception {
		XmlConfiguratorFactoryBean factoryBean = new XmlConfiguratorFactoryBean();
		factoryBean.setResource(new ClassPathResource("/udp.xml"));
		factoryBean.afterPropertiesSet();

		ProtocolStackConfigurator configurator = factoryBean.getObject();

		assertThat(configurator.getProtocolStack()).isNotEmpty();
	}

	@Test
	public void should_throw_exception_when_no_xml_resource_in_not_set() throws Exception {
		XmlConfiguratorFactoryBean factoryBean = new XmlConfiguratorFactoryBean();

		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage(is("no XML resource with JGroups configuration"));

		factoryBean.afterPropertiesSet();

		@SuppressWarnings("unused")
		ProtocolStackConfigurator stackConfigurator = factoryBean.getObject();
	}
}
