/*
 * Copyright 2002-2012 the original author or authors.
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
package org.springframework.integration.voldemort.test;

import java.io.File;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import voldemort.server.VoldemortConfig;
import voldemort.server.VoldemortServer;

/**
 * Base class for functional test cases. Handles embedded Voldemort server startup and shutdown.
 *
 * @author Lukasz Antoniak
 * @since 1.0
 */
public abstract class BaseFunctionalTestCase {
	protected VoldemortServer server = null;

	/**
	 * Starts Voldemort embedded server.
	 */
	@Before
	public void setUp() throws Exception {
		final VoldemortConfig config = configureServer();
		server = new VoldemortServer( config );
		server.start();
	}

	/**
	 * Stops Voldemort embedded server.
	 */
	@After
	public void tearDown() throws Exception {
		if ( server != null && server.isStarted() ) {
			server.stop();
		}
	}

	/**
	 * Review cluster.xml and stores.xml configuration files.
	 *
	 * @return Voldemort embedded server configuration.
	 */
	protected VoldemortConfig configureServer() throws Exception {
		final File voldemortHome = new File( System.getProperty( "java.io.tmpdir" ), "voldemort" );
		FileUtils.deleteDirectory( voldemortHome );

		final Properties properties = new Properties();
		properties.put( "node.id", "0" );
		properties.put( "voldemort.home", voldemortHome.getAbsolutePath() );
		addConfigOptions( properties );
		final VoldemortConfig config = new VoldemortConfig( properties );

		final File metadata = new File( config.getMetadataDirectory() );
		FileUtils.forceMkdir( metadata );

		FileUtils.copyFileToDirectory( getClusterConfiguration(), metadata );
		FileUtils.copyFileToDirectory( getStoreConfiguration(), metadata );

		return config;
	}

	/**
	 * @return Voldemort cluster configuration descriptor.
	 */
	protected File getClusterConfiguration() {
		return new File( "src/test/resources/cluster.xml" );
	}

	/**
	 * @return Voldemort store configuration descriptor.
	 */
	protected File getStoreConfiguration() {
		return new File( "src/test/resources/stores.xml" );
	}

	/**
	 * Subclasses may want to setup specific server configuration parameters.
	 *
	 * @param properties Voldemort server configuration properties.
	 */
	protected void addConfigOptions(Properties properties) {
	}
}
