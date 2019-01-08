/*
 * Copyright 2015-2019 the original author or authors.
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

package org.springframework.integration.cassandra.config;


import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.AbstractReactiveCassandraConfiguration;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.data.cassandra.core.cql.keyspace.CreateKeyspaceSpecification;

/**
 * Setup any spring configuration for unit tests
 *
 * @author David Webb
 * @author Matthew T. Adams
 * @author Artem Bilan
 */
@Configuration
public class IntegrationTestConfig extends AbstractReactiveCassandraConfiguration {

	public static final String HOST = "localhost";

	// public static final SpringCassandraBuildProperties PROPS = new SpringCassandraBuildProperties();
	public static final int PORT = 9043; //PROPS.getCassandraPort();

	// public static final int RPC_PORT = PROPS.getCassandraRpcPort();

	public String keyspaceName = randomKeyspaceName();

	public static String randomKeyspaceName() {
		return "ks" + UUID.randomUUID().toString().replace("-", "");
	}

	@Override
	protected int getPort() {
		return PORT;
	}

	@Override
	public SchemaAction getSchemaAction() {
		return SchemaAction.RECREATE;
	}

	@Override
	protected String getKeyspaceName() {
		return this.keyspaceName;
	}

	@Override
	protected List<CreateKeyspaceSpecification> getKeyspaceCreations() {
		return Collections.singletonList(
				CreateKeyspaceSpecification.createKeyspace(getKeyspaceName())
						.withSimpleReplication());
	}

}
