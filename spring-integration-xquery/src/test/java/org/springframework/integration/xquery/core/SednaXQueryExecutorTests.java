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
package org.springframework.integration.xquery.core;

import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQException;

import net.xqj.sedna.SednaXQDataSource;

/**
 * The Test class that uses {@link SednaXQDataSource} in {@link XQueryExecutor}
 *
 * This test is intentionally excluded from being executed by surefire as it will
 * fail if the Sedna database is not up and running
 *
 * To run this test we need to have the Sedna XML Database up and running.
 * Visit {@link http://xqj.net/sedna/} for more details about Sedna.
 *
 * Once the Sedna xml database is downloaded and extracted the you will find
 * various executables in the bin directory, do the following to start it and create
 * a new database called 'test'
 *
 *   1. Start the governer by executing se_gov
 *   2. Create a new database called 'test' by executing se_cdb test
 *   3. Start the newly created database by running se_sm test. If the database was already
 *   	created, you can skip the step 2 and execute this step directly.
 *
 * Once the database is up and running, you can execute the below test case.
 *
 * @author Amol Nayak
 *
 * @since 1.0
 *
 */
public class SednaXQueryExecutorTests extends AbstractXQueryExecutorTests {


	@Override
	/**
	 * Gets an instance of SednaXQDataSource for the tests.
	 *
	 */
	protected XQueryExecutor getExecutor() {
		XQueryExecutor executor = new XQueryExecutor();
		XQDataSource ds;
		try {
			ds = new SednaXQDataSource();
			ds.setProperty("serverName", "localhost");
			ds.setProperty("databaseName", "test");
		} catch (XQException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		executor.setXQDataSource(ds);
		return executor;
	}
}
